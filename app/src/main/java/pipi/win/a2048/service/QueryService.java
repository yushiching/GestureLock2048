package pipi.win.a2048.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import okhttp3.ResponseBody;
import pipi.win.a2048.network.ClientFactory;
import pipi.win.a2048.network.ICareInterface;
import pipi.win.a2048.service.base.BaseService;
import pipi.win.a2048.utility.FileUtil;
import pipi.win.a2048.utility.LogUtil;
import retrofit2.Response;

public class QueryService extends BaseService {
    public static final int BUFFERSIZE=100;
    public static final long[] PATTERN=new long[]{0,300,100,300,100};
    public static int THREASHOLD=50;

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

    protected String queryType;
    protected Thread queryth;

    protected Vibrator vibrator;




    @Override
    public void onCreate() {
        super.onCreate();
        sensorWritor=new StringWriter(BUFFERSIZE);
        touchWritor =new StringWriter(BUFFERSIZE);

        queryth=new Thread();
        binder=new QueryBind();
        iCareInterface= ClientFactory.newInterface();
        vibrator=(Vibrator) getSystemService(VIBRATOR_SERVICE);



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

        if(sensorCnt > THREASHOLD &&
                touchCnt > THREASHOLD){
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
            Response<ResponseBody> queryStatus;

            try{
                Response<String> response=iCareInterface.uploadData(sensordata,touchdata).execute();
                String qid =response.body();
                do{
                    queryStatus =iCareInterface.queryID(qid).execute();
                    Thread.sleep(1000);
                    loopcnt++;
                    if(loopcnt > 100){
                        throw new IOException("Time Out");
                    }
                }while (queryStatus.code()!= 200);
                queryType= queryStatus.body().string();


                showMessage(queryType);
                LogUtil.i("iCare Result: "+ queryType);
            }catch (IOException e){
                LogUtil.e(e,"Query IO Failed");
            }catch (InterruptedException e){
                LogUtil.e(e,"Sleep err");
            }
            cleanDataZone();




        }
    }

    private void showMessage(final String msg){
        Handler handler=new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(QueryService.this, msg, Toast.LENGTH_LONG).show();

                if(msg.toLowerCase().startsWith("c")){
                    vibrator.vibrate(2000);
                }else {
                    vibrator.vibrate(PATTERN,-1);
                }
            }
        });
    }


    private void cleanDataZone(){
        sensorCnt=0;
        touchCnt=0;
        sensorWritor=new StringWriter();
        touchWritor=new StringWriter();
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

            if(queryth.isAlive()){
                return;
            }
            FileUtil.writeToBuffer(sensorWritor,data);
            sensorCnt++;
            stateChecker();

        }

        public void cacheTouchData (List<String[]> data){

            if(queryth.isAlive()){
                return;
            }
            FileUtil.writeToBuffer(touchWritor,data);
            touchCnt++;
        }


    }
}
