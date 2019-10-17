package huins.ex.core.Flight;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightInterfaces.OnFlightListener;
import huins.ex.core.model.Flight;

public class FlightEvents extends FlightVariable {

    private static final long EVENT_DISPATCHING_DELAY = 33l; //milliseconds

    private final AtomicBoolean isDispatcherRunning = new AtomicBoolean(false);

    private final Runnable eventDispatcher = new Runnable() {

        @Override
        public void run() {
            handler.removeCallbacks(this);

            final FlightEventsType event = eventQueue.poll();
            if (event == null) {
                isDispatcherRunning.set(false);
                return;
            }

            for (OnFlightListener listener : flightListeners) {
                listener.onFlightEvent(event, mflight);
            }

            handler.removeCallbacks(this);
            handler.postDelayed(this, EVENT_DISPATCHING_DELAY);

            isDispatcherRunning.set(true);
        }
    };

    private final FlightInterfaces.Handler handler;

    public FlightEvents(Flight myFlight, FlightInterfaces.Handler handler) {
        super(myFlight);
        this.handler = handler;
    }

    private final ConcurrentLinkedQueue<OnFlightListener> flightListeners = new ConcurrentLinkedQueue<OnFlightListener>();
    private final ConcurrentLinkedQueue<FlightEventsType> eventQueue = new ConcurrentLinkedQueue<>();

    public void addFlightListener(OnFlightListener listener) {
        if (listener != null & !flightListeners.contains(listener))
            flightListeners.add(listener);
    }

    public void removeFlightListener(OnFlightListener listener) {
        if (listener != null && flightListeners.contains(listener))
            flightListeners.remove(listener);
    }

    public void notifyFlightEvent(FlightEventsType event) {
        if (event == null )
            return;

        if(flightListeners.isEmpty())
            return;

        if(eventQueue.contains(event))
            return;

        eventQueue.add(event);
        if (isDispatcherRunning.compareAndSet(false, true))
            handler.postDelayed(eventDispatcher, EVENT_DISPATCHING_DELAY);
    }
}
