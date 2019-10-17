package huins.ex.proto;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import huins.ex.interfaces.CalibrationApi;
import huins.ex.interfaces.ConnectInterface;
import huins.ex.interfaces.FlightListener;
import huins.ex.interfaces.FlightStateInterface;
import huins.ex.interfaces.FollowInterface;
import huins.ex.interfaces.GuidedInterface;
import huins.ex.model.FlightAttributes;
import huins.ex.model.FlightEvent;
import huins.ex.proto.action.Action;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.connection.ConnectionResult;
import huins.ex.proto.gcs.follow.FollowState;
import huins.ex.proto.gcs.follow.FollowType;
import huins.ex.proto.mission.Mission;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.MissionItem.ComplexItem;
import huins.ex.proto.property.Altitude;
import huins.ex.proto.property.Attitude;
import huins.ex.proto.property.Battery;
import huins.ex.proto.property.FlightState;
import huins.ex.proto.property.Gps;
import huins.ex.proto.property.GuidedState;
import huins.ex.proto.property.Home;
import huins.ex.proto.property.Parameter;
import huins.ex.proto.property.Parameters;
import huins.ex.proto.property.Signal;
import huins.ex.proto.property.Speed;
import huins.ex.proto.property.State;
import huins.ex.proto.property.Type;
import huins.ex.proto.property.VehicleMode;
import huins.ex.service.GCSinterface.IFlightInterface;

//import huins.uav_gcs.service.GCSinterface.IFlightInterface;

/**
 * Created by suhak on 15. 6. 9.
 */
public class Flight {

    //valuable...
    private boolean is_connected = false;
    final Context context;
    private Handler handler;
    private ControlTower controlTower;
    private FlightObserver flightObserver;
    private FlightApiListener apiListener;

    private ExecutorService asyncScheduler;
    private long startTime = 0L;
    private long elapsedFlightTime = 0L;

    private IFlightInterface iFlightInterface;

    private ConnectionParameter connectionParameter;

    private static final String CLASS_NAME = Flight.class.getName();
    private static final String TAG = Flight.class.getSimpleName();

    //collision
    public static final int COLLISION_SECONDS_BEFORE_COLLISION = 2;
    public static final double COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND = -3.0;
    public static final double COLLISION_SAFE_ALTITUDE_METERS = 1.0;

    public static final String ACTION_GROUND_COLLISION_IMMINENT = CLASS_NAME + ".ACTION_GROUND_COLLISION_IMMINENT";
    public static final String EXTRA_IS_GROUND_COLLISION_IMMINENT = "extra_is_ground_collision_imminent";

    public void loadWaypoints() {
//        MissionApi.loadWaypoints(this);
    }

