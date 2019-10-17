package huins.ex.proto;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import huins.ex.interfaces.TowerListener;
import huins.ex.util.constants.ConstantsBase;
import huins.ex.util.log.GCSLogger;

/**
 * Created by suhak on 15. 6. 25.
 */
public class ControlTower {
    private static final String TAG = ControlTower.class.getSimpleName();

    private final Intent serviceIntent = new Intent(IGCSServices.class.getName());

    private final AtomicBoolean isServiceConnecting = new AtomicBoolean(false);

    private final Context context;
    private TowerListener towerListener;
    private IGCSServices GCSServices;

    String getApplicationId() {
        return context.getPackageName();
    }

    private final IBinder.DeathRecipient binderDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            notifyTowerDisconnected();
        }
    };

    private final ServiceConnection servicesConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GCSLogger.WriteLog(GCSLogger.GCS_LOGGER.LOG_INFO, TAG, "onServiceConnected");
            GCSServices = IGCSServices.Stub.asInterface(service);
            try {
                final int libVersionCode = GCSServices.getApiVersionCode();
                if (libVersionCode < ConstantsBase.GCS_VERSION) {
                    //Prompt the user to update the 3DR Services app.
                    GCSServices = null;
                    promptForGCSUpdate();
                    context.unbindService(servicesConnection);
                } else {
                    GCSServices.asBinder().linkToDeath(binderDeathRecipient, 0);
                    notifyTowerConnected();
                }
            } catch (RemoteException e) {
                notifyTowerDisconnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            GCSLogger.WriteLog(GCSLogger.GCS_LOGGER.LOG_INFO, TAG, "onServiceDisconnected");
            isServiceConnecting.set(false);
            notifyTowerDisconnected();
        }
    };


    public ControlTower(Context context) {
        this.context = context;
    }

    IGCSServices getGCSServices() {
        return GCSServices;
    }

    void notifyTowerConnected() {
        if (towerListener == null)
            return;

        towerListener.onTowerConnected();
    }

    void notifyTowerDisconnected() {
        if (towerListener == null)
            return;

        towerListener.onTowerDisconnected();
    }

    private void promptForGCSUpdate() {
    //    context.startActivity(new Intent(context, UpdateServiceDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public boolean isTowerConnected() {
        return this.GCSServices != null && this.GCSServices.asBinder().pingBinder();
    }

    public void registerFlight(Flight flight, Handler handler) {
        if(flight != null) {
            if(!this.isTowerConnected()) {
                throw new IllegalStateException("Control Tower must be connected.");
            } else {
                flight.init(this, handler);
                flight.start();
            }
        }
    }

    public void unregisterFlight(Flight flight) {
        if(flight != null) {
            flight.destroy();
        }

    }

    public void connect(TowerListener listener) {
        if(this.towerListener == null || !isServiceConnecting.get() && !isTowerConnected()) {
            if(listener == null) {
                throw new IllegalArgumentException("ServiceListener argument cannot be null.");
            } else {
                this.towerListener = listener;
                if(!isTowerConnected() && !isServiceConnecting.get()) {
                    final ResolveInfo info = context.getPackageManager().resolveService(serviceIntent, 0);
                    if (info != null) {
                        serviceIntent.setClassName(info.serviceInfo.packageName, info.serviceInfo.name);
                        isServiceConnecting.set(context.bindService(serviceIntent, servicesConnection,
                                Context.BIND_AUTO_CREATE));
                    }
                 //   isServiceConnecting.set(context.bindService(serviceIntent, servicesConnection, Context.BIND_AUTO_CREATE));
                }
            }
        }
    }

    public void disconnect() {
        if(this.GCSServices != null) {
            this.GCSServices.asBinder().unlinkToDeath(this.binderDeathRecipient, 0);
            this.GCSServices = null;
        }

        this.towerListener = null;

        try {
            this.context.unbindService(this.servicesConnection);
            //onServiceDisconnected(ComponentName name) 에서 수행할 내용을 unbindService()이후에도 수행하도록 수정.
            isServiceConnecting.set(false);
            notifyTowerDisconnected();
        } catch (Exception var2) {
            Log.e(TAG, "Error occurred while unbinding from 3DR Services.", var2);
        }

    }

}
