package huins.ex.service.GCSinterface;
 
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import huins.ex.R;
import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import huins.ex.com.MAVLink.ardupilotmega.msg_mag_cal_report;
import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.variables.calibration.AccelCalibration;
import huins.ex.core.MAVLink.MavLinkCameraOn;
import huins.ex.core.MAVLink.MavLinkGimbalControl;
import huins.ex.core.MAVLink.command.doCmd.MavLinkDoCmds;
import huins.ex.core.MAVLink.MavLinkCameraResolution;
import huins.ex.core.gcs.follow.Follow;
import huins.ex.core.gcs.follow.FollowAlgorithm;
import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.helpers.coordinates.Coord3D;
import huins.ex.core.model.Flight;
import huins.ex.core.parameters.Parameter;
import huins.ex.interfaces.FlightEventsListener;
import huins.ex.proto.ConnectionParameter;
import huins.ex.proto.Coordinate;
import huins.ex.proto.CoordinateExtend;
import huins.ex.proto.IApiListener;
import huins.ex.proto.IMavlinkObserver;
import huins.ex.proto.IObserver;
import huins.ex.proto.action.Action;
import huins.ex.proto.action.ConnectionActions;
import huins.ex.proto.action.ExperimentalActions;
import huins.ex.proto.action.GimbalAction;
import huins.ex.proto.action.GuidedActions;
import huins.ex.proto.action.ParameterActions;
import huins.ex.proto.action.RCAction;
import huins.ex.proto.action.StateActions;
import huins.ex.proto.action.StreamActions;
import huins.ex.proto.attribute.AttributeEvent;
import huins.ex.proto.attribute.AttributeEventExtra;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.camera.action.CameraActions;
import huins.ex.proto.connection.ConnectionResult;
import huins.ex.proto.connection.FlightSharePreferences;
import huins.ex.proto.gcs.action.CalibrationActions;
import huins.ex.proto.gcs.action.FollowMeActions;
import huins.ex.proto.gcs.event.GCSEvent;
import huins.ex.proto.gcs.follow.FollowType;
import huins.ex.proto.mavlink.MavlinkMessageWrapper;
import huins.ex.proto.mission.Mission;
import huins.ex.proto.mission.action.MissionActions;
import huins.ex.proto.property.Parameters;
import huins.ex.proto.property.VehicleMode;
import huins.ex.service.FlightManager;
import huins.ex.service.GCSFlightService;
import huins.ex.util.ConnectionException;

/**
 * Created by suhak on 15. 6. 15.
 */
public class FlightInterface  extends IFlightInterface.Stub implements FlightEventsListener, IBinder.DeathRecipient{


    private final static String TAG = FlightInterface.class.getSimpleName();

    private final Context context;
    private ConnectionParameter connectionParams;
    private FlightManager flightmanager;
    private GCSFlightService gcsFlightService;
    private final String ownerId;
    private final FlightInterfaces.Handler flighthandler;
    private final IApiListener apiListener;

    private final ConcurrentLinkedQueue<IObserver> observersList;
    private final ConcurrentLinkedQueue<IMavlinkObserver> mavlinkObserversList;

    public String getOwnerId() {
        return ownerId;
    }


    //construction FlightInterface
    public FlightInterface(GCSFlightService gcsService, Looper looper, IApiListener listener, String ownerId) {

        this.gcsFlightService = gcsService;
        this.context = gcsService.getApplicationContext();

        final Handler handler = new Handler(looper);

        this.flighthandler = new FlightInterfaces.Handler() {
            @Override
            public void removeCallbacks(Runnable thread) {
                handler.removeCallbacks(thread);
            }

            @Override
            public void post(Runnable thread) {
                handler.post(thread);
            }

            @Override
            public void postDelayed(Runnable thread, long timeout) {
                handler.postDelayed(thread, timeout);
            }
        };
        this.ownerId = ownerId;

        observersList = new ConcurrentLinkedQueue<>();
        mavlinkObserversList = new ConcurrentLinkedQueue<>();

        this.apiListener = listener;
        try {
            this.apiListener.asBinder().linkToDeath(this, 0);
            checkForSelfRelease();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            gcsService.releaseFlightInterface(this.ownerId);
        }
    }


