/** 
* Enumeration of possible mount operation modes
*/
package huins.ex.com.MAVLink.enums;

public class MAV_MOUNT_MODE {
	public static final int MAV_MOUNT_MODE_RETRACT = 0; /* Load and keep safe position (Roll,Pitch,Yaw) from permant memory and stop stabilization | */
	public static final int MAV_MOUNT_MODE_NEUTRAL = 1; /* Load and keep neutral position (Roll,Pitch,Yaw) from permanent memory. | */
	public static final int MAV_MOUNT_MODE_MAVLINK_TARGETING = 2; /* Load neutral position and start MAVLink Roll,Pitch,Yaw control with stabilization | */
	public static final int MAV_MOUNT_MODE_RC_TARGETING = 3; /* Load neutral position and start RC Roll,Pitch,Yaw control with stabilization | */
	public static final int MAV_MOUNT_MODE_GPS_POINT = 4; /* Load neutral position and start to point to Lat,Lon,Alt | */
	public static final int MAV_MOUNT_MODE_ENUM_END = 5; /*  | */
}
            