package huins.ex.core.mission.commands;

import java.util.List;

import huins.ex.com.MAVLink.common.msg_mission_item;
import huins.ex.core.mission.Mission;
import huins.ex.core.mission.MissionItem;

public abstract class MissionCMD extends MissionItem {

	public MissionCMD(Mission mission) {
		super(mission);
	}

	public MissionCMD(MissionItem item) {
		super(item);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		return super.packMissionItem();
	}

}