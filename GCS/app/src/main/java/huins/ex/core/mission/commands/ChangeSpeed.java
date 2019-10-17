package huins.ex.core.mission.commands;

import java.util.List;

import huins.ex.com.MAVLink.common.msg_mission_item;
import huins.ex.com.MAVLink.enums.MAV_CMD;
import huins.ex.com.MAVLink.enums.MAV_FRAME;
import huins.ex.core.mission.Mission;
import huins.ex.core.mission.MissionItem;
import huins.ex.core.mission.MissionItemType;

public class ChangeSpeed extends MissionCMD {
	private double speed = 5; //meters per second

	public ChangeSpeed(MissionItem item) {
		super(item);
	}

	public ChangeSpeed(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public ChangeSpeed(Mission mission, double speed) {
		super(mission);
		this.speed = speed;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_CHANGE_SPEED;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.param2 = (float) speed;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		speed = mavMsg.param2;
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CHANGE_SPEED;
	}

    /**
     * @return the set speed in meters per second.
     */
	public double getSpeed() {
		return speed;
	}

    /**
     * Set the speed
     * @param speed speed in meters per second.
     */
	public void setSpeed(double speed) {
		this.speed = speed;
	}
}