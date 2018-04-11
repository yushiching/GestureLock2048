package pipi.win.a2048;

import android.app.Application;

import pipi.win.a2048.utility.LogUtil;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.initLogger();
    }
}
