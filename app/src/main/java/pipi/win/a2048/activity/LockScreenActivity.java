package pipi.win.a2048.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.rxpatternlockview.RxPatternLockView;
import com.andrognito.rxpatternlockview.events.PatternLockCompoundEvent;
import com.uberspot.a2048.LoginActivity;
import com.uberspot.a2048.MainActivity;
import com.uberspot.a2048.R;
import com.uberspot.a2048.SensorService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.functions.Consumer;
import pipi.win.a2048.activity.base.BaseActivity;
import pipi.win.a2048.utility.FileUtil;
import pipi.win.a2048.view.CorrectHintImageView;

import static com.andrognito.patternlockview.PatternLockView.PatternViewMode;
import static com.andrognito.rxpatternlockview.events.PatternLockCompoundEvent.EventType;

public class LockScreenActivity extends BaseActivity {

    public static final String TAG=LockScreenActivity.class.getSimpleName();
    private static final String DUMMY_TEST_PATTERN = "0124876";

    @BindView(R.id.pattern_lock_view)
    PatternLockView mPatternLockView;
    @BindView(R.id.lock_screen_constrain_layout)
    ConstraintLayout mLockScreenConstrainLayout;
    @BindView(R.id.tv_title_screenlock)
    TextView tvTitleScreenlock;
    @BindView(R.id.pattern_lock_view_display)
    PatternLockView patternLockViewIndicator;
    @BindView(R.id.pattern_lock_inputzone_checkres)
    CorrectHintImageView patternInputCheckResIndicator;
    @BindView(R.id.lock_screen_view_touch_frame)
    View lockScreenTouchFrame;


