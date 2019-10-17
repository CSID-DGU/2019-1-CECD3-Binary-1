package huins.ex.proto.status;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import huins.ex.proto.StatusCustomButton;

/**
 * Created by suhak on 15. 10. 5.
 */
public class StatusSpeedView extends StatusCustomButton {

    public void set_gcs_mps(String _gcs_mps) {
        this._gcs_mps = _gcs_mps;
    }

    public void set_gcs_miles(String _gcs_miles) {
        this._gcs_miles = _gcs_miles;
    } 

    private String _gcs_mps;
    private String _gcs_miles;


    public StatusSpeedView(Context context) {
        super(context);

        initialize();
    }

    public StatusSpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
    }

    public StatusSpeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize();

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StatusSpeedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialize();
    }

    private void initialize()
    {
        _gcs_mps = "- -";
        _gcs_miles = "- -";

    }


    @Override
    public void updateUI() {
        super.updateUI();

        if(_mmeasure.size() > 0)
        {
            switch (index)
            {
                case 0: setValue(_gcs_mps.toString() + " " + _mmeasure.get(index)); break;
                case 1: setValue(_gcs_miles.toString() + " "  + _mmeasure.get(index)); break;

            }
        }
    }
}
