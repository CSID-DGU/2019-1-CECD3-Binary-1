package huins.ex.service.GCSinterface;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import huins.ex.com.MAVLink.Messages.ApmModes;
import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.ardupilotmega.msg_ekf_status_report;
import huins.ex.com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import huins.ex.com.MAVLink.ardupilotmega.msg_mag_cal_report;
import huins.ex.com.MAVLink.enums.MAG_CAL_STATUS;
import huins.ex.com.MAVLink.enums.MAV_TYPE;
import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.camera.GoProImpl;
import huins.ex.core.Flight.profiles.VehicleProfile;
import huins.ex.core.Flight.variables.Camera;
import huins.ex.core.Flight.variables.GPS;
import huins.ex.core.Flight.variables.GuidedPoint;
import huins.ex.core.Flight.variables.Orientation;
import huins.ex.core.Flight.variables.Radio;
import huins.ex.core.Flight.variables.calibration.AccelCalibration;
import huins.ex.core.Flight.variables.calibration.MagnetometerCalibrationImpl;
import huins.ex.core.MAVLink.MavLinkArm;
import huins.ex.core.MAVLink.command.doCmd.MavLinkDoCmds;
import huins.ex.core.gcs.follow.Follow;
import huins.ex.core.gcs.follow.FollowAlgorithm;
import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.helpers.coordinates.Coord3D;
import huins.ex.core.model.Flight;
import huins.ex.core.survey.Footprint;
import huins.ex.proto.Coordinate;
import huins.ex.proto.CoordinateExtend;
import huins.ex.proto.calibration.magnetometer.MagnetometerCalibrationProgress;
import huins.ex.proto.calibration.magnetometer.MagnetometerCalibrationResult;
import huins.ex.proto.calibration.magnetometer.MagnetometerCalibrationStatus;
import huins.ex.proto.camera.GoPro;
import huins.ex.proto.gcs.follow.FollowState;
import huins.ex.proto.gcs.follow.FollowType;
import huins.ex.proto.mavlink.MavlinkMessageWrapper;
import huins.ex.proto.mission.Mission;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.complex.CameraDetail;
import huins.ex.proto.mission.item.complex.StructureScanner;
import huins.ex.proto.mission.item.complex.Survey;
import huins.ex.proto.property.Altitude;
import huins.ex.proto.property.Attitude;
import huins.ex.proto.property.Battery;
import huins.ex.proto.property.CameraProxy;
import huins.ex.proto.property.EkfStatus;
import huins.ex.proto.property.FootPrint;
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
import huins.ex.service.FlightManager;
import huins.ex.util.File.IO.ParameterMetadataLoader;
import huins.ex.util.Math.MathUtils;
import huins.ex.util.ProxyUtils;

/**
 * Created by suhak on 15. 7. 15.
 */
public class FlightApiUtils {

    private static final String TAG = FlightApiUtils.class.getSimpleName();