    private List<String> mPatternCheckLists = new ArrayList<>();
    private int currentPattern;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, LockScreenActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);
        ButterKnife.bind(this);
        initData();
        initUI();

    }

    @Override
    public void onPause() {
        SensorService.stopService(this);
        super.onPause();
    }

    @Override
    public void onResume() {

        SensorService.startService(this);
        super.onResume();
    }


    protected void initData() {
        String[] checkarrays = getResources().getStringArray(R.array.patterns_check_list);
        addPatternsToShow(Arrays.asList(checkarrays));
    }

    protected LockScreenTouchEvent lockScreenTouchEventListener;


    protected void initUI() {
        patternInputCheckResIndicator.setImageAlpha(0);

        mPatternLockView.setTactileFeedbackEnabled(false);//close vibration
        RxPatternLockView.patternChanges(mPatternLockView)
                .subscribe(new PatternConsumer());

        lockScreenTouchEventListener =new LockScreenTouchEvent(this);
        lockScreenTouchFrame.setOnTouchListener(lockScreenTouchEventListener);


        mPatternLockView.setExternalHookerOnTouch(lockScreenTouchEventListener);


        //https://stackoverflow.com/questions/21247229/view-ontouchlistener-does-not-work-on-parent-layout
        //move touch listener to a subframe;


        patternLockViewIndicator.setInputEnabled(false);
        patternLockViewIndicator.setEnabled(false);
        currentPattern = 0;
        String initPattern = mPatternCheckLists.size() > 0 ?
                mPatternCheckLists.get(currentPattern) : DUMMY_TEST_PATTERN;
        setPatternIndictor(initPattern);
    }


    protected void setPatternIndictor(String s) {
        List<PatternLockView.Dot> dots = PatternLockUtils.stringToPattern(patternLockViewIndicator, s);
        patternLockViewIndicator.setPattern(PatternViewMode.CORRECT, dots);
    }


    protected class PatternConsumer implements Consumer<PatternLockCompoundEvent> {
        @Override
        public void accept(PatternLockCompoundEvent event) throws Exception {
            int tid = event.getEventType();
            switch (tid) {
                case EventType.PATTERN_STARTED:
                    logi("Pattern drawing started");
                    patternInputCheckResIndicator.setImageAlpha(0);
                    break;
                case EventType.PATTERN_COMPLETE:

                    String userInputPattern = PatternLockUtils.patternToString(mPatternLockView, event.getPattern());
                    logi("Pattern complete: " + userInputPattern);
                    checkUserInputPattern(userInputPattern);

                    break;
            }
        }
    }


    protected void checkUserInputPattern(String userInput) {
        if (mPatternCheckLists.size() == 0) {
            loge(new ArrayIndexOutOfBoundsException(), "CheckList is Empty!");
            return;
        }
        final String correct = mPatternCheckLists.get(currentPattern).trim();
        String reversedUserInput = new StringBuilder(userInput).reverse().toString();
        if (correct.equalsIgnoreCase(userInput) || correct.equalsIgnoreCase(reversedUserInput)) {
            //show correct;

            int leftpattern = mPatternCheckLists.size() - currentPattern - 1;

            if (currentPattern == mPatternCheckLists.size() - 1) {
                MainActivity.startActivity(this);
                patternInputCheckResIndicator.showAllOverAni(1200);
                finish();
                return;
            } else {
                currentPattern++;
                setPatternIndictor(mPatternCheckLists.get(currentPattern));
                patternInputCheckResIndicator.showCorrectAni(1200);
                mkToast("Correct!" + leftpattern + " more");
            }

        } else {
            //show error;
            patternInputCheckResIndicator.showWrongAni(1700);
            mkToast("Error~");
        }
        mPatternLockView.clearPattern();
    }


    public void addPatternsToShow(List<String> list) {
        mPatternCheckLists.clear();
        mPatternCheckLists.addAll(list);
    }




    public class LockScreenTouchEvent implements View.OnTouchListener, GestureDetector.OnGestureListener,
            GestureDetector.OnDoubleTapListener {

        private long mLastTouch = 0;
        private long mTouchThreshold = 2000;
        private GestureDetectorCompat mDetector;
        private VelocityTracker mVelocityTracker;
        private List<String[]> mTouchData = new ArrayList<>();

        public LockScreenTouchEvent(Context context) {
            mDetector = new GestureDetectorCompat(context, LockScreenTouchEvent.this);
        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Implement a long touch action by comparing
            // time between action up and action down
            long currentTime = System.currentTimeMillis();
            int actionfull = event.getAction();
            if ((actionfull == MotionEvent.ACTION_UP)
                    && (Math.abs(currentTime - mLastTouch) > mTouchThreshold)) {
            } else if (actionfull == MotionEvent.ACTION_DOWN) {
                mLastTouch = currentTime;
            }

            /* Added by Xiaopeng. Detect touch gestures using onTouchEvent and collect touch data. */
            if (mDetector != null) {
                mDetector.onTouchEvent(event);
            }

            int action = event.getActionMasked();

            tLog("OnTouch " + actionfull);
            switch (action) {

                case MotionEvent.ACTION_UP:
                    // Get the index of the pointer associated with the action.
                    int eventIndex = event.getActionIndex();
                    int id = event.getPointerId(eventIndex);

                    String[] data = new String[13];
                    // data[0] is reserved for later use
                    data[1] = Long.toString(event.getEventTime());
                    data[2] = "2"; //2 denotes action type "ACTION_UP"
                    data[3] = Float.toString(getResources().getConfiguration().orientation);
                    data[4] = Float.toString(event.getOrientation(0));
                    data[5] = Float.toString(event.getX(0));
                    data[6] = Float.toString(event.getY(0));
                    data[7] = Float.toString(event.getPressure(0));
                    data[8] = Float.toString(event.getSize(0));
                    data[11] = Long.toString(id);
                    data[12] = Long.toString(currentTime);
                    mTouchData.add(data);

                    tLog("onTouch: WriteData");
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

            //https://stackoverflow.com/questions/15799839/motionevent-action-up-not-called
            return false;
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
            data[3] = Float.toString(getResources().getConfiguration().orientation);
            ;
            data[4] = Float.toString(event.getOrientation(0));
            data[5] = Float.toString(event.getX(0));
            data[6] = Float.toString(event.getY(0));
            data[7] = Float.toString(event.getPressure(0));
            data[8] = Float.toString(event.getSize(0));
            data[11] = Long.toString(id);
            data[12] = Long.toString(currentTime);
            mTouchData.add(data);

            tLog("onDown: " + currentTime);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
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
                for (int h = 0; h < (historySize - 1); h++) {
                    String[] data = new String[12];
                    // data[0] is reserved for later use
                    data[1] = Long.toString(e2.getHistoricalEventTime(h));
                    data[2] = "1";  //1 denotes action type "ACTION_MOVE"
                    data[3] = Float.toString(getResources().getConfiguration().orientation);
                    ;
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
                data[3] = Float.toString(getResources().getConfiguration().orientation);
                ;
                data[4] = Float.toString(e2.getOrientation(index));
                data[5] = Float.toString(e2.getX(index));
                data[6] = Float.toString(e2.getY(index));
                data[7] = Float.toString(e2.getPressure(index));
                data[8] = Float.toString(e2.getSize(index));
                data[9] = Float.toString(mVelocityTracker.getXVelocity(e2.getPointerId(index)));
                data[10] = Float.toString(mVelocityTracker.getYVelocity(e2.getPointerId(index)));
                data[11] = Long.toString(id);
                mTouchData.add(data);
                tLog("onScroll:mData added");
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent event) {
        }

        @Override
        public void onLongPress(MotionEvent event) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            String[] data = new String[3];
            data[2] = "4";  //4 denotes action type "DOUBLE_TAP", Tell when double tap occurs
            mTouchData.add(data);
            tLog("onDoubleTap");
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            String[] data = new String[3];
            data[2] = "3";  //3 denotes action type "SINGLE_TAP", Tell when single tap occurs
            mTouchData.add(data);
            tLog("onSingleTapConfirmed");
            return true;
        }

        private void tLog(String msg) {
            Log.i(this.getClass().getSimpleName(), msg);
        }
    }


}
