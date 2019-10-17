package huins.ex.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import huins.ex.util.log.GCSLogger;

/**
 * Created by skku on 2016-05-31.
 */
public class Utils {
    public static boolean checkTopActivity(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
//        Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(context.getPackageName());
    }
    public static void saveBitmapFile(Bitmap bitmap) {
        if (bitmap != null) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                Date currentTime_1 = new Date();
                String dateString = formatter.format(currentTime_1);

                String path = Environment.getExternalStorageDirectory().toString();
                OutputStream fOut = null;
                File file = new File(path + "/", dateString + ".jpg");
                fOut = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                fOut.flush();
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static int checkNetworkConnectionType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null) {
            return 2;
        } else {
            if(networkInfo.getType() == 0) {
                return 0;
            } else {
                return 1;
            }
        }
    }
    public static void doMediaScan(Context context) {
        MediaScanner scanner = MediaScanner.newInstance(context);
        String path = Environment.getExternalStorageDirectory().getPath() + "/Movies/";
        scanner.mediaScanning(path);
    }
    public static void checkIfDirectoryExist() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/Movies/";
        File targetDir = new File(path);
        if(!targetDir.exists()) {
            targetDir.mkdirs();
            GCSLogger.WriteLog(GCSLogger.GCS_LOGGER.LOG_INFO, "ssw", path + " is made.");
        }
    }
}
