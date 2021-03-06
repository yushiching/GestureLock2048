package pipi.win.a2048.activity.base;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

//import com.baidu.mobstat.StatService;
import com.orhanobut.logger.Logger;


/**
 * Created by pip on 2017/7/11.
 */

public class BaseActivity extends AppCompatActivity {
    public static String TAG="BaseActivity";

    public static void startActivity(Context context){
        throw new UnsupportedClassVersionError("Not Implemented Static Method");
    }

    protected void mkToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
    protected void mkToastL(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        Logger.t(this.getClass().getSimpleName()).d("onCreate");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.t(this.getClass().getSimpleName()).d("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.t(this.getClass().getSimpleName()).d( "onResume");



    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.t(this.getClass().getSimpleName()).d("onPause");


    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.t(this.getClass().getSimpleName()).d("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        Logger.t(this.getClass().getSimpleName()).d("onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.t(this.getClass().getSimpleName()).d("onRestart");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(this.getClass().getSimpleName(), "onConfigurationChanged: "+newConfig.toString());
    }

    protected void loge(Throwable e, String msg){
        Logger.t(this.getClass().getSimpleName()).e(e,msg);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                //WARNING:android.R.id   =.=
                finish();
                break;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void logi(String msg){
        Logger.t(this.getClass().getSimpleName()).i(msg);
    }
}
