package pipi.win.a2048.service.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public abstract class BaseService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(this.getClass().getSimpleName(), "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(this.getClass().getSimpleName(), "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(this.getClass().getSimpleName(), "onDestroy: ");
    }
}
