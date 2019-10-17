package huins.ex.core.Flight.variables;

import huins.ex.com.MAVLink.common.msg_mission_item;
import huins.ex.com.MAVLink.enums.MAV_CMD;
import huins.ex.com.MAVLink.enums.MAV_FRAME;
import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.MAVLink.MavLinkWaypoint;
import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.model.Flight;

public class Home extends FlightVariable implements FlightInterfaces.OnFlightListener {
    public static final int HOME_WAYPOINT_INDEX = 0;

    private Coord2D coordinate;
    private double altitude = 0;

    public Home(Flight drone) {
        super(drone);
        drone.addFlightListener(this);
    }

    public boolean isValid() {
        return (coordinate != null);
    }

    public Home getHome() {
        return this;
    }

    public Coord2D getCoord() {
        return coordinate;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setHome(msg_mission_item msg) {
        boolean homeLocationUpdated = false;

        if (this.coordinate == null) {
            this.coordinate = new Coord2D(msg.x, msg.y);
            homeLocationUpdated = true;
        } else if (this.coordinate.getLat() != msg.x || this.coordinate.getLng() != msg.y) {
            this.coordinate.set(msg.x, msg.y);
            homeLocationUpdated = true;
        }

        if (this.altitude != msg.z) {
            this.altitude = msg.z;
            homeLocationUpdated = true;
        }

        if (homeLocationUpdated)
            mflight.notifyFlightEvent(FlightEventsType.HOME);
    }

    public msg_mission_item packMavlink() {
        msg_mission_item mavMsg = new msg_mission_item();
        mavMsg.autocontinue = 1;
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
        mavMsg.current = 0;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
        mavMsg.target_system = mflight.getSysid();
        mavMsg.target_component = mflight.getCompid();
        if (isValid()) {
            mavMsg.x = (float) coordinate.getLat();
            mavMsg.y = (float) coordinate.getLng();
            mavMsg.z = (float) altitude;
        }

        return mavMsg;
    }

    @Override
    public void onFlightEvent(FlightEventsType event, Flight flight) {
        switch(event){
            case EKF_POSITION_STATE_UPDATE:
                if(flight.getState().isEkfPositionOk())
                    requestHomeUpdate(mflight);
                break;
        }
    }

    private static void requestHomeUpdate(Flight flight){
        MavLinkWaypoint.requestWayPoint(flight, HOME_WAYPOINT_INDEX);
    }
}
