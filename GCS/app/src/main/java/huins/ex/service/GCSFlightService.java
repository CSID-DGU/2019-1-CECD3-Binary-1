package huins.ex.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import huins.ex.communications.AndroidUdpConnection;
import huins.ex.communications.BluetoothConnection;
import huins.ex.communications.MavLinkConnection;
import huins.ex.communications.usb.UsbConnection;
import huins.ex.core.MAVLink.connection.MavLinkConnectionListener;
import huins.ex.core.survey.CameraInfo;
import huins.ex.R;
import huins.ex.communications.AndroidMavLinkConnection;
import huins.ex.communications.AndroidTcpConnection;
import huins.ex.interfaces.FlightEventsListener;
import huins.ex.proto.ConnectionParameter;
import huins.ex.proto.IApiListener;
import huins.ex.proto.IGCSServices;
import huins.ex.proto.mission.item.complex.CameraDetail;
import huins.ex.service.GCSinterface.FlightInterface;
import huins.ex.util.ConnectionException;
import huins.ex.util.File.IO.CameraInfoLoader;
import huins.ex.util.analytics.GAUtils;
import huins.ex.util.constants.ConnectionType;
import huins.ex.view.activities.GCSMainActivity;

/**
 * Created by suhak on 15. 6. 11.
 */
public class GCSFlightService extends Service {

    private static final String TAG = GCSFlightService.class.getSimpleName();

    private static final int FOREGROUND_ID = 101;

    public static final String ACTION_DRONE_CREATED = "ACTION_DRONE_CREATED";
    public static final String ACTION_DRONE_DESTROYED = "ACTION_DRONE_DESTROYED";
    public static final String ACTION_KICK_START_DRONESHARE_UPLOADS = "ACTION_KICK_START_DRONESHARE_UPLOADS";
    public static final String ACTION_RELEASE_API_INSTANCE = "action.RELEASE_API_INSTANCE";
    public static final String EXTRA_API_INSTANCE_APP_ID = "extra_api_instance_app_id";

    private LocalBroadcastManager lbm;

    final ConcurrentHashMap<String, FlightInterface> flightinterfaceStore = new ConcurrentHashMap<>();

    /**
     * Caches mavlink connections per connection type.
     */
    final ConcurrentHashMap<String, AndroidMavLinkConnection> mavConnections = new ConcurrentHashMap<>();

    /**
     * Caches drone managers per connection type.
     */
    final ConcurrentHashMap<ConnectionParameter, FlightManager> flightManagers = new ConcurrentHashMap<>();

    private FlightServices flightServices;
    private FlightAccess flightAccess;
    private MavLinkServiceInterface mavLinkServiceInterface;

    private CameraInfoLoader cameraInfoLoader;
    private List<CameraDetail> cachedCameraDetails;

    FlightInterface registerFlightApi(IApiListener listener, String appId) {
        if (listener == null)
            return null;

        FlightInterface flightInterface = new FlightInterface(this, Looper.getMainLooper(), listener, appId);
        flightinterfaceStore.put(appId, flightInterface);
        lbm.sendBroadcast(new Intent(ACTION_DRONE_CREATED));
        updateForegroundNotification();
        return flightInterface;
    }

    public void releaseFlightInterface(String appId) {
        if (appId == null)
            return;

        FlightInterface flightInterface = flightinterfaceStore.remove(appId);
        if (flightInterface != null) {
            Log.d(TAG, "Releasing drone api instance for " + appId);
            flightInterface.destroy();
            lbm.sendBroadcast(new Intent(ACTION_DRONE_DESTROYED));
            updateForegroundNotification();
        }
    }

    public FlightManager connectFlightManager(ConnectionParameter connParams, String appId, FlightEventsListener listener) throws ConnectionException {
        if (connParams == null || TextUtils.isEmpty(appId) || listener == null)
            return null;

        FlightManager flightManager = flightManagers.get(connParams);
        if (flightManager == null) {
            Log.d(TAG, "Generating new drone manager.");
            flightManager = new FlightManager(getApplicationContext(), connParams, new Handler(Looper.getMainLooper()),
                    mavLinkServiceInterface);
            flightManagers.put(connParams, flightManager);
        }

        Log.d(TAG, "Drone manager connection for " + appId);
        flightManager.connect(appId, listener);
        return flightManager;
    }

