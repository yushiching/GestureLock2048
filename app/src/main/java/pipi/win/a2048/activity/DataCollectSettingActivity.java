package pipi.win.a2048.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.uberspot.a2048.R;

import java.util.List;

import pipi.win.a2048.activity.base.AppCompatPreferenceActivity;

public class DataCollectSettingActivity extends AppCompatPreferenceActivity {

    public static void startActivity(Context context){
        context.startActivity(new Intent(context,DataCollectSettingActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.data_collect_settings);

    }

}
