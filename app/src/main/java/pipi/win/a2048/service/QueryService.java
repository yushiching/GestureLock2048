package pipi.win.a2048.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import pipi.win.a2048.network.ClientFactory;
import pipi.win.a2048.network.ICareInterface;
import pipi.win.a2048.service.base.BaseService;
import pipi.win.a2048.utility.FileUtil;
import pipi.win.a2048.utility.LogUtil;
import retrofit2.Response;

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
    protected long sensorCnt,touchCnt;
    protected Binder binder;
    protected ICareInterface iCareInterface;

    protected Thread queryth;




    @Override
    public void onCreate() {
        super.onCreate();
        sensorWritor=new StringWriter(BUFFERSIZE);
        touchWritor =new StringWriter(BUFFERSIZE);

        queryth=new Thread();
        binder=new QueryBind();
        iCareInterface= ClientFactory.newInterface();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected void stateChecker(){
        if(queryth.isAlive()){
            return;
            //thread dead continues;
        }

        if(sensorCnt>100 && touchCnt >100){
            LogUtil.i(this.getClass().getSimpleName()+"Launch Network Query");
            queryth=new Thread(new QueryRunnable());
            queryth.start();
        }
    }

    protected class QueryRunnable implements Runnable{
        protected int loopcnt;
        @Override
        public void run() {


            String sensordata=sensorWritor.toString();
            String touchdata=touchWritor.toString();
            Response<String> status;
            String result;
            try{
                Response<String> response=iCareInterface.uploadData(sensordata,touchdata).execute();
                String payload=response.body();
                do{
                    status=iCareInterface.queryID(payload).execute();
                    Thread.sleep(1000);
                    loopcnt++;
                    if(loopcnt > 30){
                        throw new IOException("Time Out");
                    }
                }while (status.code()!= 200);
                result=status.body();
                LogUtil.i("iCare Result: "+result);
            }catch (IOException e){
                LogUtil.e(e,"Query IO Failed");
            }catch (InterruptedException e){
                LogUtil.e(e,"Sleep err");
            }

        }
    }

    public static class QueryInit{
        public QueryBind getBind() {
            return bind;
        }

        private QueryBind bind;
        private ServiceConnection connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bind=(QueryService.QueryBind)service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bind=null;
            }
        };
        public void bindservice(Context context){
            Intent intent=new Intent(context, QueryService.class);
            context.bindService(intent,connection, Service.BIND_AUTO_CREATE);
        }
        public void unbindservice(Context context){
            context.unbindService(connection);
        }
    }



    public class QueryBind extends Binder{
        public void cacheSensorData(List<String[]> data){

            FileUtil.writeToBuffer(sensorWritor,data);
            sensorCnt++;
            stateChecker();

        }

        public void cacheTouchData (List<String[]> data){

            FileUtil.writeToBuffer(touchWritor,data);
            touchCnt++;
        }


    }
}
