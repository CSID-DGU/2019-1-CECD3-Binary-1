package huins.ex.util.log;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;

import huins.ex.util.constants.ConstantsBase;

/**
 * Created by suhak on 15. 6. 9.
 */
public class GCSLogger {

    public enum GCS_LOGGER {LOG_INFO, LOG_WORNING, LOG_DEBUG, LOG_ERROR}

    ;

    /******************************
     * ERROR
     *********************************/
    public static void Log_error(String _class, String _message, Exception _error) {
        if (ConstantsBase.DEBUG_USAGE) {
            if (_message != null) Log.e(_class, _message, _error);
            writeMessage(_class, "LOG_ERROR :::" + _message);
        }
    }

    /******************************
     * INFO
     **********************************/
    private static void Log_info(String _class, String _message) {
        if (ConstantsBase.DEBUG_USAGE) {
            if (_message != null) Log.i(_class, _message);
            writeMessage(_class, "LOG_INFO :::" + _message);
        }
    }

    /******************************
     * WARNING
     ********************************/
    private static void Log_worning(String _class, String _message) {
        if (ConstantsBase.DEBUG_USAGE) {
            if (_message != null) Log.w(_class, _message);
            writeMessage(_class, "LOG_WORNING :::" + _message);
        }
    }

    /******************************
     * DEBUG
     *********************************/
    private static void Log_debug(String _class, String _message) {
        if (ConstantsBase.DEBUG_USAGE) {
            if (_message != null) Log.d(_class, _message);
            writeMessage(_class, "LOG_DEBUG :::" + _message);
        }
    }

    private static void writeMessage(String _class, String _message) {
        FileWriter _filewriter;

        GregorianCalendar calendar = new GregorianCalendar();

        if (ConstantsBase.DEBUG_LEVEL < 2)
            return;

        try {
            _filewriter = new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() + "/uav_gcs" + ".log", true);

            BufferedWriter _bufferedwriter = new BufferedWriter(_filewriter);

            _bufferedwriter.write("===" + String.valueOf(calendar.HOUR) + ":" + String.valueOf(calendar.MINUTE) + ":" + String.valueOf(calendar.SECOND) + "[" + _class + "] " + _message + "     ");
            _bufferedwriter.write("\n");

            _bufferedwriter.close();
            _filewriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WriteLog(GCS_LOGGER type, String _class, String _message) {
        switch (type) {
            case LOG_INFO:
                Log_info(_class, _message);
                break;
            case LOG_WORNING:
                Log_worning(_class, _message);
                break;
            case LOG_DEBUG:
                Log_debug(_class, _message);
                break;

            default:
                break;
        }
    }
}
