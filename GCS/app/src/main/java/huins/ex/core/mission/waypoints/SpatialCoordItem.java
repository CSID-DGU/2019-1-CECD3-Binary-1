package huins.ex.core.mission.waypoints;

import java.util.List;

import huins.ex.com.MAVLink.common.msg_mission_item;
import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.helpers.coordinates.Coord3D;
import huins.ex.core.mission.Mission;
import huins.ex.core.mission.MissionItem;

public abstract class SpatialCoordItem extends MissionItem {

    protected Coord3D coordinate;

    public SpatialCoordItem(Mission mission, Coord3D coord) {
        super(mission);
        this.coordinate = coord;
    }

    public SpatialCoordItem(MissionItem item) {
        super(item);
        if (item instanceof SpatialCoordItem) {
            coordinate = ((SpatialCoordItem) item).getCoordinate();
        } else {
            coordinate = new Coord3D(0, 0, (0));
        }
    }

    public void setCoordinate(Coord3D coordNew) {
        coordinate = coordNew;
    }

    public Coord3D getCoordinate() {
        return coordinate;
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.x = (float) coordinate.getLat();
        mavMsg.y = (float) coordinate.getLng();
        mavMsg.z = (float) coordinate.getAltitude();
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        setCoordinate(new Coord3D(mavMsg.x, mavMsg.y, mavMsg.z));
    }

    public void setAltitude(double altitude) {
        coordinate.set(coordinate.getLat(), coordinate.getLng(), altitude);
    }

    public void setPosition(Coord2D position) {
        coordinate.set(position);
    }

}