    public void destroy() {
        Log.d(TAG, "Destroying flight api instance for " + this.ownerId);
        this.observersList.clear();
        this.mavlinkObserversList.clear();

        this.apiListener.asBinder().unlinkToDeath(this, 0);

        try {
            this.gcsFlightService.disconnectFlightManager(this.flightmanager, this.ownerId);
        } catch (ConnectionException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public boolean isConnected() {
         return flightmanager != null && flightmanager.isConnected();
    }

    public FlightManager getFlightManager() {
        return this.flightmanager;
    }

    private Flight getFlight() {
        if (this.flightmanager == null)
            return null;

        return this.flightmanager.getFlight();
    }


    private Follow getFollowMe() {
        if (this.flightmanager == null)
            return null;

        return this.flightmanager.getFollowMe();
    }
    @Override
    public Bundle getAttribute(String type) throws RemoteException {
        Bundle carrier = new Bundle();
        final Flight flight = getFlight();

        switch (type) {
            case AttributeType.STATE:
                carrier.putParcelable(type, FlightApiUtils.getState(flight, isConnected()));
                break;
            case AttributeType.GPS:
                carrier.putParcelable(type, FlightApiUtils.getGps(flight));
                break;
            case AttributeType.PARAMETERS:
                carrier.putParcelable(type, FlightApiUtils.getParameters(flight, context));
                break;
            case AttributeType.SPEED:
                carrier.putParcelable(type, FlightApiUtils.getSpeed(flight));
                break;
            case AttributeType.ATTITUDE:
                carrier.putParcelable(type, FlightApiUtils.getAttitude(flight));
                break;
            case AttributeType.HOME:
                carrier.putParcelable(type, FlightApiUtils.getHome(flight));
                break;
            case AttributeType.BATTERY:
                carrier.putParcelable(type, FlightApiUtils.getBattery(flight));
                break;
            case AttributeType.ALTITUDE:
                carrier.putParcelable(type, FlightApiUtils.getAltitude(flight));
                break;
            case AttributeType.MISSION:
                carrier.putParcelable(type, FlightApiUtils.getMission(flight));
                break;
            case AttributeType.SIGNAL:
                carrier.putParcelable(type, FlightApiUtils.getSignal(flight));
                break;
            case AttributeType.TYPE:
                carrier.putParcelable(type, FlightApiUtils.getType(flight));
                break;
            case AttributeType.GUIDED_STATE:
                carrier.putParcelable(type, FlightApiUtils.getGuidedState(flight));
                break;
            case AttributeType.FOLLOW_STATE:
                carrier.putParcelable(type, FlightApiUtils.getFollowState(getFollowMe()));
                break;
            case AttributeType.CAMERA:
                carrier.putParcelable(type, FlightApiUtils.getCameraProxy(flight, gcsFlightService.getCameraDetails()));
                break;

            case AttributeType.GOPRO:
                carrier.putParcelable(type, FlightApiUtils.getGoPro(flight));
                break;

            case AttributeType.MAGNETOMETER_CALIBRATION_STATUS:
                carrier.putParcelable(type, FlightApiUtils.getMagnetometerCalibrationStatus(flight));
                break;
        }

        return carrier;
    }

    @Override
    public void performAction(Action action) throws RemoteException {
        if (action == null)
            return;

        final String type = action.getType();
        if (type == null)
            return;

        Bundle data = action.getData();
        switch (type) {

            //Stream Actions
            case GimbalAction.ACTION_EO_START:
                Bundle eostartBundle = new Bundle(1);
                notifyAttributeUpdate(AttributeEvent.VIDEO_EO_START, eostartBundle);
                break;
            case GimbalAction.ACTION_IR_START:
                Bundle irstartpBundle = new Bundle(1);
                notifyAttributeUpdate(AttributeEvent.VIDEO_IR_START, irstartpBundle);
                break;
            case GimbalAction.ACTION_GIMBAL_PITCH_SEND:
                float pitch = data.getFloat(GimbalAction.EXTRA_GIMBALL_DATA);
                MavLinkGimbalControl.sendGimbalAxisPitch(getFlight(), pitch);
                break;
            case GimbalAction.ACTION_GIMBAL_YAW_SEND:
                float yaw = data.getFloat(GimbalAction.EXTRA_GIMBALL_DATA);
                MavLinkGimbalControl.sendGimbalAxisYaw(getFlight(), yaw);
                break;
            case GimbalAction.ACTION_GIMBAL_ROLL_SEND:
                float roll = data.getFloat(GimbalAction.EXTRA_GIMBALL_DATA);
                MavLinkGimbalControl.sendGimbalAxisRoll(getFlight(), roll);
                break;
            //Stream Actions
            case StreamActions.ACTION_RECORD_START:
                Bundle recordstartBundle = new Bundle(1);
                notifyAttributeUpdate(AttributeEvent.RTSP_STREAM_START, recordstartBundle);
                break;
            case StreamActions.ACTION_RECORD_STOP:
                Bundle recordstopBundle = new Bundle(1);
                notifyAttributeUpdate(AttributeEvent.RTSP_STREAM_STOP, recordstopBundle);
                break;
            case StreamActions.ACTION_STREAM_CAPTURE:
                Bundle rtspStreamCapture = new Bundle(1);
                notifyAttributeUpdate(AttributeEvent.RTSP_STREAM_CAPTURE, rtspStreamCapture);
                break;
            case StreamActions.ACTION_STREAM_ONOFF:
                Bundle rtspStreamOnOff = new Bundle(1);
                notifyAttributeUpdate(AttributeEvent.RTSP_STREAM_ONOFF, rtspStreamOnOff);
                break;
            case StreamActions.ACTION_CAMERA_ON:
                boolean is_on = data.getBoolean(StreamActions.EXTRA_CAMERA_DATA);
                MavLinkCameraOn.sendCameraOn(getFlight(), is_on);

                Bundle cameraOn = new Bundle(1);
                notifyAttributeUpdate(AttributeEvent.DRONE_CAMERA_ON, cameraOn);
                break;
            case StreamActions.ACTION_CAMERA_OFF:
                boolean is_off = data.getBoolean(StreamActions.EXTRA_CAMERA_DATA);
                MavLinkCameraOn.sendCameraOn(getFlight(), is_off);

                Bundle cameraOff = new Bundle(1);
                notifyAttributeUpdate(AttributeEvent.DRONE_CAMERA_OFF, cameraOff);
                break;
            case StreamActions.ACTION_SET_RESOLUTION:
                int resolutionType = data.getInt(StreamActions.EXTRA_CAMERA_RESOLUTION);
                MavLinkCameraResolution.sendCameraResolution(getFlight(), resolutionType);
            case RCAction.ACTION_RC_CTR_START:
                flightmanager.getRcOutput().enableRcOverride();
                flightmanager.getRcOutput().setRcChannel(flightmanager.getRcOutput().RUDDER, 0);
                flightmanager.getRcOutput().setRcChannel(flightmanager.getRcOutput().ELEVATOR, 0);
                flightmanager.getRcOutput().setRcChannel(flightmanager.getRcOutput().TROTTLE, 0);
                flightmanager.getRcOutput().setRcChannel(flightmanager.getRcOutput().AILERON, 0);
                break;

            case RCAction.ACTION_RC_CTR_END:
                flightmanager.getRcOutput().disableRcOverride();
                break;

            case RCAction.ACTION_RC_RUDDER_SEND:
                double rudder_data = data.getDouble(RCAction.EXTRA_RC_DATA);
                flightmanager.getRcOutput().setRcChannel(flightmanager.getRcOutput().RUDDER, rudder_data);
                break;
            case RCAction.ACTION_RC_ELEVATOR_SEND:
                double elevator_data = data.getDouble(RCAction.EXTRA_RC_DATA);
                flightmanager.getRcOutput().setRcChannel(flightmanager.getRcOutput().ELEVATOR, elevator_data);
                break;
            case RCAction.ACTION_RC_THROTTLE_SEND:
                double throttle_data = data.getDouble(RCAction.EXTRA_RC_DATA);
                flightmanager.getRcOutput().setRcChannel(flightmanager.getRcOutput().TROTTLE, throttle_data);
                break;
            case RCAction.ACTION_RC_AILERON_SEND:
                double aileron_data = data.getDouble(RCAction.EXTRA_RC_DATA);
                flightmanager.getRcOutput().setRcChannel(flightmanager.getRcOutput().AILERON, aileron_data);
                break;

            // MISSION ACTIONS
            case MissionActions.ACTION_GENERATE_DRONIE:
                final float bearing = FlightApiUtils.generateDronie(getFlight());
                if (bearing != -1) {
                    Bundle bundle = new Bundle(1);
                    bundle.putFloat(AttributeEventExtra.EXTRA_MISSION_DRONIE_BEARING, bearing);
                    notifyAttributeUpdate(AttributeEvent.MISSION_DRONIE_CREATED, bundle);
                }
                break;

            case MissionActions.ACTION_LOAD_WAYPOINTS:
                FlightApiUtils.loadWaypoints(getFlight());
                break;

            case MissionActions.ACTION_SET_MISSION:
                data.setClassLoader(Mission.class.getClassLoader());
                Mission mission = data.getParcelable(MissionActions.EXTRA_MISSION);
                //boolean pushToDrone = data.getBoolean(MissionActions.EXTRA_PUSH_TO_DRONE); ??? need to fix_ suhak
                boolean pushToDrone = true;
                FlightApiUtils.setMission(getFlight(), mission, pushToDrone);
                break;

            case MissionActions.ACTION_BUILD_COMPLEX_MISSION_ITEM:
                FlightApiUtils.buildComplexMissionItem(getFlight(), data);
                break;

            //CONNECTION ACTIONS
            case ConnectionActions.ACTION_CONNECT:
                data.setClassLoader(ConnectionParameter.class.getClassLoader());
                ConnectionParameter parameter = data.getParcelable(ConnectionActions.EXTRA_CONNECT_PARAMETER);
                connect(parameter);
                break;

            case ConnectionActions.ACTION_DISCONNECT:
                disconnect();
                break;

            //EXPERIMENTAL ACTIONS
            case ExperimentalActions.ACTION_EPM_COMMAND:
                boolean release = data.getBoolean(ExperimentalActions.EXTRA_EPM_RELEASE);
                FlightApiUtils.epmCommand(getFlight(), release);
                break;

            case ExperimentalActions.ACTION_TRIGGER_CAMERA:
                FlightApiUtils.triggerCamera(getFlight());
                break;

            case ExperimentalActions.ACTION_SEND_MAVLINK_MESSAGE:
                data.setClassLoader(MavlinkMessageWrapper.class.getClassLoader());
                MavlinkMessageWrapper messageWrapper = data.getParcelable(ExperimentalActions.EXTRA_MAVLINK_MESSAGE);
                FlightApiUtils.sendMavlinkMessage(getFlight(), messageWrapper);
                break;

            case ExperimentalActions.ACTION_SET_RELAY:
                if (flightmanager != null) {
                    int relayNumber = data.getInt(ExperimentalActions.EXTRA_RELAY_NUMBER);
                    boolean isOn = data.getBoolean(ExperimentalActions.EXTRA_IS_RELAY_ON);
                    MavLinkDoCmds.setRelay(flightmanager.getFlight(), relayNumber, isOn);
                }
            case ExperimentalActions.ACTION_SET_SERVO:
                if (flightmanager != null) {
                    int channel = data.getInt(ExperimentalActions.EXTRA_SERVO_CHANNEL);
                    int pwm = data.getInt(ExperimentalActions.EXTRA_SERVO_PWM);
                    MavLinkDoCmds.setServo(flightmanager.getFlight(), channel, pwm);
                }
                break;

            //GUIDED ACTIONS
            case GuidedActions.ACTION_DO_GUIDED_TAKEOFF:
                double takeoffAltitude = data.getDouble(GuidedActions.EXTRA_ALTITUDE);
                FlightApiUtils.doGuidedTakeoff(getFlight(), takeoffAltitude);
                break;

            case GuidedActions.ACTION_SEND_GUIDED_POINT:
                data.setClassLoader(Coordinate.class.getClassLoader());
                boolean force = data.getBoolean(GuidedActions.EXTRA_FORCE_GUIDED_POINT);
                Coordinate guidedPoint = data.getParcelable(GuidedActions.EXTRA_GUIDED_POINT);
                FlightApiUtils.sendGuidedPoint(getFlight(), guidedPoint, force);
                break;

            case GuidedActions.ACTION_SET_GUIDED_ALTITUDE:
                double guidedAltitude = data.getDouble(GuidedActions.EXTRA_ALTITUDE);
                FlightApiUtils.setGuidedAltitude(getFlight(), guidedAltitude);
                break;

            //PARAMETER ACTIONS
            case ParameterActions.ACTION_REFRESH_PARAMETERS:
                FlightApiUtils.refreshParameters(getFlight());
                break;

            case ParameterActions.ACTION_WRITE_PARAMETERS:
                data.setClassLoader(Parameters.class.getClassLoader());
                Parameters parameters = data.getParcelable(ParameterActions.EXTRA_PARAMETERS);
                FlightApiUtils.writeParameters(getFlight(), parameters);
                break;

            //DRONE STATE ACTIONS
            case StateActions.ACTION_ARM:
                boolean doArm = data.getBoolean(StateActions.EXTRA_ARM);
                FlightApiUtils.arm(getFlight(), doArm);
                break;

            case StateActions.ACTION_SET_VEHICLE_MODE:
                data.setClassLoader(VehicleMode.class.getClassLoader());
                VehicleMode newMode = data.getParcelable(StateActions.EXTRA_VEHICLE_MODE);
                FlightApiUtils.changeVehicleMode(getFlight(), newMode);
                break;

            //CALIBRATION ACTIONS
            case CalibrationActions.ACTION_START_IMU_CALIBRATION:
                if (!FlightApiUtils.startIMUCalibration(getFlight())) {
                    Bundle extrasBundle = new Bundle(1);
                    extrasBundle.putString(AttributeEventExtra.EXTRA_CALIBRATION_IMU_MESSAGE,
                            context.getString(R.string.failed_start_calibration_message));
                    notifyAttributeUpdate(AttributeEvent.CALIBRATION_IMU_ERROR, extrasBundle);
                }
                break;

            case CalibrationActions.ACTION_SEND_IMU_CALIBRATION_ACK:
                int imuAck = data.getInt(CalibrationActions.EXTRA_IMU_STEP);
                FlightApiUtils.sendIMUCalibrationAck(getFlight(), imuAck);
                break;

            case CalibrationActions.ACTION_START_MAGNETOMETER_CALIBRATION:
                final boolean retryOnFailure = data.getBoolean(CalibrationActions.EXTRA_RETRY_ON_FAILURE, false);
                final boolean saveAutomatically = data.getBoolean(CalibrationActions.EXTRA_SAVE_AUTOMATICALLY, true);
                final int startDelay = data.getInt(CalibrationActions.EXTRA_START_DELAY, 0);
                FlightApiUtils.startMagnetometerCalibration(getFlight(), retryOnFailure, saveAutomatically, startDelay);
                break;

            case CalibrationActions.ACTION_CANCEL_MAGNETOMETER_CALIBRATION:
                FlightApiUtils.cancelMagnetometerCalibration(getFlight());
                break;

            case CalibrationActions.ACTION_ACCEPT_MAGNETOMETER_CALIBRATION:
                FlightApiUtils.acceptMagnetometerCalibration(getFlight());
                break;

            //FOLLOW-ME ACTIONS
            case FollowMeActions.ACTION_ENABLE_FOLLOW_ME:
                data.setClassLoader(FollowType.class.getClassLoader());
                FollowType followType = data.getParcelable(FollowMeActions.EXTRA_FOLLOW_TYPE);
                FlightApiUtils.enableFollowMe(getFlightManager(), flighthandler, followType);
                break;

            case FollowMeActions.ACTION_UPDATE_FOLLOW_PARAMS:
                if (flightmanager != null) {
                    data.setClassLoader(Coordinate.class.getClassLoader());

                    final FollowAlgorithm followAlgorithm = this.flightmanager.getFollowMe().getFollowAlgorithm();
                    if (followAlgorithm != null) {
                        Map<String, Object> paramsMap = new HashMap<>();
                        Set<String> dataKeys = data.keySet();

                        for (String key : dataKeys) {
                            if (FollowType.EXTRA_FOLLOW_ROI_TARGET.equals(key)) {
                                Coordinate target = data.getParcelable(key);
                                if (target != null) {
                                    final Coord2D roiTarget;
                                    if (target instanceof CoordinateExtend) {
                                        roiTarget = new Coord3D(target.getLatitude(), target.getLongitude(),
                                                ((CoordinateExtend) target).getAltitude());
                                    } else {
                                        roiTarget = new Coord2D(target.getLatitude(), target.getLongitude());
                                    }
                                    paramsMap.put(key, roiTarget);
                                }
                            } else
                                paramsMap.put(key, data.get(key));
                        }

                        followAlgorithm.updateAlgorithmParams(paramsMap);
                    }
                }
                break;

            case FollowMeActions.ACTION_DISABLE_FOLLOW_ME:
                FlightApiUtils.disableFollowMe(getFollowMe());
                break;

            //************ CAMERA ACTIONS *************//
            case CameraActions.ACTION_START_VIDEO_RECORDING:
                FlightApiUtils.startVideoRecording(getFlight());
                break;

            case CameraActions.ACTION_STOP_VIDEO_RECORDING:
                FlightApiUtils.stopVideoRecording(getFlight());
                break;
        }
    }

    @Override
    public void performAsyncAction(Action action) throws RemoteException {
        performAction(action);
    }


    public void connect(ConnectionParameter connParams) {
        try {
            this.connectionParams = connParams;
            this.flightmanager = gcsFlightService.connectFlightManager(connParams, ownerId, this);
        } catch (ConnectionException e) {
            notifyConnectionFailed(new ConnectionResult(0, e.getMessage()));
            disconnect();
        }
    }

    public void disconnect() {
        try {
            gcsFlightService.disconnectFlightManager(this.flightmanager, this.ownerId);
            this.flightmanager = null;
        } catch (ConnectionException e) {
            notifyConnectionFailed(new ConnectionResult(0, e.getMessage()));
        }
    }

    private void checkForSelfRelease() {
        //Check if the apiListener is still connected instead.
        if (!apiListener.asBinder().pingBinder()) {
            Log.w(TAG, "Client is not longer available.");
            this.context.startService(new Intent(this.context, GCSFlightService.class)
                    .setAction(GCSFlightService.ACTION_RELEASE_API_INSTANCE)
                    .putExtra(GCSFlightService.EXTRA_API_INSTANCE_APP_ID, this.ownerId));
        }
    }

    @Override
    public void addAttributesObserver(IObserver observer) throws RemoteException {
        if (observer != null)
            observersList.add(observer);
    }

    @Override
    public void removeAttributesObserver(IObserver observer) throws RemoteException {
        if (observer != null) {
            observersList.remove(observer);

            checkForSelfRelease();
        }
    }

    @Override
    public void addMavlinkObserver(IMavlinkObserver observer) throws RemoteException {
        if (observer != null)
            mavlinkObserversList.add(observer);
    }

    @Override
    public void removeMavlinkObserver(IMavlinkObserver observer) throws RemoteException {
        if (observer != null) {
            mavlinkObserversList.remove(observer);
            checkForSelfRelease();
        }
    }


    private void notifyAttributeUpdate(List<Pair<String, Bundle>> attributesInfo) {
        if (observersList.isEmpty() || attributesInfo == null || attributesInfo.isEmpty())
            return;

        for (Pair<String, Bundle> info : attributesInfo) {
            notifyAttributeUpdate(info.first, info.second);
        }
    }

    private void notifyAttributeUpdate(String attributeEvent, Bundle extrasBundle) {
        if (observersList.isEmpty())
            return;

        if (attributeEvent != null) {
            for (IObserver observer : observersList) {
                try {
                    observer.onAttributeUpdated(attributeEvent, extrasBundle);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                    try {
                        removeAttributesObserver(observer);
                    } catch (RemoteException e1) {
                        Log.e(TAG, e1.getMessage(), e1);
                    }
                }
            }
        }
    }

    private void notifyConnectionFailed(ConnectionResult result) {
        if (result != null) {
            try {
                apiListener.onConnectionFailed(result);
                return;
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to forward connection fail to client.", e);
            }
            checkForSelfRelease();
        }
    }

    @Override
    public void onReceivedMavLinkMessage(MAVLinkMessage msg) {
        if (mavlinkObserversList.isEmpty())
            return;

        if (msg != null) {
            final MavlinkMessageWrapper msgWrapper = new MavlinkMessageWrapper(msg);
            for (IMavlinkObserver observer : mavlinkObserversList) {
                try {
                    observer.onMavlinkMessageReceived(msgWrapper);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                    try {
                        removeMavlinkObserver(observer);
                    } catch (RemoteException e1) {
                        Log.e(TAG, e1.getMessage(), e1);
                    }
                }
            }
        }
    }

    @Override
    public void onMessageLogged(int logLevel, String message) {
        final Bundle args = new Bundle(2);
        args.putInt(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE_LEVEL, logLevel);
        args.putString(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE, message);
        notifyAttributeUpdate(AttributeEvent.AUTOPILOT_MESSAGE, args);
    }

    @Override
    public void onFlightEvent(FlightInterfaces.FlightEventsType event, Flight flight) {
        Bundle extrasBundle = null;
        String flightEvent = null;
        final List<Pair<String, Bundle>> attributesInfo = new ArrayList<>();

        switch (event) {
            case DISCONNECTED:
                //Broadcast the disconnection with the vehicle.
                context.sendBroadcast(new Intent(GCSEvent.ACTION_VEHICLE_DISCONNECTION)
                        .putExtra(GCSEvent.EXTRA_APP_ID, ownerId));

                flightEvent = AttributeEvent.STATE_DISCONNECTED;
                break;

            case GUIDEDPOINT:
                flightEvent = AttributeEvent.GUIDED_POINT_UPDATED;
                break;

            case RADIO:
                flightEvent = AttributeEvent.SIGNAL_UPDATED;
                break;

            case RC_IN:
                break;
            case RC_OUT:
                break;

            case ARMING_STARTED:
            case ARMING:
                flightEvent = AttributeEvent.STATE_ARMING;
                break;

            case AUTOPILOT_WARNING:
                extrasBundle = new Bundle(1);
                extrasBundle.putString(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID, flight.getState().getErrorId());
                flightEvent = AttributeEvent.AUTOPILOT_ERROR;
                break;

            case MODE:
                flightEvent = AttributeEvent.STATE_VEHICLE_MODE;
                break;

            case NAVIGATION:
            case ATTITUDE:
            case ORIENTATION:
                flightEvent = AttributeEvent.ATTITUDE_UPDATED;
                break;

            case SPEED:
                flightEvent = AttributeEvent.SPEED_UPDATED;
                break;

            case BATTERY:
                flightEvent = AttributeEvent.BATTERY_UPDATED;
                break;

            case STATE:
                flightEvent = AttributeEvent.STATE_UPDATED;
                break;

            case MISSION_UPDATE:
                flightEvent = AttributeEvent.MISSION_UPDATED;
                break;

            case MISSION_RECEIVED:
                flightEvent = AttributeEvent.MISSION_RECEIVED;
                break;

            case FIRMWARE:
            case TYPE:
                flightEvent = AttributeEvent.TYPE_UPDATED;
                break;

            case HOME:
                flightEvent = AttributeEvent.HOME_UPDATED;
                break;

            case GPS:
                flightEvent = AttributeEvent.GPS_POSITION;
                break;

            case GPS_FIX:
                flightEvent = AttributeEvent.GPS_FIX;
                break;

            case GPS_COUNT:
                flightEvent = AttributeEvent.GPS_COUNT;
                break;

            case CALIBRATION_IMU:
                final String calIMUMessage = flight.getCalibrationSetup().getMessage();
                extrasBundle = new Bundle(1);
                extrasBundle.putString(AttributeEventExtra.EXTRA_CALIBRATION_IMU_MESSAGE, calIMUMessage);
                flightEvent = AttributeEvent.CALIBRATION_IMU;
                break;

            case CALIBRATION_TIMEOUT:
                    /*
                 * here we will check if we are in calibration mode but if at
				 * the same time 'msg' is empty - then it is actually not doing
				 * calibration what we should do is to reset the calibration
				 * flag and re-trigger the HEARBEAT_TIMEOUT this however should
				 * not be happening
				 */
                final AccelCalibration accelCalibration = flight.getCalibrationSetup();
                final String message = accelCalibration.getMessage();
                if (accelCalibration.isCalibrating() && TextUtils.isEmpty(message)) {
                    accelCalibration.cancelCalibration();
                    flightEvent = AttributeEvent.HEARTBEAT_TIMEOUT;
                } else {
                    extrasBundle = new Bundle(1);
                    extrasBundle.putString(AttributeEventExtra.EXTRA_CALIBRATION_IMU_MESSAGE, message);
                    flightEvent = AttributeEvent.CALIBRATION_IMU_TIMEOUT;
                }

                break;

            case HEARTBEAT_TIMEOUT:
                flightEvent = AttributeEvent.HEARTBEAT_TIMEOUT;
                break;

            case CONNECTING:
                flightEvent = AttributeEvent.STATE_CONNECTING;
                break;

            case CONNECTION_FAILED:
                disconnect();
                onConnectionFailed("");
                break;

            case HEARTBEAT_FIRST:
                final Bundle heartBeatExtras = new Bundle(1);
                heartBeatExtras.putInt(AttributeEventExtra.EXTRA_MAVLINK_VERSION, flight.getMavlinkVersion());
                attributesInfo.add(Pair.create(AttributeEvent.HEARTBEAT_FIRST, heartBeatExtras));

            case CONNECTED:
                //Broadcast the vehicle connection.
                final ConnectionParameter sanitizedParameter = new ConnectionParameter(connectionParams
                        .getConnectionType(), connectionParams.getParamsBundle(), null);

                context.sendBroadcast(new Intent(GCSEvent.ACTION_VEHICLE_CONNECTION)
                        .putExtra(GCSEvent.EXTRA_APP_ID, ownerId)
                        .putExtra(GCSEvent.EXTRA_VEHICLE_CONNECTION_PARAMETER, sanitizedParameter));

                attributesInfo.add(Pair.<String, Bundle>create(AttributeEvent.STATE_CONNECTED, null));
                break;

            case HEARTBEAT_RESTORED:
                extrasBundle = new Bundle(1);
                extrasBundle.putInt(AttributeEventExtra.EXTRA_MAVLINK_VERSION, flight.getMavlinkVersion());
                flightEvent = AttributeEvent.HEARTBEAT_RESTORED;
                break;

            case MISSION_SENT:
                flightEvent = AttributeEvent.MISSION_SENT;
                break;

            case INVALID_POLYGON:
                break;

            case MISSION_WP_UPDATE:
                final int currentWaypoint = flight.getMissionStats().getCurrentWP();
                extrasBundle = new Bundle(1);
                extrasBundle.putInt(AttributeEventExtra.EXTRA_MISSION_CURRENT_WAYPOINT, currentWaypoint);
                flightEvent = AttributeEvent.MISSION_ITEM_UPDATED;
                break;

            case FOLLOW_START:
                flightEvent = AttributeEvent.FOLLOW_START;
                break;

            case FOLLOW_STOP:
                flightEvent = AttributeEvent.FOLLOW_STOP;
                break;

            case FOLLOW_UPDATE:
            case FOLLOW_CHANGE_TYPE:
                flightEvent = AttributeEvent.FOLLOW_UPDATE;
                break;

            case ALTITUDE:
                flightEvent = AttributeEvent.ALTITUDE_UPDATED;
                break;

            case WARNING_SIGNAL_WEAK:
                flightEvent = AttributeEvent.SIGNAL_WEAK;
                break;

            case WARNING_NO_GPS:
                flightEvent = AttributeEvent.WARNING_NO_GPS;
                break;

            case MAGNETOMETER:
                break;

            case FOOTPRINT:
                flightEvent = AttributeEvent.CAMERA_FOOTPRINTS_UPDATED;
                break;

            case GOPRO_STATUS_UPDATE:
                flightEvent = AttributeEvent.GOPRO_STATE_UPDATED;
                break;

            case EKF_STATUS_UPDATE:
                flightEvent = AttributeEvent.STATE_EKF_REPORT;
                break;

            case EKF_POSITION_STATE_UPDATE:
                flightEvent = AttributeEvent.STATE_EKF_POSITION;
                break;
        }

        if (flightEvent != null) {
            notifyAttributeUpdate(flightEvent, extrasBundle);
        }

        if (!attributesInfo.isEmpty()) {
            notifyAttributeUpdate(attributesInfo);
        }
    }

    @Override
    public void onBeginReceivingParameters() {
        notifyAttributeUpdate(AttributeEvent.PARAMETERS_REFRESH_STARTED, null);
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        Bundle paramsBundle = new Bundle(4);
        paramsBundle.putInt(AttributeEventExtra.EXTRA_PARAMETER_INDEX, index);
        paramsBundle.putInt(AttributeEventExtra.EXTRA_PARAMETERS_COUNT, count);
        paramsBundle.putString(AttributeEventExtra.EXTRA_PARAMETER_NAME, parameter.name);
        paramsBundle.putDouble(AttributeEventExtra.EXTRA_PARAMETER_VALUE, parameter.value);
        notifyAttributeUpdate(AttributeEvent.PARAMETER_RECEIVED, paramsBundle);
    }

    @Override
    public void onEndReceivingParameters() {
        notifyAttributeUpdate(AttributeEvent.PARAMETERS_REFRESH_COMPLETED, null);
    }

    @Override
    public FlightSharePreferences getFlightSharePreferences() {
        if (connectionParams == null)
            return null;

        return connectionParams.getFlightSharePreferences();
    }

    @Override
    public void onConnectionFailed(String error) {
        notifyConnectionFailed(new ConnectionResult(0, error));
    }

    @Override
    public void binderDied() {
        checkForSelfRelease();
    }

    @Override
    public void onCalibrationCancelled() {
        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_CANCELLED, null);
    }

    @Override
    public void onCalibrationProgress(msg_mag_cal_progress progress) {
        Bundle progressBundle = new Bundle(1);
        progressBundle.putParcelable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_PROGRESS,
                FlightApiUtils.getMagnetometerCalibrationProgress(progress));

        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_PROGRESS, progressBundle);
    }

    @Override
    public void onCalibrationCompleted(msg_mag_cal_report report) {
        Bundle reportBundle = new Bundle(1);
        reportBundle.putParcelable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_RESULT,
                FlightApiUtils.getMagnetometerCalibrationResult(report));

        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_COMPLETED, reportBundle);
    }
}

