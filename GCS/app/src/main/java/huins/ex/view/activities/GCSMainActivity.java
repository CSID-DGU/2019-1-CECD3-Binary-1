package huins.ex.view.activities;

import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import huins.ex.R;
import huins.ex.proto.attribute.AttributeEvent;
import huins.ex.util.constants.ConstantsIntent;
import huins.ex.view.activities.base.BaseUIActivity;


public class GCSMainActivity extends BaseUIActivity {

    public enum GCS_MENU {HOME, CALIBORATION,DRONE_CONNECT, FLIGHT_MANAGER, MISSION_PLANNER,MISSION_PLANNER_EDITOR, VIDEO_PLAYER, SETTING};

    private Button _connect_button;
    private Button _caliboration_button;
    private Button _mission_button;
    private Button _editor_button;
    private Button _control_button;
    private Button _setting_button;
    private Button _player_button;

    private Switch _connect_switch;
    private ConnectCheckedChangeListener _connect_event_listener;

    private boolean initial = true;

    private RemoteButtonClickListener remote_button_click_listener;

    private Intent _current_intent;

    private static final IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        eventFilter.addAction(ConstantsIntent.INTENT_GCS_AUTOPILOT_ERROR);
        eventFilter.addAction(ConstantsIntent.INTENT_GCS_CHANGE_MENU);
        eventFilter.addAction(GCSBaseApp.ACTION_DRONE_CONNECTION_FAILED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case ConstantsIntent.INTENT_GCS_AUTOPILOT_ERROR:
                    break;
                case ConstantsIntent.INTENT_GCS_CHANGE_MENU:
                    break;
                case AttributeEvent.STATE_CONNECTED:
                   // _connect_switch.setChecked(baseApp.getFlight().IsConnected());
                    //Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                    //break;
                case AttributeEvent.STATE_DISCONNECTED:
                    //Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                    //break;
                case GCSBaseApp.ACTION_DRONE_CONNECTION_FAILED:
                    //Toast.makeText(getApplicationContext(), "Connect Failed", Toast.LENGTH_SHORT).show();
                    _connect_switch.setChecked(baseApp.getFlight().IsConnected());
                    break;
            }

        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();
        //   enableSlidingUpPanel(baseApp.getFlight());
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();
        //   enableSlidingUpPanel(baseApp.getFlight());
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gcsmain);
        remote_button_click_listener = new RemoteButtonClickListener();

        _caliboration_button = (Button)findViewById(R.id.BTN_GCS_MAIN_CALIBRATION);
        _mission_button = (Button)findViewById(R.id.BTN_GCS_MAIN_MISSION);
        _editor_button = (Button)findViewById(R.id.BTN_GCS_MAIN_EDITOR);
        _control_button = (Button)findViewById(R.id.BTN_GCS_MAIN_CONTROL);
        _setting_button = (Button)findViewById(R.id.BTN_GCS_MAIN_SETTING);
        _player_button = (Button)findViewById(R.id.BTN_GCS_MAIN_VIDEO);

        _connect_switch = (Switch)findViewById(R.id.SWITCH_GCS_MAIN_CONNECT);

        _mission_button.setOnClickListener(remote_button_click_listener);
        _caliboration_button.setOnClickListener(remote_button_click_listener);
        _editor_button.setOnClickListener(remote_button_click_listener);
        _control_button.setOnClickListener(remote_button_click_listener);
        _setting_button.setOnClickListener(remote_button_click_listener);
        _player_button.setOnClickListener(remote_button_click_listener);

        _connect_event_listener = new ConnectCheckedChangeListener();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);

        if(initial) {
            initial = false;

            setViewLocation(_mission_button, 2050, 940);
            setViewLocation(_caliboration_button, 450, 620);
            setViewLocation(_setting_button, 770, 940);
            setViewLocation(_editor_button, 1410, 940);
            setViewLocation(_control_button, 1730, 620);
            setViewLocation(_player_button, 130, 940);
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void setViewLocation(View view, int x, int y)
    {
        float widthrate = getDisplayWidth() / 2560f;
        float heightrate = getDisplayheight() / 1600f;
        float drawrate = (widthrate + heightrate) / 2f;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
        params.setMargins((int)(x * widthrate), (int)(y * heightrate), 0, 0);
        view.setLayoutParams(params);

    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        _connect_switch.setChecked(baseApp.getFlight().IsConnected());

        _connect_switch.setOnClickListener(_connect_event_listener);
        _connect_switch.setOnCheckedChangeListener(_connect_event_listener);
    }

    public class ConnectCheckedChangeListener implements CompoundButton.OnClickListener, CompoundButton.OnCheckedChangeListener
    {
        @Override
        public void onClick(View view) {
            toggleFlightConnection();
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked && !baseApp.getFlight().IsConnected()) {
                _connect_switch.setChecked(false);
            }
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        _connect_switch.setOnCheckedChangeListener(null);
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    public void onBackPressed() {
        // super.onBackPressed(); //지워야 실행됨

        Builder d = new Builder(this);
        d.setMessage("정말 종료하시겠습니까?");
        d.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // process전체 종료
                finish();
                baseApp.notifyBackPressedFromMain();
            }
        });
        d.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        d.show();
    }

    private static final int ACTIVITY = 0;

    private void open_sub_activity(int id)
    {
        Intent intent;

        switch (id) {
            case R.id.BTN_GCS_MAIN_CALIBRATION:
            case R.id.BTN_GCS_MAIN_VIDEO:
            case R.id.BTN_GCS_MAIN_SETTING:
            case R.id.BTN_GCS_MAIN_CONTROL:
            case R.id.BTN_GCS_MAIN_MISSION:
                //TODO : Not implemented.
                Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
                break;
            case R.id.BTN_GCS_MAIN_EDITOR:
                _current_intent = new Intent(this, GCSEditorActivity.class);
                startActivityForResult(_current_intent, ACTIVITY);
                break;
        }
    }


    class RemoteButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            open_sub_activity(v.getId());

        }
    }

}