    static VehicleMode getVehicleMode(ApmModes mode) {
        switch (mode) {
            case FIXED_WING_MANUAL:
                return VehicleMode.PLANE_MANUAL;
            case FIXED_WING_CIRCLE:
                return VehicleMode.PLANE_CIRCLE;

            case FIXED_WING_STABILIZE:
                return VehicleMode.PLANE_STABILIZE;

            case FIXED_WING_TRAINING:
                return VehicleMode.PLANE_TRAINING;

            case FIXED_WING_FLY_BY_WIRE_A:
                return VehicleMode.PLANE_FLY_BY_WIRE_A;

            case FIXED_WING_FLY_BY_WIRE_B:
                return VehicleMode.PLANE_FLY_BY_WIRE_B;

            case FIXED_WING_AUTO:
                return VehicleMode.PLANE_AUTO;

            case FIXED_WING_RTL:
                return VehicleMode.PLANE_RTL;

            case FIXED_WING_LOITER:
                return VehicleMode.PLANE_LOITER;

            case FIXED_WING_GUIDED:
                return VehicleMode.PLANE_GUIDED;


            case ROTOR_STABILIZE:
                return VehicleMode.COPTER_STABILIZE;

            case ROTOR_ACRO:
                return VehicleMode.COPTER_ACRO;

            case ROTOR_ALT_HOLD:
                return VehicleMode.COPTER_ALT_HOLD;

            case ROTOR_AUTO:
                return VehicleMode.COPTER_AUTO;

            case ROTOR_GUIDED:
                return VehicleMode.COPTER_GUIDED;

            case ROTOR_LOITER:
                return VehicleMode.COPTER_LOITER;

            case ROTOR_RTL:
                return VehicleMode.COPTER_RTL;

            case ROTOR_CIRCLE:
                return VehicleMode.COPTER_CIRCLE;

            case ROTOR_LAND:
                return VehicleMode.COPTER_LAND;

            case ROTOR_TOY:
                return VehicleMode.COPTER_DRIFT;

            case ROTOR_SPORT:
                return VehicleMode.COPTER_SPORT;

            case ROTOR_AUTOTUNE:
                return VehicleMode.COPTER_AUTOTUNE;

            case ROTOR_POSHOLD:
                return VehicleMode.COPTER_POSHOLD;

            case ROTOR_BRAKE:
                return VehicleMode.COPTER_BRAKE;


            case ROVER_MANUAL:
                return VehicleMode.ROVER_MANUAL;

            case ROVER_LEARNING:
                return VehicleMode.ROVER_LEARNING;

            case ROVER_STEERING:
                return VehicleMode.ROVER_STEERING;

            case ROVER_HOLD:
                return VehicleMode.ROVER_HOLD;

            case ROVER_AUTO:
                return VehicleMode.ROVER_AUTO;

            case ROVER_RTL:
                return VehicleMode.ROVER_RTL;

            case ROVER_GUIDED:
                return VehicleMode.ROVER_GUIDED;

            case ROVER_INITIALIZING:
                return VehicleMode.ROVER_INITIALIZING;

            default:
            case UNKNOWN:
                return null;

        }
    }

    static int getDroneProxyType(int originalType) {
        switch (originalType) {
            case MAV_TYPE.MAV_TYPE_TRICOPTER:
            case MAV_TYPE.MAV_TYPE_QUADROTOR:
            case MAV_TYPE.MAV_TYPE_HEXAROTOR:
            case MAV_TYPE.MAV_TYPE_OCTOROTOR:
            case MAV_TYPE.MAV_TYPE_HELICOPTER:
                return Type.TYPE_COPTER;

            case MAV_TYPE.MAV_TYPE_FIXED_WING:
                return Type.TYPE_PLANE;

            case MAV_TYPE.MAV_TYPE_GROUND_ROVER:
            case MAV_TYPE.MAV_TYPE_SURFACE_BOAT:
                return Type.TYPE_ROVER;

            default:
                return -1;
        }
    }

    static FootPrint getProxyCameraFootPrint(Footprint footprint) {
        if (footprint == null) return null;

        return new FootPrint(footprint.getGSD(),
                MathUtils.coord2DToLatLong(footprint.getVertexInGlobalFrame()));
    }

    static FollowAlgorithm.FollowModes followTypeToMode(FollowType followType) {
        final FollowAlgorithm.FollowModes followMode;

        switch (followType) {
            case ABOVE:
                followMode = FollowAlgorithm.FollowModes.ABOVE;
                break;

            case LEAD:
                followMode = FollowAlgorithm.FollowModes.LEAD;
                break;

            default:
            case LEASH:
                followMode = FollowAlgorithm.FollowModes.LEASH;
                break;

            case CIRCLE:
                followMode = FollowAlgorithm.FollowModes.CIRCLE;
                break;

            case LEFT:
                followMode = FollowAlgorithm.FollowModes.LEFT;
                break;

            case RIGHT:
                followMode = FollowAlgorithm.FollowModes.RIGHT;
                break;

            case SPLINE_LEASH:
                followMode = FollowAlgorithm.FollowModes.SPLINE_LEASH;
                break;

            case SPLINE_ABOVE:
                followMode = FollowAlgorithm.FollowModes.SPLINE_ABOVE;
                break;

            case GUIDED_SCAN:
                followMode = FollowAlgorithm.FollowModes.GUIDED_SCAN;
                break;

            case LOOK_AT_ME:
                followMode = FollowAlgorithm.FollowModes.LOOK_AT_ME;
                break;
        }
        return followMode;
    }

