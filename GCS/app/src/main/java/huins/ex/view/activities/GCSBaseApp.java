package huins.ex.view.activities;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import huins.ex.interfaces.FlightListener;
import huins.ex.interfaces.TowerListener;
import huins.ex.model.MissionProxy;
import huins.ex.proto.ConnectionParameter;
import huins.ex.proto.ControlTower;
import huins.ex.proto.Flight;
import huins.ex.proto.attribute.AttributeEvent;
import huins.ex.proto.connection.ConnectionResult;
import huins.ex.proto.connection.FlightSharePreferences;
import huins.ex.util.ExceptionWriter;
import huins.ex.util.NotificationHandler;
import huins.ex.util.constants.ConnectionType;
import huins.ex.util.constants.GCSPreferences;
import huins.ex.util.log.GCSLogger;

/**
 * Created by suhak on 15. 6. 11.
 */
public class GCSBaseApp extends Application implements TowerListener, FlightListener {

    private static final String TAG = GCSBaseApp.class.getSimpleName();

    private static final long DELAY_TO_DISCONNECTION = 1000l; // ms

    public static final String ACTION_TOGGLE_DRONE_CONNECTION = "ACTION_TOGGLE_DRONE_CONNECTION";
    public static final String EXTRA_ESTABLISH_CONNECTION = "EXTRA_ESTABLISH_CONNECTION";
    public static final String ACTION_DRONE_CONNECTION_FAILED = "ACTION_DRONE_CONNECTION_FAILED";

    public static final String EXTRA_CONNECTION_FAILED_ERROR_CODE = "extra_connection_failed_error_code";

    public static final String EXTRA_CONNECTION_FAILED_ERROR_MESSAGE = "extra_connection_failed_error_message";

    public static final String ACTION_DRONE_EVENT = "ACTION_DRONE_EVENT";
    public static final String EXTRA_DRONE_EVENT = "extra_drone_event";

    //To broadcast message
    private LocalBroadcastManager localBroadcastManager;
    //To notify message
    private NotificationHandler notificationHandler;

    private ControlTower controlTower;
    private Flight flight;
    private MissionProxy missionProxy;

    private GCSPreferences GCSPrefs;

    private boolean bIsBackPressed = false;
    private boolean _is_binded;
    private Messenger mService = null;

    private Thread.UncaughtExceptionHandler exceptionHandler;

    private final Handler handler = new Handler();
    private final List<GCSApiListener> apiListeners = new ArrayList<GCSApiListener>();

   // private GCSFlightService flightService;
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    //getter setter
    public GCSPreferences getAppPreferences() {
        return GCSPrefs;
    }
    public huins.ex.proto.Flight getFlight() {
        return this.flight;
    }

