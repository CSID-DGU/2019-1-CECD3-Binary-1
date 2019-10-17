package huins.ex.core.mission.commands;
import java.util.List;

import huins.ex.com.MAVLink.common.msg_mission_item;
import huins.ex.com.MAVLink.enums.MAV_CMD;
import huins.ex.com.MAVLink.enums.MAV_FRAME;
import huins.ex.core.mission.Mission;
import huins.ex.core.mission.MissionItem;
import huins.ex.core.mission.MissionItemType;

public class ReturnToHome extends MissionCMD {

    private double returnAltitude;

    public ReturnToHome(MissionItem item) {
        super(item);
        returnAltitude = (0);
    }

    public ReturnToHome(msg_mission_item msg, Mission mission) {
        super(mission);
        unpackMAVMessage(msg);
    }

    public ReturnToHome(Mission mission) {
        super(mission);
        returnAltitude = (0.0);
    }

    public double getHeight() {
        return returnAltitude;
    }

    public void setHeight(double altitude) {
        returnAltitude = altitude;
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        mavMsg.z = (float) returnAltitude;
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMessageItem) {
        returnAltitude = (mavMessageItem.z);
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.RTL;
    }

}