    static FollowType followModeToType(FollowAlgorithm.FollowModes followMode) {
        final FollowType followType;

        switch (followMode) {
            default:
            case LEASH:
                followType = FollowType.LEASH;
                break;

            case LEAD:
                followType = FollowType.LEAD;
                break;

            case RIGHT:
                followType = FollowType.RIGHT;
                break;

            case LEFT:
                followType = FollowType.LEFT;
                break;

            case CIRCLE:
                followType = FollowType.CIRCLE;
                break;

            case ABOVE:
                followType = FollowType.ABOVE;
                break;

            case SPLINE_LEASH:
                followType = FollowType.SPLINE_LEASH;
                break;

            case SPLINE_ABOVE:
                followType = FollowType.SPLINE_ABOVE;
                break;

            case GUIDED_SCAN:
                followType = FollowType.GUIDED_SCAN;
                break;

            case LOOK_AT_ME:
                followType = FollowType.LOOK_AT_ME;
                break;
        }

        return followType;
    }

    static CameraProxy getCameraProxy(Flight flight, List<CameraDetail> cameraDetails) {
        final CameraDetail camDetail;
        final FootPrint currentFieldOfView;
        final List<FootPrint> proxyPrints = new ArrayList<>();

        if (flight == null) {
            camDetail = new CameraDetail();
            currentFieldOfView = new FootPrint();
        } else {
            Camera droneCamera = flight.getCamera();

            camDetail = ProxyUtils.getCameraDetail(droneCamera.getCamera());

            List<Footprint> footprints = droneCamera.getFootprints();
            for (Footprint footprint : footprints) {
                proxyPrints.add(FlightApiUtils.getProxyCameraFootPrint(footprint));
            }

            GPS droneGps = flight.getGps();
            currentFieldOfView = droneGps.isPositionValid()
                    ? FlightApiUtils.getProxyCameraFootPrint(droneCamera.getCurrentFieldOfView())
                    : new FootPrint();
        }

        return new CameraProxy(camDetail, currentFieldOfView, proxyPrints, cameraDetails);
    }

    static Gps getGps(Flight flight) {
        if (flight == null)
            return new Gps();

        final GPS droneGps = flight.getGps();
        Coordinate dronePosition = droneGps.isPositionValid()
                ? new Coordinate(droneGps.getPosition().getLat(), droneGps.getPosition().getLng())
                : null;

        return new Gps(dronePosition, droneGps.getGpsEPH(), droneGps.getSatCount(),
                droneGps.getFixTypeNumeric());
    }

    static State getState(Flight flight, boolean isConnected) {
        if (flight == null)
            return new State();

        huins.ex.core.Flight.variables.State droneState = flight.getState();
        ApmModes droneMode = droneState.getMode();
        AccelCalibration accelCalibration = flight.getCalibrationSetup();
        String calibrationMessage = accelCalibration.isCalibrating() ? accelCalibration.getMessage() : null;
        final msg_ekf_status_report ekfStatus = droneState.getEkfStatus();
        final EkfStatus proxyEkfStatus = ekfStatus == null
                ? new EkfStatus()
                : new EkfStatus(ekfStatus.flags, ekfStatus.compass_variance, ekfStatus.pos_horiz_variance, ekfStatus
                .terrain_alt_variance, ekfStatus.velocity_variance, ekfStatus.pos_vert_variance);

        return new State(isConnected, FlightApiUtils.getVehicleMode(droneMode), droneState.isArmed(), droneState.isFlying(),
                droneState.getErrorId(), flight.getMavlinkVersion(), calibrationMessage,
                droneState.getFlightStartTime(), proxyEkfStatus, isConnected && flight.isConnectionAlive());
    }