    private final IBinder.DeathRecipient binderDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            notifyDroneServiceInterrupted("Lost access to the drone api.");
        }
    };

    private final ConcurrentLinkedQueue<FlightListener> flightListeners = new ConcurrentLinkedQueue();


    void start()
    {
        if (!controlTower.isTowerConnected())
                throw new IllegalStateException("Service manager must be connected.");

        if (isStarted())
                return;

        try {
            this.iFlightInterface = controlTower.getGCSServices().registerFlightApi(this.apiListener, controlTower.getApplicationId());
            this.iFlightInterface.asBinder().linkToDeath(binderDeathRecipient, 0);
        } catch (RemoteException e) {
            throw new IllegalStateException("Unable to retrieve a valid drone handle.");
        }

        if (asyncScheduler == null || asyncScheduler.isShutdown())
        asyncScheduler = Executors.newFixedThreadPool(1);

        addAttributesObserver(this.flightObserver);
        resetFlightTimer();
    }

    void destroy() {
        removeAttributesObserver(this.flightObserver);

        try {
            if (isStarted()) {
                this.iFlightInterface.asBinder().unlinkToDeath(binderDeathRecipient, 0);
                controlTower.getGCSServices().releaseFlightApi(this.iFlightInterface);
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (asyncScheduler != null) {
            asyncScheduler.shutdownNow();
            asyncScheduler = null;
        }

        this.iFlightInterface = null;
        flightListeners.clear();
    }



    public interface OnAttributeRetrievedCallback<T extends Parcelable> {
        void onRetrievalSucceed(T attribute);

        void onRetrievalFailed();
    }

    public static class AttributeRetrievedListener<T extends Parcelable> implements OnAttributeRetrievedCallback<T> {

        @Override
        public void onRetrievalSucceed(T attribute) {
        }

        @Override
        public void onRetrievalFailed() {
        }
    }



    void init(ControlTower controlTower, Handler handler) {
        this.handler = handler;
        this.controlTower = controlTower;
        this.apiListener = new FlightApiListener(this);
        this.flightObserver = new FlightObserver(this);
    }
    void destory()
    {
        this.removeAttributesObserver(this.flightObserver);

        try {
            if(this.isStarted()) {
                this.iFlightInterface.asBinder().unlinkToDeath(this.binderDeathRecipient, 0);
                this.controlTower.getGCSServices().releaseFlightApi(this.iFlightInterface);
            }
        } catch (RemoteException var2) {
            //Log.e(TAG, var2.getMessage(), var2);
        }

        if(this.asyncScheduler != null) {
            this.asyncScheduler.shutdownNow();
            this.asyncScheduler = null;
        }

        this.iFlightInterface = null;
        this.flightListeners.clear();
    }

    public boolean isStarted() {
        return iFlightInterface != null && iFlightInterface.asBinder().pingBinder();
    }

    public Flight(Context context) {
        this.context = context;
    }

    public void setMission(Mission mission, boolean pushToFlight) {
//        MissionApi.setMission(this, mission, pushToFlight);
    }

    public void generateFlight() {
//        MissionApi.generateFlight(this);
    }

    public void changeVehicleMode(VehicleMode newMode) {
        FlightStateInterface.setVehicleMode(this, newMode);
    }

    public void arm(boolean arm) {
        FlightStateInterface.arm(this, arm);
    }

    /*
    public void startMagnetometerCalibration(double[] startPointsX, double[] startPointsY, double[] startPointsZ) {
        CalibrationApi.startMagnetometerCalibration(this, startPointsX, startPointsY, startPointsZ);
    }

    public void stopMagnetometerCalibration() {
        CalibrationApi.stopMagnetometerCalibration(this);
    }
    */

    public void startIMUCalibration() {
        CalibrationApi.startIMUCalibration(this);
    }

    public void sendIMUCalibrationAck(int step) {
        CalibrationApi.sendIMUAck(this, step);
    }


    //TODO...
    public boolean  IsConnected() {

        State flightState = getAttribute(AttributeType.STATE);
        return isStarted() && flightState.isConnected();
    }
    public ConnectionParameter getConnectionParameter() {
        return this.connectionParameter;
    }

    public  void connect(ConnectionParameter connParams)
    {
        if(ConnectInterface.connect(this, connParams)) {
            this.connectionParameter = connParams;
        }
    }

    public  void disconnect()
    {
        if(ConnectInterface.disconnect(this)) {
            this.connectionParameter = null;
        }
    }

    public boolean performAction(Action action) {
        if(this.isStarted()) {
            try {
                this.iFlightInterface.performAction(action);
                return true;
            } catch (RemoteException var3) {
                this.handleRemoteException(var3);
            }
        }

        return false;
    }

    public boolean performAsyncAction(Action action) {
        if (isStarted()) {
            try {
                iFlightInterface.performAsyncAction(action);
                return true;
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return false;
    }

    private void handleRemoteException(RemoteException e) {
        if (iFlightInterface != null && !iFlightInterface.asBinder().pingBinder()) {
            final String errorMsg = e.getMessage();
            Log.e(TAG, errorMsg, e);
            notifyDroneServiceInterrupted(errorMsg);
        }
    }


    public void unregisterFlightListener(FlightListener listener) {
        if(listener != null) {
            this.flightListeners.remove(listener);
        }
    }

    public void registerFlightListener(FlightListener listener) {
        if(listener != null) {
            if(!this.flightListeners.contains(listener)) {
                this.flightListeners.add(listener);
            }

        }
    }

    public void enableFollowMe(FollowType followType) {
        FollowInterface.enableFollowMe(this, followType);
    }

    public void disableFollowMe() {
        FollowInterface.disableFollowMe(this);
    }


    private void removeAttributesObserver(IObserver observer) {
        if (isStarted()) {
            try {
                this.iFlightInterface.removeAttributesObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    private void addAttributesObserver(IObserver observer) {
        if (isStarted()) {
            try {
                this.iFlightInterface.addAttributesObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public double getSpeedParameter() {
        Parameters params = (Parameters)this.getAttribute("huins.gcs.PARAMETERS");
        if(params != null) {
            Parameter speedParam = params.getParameter("WPNAV_SPEED");
            if(speedParam != null) {
                return speedParam.getValue();
            }
        }

        return 0.0D;
    }

    public void resetFlightTimer() {
        this.elapsedFlightTime = 0L;
        this.startTime = SystemClock.elapsedRealtime();
    }

    public void stopTimer() {
        this.elapsedFlightTime += SystemClock.elapsedRealtime() - this.startTime;
        this.startTime = SystemClock.elapsedRealtime();
    }

    public long getFlightTime() {
        FlightState flightState = (FlightState)this.getAttribute("huins.gcs.STATE");
        if(flightState != null && flightState.isFlying()) {
            this.elapsedFlightTime += SystemClock.elapsedRealtime() - this.startTime;
            this.startTime = SystemClock.elapsedRealtime();
        }

        return this.elapsedFlightTime / 1000L;
    }


    public <T extends MissionItem> void buildMissionItemsAsync(final Flight.OnMissionItemsBuiltCallback<T> callback, final ComplexItem... missionItems) {
        if(callback == null) {
            throw new IllegalArgumentException("Callback must be non-null.");
        } else if(missionItems != null && missionItems.length != 0) {
            this.asyncScheduler.execute(new Runnable() {
                public void run() {
                    ComplexItem[] arr$ = missionItems;
                    int len$ = arr$.length;

                    for(int i$ = 0; i$ < len$; ++i$) {
                        ComplexItem missionItem = arr$[i$];
//                        MissionApi.buildMissionItem(Flight.this, missionItem);
                    }

                    Flight.this.handler.post(new Runnable() {
                        public void run() {
                            callback.onMissionItemsBuilt(missionItems);
                        }
                    });
                }
            });
        }
    }

    //attributes
    public <T extends Parcelable> T getAttribute(String type) {
        if (!isStarted() || type == null)
            return this.getAttributeDefaultValue(type);

        T attribute = null;
        Bundle carrier = null;
        try {
            carrier = iFlightInterface.getAttribute(type);
        } catch (RemoteException e) {
            //handleRemoteException(e);
        }

        if (carrier != null) {
            ClassLoader classLoader = this.context.getClassLoader();
            if (classLoader != null) {
                carrier.setClassLoader(classLoader);
                attribute = carrier.getParcelable(type);
            }
        }

        return attribute == null ? this.<T>getAttributeDefaultValue(type) : attribute;
    }

    public <T extends Parcelable> void getAttributeAsync(final String attributeType,
                                                         final OnAttributeRetrievedCallback<T> callback) {
        if (callback == null)
            throw new IllegalArgumentException("Callback must be non-null.");

        if (!isStarted()) {
            callback.onRetrievalFailed();
            return;
        }

        asyncScheduler.execute(new Runnable() {
            @Override
            public void run() {
                final T attribute = getAttribute(attributeType);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (attribute == null)
                            callback.onRetrievalFailed();
                        else
                            callback.onRetrievalSucceed(attribute);
                    }
                });
            }
        });
    }

    private <T extends Parcelable> T getAttributeDefaultValue(String attributeType) {
        if(attributeType == null)
            return null;

        switch (attributeType) {
            case FlightAttributes.ALTITUDE:
                return (T) new Altitude();

            case FlightAttributes.GPS:
                return (T) new Gps();

            case FlightAttributes.STATE:
                return (T) new FlightState();

            case FlightAttributes.PARAMETERS:
                return (T) new Parameters();

            case FlightAttributes.SPEED:
                return (T) new Speed();

            case FlightAttributes.ATTITUDE:
                return (T) new Attitude();

            case FlightAttributes.HOME:
                return (T) new Home();

            case FlightAttributes.BATTERY:
                return (T) new Battery();

            case FlightAttributes.MISSION:
                return (T) new Mission();

            case FlightAttributes.SIGNAL:
                return (T) new Signal();

            case FlightAttributes.GUIDED_STATE:
                return (T) new GuidedState();

            case FlightAttributes.TYPE:
                return (T) new Type();

            case FlightAttributes.FOLLOW_STATE:
                return (T) new FollowState();


            case FlightAttributes.MAGNETOMETER_CALIBRATION_STATUS:
            //    return (T) new MagnetometerCalibrationStatus();

            //case AttributeType.CAMERA:
            default:
                return null;
        }
    }

    //notify


    void notifyFlightConnectionFailed(final ConnectionResult result) {
        if (flightListeners.isEmpty())
            return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (FlightListener listener : flightListeners)
                    listener.onFlightConnectionFailed(result);
            }
        });
    }


    void notifyAttributeUpdated(final String attributeEvent, final Bundle extras) {
        //Update the bundle classloader
        if(extras != null)
            extras.setClassLoader(context.getClassLoader());

        if (FlightEvent.STATE_UPDATED.equals(attributeEvent)) {
            getAttributeAsync(FlightAttributes.STATE, new OnAttributeRetrievedCallback<FlightState>() {
                @Override
                public void onRetrievalSucceed(FlightState flightState) {
                    if (flightState.isFlying())
                        resetFlightTimer();
                    else
                        stopTimer();
                }

                @Override
                public void onRetrievalFailed() {
                    stopTimer();
                }
            });
        } else if (FlightEvent.SPEED_UPDATED.equals(attributeEvent)) {
            checkForGroundCollision();
        }

        if (flightListeners.isEmpty())
            return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (FlightListener listener : flightListeners)
                    listener.onFlightEvent(attributeEvent, extras);
            }
        });
    }

    void notifyDroneServiceInterrupted(final String errorMsg) {
        if (flightListeners.isEmpty())
            return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (FlightListener listener : flightListeners)
                    listener.onFlightServiceInterrupted(errorMsg);
            }
        });
    }
    private void checkForGroundCollision() {
        Speed speed = getAttribute(FlightAttributes.SPEED);
        Altitude altitude = getAttribute(FlightAttributes.ALTITUDE);
        if (speed == null || altitude == null)
            return;

        double verticalSpeed = speed.getVerticalSpeed();
        double altitudeValue = altitude.getAltitude();

        boolean isCollisionImminent = altitudeValue
                + (verticalSpeed * COLLISION_SECONDS_BEFORE_COLLISION) < 0
                && verticalSpeed < COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND
                && altitudeValue > COLLISION_SAFE_ALTITUDE_METERS;

        Bundle extrasBundle = new Bundle(1);
        extrasBundle.putBoolean(EXTRA_IS_GROUND_COLLISION_IMMINENT, isCollisionImminent);
        notifyAttributeUpdated(ACTION_GROUND_COLLISION_IMMINENT, extrasBundle);
    }


    public interface OnMissionItemsBuiltCallback<T extends MissionItem> {
        void onMissionItemsBuilt(ComplexItem<T>[] var1);
    }

    public void doGuidedTakeoff(double altitude) {
        GuidedInterface.takeoff(this, altitude);
    }

    public void pauseAtCurrentLocation() {
        GuidedInterface.pauseAtCurrentLocation(this);
    }

    public void sendGuidedPoint(Coordinate point, boolean force) {
        GuidedInterface.sendGuidedPoint(this, point, force);
    }

}
