package pipi.win.a2048.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.StringWriter;
import java.util.List;

import pipi.win.a2048.service.base.BaseService;
import pipi.win.a2048.utility.FileUtil;

public class QueryService extends BaseService {
    public static final int BUFFERSIZE=100;

    public QueryService() {
    }
    public static void startService(Context context){
        context.startService(new Intent( context, QueryService.class));
    }
    public static void stopService(Context context){
        context.stopService(new Intent(context, QueryService.class));
    }
    protected StringWriter sensorWritor, touchWritor;
    protected int sensorCnt,touchCnt;
    protected Binder binder;




    @Override
    public void onCreate() {
        super.onCreate();
        sensorWritor=new StringWriter(BUFFERSIZE);
        touchWritor =new StringWriter(BUFFERSIZE);

        binder=new QueryBind();


    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public class QueryBind extends Binder{
        public void cacheSensorData(List<String[]> data){

            FileUtil.writeToBuffer(sensorWritor,data);
            sensorCnt++;

        }

        public void cacheTouchData (List<String[]> data){

            FileUtil.writeToBuffer(touchWritor,data);
            touchCnt++;
        }
    }
}
