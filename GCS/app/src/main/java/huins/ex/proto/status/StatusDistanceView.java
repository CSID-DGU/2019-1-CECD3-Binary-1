package huins.ex.proto.status;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import huins.ex.proto.StatusCustomButton;

/**
 * Created by suhak on 15. 10. 5.
 */
public class StatusDistanceView extends StatusCustomButton {

    public void set_gcs_meter(String _gcs_meter) {
        this._gcs_meter = _gcs_meter;
    }

    public void set_gcs_feet(String _gcs_feet) {
        this._gcs_feet = _gcs_feet;
    }


    private String _gcs_meter;
    private String _gcs_feet;


    public StatusDistanceView(Context context) {
        super(context);

        initialize();
    }

    public StatusDistanceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
    }

    public StatusDistanceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize();

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StatusDistanceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialize();
    }

    private void initialize()
    {
        _gcs_meter = "- -";
        _gcs_feet = "- -";

    }


    @Override
    public void updateUI() {
        super.updateUI();

        if(_mmeasure.size() > 0)
        {
            switch (index)
            {
                case 0: setValue(_gcs_meter.toString() + " " + _mmeasure.get(index)); break;
                case 1: setValue(_gcs_feet.toString() + " "  + _mmeasure.get(index)); break;

            }
        }
    }
}
