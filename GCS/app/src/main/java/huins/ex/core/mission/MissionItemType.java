package huins.ex.core.mission;

import java.util.Collections;

import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.mission.commands.CameraTrigger;
import huins.ex.core.mission.commands.ChangeSpeed;
import huins.ex.core.mission.commands.ConditionYaw;
import huins.ex.core.mission.commands.EpmGripper;
import huins.ex.core.mission.commands.ReturnToHome;
import huins.ex.core.mission.commands.SetRelayImpl;
import huins.ex.core.mission.commands.SetServo;
import huins.ex.core.mission.commands.Takeoff;
import huins.ex.core.mission.survey.Survey;
import huins.ex.core.mission.waypoints.Circle;
import huins.ex.core.mission.waypoints.Land;
import huins.ex.core.mission.waypoints.RegionOfInterest;
import huins.ex.core.mission.waypoints.SplineWaypoint;
import huins.ex.core.mission.waypoints.StructureScanner;
import huins.ex.core.mission.waypoints.Waypoint;

public enum MissionItemType {
    WAYPOINT("Waypoint"),
    SPLINE_WAYPOINT("Spline Waypoint"),
    TAKEOFF("Takeoff"),
    RTL("Return to Launch"),
    LAND("Land"),
    CIRCLE("Circle"),
    ROI("Region of Interest"),
    SURVEY("Survey"),
    CYLINDRICAL_SURVEY("Structure Scan"),
    CHANGE_SPEED("Change Speed"),
    CAMERA_TRIGGER("Camera Trigger"),
    EPM_GRIPPER("EPM"),
    SET_SERVO("Set Servo"),
    CONDITION_YAW("Set Yaw"),
    SET_RELAY("Set Relay");

    private final String name;

    private MissionItemType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public MissionItem getNewItem(MissionItem referenceItem) throws IllegalArgumentException {
        switch (this) {
            case WAYPOINT:
                return new Waypoint(referenceItem);
            case SPLINE_WAYPOINT:
                return new SplineWaypoint(referenceItem);
            case TAKEOFF:
                return new Takeoff(referenceItem);
            case CHANGE_SPEED:
                return new ChangeSpeed(referenceItem);
            case CAMERA_TRIGGER:
                return new CameraTrigger(referenceItem);
            case EPM_GRIPPER:
                return new EpmGripper(referenceItem);
            case RTL:
                return new ReturnToHome(referenceItem);
            case LAND:
                return new Land(referenceItem);
            case CIRCLE:
                return new Circle(referenceItem);
            case ROI:
                return new RegionOfInterest(referenceItem);
            case SURVEY:
                return new Survey(referenceItem.getMission(), Collections.<Coord2D>emptyList());
            case CYLINDRICAL_SURVEY:
                return new StructureScanner(referenceItem);
            case SET_SERVO:
                return new SetServo(referenceItem);
            case CONDITION_YAW:
                return new ConditionYaw(referenceItem);
            case SET_RELAY:
                return new SetRelayImpl(referenceItem);
            default:
                throw new IllegalArgumentException("Unrecognized mission item type (" + name + ")" + "");
        }
    }
}
