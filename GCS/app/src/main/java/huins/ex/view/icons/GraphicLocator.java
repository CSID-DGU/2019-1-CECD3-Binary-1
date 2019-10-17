package huins.ex.view.icons;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import huins.ex.R;
import huins.ex.interfaces.MarkerInfo;
import huins.ex.proto.Coordinate;

public class GraphicLocator extends MarkerInfo.SimpleMarkerInfo {

	private Coordinate lastPosition;

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
		return lastPosition;
	}

	@Override
	public Bitmap getIcon(Resources res) {
		return BitmapFactory.decodeResource(res, R.drawable.quad);
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
		return 0;
	}

	public void setLastPosition(Coordinate lastPosition) {
		this.lastPosition = lastPosition;
	}
}