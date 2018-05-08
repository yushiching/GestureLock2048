
package com.uberspot.a2048;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.widget.Toast;

import com.opencsv.CSVWriter;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.cketti.library.changelog.ChangeLog;
import pipi.win.a2048.utility.FileUtil;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {
    /* Added by Xiaopeng. Declare some variables. */
    private static final String DEBUG_TAG = "Add_Touch_Sensors";
    private static final String LOG_TAG = "Log";
    private GestureDetectorCompat mDetector;
    private VelocityTracker mVelocityTracker = null;

    private List<String[]> mTouchData = new ArrayList<String[]>();
    /* End. Declare some variables. */

    private static final String MAIN_ACTIVITY_TAG = "2048_MainActivity";

    private WebView mWebView;
    private long mLastBackPress;
    private static final long M_BACK_PRESS_THRESHOLD = 3500;
    private static final String IS_FULLSCREEN_PREF = "is_fullscreen_pref";
    private static boolean DEF_FULLSCREEN = true;
    private long mLastTouch;
    private static final long mTouchThreshold = 2000;
    private Toast pressBackToast;



    public static void startActivity(Context context){
        context.startActivity(new Intent(context, MainActivity.class));
    }

    @SuppressLint({"SetJavaScriptEnabled", "NewApi", "ShowToast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Don't show an action bar or title
        requestWindowFeature(Window.FEATURE_NO_TITLE);



        // Apply previous setting about showing status bar or not
        applyFullScreen(isFullScreen());

        // Check if screen rotation is locked in settings
        boolean isOrientationEnabled = false;
        try {
            isOrientationEnabled = Settings.System.getInt(getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION) == 1;
        } catch (SettingNotFoundException e) {
            Log.d(MAIN_ACTIVITY_TAG, "Settings could not be loaded");
        }

        // If rotation isn't locked and it's a LARGE screen then add orientation changes based on sensor
        int screenLayout = getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (((screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE)
                || (screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE))
                && isOrientationEnabled) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        setContentView(R.layout.activity_main);

        // Added by Xiaopeng. Start service to listen sensors e.g., accl, gyro and light
        SensorService.startService(this);
        // End. Start service to listen sensors e.g., accl, gyro and light


        // Load webview with game
        mWebView = (WebView) findViewById(R.id.mainWebView);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setRenderPriority(RenderPriority.HIGH);
        settings.setDatabasePath(getFilesDir().getParentFile().getPath() + "/databases");

        // If there is a previous instance restore it in the webview
        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            // Load webview with current Locale language
            mWebView.loadUrl("file:///android_asset/2048/index.html?lang=" + Locale.getDefault().getLanguage());
        }

        Toast.makeText(getApplication(), R.string.toggle_fullscreen, Toast.LENGTH_SHORT).show();
        // Set fullscreen toggle on webview LongClick
        mWebView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Implement a long touch action by comparing
                // time between action up and action down
                long currentTime = System.currentTimeMillis();
                if ((event.getAction() == MotionEvent.ACTION_UP)
                        && (Math.abs(currentTime - mLastTouch) > mTouchThreshold)) {
                    boolean toggledFullScreen = !isFullScreen();
                    saveFullScreen(toggledFullScreen);
                    applyFullScreen(toggledFullScreen);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mLastTouch = currentTime;
                }

                /* Added by Xiaopeng. Detect touch gestures using onTouchEvent and collect touch data. */
                mDetector.onTouchEvent(event);
                int action = event.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_UP:
                        // Get the index of the pointer associated with the action.
                        int eventIndex = event.getActionIndex();
                        int id = event.getPointerId(eventIndex);

                        String[] data = new String[13];
                        // data[0] is reserved for later use
                        data[1] = Long.toString(event.getEventTime());
                        data[2] = "2"; //2 denotes action type "ACTION_UP"
                        data[3] = Float.toString(getResources().getConfiguration().orientation);;
                        data[4] = Float.toString(event.getOrientation(0));
                        data[5] = Float.toString(event.getX(0));
                        data[6] = Float.toString(event.getY(0));
                        data[7] = Float.toString(event.getPressure(0));
                        data[8] = Float.toString(event.getSize(0));
                        data[11] = Long.toString(id);
                        data[12] = Long.toString(currentTime);
                        mTouchData.add(data);


                        FileUtil.writeToFile(LoginActivity.mTouchFilePath,mTouchData);
                        mTouchData.clear();
                        mVelocityTracker.clear();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        mTouchData.clear();
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                        break;
                }
                /* End. Detect touch gestures using onTouchEvent and collect touch data. */

                // return so that the event isn't consumed but used
                // by the webview as well
                return false;
            }
        });


        pressBackToast = Toast.makeText(getApplicationContext(), R.string.press_back_again_to_exit,
                Toast.LENGTH_SHORT);

        // Instantiate the gesture detector with the application context and an implementation of
        // GestureDetector.OnGestureListener
        mDetector = new GestureDetectorCompat(this, this);
        // Set the gesture detector as the double tap listener.
        //mDetector.setOnDoubleTapListener(this);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        long currentTime = System.currentTimeMillis();
        Log.i("MainActivity", "onDown: "+currentTime);

        if (mVelocityTracker == null) {
            // Retrieve a new VelocityTracker object to watch the velocity of a motion.
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            // Reset the velocity tracker back to its initial state.
            mVelocityTracker.clear();
        }
        // Add a user's movement to the tracker.
        mVelocityTracker.addMovement(event);

        //Log.d(DEBUG_TAG, "onDown: " + event.toString());
        //Log.d(DEBUG_TAG, "onDown pressure: " + event.getPressure(0));
        int id = event.getPointerId(0);
        String[] data = new String[13];
        // data[0] is reserved for later use
        data[1] = Long.toString(event.getEventTime());
        data[2] = "0"; //0 denotes action type "ACTION_DOWN"
        data[3] = Float.toString(getResources().getConfiguration().orientation);;
        data[4] = Float.toString(event.getOrientation(0));
        data[5] = Float.toString(event.getX(0));
        data[6] = Float.toString(event.getY(0));
        data[7] = Float.toString(event.getPressure(0));
        data[8] = Float.toString(event.getSize(0));
        data[11] = Long.toString(id);
        data[12] = Long.toString(currentTime);
        mTouchData.add(data);

        return true;
    }

    @Override
    // Can be combined with scroll action
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
/*            Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
            Log.d(DEBUG_TAG, "onFling: " + event1.getPressure(0)+ " " + event2.getPressure());
            Log.d(DEBUG_TAG, "onFling size: " + event1.getSize(0)+ " " + event2.getSize());*/
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        //Log.d(DEBUG_TAG, "onScroll: " + e1.toString() + e2.toString());
        mVelocityTracker.addMovement(e2);
        // When you want to determine the velocity, call
        // computeCurrentVelocity(). Then call getXVelocity()
        // and getYVelocity() to retrieve the velocity for each pointer ID.
        mVelocityTracker.computeCurrentVelocity(1000);

        //int actionIndex = e2.getActionIndex();
        final int historySize = e2.getHistorySize();
        for (int index = 0; index < e2.getPointerCount(); index++) {
            int id = e2.getPointerId(index);
            for (int h = 0; h < (historySize-1); h++) {
                String[] data = new String[12];
                // data[0] is reserved for later use
                data[1] = Long.toString(e2.getHistoricalEventTime(h));
                data[2] = "1";  //1 denotes action type "ACTION_MOVE"
                data[3] = Float.toString(getResources().getConfiguration().orientation);;
                data[4] = Float.toString(e2.getHistoricalOrientation(index, h));
                data[5] = Float.toString(e2.getHistoricalX(index, h));
                data[6] = Float.toString(e2.getHistoricalY(index, h));
                data[7] = Float.toString(e2.getHistoricalPressure(index, h));
                data[8] = Float.toString(e2.getHistoricalSize(index, h));
                data[11] = Long.toString(id);
                mTouchData.add(data);
                //Log.d(DEBUG_TAG, "id:" + id + " " + "onScroll History X: " + e2.getHistoricalX(index, h));
                //Log.d(DEBUG_TAG, "id:" + id + " " + "onScroll History Y: " + e2.getHistoricalY(index, h));
            }
            String[] data = new String[12];
            // data[0] is reserved for later use
            data[1] = Long.toString(e2.getEventTime());
            data[2] = "1";  //1 denotes action type "ACTION_MOVE"
            data[3] = Float.toString(getResources().getConfiguration().orientation);;
            data[4] = Float.toString(e2.getOrientation(index));
            data[5] = Float.toString(e2.getX(index));
            data[6] = Float.toString(e2.getY(index));
            data[7] = Float.toString(e2.getPressure(index));
            data[8] = Float.toString(e2.getSize(index));
            data[9] = Float.toString(mVelocityTracker.getXVelocity(e2.getPointerId(index)));
            data[10] = Float.toString(mVelocityTracker.getYVelocity(e2.getPointerId(index)));
            data[11] = Long.toString(id);
            mTouchData.add(data);
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
            /*Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
            Log.d(DEBUG_TAG, "onShowPress: " + event.getPressure(0));
            Log.d(DEBUG_TAG, "onShowPress size: " + event.getSize(0));*/
    }

    @Override
    public void onLongPress(MotionEvent event) {
            /*Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
            Log.d(DEBUG_TAG, "onLongPress: " + event.getPressure(0));
            Log.d(DEBUG_TAG, "onLongPress size: " + event.getSize(0));*/
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
/*        Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        Log.d(DEBUG_TAG, "onSingleTapUp pressure:" + event.getPressure(0));
        Log.d(DEBUG_TAG, "onSingleTapUp size: " + event.getSize(0));*/
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        String[] data = new String[3];
        data[2] = "4";  //4 denotes action type "DOUBLE_TAP", Tell when double tap occurs
        mTouchData.add(data);
/*        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        Log.d(DEBUG_TAG, "onDoubleTap pressure:" + event.getPressure(0));
        Log.d(DEBUG_TAG, "onDoubleTap size: " + event.getSize(0));*/
        return true;
    }


    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
