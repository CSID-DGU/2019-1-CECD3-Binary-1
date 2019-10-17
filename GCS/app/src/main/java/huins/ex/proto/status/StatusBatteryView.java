package huins.ex.proto.status;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import huins.ex.proto.StatusCustomButton;

/**
 * Created by suhak on 15. 10. 5.
 */
public class StatusBatteryView extends StatusCustomButton {

    public void set_gcs_voltage(String _gcs_voltages) {
        this._gcs_voltages = _gcs_voltages;
    }

    public void set_gcs_current(String _gcs_current) {
        this._gcs_current = _gcs_current;
    }

    public void set_gcs_percent(String _gcs_percent) {
        this._gcs_percent = _gcs_percent;
    }

    private String _gcs_voltages;
    private String _gcs_current;
    private String _gcs_percent;


    public StatusBatteryView(Context context) {
        super(context);

        initialize();
    }

    public StatusBatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
    }

    public StatusBatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize();

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StatusBatteryView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialize();
    }

    private void initialize()
    {
        _gcs_voltages = "- -";
        _gcs_current = "- -";
        _gcs_percent = "- -";

    }


    @Override
    public void updateUI() {
        super.updateUI();

        if(_mmeasure.size() > 0)
        {
            switch (index)
            {
                case 0: setValue(_gcs_voltages.toString() + " " + _mmeasure.get(index)); break;
                case 1: setValue(_gcs_current.toString() + " "  + _mmeasure.get(index)); break;
                case 2: setValue(_gcs_percent.toString() + " "  + _mmeasure.get(index)); break;

            }
        }
    }
}
