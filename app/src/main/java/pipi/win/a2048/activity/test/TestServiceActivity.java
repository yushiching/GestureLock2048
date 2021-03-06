package pipi.win.a2048.activity.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.uberspot.a2048.R;
import com.uberspot.a2048.SensorService;

import pipi.win.a2048.activity.base.BaseActivity;

public class TestServiceActivity extends BaseActivity {


    //To honest, writing test codes in major branch is not a good idea.

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, TestServiceActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_service);

    }

    @Override
    public void onPause() {
        SensorService.stopService(getApplicationContext());
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onResume() {

        SensorService.startService(getApplicationContext());
        super.onResume();
    }


}