    static Parameters getParameters(Flight flight, Context context) {
        if (flight == null)
            return new Parameters();

        final Map<String, Parameter> proxyParams = new HashMap<>();

        Map<String, huins.ex.core.parameters.Parameter> droneParameters = flight.getParameters().getParameters();
        if (!droneParameters.isEmpty()) {
            for (huins.ex.core.parameters.Parameter param : droneParameters.values()) {
                if (param.name != null) {
                    proxyParams.put(param.name, new Parameter(param.name, param.value, param.type));
                }
            }

            try {
                final VehicleProfile profile = flight.getVehicleProfile();
                if (profile != null) {
                    String metadataType = profile.getParameterMetadataType();
                    if (metadataType != null) {
                        ParameterMetadataLoader.load(context, metadataType, proxyParams);
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return new Parameters(new ArrayList<>(proxyParams.values()));
    }

    static Speed getSpeed(Flight flight) {
        if (flight == null)
            return new Speed();

        huins.ex.core.Flight.variables.Speed droneSpeed = flight.getSpeed();
        return new Speed(droneSpeed.getVerticalSpeed(), droneSpeed.getGroundSpeed(), droneSpeed.getAirSpeed());
    }

    static Attitude getAttitude(Flight flight) {
        if (flight == null)
            return new Attitude();

        Orientation droneOrientation = flight.getOrientation();
        return new Attitude(droneOrientation.getRoll(), droneOrientation.getPitch(),
                droneOrientation.getYaw());
    }

    static Home getHome(Flight flight) {
        if (flight == null)
            return new Home();

        huins.ex.core.Flight.variables.Home droneHome = flight.getHome();
        CoordinateExtend homePosition = droneHome.isValid()
                ? new CoordinateExtend(droneHome.getCoord().getLat(), droneHome.getCoord().getLng(),
                droneHome.getAltitude())
                : null;

        return new Home(homePosition);
    }

    static GoPro getGoPro(Flight flight) {
        if (flight == null)
            return new GoPro();

        GoProImpl impl = flight.getGoProImpl();
        return new GoPro(impl.isConnected(), impl.isRecording());
    }

    static Battery getBattery(Flight flight) {
        if (flight == null)
            return new Battery();

        huins.ex.core.Flight.variables.Battery droneBattery = flight.getBattery();
        return new Battery(droneBattery.getBattVolt(), droneBattery.getBattRemain(),
                droneBattery.getBattCurrent(), droneBattery.getBattDischarge());
    }

    static Altitude getAltitude(Flight flight) {
        if (flight == null)
            return new Altitude();

        huins.ex.core.Flight.variables.Altitude droneAltitude = flight.getAltitude();
        return new Altitude(droneAltitude.getAltitude(), droneAltitude.getTargetAltitude());
    }

    static Mission getMission(Flight flight) {
        Mission proxyMission = new Mission();
        if (flight == null)
            return proxyMission;

        huins.ex.core.mission.Mission droneMission = flight.getMission();
        List<huins.ex.core.mission.MissionItem> droneMissionItems = droneMission.getItems();


        proxyMission.setCurrentMissionItem((short) flight.getMissionStats().getCurrentWP());
        if (!droneMissionItems.isEmpty()) {
            for (huins.ex.core.mission.MissionItem item : droneMissionItems) {
                proxyMission.addMissionItem(ProxyUtils.getProxyMissionItem(item));
            }
        }

        return proxyMission;
    }

    static Signal getSignal(Flight flight) {
        if (flight == null)
            return new Signal();

        Radio droneRadio = flight.getRadio();
        return new Signal(droneRadio.isValid(), droneRadio.getRxErrors(), droneRadio.getFixed(),
                droneRadio.getTxBuf(), droneRadio.getRssi(), droneRadio.getRemRssi(),
                droneRadio.getNoise(), droneRadio.getRemNoise());
    }

    static Type getType(Flight flight) {
        if (flight == null)
            return new Type();

        return new Type(FlightApiUtils.getDroneProxyType(flight.getType()), flight.getFirmwareVersion());
    }

    static GuidedState getGuidedState(Flight flight) {
        if (flight == null)
            return new GuidedState();

        final GuidedPoint guidedPoint = flight.getGuidedPoint();
        int guidedState;
        switch (guidedPoint.getState()) {
            default:
            case UNINITIALIZED:
                guidedState = GuidedState.STATE_UNINITIALIZED;
                break;

            case ACTIVE:
                guidedState = GuidedState.STATE_ACTIVE;
                break;

            case IDLE:
                guidedState = GuidedState.STATE_IDLE;
                break;
        }

        Coord2D guidedCoord = guidedPoint.getCoord() == null
                ? new Coord2D(0, 0)
                : guidedPoint.getCoord();
        double guidedAlt = guidedPoint.getAltitude();
        return new GuidedState(guidedState, new CoordinateExtend(guidedCoord.getLat(), guidedCoord.getLng(), guidedAlt));
    }

    static void changeVehicleMode(Flight flight, VehicleMode newMode) {
        if (flight == null)
            return;

        int mavType;
        switch (newMode.getDroneType()) {
            default:
            case Type.TYPE_COPTER:
                mavType = MAV_TYPE.MAV_TYPE_QUADROTOR;
                break;

            case Type.TYPE_PLANE:
                mavType = MAV_TYPE.MAV_TYPE_FIXED_WING;
                break;

            case Type.TYPE_ROVER:
                mavType = MAV_TYPE.MAV_TYPE_GROUND_ROVER;
                break;
        }

        flight.getState().changeFlightMode(ApmModes.getMode(newMode.getMode(), mavType));
    }

    static FollowState getFollowState(Follow followMe) {
        if (followMe == null)
            return new FollowState();

        final int state;
        switch (followMe.getState()) {

            default:
            case FOLLOW_INVALID_STATE:
                state = FollowState.STATE_INVALID;
                break;

            case FOLLOW_DRONE_NOT_ARMED:
                state = FollowState.STATE_DRONE_NOT_ARMED;
                break;

            case FOLLOW_DRONE_DISCONNECTED:
                state = FollowState.STATE_DRONE_DISCONNECTED;
                break;

            case FOLLOW_START:
                state = FollowState.STATE_START;
                break;

            case FOLLOW_RUNNING:
                state = FollowState.STATE_RUNNING;
                break;

            case FOLLOW_END:
                state = FollowState.STATE_END;
                break;
        }

        final FollowAlgorithm currentAlg = followMe.getFollowAlgorithm();
        Map<String, Object> modeParams = currentAlg.getParams();
        Bundle params = new Bundle();
        for (Map.Entry<String, Object> entry : modeParams.entrySet()) {
            switch (entry.getKey()) {
                case FollowType.EXTRA_FOLLOW_ROI_TARGET:
                    Coord3D target = (Coord3D) entry.getValue();
                    if (target != null) {
                        params.putParcelable(entry.getKey(), new CoordinateExtend(target.getLat(), target.getLng(),
                                target.getAltitude()));
                    }
                    break;

                case FollowType.EXTRA_FOLLOW_RADIUS:
                    Double radius = (Double) entry.getValue();
                    if (radius != null)
                        params.putDouble(entry.getKey(), radius);
                    break;
            }
        }
        return new FollowState(state, FlightApiUtils.followModeToType(currentAlg.getType()), params);
    }

    static void disableFollowMe(Follow follow) {
        if (follow == null)
            return;

        if (follow.isEnabled())
            follow.toggleFollowMeState();
    }

    static void triggerCamera(Flight flight) throws RemoteException {
        if (flight == null)
            return;
        MavLinkDoCmds.triggerCamera(flight);
    }

    static void epmCommand(Flight flight, boolean release) {
        if (flight == null)
            return;

        MavLinkDoCmds.empCommand(flight, release);
    }

    static void loadWaypoints(Flight flight) {
        if (flight == null)
            return;
        flight.getWaypointManager().getWaypoints();
    }

    static void refreshParameters(Flight flight) {
        if (flight == null)
            return;
        flight.getParameters().refreshParameters();
    }

    static void writeParameters(Flight flight, Parameters parameters) {
        if (flight == null || parameters == null) return;

        List<Parameter> parametersList = parameters.getParameters();
        if (parametersList.isEmpty())
            return;

        huins.ex.core.Flight.profiles.Parameters droneParams = flight.getParameters();
        for (Parameter proxyParam : parametersList) {
            droneParams.sendParameter(new huins.ex.core.parameters.Parameter(proxyParam.getName(), proxyParam.getValue(), proxyParam.getType()));
        }
    }

    static void setMission(Flight flight, Mission mission, boolean pushToDrone) {
        if (flight == null)
            return;

        huins.ex.core.mission.Mission droneMission = flight.getMission();
        droneMission.clearMissionItems();

        List<MissionItem> itemsList = mission.getMissionItems();
        for (MissionItem item : itemsList) {
            droneMission.addMissionItem(ProxyUtils.getMissionItemImpl(droneMission, item));
        }

        if (pushToDrone)
            droneMission.sendMissionToAPM();
    }

    static float generateDronie(Flight flight) {
        if (flight == null)
            return -1;

        return (float) flight.getMission().makeAndUploadDronie();
    }

    static void arm(Flight flight, boolean arm) {
        if (flight == null)
            return;
        MavLinkArm.sendArmMessage(flight, arm);
    }

    static void startMagnetometerCalibration(Flight flight, boolean retryOnFailure, boolean saveAutomatically, int
            startDelay) {
        if (flight == null)
            return;

        flight.getMagnetometerCalibration().startCalibration(retryOnFailure, saveAutomatically, startDelay);
    }

    static void cancelMagnetometerCalibration(Flight flight) {
        if (flight == null)
            return;

        flight.getMagnetometerCalibration().cancelCalibration();
    }

    public static void acceptMagnetometerCalibration(Flight flight) {
        if (flight == null)
            return;

        flight.getMagnetometerCalibration().acceptCalibration();
    }

    static boolean startIMUCalibration(Flight flight) {
        return flight != null && flight.getCalibrationSetup().startCalibration();
    }

    static void sendIMUCalibrationAck(Flight flight, int step) {
        if (flight == null)
            return;

        flight.getCalibrationSetup().sendAck(step);
    }

    static void doGuidedTakeoff(Flight flight, double altitude) {
        if (flight == null)
            return;

        flight.getGuidedPoint().doGuidedTakeoff(altitude);
    }

    static void sendMavlinkMessage(Flight flight, MavlinkMessageWrapper messageWrapper) {
        if (flight == null || messageWrapper == null)
            return;

        MAVLinkMessage message = messageWrapper.getMavLinkMessage();
        if (message == null)
            return;

        message.compid = flight.getCompid();
        message.sysid = flight.getSysid();

        //Set the target system and target component for MAVLink messages that support those
        //attributes.
        try {
            Class<?> tempMessage = message.getClass();
            Field target_system = tempMessage.getDeclaredField("target_system");
            Field target_component = tempMessage.getDeclaredField("target_component");

            target_system.setByte(message, (byte) message.sysid);
            target_component.setByte(message, (byte) message.compid);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException | ExceptionInInitializerError e) {
            Log.e(TAG, e.getMessage(), e);
        }

        flight.getMavClient().sendMavPacket(message.pack());
    }

    static void sendGuidedPoint(Flight flight, Coordinate point, boolean force) {
        if (flight == null)
            return;

        GuidedPoint guidedPoint = flight.getGuidedPoint();
        if (guidedPoint.isInitialized()) {
            guidedPoint.newGuidedCoord(MathUtils.latLongToCoord2D(point));
        } else if (force) {
            try {
                guidedPoint.forcedGuidedCoordinate(MathUtils.latLongToCoord2D(point));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    static void setGuidedAltitude(Flight flight, double altitude) {
        if (flight == null)
            return;

        flight.getGuidedPoint().changeGuidedAltitude(altitude);
    }

    static void enableFollowMe(FlightManager flightMgr, FlightInterfaces.Handler droneHandler, FollowType followType) {
        if (flightMgr == null)
            return;

        final FollowAlgorithm.FollowModes selectedMode = FlightApiUtils.followTypeToMode(followType);

        if (selectedMode != null) {
            final Follow followMe = flightMgr.getFollowMe();
            if (!followMe.isEnabled())
                followMe.toggleFollowMeState();

            FollowAlgorithm currentAlg = followMe.getFollowAlgorithm();
            if (currentAlg.getType() != selectedMode) {
                followMe.setAlgorithm(selectedMode.getAlgorithmType(flightMgr.getFlight(), droneHandler));
            }
        }
    }

    static void buildComplexMissionItem(Flight flight, Bundle itemBundle) {
        MissionItem missionItem = MissionItemType.restoreMissionItemFromBundle(itemBundle);
        if (missionItem == null || !(missionItem instanceof MissionItem.ComplexItem))
            return;

        final MissionItemType itemType = missionItem.getType();
        switch (itemType) {
            case SURVEY:
                Survey updatedSurvey = buildSurvey(flight, (Survey) missionItem);
                if (updatedSurvey != null)
                    itemType.storeMissionItem(updatedSurvey, itemBundle);
                break;

            case STRUCTURE_SCANNER:
                StructureScanner updatedScanner = buildStructureScanner(flight, (StructureScanner) missionItem);
                if (updatedScanner != null)
                    itemType.storeMissionItem(updatedScanner, itemBundle);
                break;

            default:
                Log.w(TAG, "Unrecognized complex mission item.");
                break;
        }
    }

    static Survey buildSurvey(Flight flight, Survey survey) {
        huins.ex.core.mission.Mission droneMission = flight == null ? null : flight.getMission();
        huins.ex.core.mission.survey.Survey updatedSurvey = (huins.ex.core.mission.survey.Survey) ProxyUtils.getMissionItemImpl
                (droneMission, survey);

        return (Survey) ProxyUtils.getProxyMissionItem(updatedSurvey);
    }

    static StructureScanner buildStructureScanner(Flight flight, StructureScanner item) {
        huins.ex.core.mission.Mission droneMission = flight == null ? null : flight.getMission();
        huins.ex.core.mission.waypoints.StructureScanner updatedScan = (huins.ex.core.mission.waypoints.StructureScanner) ProxyUtils
                .getMissionItemImpl(droneMission, item);

        StructureScanner proxyScanner = (StructureScanner) ProxyUtils.getProxyMissionItem(updatedScan);
        return proxyScanner;
    }

    static void startVideoRecording(Flight flight) {
        if (flight == null)
            return;

        flight.getGoProImpl().startRecording();
    }

    static void stopVideoRecording(Flight flight) {
        if (flight == null)
            return;

        flight.getGoProImpl().stopRecording();
    }

    static MagnetometerCalibrationStatus getMagnetometerCalibrationStatus(Flight flight) {
        final MagnetometerCalibrationStatus calStatus = new MagnetometerCalibrationStatus();
        if (flight != null) {
            final MagnetometerCalibrationImpl magCalImpl = flight.getMagnetometerCalibration();
            calStatus.setCalibrationCancelled(magCalImpl.isCancelled());

            Collection<MagnetometerCalibrationImpl.Info> calibrationInfo = magCalImpl.getMagCalibrationTracker().values();
            for (MagnetometerCalibrationImpl.Info info : calibrationInfo) {
                calStatus.addCalibrationProgress(getMagnetometerCalibrationProgress(info.getCalProgress()));
                calStatus.addCalibrationResult(getMagnetometerCalibrationResult(info.getCalReport()));
            }
        }

        return calStatus;
    }

    static MagnetometerCalibrationProgress getMagnetometerCalibrationProgress(msg_mag_cal_progress msgProgress) {
        if (msgProgress == null)
            return null;

        return new MagnetometerCalibrationProgress(msgProgress.compass_id, msgProgress.completion_pct,
                msgProgress.direction_x, msgProgress.direction_y, msgProgress.direction_z);
    }

    static MagnetometerCalibrationResult getMagnetometerCalibrationResult(msg_mag_cal_report msgReport) {
        if (msgReport == null)
            return null;

        return new MagnetometerCalibrationResult(msgReport.compass_id,
                msgReport.cal_status == MAG_CAL_STATUS.MAG_CAL_SUCCESS, msgReport.autosaved == 1, msgReport.fitness,
                msgReport.ofs_x, msgReport.ofs_y, msgReport.ofs_z,
                msgReport.diag_x, msgReport.diag_y, msgReport.diag_z,
                msgReport.offdiag_x, msgReport.offdiag_y, msgReport.offdiag_z);
    }
}

