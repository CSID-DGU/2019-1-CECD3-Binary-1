package huins.ex.proto.status;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import huins.ex.proto.StatusCustomButton;

/**
 * Created by suhak on 15. 10. 5.
 */
public class StatusGPSView extends StatusCustomButton {

    public void set_gcs_hdop(String _gcs_hdop) {
        this._gcs_hdop = _gcs_hdop;
    }

    public void set_gcs_status(String _gcs_status) {
        this._gcs_status = _gcs_status;
    }

    public void set_gcs_stations(String _gcs_stations) {
        this._gcs_stations = _gcs_stations;
    }

    private String _gcs_status;
    private String _gcs_stations;
    private String _gcs_hdop;


    public StatusGPSView(Context context) {
        super(context);

        initialize();
    }

    public StatusGPSView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
    }

    public StatusGPSView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize();

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StatusGPSView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialize();
    }

    private void initialize()
    {
        _gcs_status = "- -";
        _gcs_stations = "- -";
        _gcs_hdop = "- -";

    }


    @Override
    public void updateUI() {
        super.updateUI();

        if(_mmeasure.size() > 0)
        {
            switch (index)
            {
                case 0: setValue(_gcs_status.toString() + " " + _mmeasure.get(index)); break;
                case 1: setValue(_gcs_hdop.toString() + " "  + _mmeasure.get(index)); break;
                case 2: setValue(_gcs_stations.toString() + " "  + _mmeasure.get(index)); break;

            }
        }
    }
}
