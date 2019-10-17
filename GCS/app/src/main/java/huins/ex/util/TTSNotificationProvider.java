package huins.ex.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import huins.ex.R;
import huins.ex.core.ErrorType;
import huins.ex.proto.Flight;
import huins.ex.proto.attribute.AttributeEvent;
import huins.ex.proto.attribute.AttributeEventExtra;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.property.Altitude;
import huins.ex.proto.property.Battery;
import huins.ex.proto.property.Gps;
import huins.ex.proto.property.Signal;
import huins.ex.proto.property.Speed;
import huins.ex.proto.property.State;
import huins.ex.proto.property.VehicleMode;
import huins.ex.util.constants.GCSPreferences;

/**
 * Implements DroidPlanner audible notifications.
 */
	public class TTSNotificationProvider implements OnInitListener,
		NotificationHandler.NotificationProvider {

	private static final String CLAZZ_NAME = TTSNotificationProvider.class.getName();
	private static final String TAG = TTSNotificationProvider.class.getSimpleName();

	private static final double BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT = 10;

    private static final double MAX_ALTITUDE = 121.92; //meters

	/**
	 * Utterance id for the periodic status speech.
	 */
	private static final String PERIODIC_STATUS_UTTERANCE_ID = "periodic_status_utterance";

	/**
	 * Action used for message to be delivered by the tts speech engine.
	 */
	public static final String ACTION_SPEAK_MESSAGE = CLAZZ_NAME + ".ACTION_SPEAK_MESSAGE";
	public static final String EXTRA_MESSAGE_TO_SPEAK = "extra_message_to_speak";



	/**
	 * Stored the parameters to be passed to the tts `speak(...)` method.
	 */
	private final HashMap<String, String> mTtsParams = new HashMap<String, String>();

	private TextToSpeech tts;
	private int lastBatteryDischargeNotification;

	private final Context context;
	private final GCSPreferences mAppPrefs;
	private final Handler handler = new Handler();
	private int statusInterval;


	public final Watchdog watchdogCallback = new Watchdog();

	private final Flight flight;

	private final static IntentFilter eventFilter = new IntentFilter();
    static {
        eventFilter.addAction(AttributeEvent.STATE_ARMING);
        eventFilter.addAction(AttributeEvent.BATTERY_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_VEHICLE_MODE);
        eventFilter.addAction(AttributeEvent.MISSION_SENT);
        eventFilter.addAction(AttributeEvent.GPS_FIX);
        eventFilter.addAction(AttributeEvent.MISSION_RECEIVED);
        eventFilter.addAction(AttributeEvent.HEARTBEAT_FIRST);
        eventFilter.addAction(AttributeEvent.HEARTBEAT_TIMEOUT);
        eventFilter.addAction(AttributeEvent.HEARTBEAT_RESTORED);
        eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        eventFilter.addAction(AttributeEvent.MISSION_ITEM_UPDATED);
        eventFilter.addAction(AttributeEvent.FOLLOW_START);
        eventFilter.addAction(AttributeEvent.AUTOPILOT_ERROR);
        eventFilter.addAction(AttributeEvent.ALTITUDE_UPDATED);
        eventFilter.addAction(AttributeEvent.SIGNAL_WEAK);
        eventFilter.addAction(AttributeEvent.WARNING_NO_GPS);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (tts == null)
                return;

            final String action = intent.getAction();
            State flightState = flight.getAttribute(AttributeType.STATE);

            switch (action) {
                case AttributeEvent.STATE_ARMING:
                    if (flightState != null)
                        speakArmedState(flightState.isArmed());
                    break;
                case AttributeEvent.BATTERY_UPDATED:
                    Battery flightBattery = flight.getAttribute(AttributeType.BATTERY);
                    if (flightBattery != null)
                        batteryDischargeNotification(flightBattery.getBatteryRemain());
                    break;
                case AttributeEvent.STATE_VEHICLE_MODE:
                    if (flightState != null)
                        speakMode(flightState.getVehicleMode());
                    break;
                case AttributeEvent.MISSION_SENT:
                    Toast.makeText(context, "Waypoints sent", Toast.LENGTH_SHORT).show();
                    speak("Waypoints saved to flight");
                    break;
                case AttributeEvent.GPS_FIX:
                    Gps flightGps = flight.getAttribute(AttributeType.GPS);
                    if (flightGps != null)
                        speakGpsMode(flightGps.getFixType());
                    break;
                case AttributeEvent.MISSION_RECEIVED:
                    Toast.makeText(context, "Waypoints received from flight", Toast.LENGTH_SHORT).show();
                    speak("Waypoints received");
                    break;
                case AttributeEvent.HEARTBEAT_FIRST:
                    watchdogCallback.setflight(flight);
                    scheduleWatchdog();
                    speak("Connected");
                    break;
                case AttributeEvent.HEARTBEAT_TIMEOUT:
                    if (mAppPrefs.getWarningOnLostOrRestoredSignal()) {
                        speak("Data link lost, check connection.");
                        handler.removeCallbacks(watchdogCallback);
                    }
                    break;
                case AttributeEvent.HEARTBEAT_RESTORED:
                    watchdogCallback.setflight(flight);
                    scheduleWatchdog();
                    if (mAppPrefs.getWarningOnLostOrRestoredSignal()) {
                        speak("Data link restored");
                    }
                    break;
                case AttributeEvent.STATE_DISCONNECTED:
                    handler.removeCallbacks(watchdogCallback);
                    break;
                case AttributeEvent.MISSION_ITEM_UPDATED:
                    int currentWaypoint = intent.getIntExtra(AttributeEventExtra.EXTRA_MISSION_CURRENT_WAYPOINT, 0);
                    speak("Going for waypoint " + currentWaypoint);
                    break;
                case AttributeEvent.FOLLOW_START:
                    speak("Following");
                    break;
                case AttributeEvent.ALTITUDE_UPDATED:
                    if (mAppPrefs.getWarningOn400ftExceeded()) {
                        final Altitude altitude = flight.getAttribute(AttributeType.ALTITUDE);
                        if (altitude.getAltitude() > MAX_ALTITUDE)
                            speak("warning, 400 feet exceeded");
                    }
                    break;
                case AttributeEvent.AUTOPILOT_ERROR:
                    if(mAppPrefs.getWarningOnAutopilotWarning()) {
                        String errorId = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                        final ErrorType errorType = ErrorType.getErrorById(errorId);
                        if (errorType != null && errorType != ErrorType.NO_ERROR) {
                            speak(errorType.getLabel(context).toString());
                        }
                    }
                    break;
                case AttributeEvent.SIGNAL_WEAK:
                    if (mAppPrefs.getWarningOnLowSignalStrength()) {
                        speak("Warning, weak signal");
                    }
                    break;
                case AttributeEvent.WARNING_NO_GPS:
                    speak("Error, no gps lock yet");
                    break;
            }
        }
    };

	private final AtomicBoolean mIsPeriodicStatusStarted = new AtomicBoolean(false);
	/**
	 * Listens for updates to the status interval.
	 */
	private final BroadcastReceiver mSpeechIntervalUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (ACTION_SPEAK_MESSAGE.equals(action)) {
				String msg = intent.getStringExtra(EXTRA_MESSAGE_TO_SPEAK);
				if (msg != null) {
					speak(msg);
				}
			}
		}
	};

	/**
	 * Monitors speech completion.
	 */
	private final TextToSpeech.OnUtteranceCompletedListener mSpeechCompleteListener = new TextToSpeech.OnUtteranceCompletedListener() {
		@Override
		public void onUtteranceCompleted(String utteranceId) {
			if (PERIODIC_STATUS_UTTERANCE_ID.equals(utteranceId)) {
				mIsPeriodicStatusStarted.set(false);
			}
		}
	};
	private class Watchdog implements Runnable {

		private final StringBuilder mMessageBuilder = new StringBuilder();
		private Flight flight;

		public void run() {
			handler.removeCallbacks(watchdogCallback);

			if (flight != null) {
                final State flightState = flight.getAttribute(AttributeType.STATE);
                if(flightState.isConnected() && flightState.isArmed())
				    speakPeriodic(flight);
			}

			if (statusInterval != 0) {
				handler.postDelayed(watchdogCallback, statusInterval * 1000);
			}
		}

		private void speakPeriodic(Flight flight) {
			// Drop the message if the previous one is not done yet.
			if (mIsPeriodicStatusStarted.compareAndSet(false, true)) {
				final SparseBooleanArray speechPrefs = mAppPrefs.getPeriodicSpeechPrefs();

				mMessageBuilder.setLength(0);
				if (speechPrefs.get(R.string.pref_tts_periodic_bat_volt_key)) {
                    final Battery flightBattery = flight.getAttribute(AttributeType.BATTERY);
					mMessageBuilder.append(String.format("battery %2.1f volts. ", flightBattery.getBatteryVoltage()));
				}

				if (speechPrefs.get(R.string.pref_tts_periodic_alt_key)) {
                    final Altitude altitude = flight.getAttribute(AttributeType.ALTITUDE);
					mMessageBuilder.append("altitude, ").append((int) (altitude.getAltitude())).append(" meters. ");
				}

				if (speechPrefs.get(R.string.pref_tts_periodic_airspeed_key)) {
                    final Speed flightSpeed = flight.getAttribute(AttributeType.SPEED);
					mMessageBuilder.append("airspeed, ").append((int) (flightSpeed.getAirSpeed()))
                            .append(" meters per second. ");
				}

				if (speechPrefs.get(R.string.pref_tts_periodic_rssi_key)) {
                    final Signal signal = flight.getAttribute(AttributeType.SIGNAL);
					mMessageBuilder.append("r s s i, ").append((int) signal.getRssi()).append(" decibels");
				}

				speak(mMessageBuilder.toString(), true, PERIODIC_STATUS_UTTERANCE_ID);
			}
		}

		public void setflight(Flight flight) {
			this.flight = flight;
		}
	}

	TTSNotificationProvider(Context context, Flight flight) {
		this.context = context;
		this.flight = flight;
		tts = new TextToSpeech(context, this);
		mAppPrefs = new GCSPreferences(context);

        LocalBroadcastManager.getInstance(context).registerReceiver(eventReceiver, eventFilter);
	}

	private void scheduleWatchdog() {
		handler.removeCallbacks(watchdogCallback);
		statusInterval = mAppPrefs.getSpokenStatusInterval();
		if (statusInterval != 0) {
			handler.postDelayed(watchdogCallback, statusInterval * 1000);
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			// TODO: check if the language is available
			Locale ttsLanguage;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				ttsLanguage = tts.getDefaultLanguage();
			} else {
				ttsLanguage = tts.getLanguage();
			}

			if (ttsLanguage == null) {
				ttsLanguage = Locale.US;
			}

			int supportStatus = tts.setLanguage(ttsLanguage);
			switch (supportStatus) {
			case TextToSpeech.LANG_MISSING_DATA:
			case TextToSpeech.LANG_NOT_SUPPORTED:
				tts.shutdown();
				tts = null;

				Log.e(TAG, "TTS Language data is not available.");
				Toast.makeText(context, "Unable to set 'Text to Speech' language!",
						Toast.LENGTH_LONG).show();
				break;
			}

			if (tts != null) {
				tts.setOnUtteranceCompletedListener(mSpeechCompleteListener);

				// Register the broadcast receiver
				final IntentFilter intentFilter = new IntentFilter();
				intentFilter.addAction(ACTION_SPEAK_MESSAGE);

				LocalBroadcastManager.getInstance(context).registerReceiver(
						mSpeechIntervalUpdateReceiver, intentFilter);
			}
		} else {
			// Notify the user that the tts engine is not available.
			Log.e(TAG, "TextToSpeech initialization failed.");
			Toast.makeText(
					context,
					"Please make sure 'Text to Speech' is enabled in the "
							+ "system accessibility settings.", Toast.LENGTH_LONG).show();
		}
	}

	private void speak(String string) {
		speak(string, false, null);
	}

	private void speak(String string, boolean append, String utteranceId) {
		if (tts != null) {
			if (shouldEnableTTS()) {
				final int queueType = append ? TextToSpeech.QUEUE_ADD : TextToSpeech.QUEUE_FLUSH;

				mTtsParams.clear();
				if (utteranceId != null) {
					mTtsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
				}

				tts.speak(string, queueType, mTtsParams);
			}
		}
	}

	private boolean shouldEnableTTS() {
		return mAppPrefs.prefs.getBoolean("pref_enable_tts", false);
	}

	private void speakArmedState(boolean armed) {
		if (armed) {
			speak("Armed");
		} else {
			speak("Disarmed");
		}
	}

	private void batteryDischargeNotification(double battRemain) {
		if (lastBatteryDischargeNotification > (int) ((battRemain - 1) / BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT)
				|| lastBatteryDischargeNotification + 1 < (int) ((battRemain - 1) / BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT)) {
			lastBatteryDischargeNotification = (int) ((battRemain - 1) / BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT);
			speak("Battery at" + (int) battRemain + "%");
		}
	}

	private void speakMode(VehicleMode mode) {
        if(mode == null)
            return;

		String modeString = "Mode ";
		switch (mode) {
		case PLANE_FLY_BY_WIRE_A:
			modeString += "Fly by wire A";
			break;
		case PLANE_FLY_BY_WIRE_B:
			modeString += "Fly by wire B";
			break;
		case COPTER_ACRO:
			modeString += "Acrobatic";
			break;
		case COPTER_ALT_HOLD:
			modeString += "Altitude hold";
			break;
		case COPTER_POSHOLD:
			modeString += "Position hold";
			break;
		case PLANE_RTL:
		case COPTER_RTL:
			modeString += "Return to launch";
			break;
		default:
			modeString += mode.getLabel();
			break;
		}
		speak(modeString);
	}

	private void speakGpsMode(int fix) {
		switch (fix) {
		case 2:
			speak("GPS 2D Lock");
			break;
		case 3:
			speak("GPS 3D Lock");
			break;
		default:
			speak("Lost GPS Lock");
			break;
		}
	}

	@Override
	public void onTerminate() {
		if (tts != null) {
			tts.shutdown();
            tts = null;
		}

        LocalBroadcastManager.getInstance(context).unregisterReceiver(eventReceiver);
	}
}