    public void disconnectFlightManager(FlightManager flightManager, String appId) throws ConnectionException {
        if (flightManager == null || TextUtils.isEmpty(appId))
            return;

        Log.d(TAG, "Drone manager disconnection for " + appId);
        flightManager.disconnect(appId);
        if (flightManager.getConnectedAppsCount() == 0) {
            Log.d(TAG, "Destroying drone manager.");
            flightManager.destroy();
            flightManagers.remove(flightManager.getConnectionParameter());
        }
    }

    void connectMAVConnection(ConnectionParameter connParams, String listenerTag,
                              MavLinkConnectionListener listener) {
        AndroidMavLinkConnection conn = mavConnections.get(connParams.getUniqueId());
        final int connectionType = connParams.getConnectionType();
        final Bundle paramsBundle = connParams.getParamsBundle();
        if (conn == null) {

            //Create a new mavlink connection

            switch (connectionType) {
                case ConnectionType.TYPE_USB:
                    final int baudRate = paramsBundle.getInt(ConnectionType.EXTRA_USB_BAUD_RATE,
                            ConnectionType.DEFAULT_USB_BAUD_RATE);
                    conn = new UsbConnection(getApplicationContext(), baudRate);
                    Log.d(TAG, "Connecting over usb.");
                    break;

                case ConnectionType.TYPE_BLUETOOTH:
                    //Retrieve the bluetooth address to connect to
                    final String bluetoothAddress = paramsBundle.getString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS);
                    conn = new BluetoothConnection(getApplicationContext(), bluetoothAddress);
                    Log.d(TAG, "Connecting over bluetooth.");
                    break;

                case ConnectionType.TYPE_TCP:
                    //Retrieve the server ip and port
                    final String tcpServerIp = paramsBundle.getString(ConnectionType.EXTRA_TCP_SERVER_IP);
                    final int tcpServerPort = paramsBundle.getInt(ConnectionType
                            .EXTRA_TCP_SERVER_PORT, ConnectionType.DEFAULT_TCP_SERVER_PORT);
                    conn = new AndroidTcpConnection(getApplicationContext(), tcpServerIp, tcpServerPort);
                    Log.d(TAG, "Connecting over tcp.");
                    break;

                case ConnectionType.TYPE_UDP:
                    final int udpServerPort = paramsBundle
                            .getInt(ConnectionType.EXTRA_UDP_SERVER_PORT, ConnectionType.DEFAULT_UDP_SERVER_PORT);
                    conn = new AndroidUdpConnection(getApplicationContext(), udpServerPort);
                    Log.d(TAG, "Connecting over udp.");
                    break;

                default:
                    Log.e(TAG, "Unrecognized connection type: " + connectionType);
                    return;
            }

            mavConnections.put(connParams.getUniqueId(), conn);
        }

        if (connectionType == ConnectionType.TYPE_UDP) {
            final String pingIpAddress = paramsBundle.getString(ConnectionType.EXTRA_UDP_PING_RECEIVER_IP);
            if (!TextUtils.isEmpty(pingIpAddress)) {
                try {
                    final InetAddress resolvedAddress = InetAddress.getByName(pingIpAddress);

                    final int pingPort = paramsBundle.getInt(ConnectionType.EXTRA_UDP_PING_RECEIVER_PORT);
                    final long pingPeriod = paramsBundle.getLong(ConnectionType.EXTRA_UDP_PING_PERIOD,
                            ConnectionType.DEFAULT_UDP_PING_PERIOD);
                    final byte[] pingPayload = paramsBundle.getByteArray(ConnectionType.EXTRA_UDP_PING_PAYLOAD);

                    ((AndroidUdpConnection) conn).addPingTarget(resolvedAddress, pingPort, pingPeriod, pingPayload);

                } catch (UnknownHostException e) {
                    Log.e(TAG, "Unable to resolve UDP ping server ip address.", e);
                }
            }
        }