    public MissionProxy getMissionProxy() {
        return this.missionProxy;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = getApplicationContext();

        GCSPrefs = new GCSPreferences(context);
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        controlTower = new ControlTower(context);
        flight = new huins.ex.proto.Flight(context);

     //   flightService = new GCSFlightService();
        final Thread.UncaughtExceptionHandler dpExceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                new ExceptionWriter(ex).saveStackTraceToSD(context);
                exceptionHandler.uncaughtException(thread, ex);
            }
        };

        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(dpExceptionHandler);


        //connect mission to flight
        missionProxy = new MissionProxy(context, this.flight);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TOGGLE_DRONE_CONNECTION);

        registerReceiver(broadcastReceiver, intentFilter);
    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(ACTION_TOGGLE_DRONE_CONNECTION)) {
                    boolean connect = intent.getBooleanExtra(EXTRA_ESTABLISH_CONNECTION,
                            !flight.IsConnected());

                    if (connect)
                        connectToFlight();
                    else
                        disconnectFromFlight();
            }
        }
    };



    @Override
    public void onFlightConnectionFailed(ConnectionResult result) {
        String errorMsg = result.getErrorMessage();
        Toast.makeText(getApplicationContext(), "Connection failed: " + errorMsg,
                Toast.LENGTH_LONG).show();

        localBroadcastManager.sendBroadcast(new Intent(ACTION_DRONE_CONNECTION_FAILED)
                .putExtra(EXTRA_CONNECTION_FAILED_ERROR_CODE, result.getErrorCode())
                .putExtra(EXTRA_CONNECTION_FAILED_ERROR_MESSAGE, result.getErrorMessage()));
    }
    @Override
    public void onFlightEvent(String event, Bundle extras) {

        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                handler.removeCallbacks(disconnectionTask);
                if (notificationHandler == null) {
                    notificationHandler = new NotificationHandler(getApplicationContext(), flight);
                }
                break;
            case AttributeEvent.STATE_DISCONNECTED:
                shouldWeTerminate();
                break;
        }

        localBroadcastManager.sendBroadcast(new Intent(ACTION_DRONE_EVENT).putExtra(EXTRA_DRONE_EVENT, event));

        final Intent droneIntent = new Intent(event);
        if (extras != null)
            droneIntent.putExtras(extras);
        localBroadcastManager.sendBroadcast(droneIntent);
    }

    @Override
    public void onFlightServiceInterrupted(String errorMsg) {
        controlTower.unregisterFlight(flight);
        if (notificationHandler != null) {
            notificationHandler.terminate();
            notificationHandler = null;
        }

        if (!TextUtils.isEmpty(errorMsg))
            Log.e(TAG, errorMsg);
    }

    @Override
    public void onTowerConnected() {
        if (notificationHandler == null) {
            notificationHandler = new NotificationHandler(getApplicationContext(), flight);
        }

        flight.unregisterFlightListener(this);
        controlTower.registerFlight(flight, handler);
        flight.registerFlightListener(this);

        notifyApiConnected();
    }

    @Override
    public void onTowerDisconnected() {
        notifyApiDisconnected();
    }

    public interface GCSApiListener {
        void onApiConnected();

        void onApiDisconnected();
    }

     public void connectToFlight() {
        final ConnectionParameter connParams = retrieveConnectionParameters();

        GCSLogger.WriteLog(GCSLogger.GCS_LOGGER.LOG_DEBUG, TAG, "Connection to Flight");
        if (connParams == null)
            return;

        boolean isFlightConnected = flight.IsConnected();
        if (!connParams.equals(flight.getConnectionParameter()) && isFlightConnected) {
            flight.disconnect();
            isFlightConnected = false;
        }

        if (!isFlightConnected)
            flight.connect(connParams);
    }


    public static void connectToFlight(Context context) {
        context.sendBroadcast(new Intent(GCSBaseApp.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(GCSBaseApp.EXTRA_ESTABLISH_CONNECTION, true));
    }

    public static void disconnectFromFlight(Context context) {
        context.sendBroadcast(new Intent(GCSBaseApp.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(GCSBaseApp.EXTRA_ESTABLISH_CONNECTION, false));
    }


    public void disconnectFromFlight() {
        if (flight.IsConnected())
            flight.disconnect();
    }


    private ConnectionParameter retrieveConnectionParameters() {
        ConnectionParameter connParams = null;

        final int connectionType = GCSPrefs.getConnectionParameterType();
        Bundle extraParams = new Bundle();
        final FlightSharePreferences flightSharePreferences = new FlightSharePreferences(GCSPrefs.getFlightShareLogin(),
                GCSPrefs.getFlightSharePassword(), GCSPrefs.isDroneshareEnabled(),
                GCSPrefs.isLiveUploadEnabled());


        switch (connectionType) {
            case ConnectionType.TYPE_UDP:
                extraParams.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, GCSPrefs.getUdpServerPort());
                if(GCSPrefs.isUdpPingEnabled()){
                    extraParams.putString(ConnectionType.EXTRA_UDP_PING_RECEIVER_IP, GCSPrefs.getUdpPingReceiverIp());
                    extraParams.putInt(ConnectionType.EXTRA_UDP_PING_RECEIVER_PORT, GCSPrefs.getUdpPingReceiverPort());
                    extraParams.putByteArray(ConnectionType.EXTRA_UDP_PING_PAYLOAD, "Hello".getBytes());
                }
                connParams = new ConnectionParameter(connectionType, extraParams, flightSharePreferences);
                break;

            case 0:
            case ConnectionType.TYPE_TCP:
                extraParams.putString(ConnectionType.EXTRA_TCP_SERVER_IP, GCSPrefs.getTcpServerIp());
                extraParams.putInt(ConnectionType.EXTRA_TCP_SERVER_PORT, GCSPrefs.getTcpServerPort());
                connParams = new ConnectionParameter(connectionType, extraParams, flightSharePreferences);
                break;

            case ConnectionType.TYPE_BLUETOOTH:
//                String btAddress = GCSPrefs.getBluetoothDeviceAddress();
//                if (TextUtils.isEmpty(btAddress)) {
//                    connParams = null;
//                    startActivity(new Intent(getApplicationContext(),
//                            BluetoothDevicesActivity.class)
//                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//
//                } else {
//                    extraParams.putString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS, btAddress);
//                    connParams = new ConnectionParameter(connectionType, extraParams, flightSharePreferences);
//                }
                break;

            default:
                Log.e(TAG, "Unrecognized connection type: " + connectionType);
                connParams = null;
                break;
        }

        return connParams;
    }

    public void addApiListener(GCSApiListener listener) {
        if (listener == null)
            return;

        handler.removeCallbacks(disconnectionTask);
        boolean isTowerConnected = controlTower.isTowerConnected();
     //   if (isTowerConnected)
            listener.onApiConnected();

        if (!isTowerConnected) {
            try {
                controlTower.connect(this);
            } catch (IllegalStateException e) {
                //Ignore
            }
        }

        apiListeners.add(listener);
    }

    public void removeApiListener(GCSApiListener listener) {
        if (listener != null) {
            apiListeners.remove(listener);
            listener.onApiDisconnected();
        }

        shouldWeTerminate();
    }

    public void notifyBackPressedFromMain() {
        this.bIsBackPressed = true;
    }

    private void shouldWeTerminate() {
        if (this.bIsBackPressed) {
            handler.postDelayed(disconnectionTask, DELAY_TO_DISCONNECTION);
            this.bIsBackPressed = false;
        } else if (apiListeners.isEmpty() && !flight.IsConnected()) {
            // Wait 30s, then disconnect the service binding.
            handler.postDelayed(disconnectionTask, DELAY_TO_DISCONNECTION);

        }
    }

    private final Runnable disconnectionTask = new Runnable() {
        @Override
        public void run() {
            controlTower.unregisterFlight(flight);
            controlTower.disconnect();

            if (notificationHandler != null) {
                notificationHandler.terminate();
                notificationHandler = null;
            }

            handler.removeCallbacks(this);
        }
    };

    private void notifyApiConnected() {
        if (apiListeners.isEmpty())
            return;

        for (GCSApiListener listener : apiListeners)
            listener.onApiConnected();
    }

    private void notifyApiDisconnected() {
        if (apiListeners.isEmpty())
            return;

        for (GCSApiListener listener : apiListeners)
            listener.onApiDisconnected();
    }

}

class IncomingHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
        String[] values;

        /*
        switch (msg.what) {
            case
            default:
        }
        */
    }
}