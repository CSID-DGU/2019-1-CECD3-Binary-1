package huins.ex.core.Flight.variables;

import java.util.ArrayList;
import java.util.List;

import huins.ex.com.MAVLink.ardupilotmega.msg_camera_feedback;
import huins.ex.com.MAVLink.ardupilotmega.msg_mount_status;
import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.model.Flight;
import huins.ex.core.survey.CameraInfo;
import huins.ex.core.survey.Footprint;

public class Camera extends FlightVariable {
	private CameraInfo camera = new CameraInfo();
	private List<Footprint> footprints = new ArrayList<Footprint>();
	private double gimbal_pitch;

	public Camera(Flight mflight) {
		super(mflight);
	}

	public void newImageLocation(msg_camera_feedback msg) {
		footprints.add(new Footprint(camera, msg));
		mflight.notifyFlightEvent(FlightEventsType.FOOTPRINT);
	}

    public List<Footprint> getFootprints(){
        return footprints;
    }

	public Footprint getLastFootprint() {
		return footprints.get(footprints.size() - 1);
	}

	public CameraInfo getCamera() {
		return camera;
	}

	public Footprint getCurrentFieldOfView() {
		double altitude = mflight.getAltitude().getAltitude();
		Coord2D position = mflight.getGps().getPosition();
		//double pitch = mflight.getOrientation().getPitch() - gimbal_pitch;
		double pitch = mflight.getOrientation().getPitch();
		double roll = mflight.getOrientation().getRoll();
		double yaw = mflight.getOrientation().getYaw();
		return new Footprint(camera, position, altitude, pitch, roll, yaw);
	}

	public void updateMountOrientation(msg_mount_status msg_mount_status) {
		gimbal_pitch = 90 - msg_mount_status.pointing_a / 100;
	}

}
