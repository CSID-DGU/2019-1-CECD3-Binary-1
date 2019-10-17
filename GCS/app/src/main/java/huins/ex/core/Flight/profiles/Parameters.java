package huins.ex.core.Flight.profiles;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.common.msg_param_value;
import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightInterfaces.Handler;
import huins.ex.core.Flight.FlightInterfaces.OnFlightListener;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.MAVLink.MavLinkParameters;
import huins.ex.core.model.Flight;
import huins.ex.core.parameters.Parameter;

/**
 * Class to manage the communication of parameters to the MAV.
 * <p/>
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 */
public class Parameters extends FlightVariable implements OnFlightListener {

    private static final int TIMEOUT = 1000; //milliseconds

    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    private int expectedParams;

    private final Map<Integer, Boolean> paramsRollCall = new HashMap<>();
    private final ConcurrentHashMap<String, Parameter> parameters = new ConcurrentHashMap<>();

    private FlightInterfaces.OnParameterManagerListener parameterListener;

    public Handler watchdog;
    public Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            onParameterStreamStopped();
        }
    };

    public Parameters(Flight mflight, Handler handler) {
        super(mflight);
        this.watchdog = handler;
        mflight.addFlightListener(this);
    }

    public void refreshParameters() {
        if (isRefreshing.compareAndSet(false, true)) {
            expectedParams = 0;
            parameters.clear();
            paramsRollCall.clear();

            if (parameterListener != null)
                parameterListener.onBeginReceivingParameters();

            MavLinkParameters.requestParametersList(mflight);
            resetWatchdog();
        }
    }

    public Map<String, Parameter> getParameters() {
        //Update the cache if it's stale. Parameters download is expensive, but we assume the caller knows what it's
        // doing.
        if (parameters.isEmpty())
            refreshParameters();

        return parameters;
    }

    /**
     * Try to process a Mavlink message if it is a parameter related message
     *
     * @param msg Mavlink message to process
     * @return Returns true if the message has been processed
     */
    public boolean processMessage(MAVLinkMessage msg) {
        if (msg.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
            processReceivedParam((msg_param_value) msg);
            return true;
        }
        return false;
    }

    private void processReceivedParam(msg_param_value m_value) {
        // collect params in parameter list
        Parameter param = new Parameter(m_value);

        parameters.put(param.name.toLowerCase(Locale.US), param);
        final int paramIndex = m_value.param_index;
        if (paramIndex == -1) {
            // update listener
            if (parameterListener != null)
                parameterListener.onParameterReceived(param, 0, 1);

            mflight.notifyFlightEvent(FlightEventsType.PARAMETERS_DOWNLOADED);

            if (parameterListener != null) {
                parameterListener.onEndReceivingParameters();
            }
            return;
        }

        paramsRollCall.put(paramIndex, true);
        expectedParams = m_value.param_count;

        // update listener
        if (parameterListener != null)
            parameterListener.onParameterReceived(param, paramIndex, m_value.param_count);

        // Are all parameters here? Notify the listener with the parameters
        if (parameters.size() >= m_value.param_count) {
            killWatchdog();
            isRefreshing.set(false);
            mflight.notifyFlightEvent(FlightEventsType.PARAMETERS_DOWNLOADED);

            if (parameterListener != null) {
                parameterListener.onEndReceivingParameters();
            }
        } else {
            resetWatchdog();
        }
    }

    private void reRequestMissingParams(int howManyParams) {
        for (int i = 0; i < howManyParams; i++) {
            Boolean isPresent = paramsRollCall.get(i);
            if (isPresent == null || !isPresent) {
                MavLinkParameters.readParameter(mflight, i);
            }
        }
    }

    public void sendParameter(Parameter parameter) {
        MavLinkParameters.sendParameter(mflight, parameter);
    }

    public void readParameter(String name) {
        MavLinkParameters.readParameter(mflight, name);
    }

    public Parameter getParameter(String name) {
        if (name == null || name.length() == 0)
            return null;

        return parameters.get(name.toLowerCase(Locale.US));
    }

    private void onParameterStreamStopped() {
        if (expectedParams > 0) {
            reRequestMissingParams(expectedParams);
            resetWatchdog();
        } else {
            isRefreshing.set(false);
        }
    }

    private void resetWatchdog() {
        watchdog.removeCallbacks(watchdogCallback);
        watchdog.postDelayed(watchdogCallback, TIMEOUT);
    }

    private void killWatchdog() {
        watchdog.removeCallbacks(watchdogCallback);
        isRefreshing.set(false);
    }

    @Override
    public void onFlightEvent(FlightEventsType event, Flight flight) {
        switch (event) {
            case HEARTBEAT_FIRST:
                if (!flight.getState().isFlying()) {
                    refreshParameters();
                }
                break;

            case DISCONNECTED:
            case HEARTBEAT_TIMEOUT:
                killWatchdog();
                break;
            default:
                break;

        }
    }

    public void setParameterListener(FlightInterfaces.OnParameterManagerListener parameterListener) {
        this.parameterListener = parameterListener;
    }
}
