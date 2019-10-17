package huins.ex.view.icons;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import huins.ex.R;
import huins.ex.interfaces.MarkerInfo;
import huins.ex.proto.Coordinate;
import huins.ex.proto.Flight;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.property.Home;


public class GraphicHome extends MarkerInfo.SimpleMarkerInfo {

	private Flight flight;

	public GraphicHome(Flight flight) {
		this.flight = flight;
	}

	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	public boolean isValid() {
        Home droneHome = flight.getAttribute(AttributeType.HOME);
		return droneHome != null && droneHome.isValid();
	}

	@Override
	public float getAnchorV() {
		return 0.5f;
	}

	@Override
	public Bitmap getIcon(Resources res) {
		return BitmapFactory.decodeResource(res, R.drawable.ic_wp_home);
	}

	@Override
	public Coordinate getPosition() {
        Home droneHome = flight.getAttribute(AttributeType.HOME);
        if(droneHome == null) return null;

		return droneHome.getCoordinate();
	}

	@Override
	public String getSnippet() {
        Home droneHome = flight.getAttribute(AttributeType.HOME);
		return "Home " + (droneHome == null ? "N/A" : droneHome.getCoordinate().getAltitude());
	}

	@Override
	public String getTitle() {
		return "Home";
	}

	@Override
	public boolean isVisible() {
		return isValid();
	}
}