/*        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        Log.d(DEBUG_TAG, "onDoubleTapEvent pressure:" + event.getPressure(0));
        Log.d(DEBUG_TAG, "onDoubleTapEvent size: " + event.getSize(0));*/
        return true;
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        String[] data = new String[3];
        data[2] = "3";  //3 denotes action type "SINGLE_TAP", Tell when single tap occurs
        mTouchData.add(data);
/*        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        Log.d(DEBUG_TAG, "onSingleTapConfirmed pressure:" + event.getPressure(0));
        Log.d(DEBUG_TAG, "onSingleTapConfirmed size: " + event.getSize(0));
        Log.d(DEBUG_TAG, "onSingleTapConfirmed toolmajor: " + event.getToolMajor(0));
        Log.d(DEBUG_TAG, "onSingleTapConfirmed touchmajor: " + event.getTouchMajor(0));*/
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Saves the full screen setting in the SharedPreferences
     *
     * @param isFullScreen
     */

    private void saveFullScreen(boolean isFullScreen) {
        // save in preferences
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(IS_FULLSCREEN_PREF, isFullScreen);
        editor.apply();
    }

    private boolean isFullScreen() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(IS_FULLSCREEN_PREF,
                DEF_FULLSCREEN);
    }

    /**
     * Toggles the activitys fullscreen mode by setting the corresponding window flag
     *
     * @param isFullScreen
     */
    private void applyFullScreen(boolean isFullScreen) {
        if (isFullScreen) {
            getWindow().clearFlags(LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Prevents app from closing on pressing back button accidentally.
     * M_BACK_PRESS_THRESHOLD specifies the maximum delay (ms) between two consecutive backpress to
     * quit the app.
     */

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - mLastBackPress) > M_BACK_PRESS_THRESHOLD) {
            pressBackToast.show();
            mLastBackPress = currentTime;
        } else {
            pressBackToast.cancel();
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onPause() {

        stopService(new Intent(getApplicationContext(), SensorService.class));
        super.onPause();
    }

    @Override
    public void onResume() {
        //SensorService sensorService = new SensorService();
        //sensorService.registorChosenSensors(sensorService.getmSensorManager(), sensorService);
        startService(new Intent(getApplicationContext(), SensorService.class));
        super.onResume();
    }
}
