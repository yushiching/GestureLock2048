
package com.uberspot.a2048;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.opencsv.CSVWriter;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import pipi.win.a2048.activity.LockScreenActivity;


public class PinEntryActivity extends Activity
        implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
    /* Added by Xiaopeng. Declare some variables for listening touch events. */
    private GestureDetectorCompat mDetector;
    private VelocityTracker mVelocityTracker = null;

    private ArrayList<String[]> mTouchData = new ArrayList<String[]>();
    /* End. Declare some variables. */

    private static final String[] PINS = {"1379", "2548", "156887", "690542"};
    int PINSIndex = 0;
    String userEntered;
    String passwordAsked;
    ArrayList<Integer> randomIndexList = getRandomNum(PINS.length);

    int countCorrect = 0;
    int countAttempts = 0;
    static final int MAXCORRECTTIMES = 8;
    static final int MAXATTEMPTS = 10;

    Context appContext;
    private Button pressedButton = null;

    TextView passwordView;
    TextView statusView;
    Button button0;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button button5;
    Button button6;
    Button button7;
    Button button8;
    Button button9;
    EditText passwordInput;
    ImageButton backSpace;

    MediaPlayer mp = null;

    public static void startActivity(Context context){
        context.startActivity(new Intent(context, PinEntryActivity.class));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_pin);

        // Added by Xiaopeng. Start service to listen sensors e.g., accl, gyro and light
        startService(new Intent(getApplicationContext(), SensorService.class));
        // End. Start service to listen sensors e.g., accl, gyro and light

        passwordAsked = PINS[randomIndexList.get(PINSIndex)];

        passwordView = (TextView) findViewById(R.id.password);
        passwordView.setText(passwordAsked);

        statusView = (TextView) findViewById(R.id.statusview);
        passwordInput = (EditText) findViewById(R.id.editText);

        iniDisplay();

        mDetector = new GestureDetectorCompat(this, this);

        View.OnTouchListener backHandler = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                long currentTime = System.currentTimeMillis();

                //backButton = (ImageButton) v;
                mDetector.onTouchEvent(event);

                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_UP:
                        CSVWriter writer = null;
                        // Get the id of the pointer associated with the action.
                        int id = event.getPointerId(event.getActionIndex());
                        String[] data = new String[13];
                        data[0] = "backSpace";  //denotes the button that the user just pressed
                        data[1] = Long.toString(event.getEventTime());
                        data[2] = "2";  //2 denotes action type "ACTION_UP"
                        data[11] = Long.toString(id);
                        data[12] = Long.toString(currentTime);
                        mTouchData.add(data);
                        writeToFile(writer, LoginActivity.mTouchFilePath, mTouchData);
                        mTouchData.clear();
                        mVelocityTracker.clear();

                        // Update what the user entered --- delete one digit each time util empty
                        String password = passwordInput.getText().toString();
                        if (userEntered.length() > 1) {
                            userEntered = userEntered.substring(0, userEntered.length() - 1);
                            password = password.substring(0, password.length()-1);
                            passwordInput.setText(password);
                        } else {
                            iniDisplay();
                        }

                        break;
                    case MotionEvent.ACTION_CANCEL:
                        mTouchData.clear();
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                        break;
                    default:
                        break;
                }
                return false;
            }
        };
        //Implement the function of "backSpace" button
        backSpace = (ImageButton) findViewById(R.id.imageView);
        backSpace.setOnTouchListener(backHandler);


        View.OnTouchListener pinButtonHandler = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                long currentTime = System.currentTimeMillis();

                pressedButton = (Button) v;
                mDetector.onTouchEvent(event);

                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_UP:
                        CSVWriter writer = null;
                        // Get the id of the pointer associated with the action.
                        int id = event.getPointerId(event.getActionIndex());
                        String[] data = new String[13];
                        data[1] = Long.toString(event.getEventTime());
                        data[2] = "2";  //2 denotes action type "ACTION_UP"
                        data[11] = Long.toString(id);
                        data[12] = Long.toString(currentTime);

                        //Track the number of entered digits for the password
                        if (userEntered.length() < passwordAsked.length()) {
                            userEntered = userEntered + pressedButton.getText();
                            data[0] = userEntered;  //denotes the button that the user just pressed
                            mTouchData.add(data);
                            writeToFile(writer, LoginActivity.mTouchFilePath, mTouchData);
                            mTouchData.clear();
                            mVelocityTracker.clear();

                            //Update pin boxes
                            passwordInput.setText(passwordInput.getText().toString() + "*");
                            //passwordInput.setSelection(passwordInput.getText().toString().length());
                            passwordInput.setTextSize(25);

                            if (userEntered.length() == passwordAsked.length()) {
                                ++countAttempts;
                                //Check the attempt times
                                if (countAttempts >= MAXATTEMPTS) {
                                    moveToNextActivity();
                                }
                                //Check if entered PIN is correct
                                if (userEntered.equals(passwordAsked)) {
                                    //Tell the user it's correct and give her a smile face
                                    statusView.setText("");
                                    Log.v("PinView", "Correct PIN");
                                    //ImageView correctFaceView = (ImageView) findViewById(R.id.face);
                                    //correctFaceView.setImageResource(R.drawable.smile0);

                                    //Reward the user a star
                                    ++countCorrect;
                                    updateStar(countCorrect);

                                    if (countCorrect >= MAXCORRECTTIMES) {
                                        moveToNextActivity();
                                    } else {
                                        ++PINSIndex;
                                        if (PINSIndex >= PINS.length) {
                                            PINSIndex = 0;
                                        }
                                        //Show next password to the user and ask for entering
                                        passwordAsked = PINS[randomIndexList.get(PINSIndex)];
                                        passwordView.setText(passwordAsked);
                                        new LockKeyPadOperation().execute("");
                                    }
                                } else {  //Provide wrong password
                                    statusView.setText("");
                                    Log.v("PinView", "Wrong PIN");
                                    mp = MediaPlayer.create(appContext, R.raw.incorrect);
                                    mp.start();
                                    ImageView correctFaceView = (ImageView) findViewById(R.id.face);
                                    correctFaceView.setImageResource(R.drawable.oops);

                                    new LockKeyPadOperation().execute("");  //Not actually lock the key pad, since we just need to collect touch data
                                }
                            }
                        } else {
                            //Roll over
                            iniDisplay();
                        }
                        pressedButton = null;

                        break;
                    case MotionEvent.ACTION_CANCEL:
                        mTouchData.clear();
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                        break;
                    default:
                        break;
                }
                return false;
            }
        };

        button0 = (Button) findViewById(R.id.button0);
        //button0.setTypeface(xpressive);
        button0.setOnTouchListener(pinButtonHandler);

        button1 = (Button) findViewById(R.id.button1);
        //button1.setTypeface(xpressive);
        button1.setOnTouchListener(pinButtonHandler);

        button2 = (Button) findViewById(R.id.button2);
        //button2.setTypeface(xpressive);
        button2.setOnTouchListener(pinButtonHandler);

        button3 = (Button) findViewById(R.id.button3);
        //button3.setTypeface(xpressive);
        button3.setOnTouchListener(pinButtonHandler);

        button4 = (Button) findViewById(R.id.button4);
        //button4.setTypeface(xpressive);
        button4.setOnTouchListener(pinButtonHandler);

        button5 = (Button) findViewById(R.id.button5);
        //button5.setTypeface(xpressive);
        button5.setOnTouchListener(pinButtonHandler);

        button6 = (Button) findViewById(R.id.button6);
        //button6.setTypeface(xpressive);
        button6.setOnTouchListener(pinButtonHandler);

        button7 = (Button) findViewById(R.id.button7);
        //button7.setTypeface(xpressive);
        button7.setOnTouchListener(pinButtonHandler);

        button8 = (Button) findViewById(R.id.button8);
        //button8.setTypeface(xpressive);
        button8.setOnTouchListener(pinButtonHandler);

        button9 = (Button) findViewById(R.id.button9);
        //button9.setTypeface(xpressive);
        button9.setOnTouchListener(pinButtonHandler);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub

        //App not allowed to go back to Parent activity until correct pin entered.
        return;
        //super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_pin_entry_view, menu);
        return true;
    }


    private class LockKeyPadOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            for (int i = 0; i < 2; i++) {
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            //Roll over
            iniDisplay();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    /* Initialization the statusView and set the tracker of user input to null */
    public void iniDisplay() {
        passwordInput.setText("");
        userEntered = "";
        ImageView correctFaceView = (ImageView) findViewById(R.id.face);
        correctFaceView.setImageDrawable(null);

        statusView.setTextColor(Color.WHITE);
        statusView.setText("Enter PIN");
    }

    public void moveToNextActivity() {
        //Clear variables and release the sound player 'mp'
        countCorrect = 0;
        PINSIndex = 0;
        mp.release();
        mVelocityTracker.recycle();

        // Finished the PIN activity. Move to next activity (a game intent)
        LockScreenActivity.startActivity(this);
        finish();
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(getApplicationContext(), SensorService.class));
        super.onDestroy();
    }

    @Override
    public void onPause() {
        SensorService sensorService = new SensorService();
        sensorService.mSensorManager.unregisterListener(sensorService);
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

    /* Update the rewarded stars */
    public void updateStar(int starID) {
        mp = MediaPlayer.create(appContext, R.raw.ding_sound);
        mp.start();

        String star = "star" + (starID-1);
        int refID = getResources().getIdentifier(star, "id", getPackageName());
        ImageView addStar = (ImageView) findViewById(refID);
        addStar.setImageResource(R.drawable.star);
    }

    public static ArrayList<Integer> getRandomNum(int length) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < length; i++) {
            list.add(new Integer(i));
        }
        Collections.shuffle(list);
        return list;
    }

    @Override
    /* The function captures touch events occurred outside of numPad. It will not be recalled
     * if the touch events occurred on number buttons.
     */
    public boolean onTouchEvent(MotionEvent event) {
        long currentTime = System.currentTimeMillis();
        this.mDetector.onTouchEvent(event);

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_UP:
                CSVWriter writer = null;
                // Get the index of the pointer associated with the action.
                int eventIndex = event.getActionIndex();
                int id = event.getPointerId(eventIndex);
                String[] data = new String[13];
                data[1] = Long.toString(event.getEventTime());
                data[2] = "2";  //2 denotes action type "ACTION_UP"
                data[11] = Long.toString(id);
                data[12] = Long.toString(currentTime);
                mTouchData.add(data);
                writeToFile(writer, LoginActivity.mTouchFilePath, mTouchData);
                mTouchData.clear();
                mVelocityTracker.clear();
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchData.clear();
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                break;
            default:
                break;
        }
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        long currentTime = System.currentTimeMillis();

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
            for (int h = 0; h < historySize; h++) {
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
/*        String[] data = new String[3];
        data[2] = "4";  //4 denotes action type "DOUBLE_TAP", Tell when double tap occurs*/
        return true;
    }


    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
/*        String[] data = new String[3];
        data[1] = Long.toString(event.getEventTime());
        data[2] = "3";  //3 denotes action type "SINGLE_TAP", Tell when single tap occurs
        mTouchData.add(data);*/
        Log.v("message: ", "single tap confirmed");
        return true;
    }

    public static void writeToFile(CSVWriter writer, String path, ArrayList<String[]> data) {
        try {
            writer = new CSVWriter(new FileWriter(path, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.writeAll(data);
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