        conn.addMavLinkConnectionListener(listenerTag, listener);
        if (conn.getConnectionStatus() == MavLinkConnection.MAVLINK_DISCONNECTED) {
            conn.connect();

            // Record which connection type is used.
            GAUtils.sendEvent(new HitBuilders.EventBuilder()
                    .setCategory(GAUtils.Category.MAVLINK_CONNECTION)
                    .setAction("MavLink connect")
                    .setLabel(connParams.toString()));
        }
    }
    void addLoggingFile(ConnectionParameter connParams, String tag, String loggingFilePath) {
        AndroidMavLinkConnection conn = mavConnections.get(connParams.getUniqueId());
        if (conn == null)
            return;

        conn.addLoggingPath(tag, loggingFilePath);
    }

    void removeLoggingFile(ConnectionParameter connParams, String tag) {
        AndroidMavLinkConnection conn = mavConnections.get(connParams.getUniqueId());
        if (conn == null)
            return;

        conn.removeLoggingPath(tag);
    }

    void disconnectMAVConnection(ConnectionParameter connParams, String listenerTag) {
        final AndroidMavLinkConnection conn = mavConnections.get(connParams.getUniqueId());
        if (conn == null)
            return;

        conn.removeMavLinkConnectionListener(listenerTag);

        if (conn.getMavLinkConnectionListenersCount() == 0 && conn.getConnectionStatus() !=
                MavLinkConnection.MAVLINK_DISCONNECTED) {
            Log.d(TAG, "Disconnecting...");
            conn.disconnect();

            GAUtils.sendEvent(new HitBuilders.EventBuilder()
                    .setCategory(GAUtils.Category.MAVLINK_CONNECTION)
                    .setAction("MavLink disconnect")
                    .setLabel(connParams.toString()));
        }
    }

    public synchronized List<CameraDetail> getCameraDetails() {
        if (cachedCameraDetails == null) {
            List<String> cameraInfoNames = cameraInfoLoader.getCameraInfoList();

            List<CameraInfo> cameraInfos = new ArrayList<>(cameraInfoNames.size());
            for (String infoName : cameraInfoNames) {
                try {
                    cameraInfos.add(cameraInfoLoader.openFile(infoName));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }

            List<CameraDetail> cameraDetails = new ArrayList<>(cameraInfos.size());
            for (CameraInfo camInfo : cameraInfos) {
                cameraDetails.add(new CameraDetail(camInfo.name, camInfo.sensorWidth,
                        camInfo.sensorHeight, camInfo.sensorResolution, camInfo.focalLength,
                        camInfo.overlap, camInfo.sidelap, camInfo.isInLandscapeOrientation));
            }

            cachedCameraDetails = cameraDetails;
        }

        return cachedCameraDetails;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Binding intent: " + intent);
        final String action = intent.getAction();
        if (IGCSServices.class.getName().equals(action)) {
            // Return binder to ipc client-server interaction.
            return flightServices;
        } else {
            // Return binder to the service.
            return flightAccess;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating 3DR Services.");

        final Context context = getApplicationContext();

//        handlerThread = new HandlerThread("Connected apps looper");
//        handlerThread.start();

        mavLinkServiceInterface = new MavLinkServiceInterface(this);
        flightAccess = new FlightAccess(this);
        flightServices = new FlightServices(this);
        lbm = LocalBroadcastManager.getInstance(context);
        this.cameraInfoLoader = new CameraInfoLoader(context);

        updateForegroundNotification();
    }

    private void updateForegroundNotification() {
        final Context context = getApplicationContext();

        //Put the service in the foreground
        final Notification.Builder notifBuilder = new Notification.Builder(context)
                .setContentTitle("3DR Services")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context,
                        GCSMainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0));

        final int connectedCount = flightinterfaceStore.size();
        if (connectedCount > 0) {
            if (connectedCount == 1) {
                notifBuilder.setContentText("1 connected app");
            } else {
                notifBuilder.setContentText(connectedCount + " connected apps");
            }
        }

        final Notification notification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                ? notifBuilder.build()
                : notifBuilder.getNotification();
        startForeground(FOREGROUND_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroying 3DR Services.");

        for (FlightInterface flightInterface : flightinterfaceStore.values()) {
            flightInterface.destroy();
        }
        flightinterfaceStore.clear();

        for (AndroidMavLinkConnection conn : mavConnections.values()) {
            conn.disconnect();
            conn.removeAllMavLinkConnectionListeners();
        }

        mavConnections.clear();
        flightServices.destroy();
//        handlerThread.quit();

        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_KICK_START_DRONESHARE_UPLOADS:
                    for (FlightManager flightManager : flightManagers.values()) {
                        flightManager.kickStartDroneShareUpload();
                    }
                    break;

                case ACTION_RELEASE_API_INSTANCE:
                    final String appId = intent.getStringExtra(EXTRA_API_INSTANCE_APP_ID);
                    releaseFlightInterface(appId);
                    break;
            }
        }

        stopSelf();
        return START_NOT_STICKY;
    }

}

