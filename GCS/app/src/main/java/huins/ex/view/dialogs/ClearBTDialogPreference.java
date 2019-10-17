package huins.ex.view.dialogs;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import huins.ex.util.constants.GCSPreferences;

/**
 * Created by suhak on 15. 10. 21.
 */
public class ClearBTDialogPreference extends DialogPreference {

    public interface OnResultListener {
        void onResult(boolean result);
    }

    private GCSPreferences mAppPrefs;

    private OnResultListener listener;

    public ClearBTDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAppPrefs = new GCSPreferences(context.getApplicationContext());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            mAppPrefs.setBluetoothDeviceAddress("");
        }

        if(listener != null)
            listener.onResult(positiveResult);
    }

    public void setOnResultListener(OnResultListener listener){
        this.listener = listener;
    }

}

