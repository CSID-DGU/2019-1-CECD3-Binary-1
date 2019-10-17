package huins.ex.view.icons;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import huins.ex.R;
import huins.ex.interfaces.MarkerInfo;
import huins.ex.model.MarkerWithText;
import huins.ex.proto.Coordinate;
import huins.ex.proto.Flight;
import huins.ex.proto.MapInterface;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.property.Gps;
import huins.ex.proto.property.GuidedState;

public class GraphicGuided extends MarkerInfo.SimpleMarkerInfo implements MapInterface.PathSource {

	private final static String TAG = GraphicGuided.class.getSimpleName();

    private final Flight flight;

	public GraphicGuided(Flight flight) {
        this.flight = flight;
	}

	@Override
	public List<Coordinate> getPathPoints() {
		List<Coordinate> path = new ArrayList<Coordinate>();
        GuidedState guidedPoint = flight.getAttribute(AttributeType.GUIDED_STATE);
		if (guidedPoint != null && guidedPoint.isActive()) {
            Gps gps = flight.getAttribute(AttributeType.GPS);
			if (gps != null && gps.isValid()) {
				path.add(gps.getPosition());
			}
			path.add(guidedPoint.getCoordinate());
		}
		return path;
	}

	@Override
	public boolean isVisible() {
        GuidedState guidedPoint = flight.getAttribute(AttributeType.GUIDED_STATE);
		return guidedPoint != null && guidedPoint.isActive();
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
        GuidedState guidedPoint = flight.getAttribute(AttributeType.GUIDED_STATE);
		return guidedPoint == null ? null : guidedPoint.getCoordinate();
	}

	@Override
	public void setPosition(Coordinate coord) {
		try {
			flight.sendGuidedPoint(coord, true);
		} catch (Exception e) {
			Log.e(TAG, "Unable to update guided point position.", e);
		}
	}

	@Override
	public Bitmap getIcon(Resources res) {
		return MarkerWithText.getMarkerWithTextAndDetail(R.drawable.ic_wp_map, "Guided", "", res);
	}

	@Override
	public boolean isDraggable() {
		return true;
	}
}