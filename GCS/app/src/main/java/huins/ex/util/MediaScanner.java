package huins.ex.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;
import java.io.FilenameFilter;

import huins.ex.util.log.GCSLogger;

/**
 * Created by ssw on 2016-06-17.
 */
public class MediaScanner {
    private Context mContext;

    private String mPath;

    private MediaScannerConnection mMediaScanner;
    private MediaScannerConnection.MediaScannerConnectionClient mMediaScannerClient;

    public static MediaScanner newInstance(Context context) {
        return new MediaScanner(context);
    }

    private MediaScanner(Context context) {
        mContext = context;
    }

    public void mediaScanning(final String path) {

        if (mMediaScanner == null) {
            mMediaScannerClient = new MediaScannerConnection.MediaScannerConnectionClient() {

                @Override
                public void onMediaScannerConnected() {
                    GCSLogger.WriteLog(GCSLogger.GCS_LOGGER.LOG_INFO, "ssw", "onMediaScannerConnected : path = "+mPath);
                    File file = new File(mPath);                // 디렉토리 가져옴

                    File[] fileNames = file.listFiles(new FilenameFilter(){ // 특정 확장자만 가진 파일들을 필터링함
                        public boolean accept(File dir, String name){
                            return name.endsWith(".mp4");
                        }
                    });

                    if (fileNames != null) {
                        for (int i = 0; i < fileNames.length ; i++) {         //  파일 갯수 만큼   scanFile을 호출함
                            mMediaScanner.scanFile(fileNames[i].getAbsolutePath(), null);
                        }
                    }
                }

                @Override
                public void onScanCompleted(String path, Uri uri) {
                    GCSLogger.WriteLog(GCSLogger.GCS_LOGGER.LOG_INFO, "ssw", "onScanCompleted(" + path + ", " + uri.toString() + ")");
                    mMediaScanner.disconnect();
                }
            };
            mMediaScanner = new MediaScannerConnection(mContext, mMediaScannerClient);
        }

        mPath = path;
        mMediaScanner.connect();
    }
}
