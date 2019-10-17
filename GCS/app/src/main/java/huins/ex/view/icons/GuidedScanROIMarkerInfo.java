package huins.ex.view.icons;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import huins.ex.R;
import huins.ex.interfaces.MarkerInfo;
import huins.ex.proto.Coordinate;
import huins.ex.proto.CoordinateExtend;

/**
 * Created by Fredia Huya-Kouadio on 1/27/15.
 */
public class GuidedScanROIMarkerInfo extends MarkerInfo.SimpleMarkerInfo {

    public static final double DEFAULT_FOLLOW_ROI_ALTITUDE = 10; //meters
    private CoordinateExtend roiCoord;

    @Override
    public void setPosition(Coordinate coord){
        if(coord == null || coord instanceof CoordinateExtend){
            roiCoord = (CoordinateExtend) coord;
        }
        else {
            double defaultHeight = DEFAULT_FOLLOW_ROI_ALTITUDE;
            if(roiCoord != null)
                defaultHeight = roiCoord.getAltitude();

            this.roiCoord = new CoordinateExtend(coord.getLatitude(), coord.getLongitude(), defaultHeight);
        }
    }

    @Override
    public CoordinateExtend getPosition(){
        return roiCoord;
    }

    @Override
    public Bitmap getIcon(Resources res){
        return BitmapFactory.decodeResource(res, R.drawable.ic_roi);
    }

    @Override
    public boolean isVisible(){
        return roiCoord != null;
    }

    @Override
    public float getAnchorU() {
        return 0.5f;
    }

    @Override
    public float getAnchorV() {
        return 0.5f;
    }
}
