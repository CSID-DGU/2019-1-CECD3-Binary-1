package huins.ex.core.Flight;

/**
 * Created by Fredia Huya-Kouadio on 3/23/15.
 */
public interface LogMessageListener {

    void onMessageLogged(int mavSeverity, String message);
}
