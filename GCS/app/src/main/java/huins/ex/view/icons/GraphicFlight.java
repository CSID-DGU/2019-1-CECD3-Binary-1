package huins.ex.view.icons;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import huins.ex.R;
import huins.ex.interfaces.MarkerInfo;
import huins.ex.proto.Coordinate;
import huins.ex.proto.Flight;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.property.Attitude;
import huins.ex.proto.property.Gps;

public class GraphicFlight extends MarkerInfo.SimpleMarkerInfo {

	private Flight flight;

	public GraphicFlight(Flight flight) {
		this.flight = flight;
	}

	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	@Override
	public float getAnchorV() {
		return 0.5f;
	}

	@Override
	public Coordinate getPosition() {
        Gps droneGps = flight.getAttribute(AttributeType.GPS);
        return isValid() ? droneGps.getPosition() :  null;
	}

	@Override
	public Bitmap getIcon(Resources res) {
		if (flight.IsConnected()) {
			return BitmapFactory.decodeResource(res, R.drawable.quad);
		}
		return BitmapFactory.decodeResource(res, R.drawable.quad_disconnect);

	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isFlat() {
		return true;
	}

	@Override
	public float getRotation() {
        Attitude attitude = flight.getAttribute(AttributeType.ATTITUDE);
		return attitude == null ? 0 : (float) attitude.getYaw();
	}

	public boolean isValid() {
        Gps droneGps = flight.getAttribute(AttributeType.GPS);
		return droneGps != null && droneGps.isValid();
	}
}
