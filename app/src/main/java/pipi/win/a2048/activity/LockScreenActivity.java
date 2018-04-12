package pipi.win.a2048.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.GestureDetectorCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.rxpatternlockview.RxPatternLockView;
import com.andrognito.rxpatternlockview.events.PatternLockCompoundEvent;
import com.uberspot.a2048.R;
import com.uberspot.a2048.SensorService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.functions.Consumer;
import pipi.win.a2048.activity.base.BaseActivity;

import static com.andrognito.patternlockview.PatternLockView.PatternViewMode;
import static com.andrognito.rxpatternlockview.events.PatternLockCompoundEvent.EventType;

public class LockScreenActivity extends BaseActivity {

    private static final String DUMMY_TEST_PATTERN = "0124876";

    @BindView(R.id.pattern_lock_view)
    PatternLockView mPatternLockView;
    @BindView(R.id.lock_screen_constrain_layout)
    ConstraintLayout mLockScreenConstrainLayout;
    @BindView(R.id.tv_title_screenlock)
    TextView tvTitleScreenlock;
    @BindView(R.id.pattern_lock_view_display)
    PatternLockView patternLockViewDisplay;
    @BindView(R.id.pattern_lock_inputzone_checkres)
    ImageView patternLockInputzoneCheckres;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, LockScreenActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);
        ButterKnife.bind(this);

        mPatternLockView.setTactileFeedbackEnabled(false);//close vibration
        RxPatternLockView.patternChanges(mPatternLockView)
                .subscribe(new PatternConsumer());

        //mLockScreenConstrainLayout.setOnTouchListener(new LockScreenTouchEvent());


        patternLockViewDisplay.setInputEnabled(false);
        patternLockViewDisplay.setEnabled(false);
        List<PatternLockView.Dot> dots = PatternLockUtils.stringToPattern(patternLockViewDisplay,
                DUMMY_TEST_PATTERN);
        patternLockViewDisplay.setPattern(PatternViewMode.CORRECT, dots);
        SensorService.startService(this);

    }

    @Override
    protected void onDestroy() {
        SensorService.stopService(this);
        super.onDestroy();
    }

    protected class PatternConsumer implements Consumer<PatternLockCompoundEvent> {
        @Override
        public void accept(PatternLockCompoundEvent event) throws Exception {
            int tid = event.getEventType();
            switch (tid) {
                case EventType.PATTERN_STARTED:
                    logi("Pattern drawing started");
                    patternLockInputzoneCheckres.setImageAlpha(0);

                    break;
                case EventType.PATTERN_PROGRESS:

                    break;
                case EventType.PATTERN_COMPLETE:
                    logi("Pattern complete: " +
                            PatternLockUtils.patternToString(mPatternLockView, event.getPattern()));
                    mPatternLockView.clearPattern();
                    patternLockInputzoneCheckres.setImageAlpha(255);
                    setHideAnimation(patternLockInputzoneCheckres, 1400);


                    break;
                case EventType.PATTERN_CLEARED:
                    logi("Pattern has been cleared");
                    break;
            }
        }
    }



    private AlphaAnimation mHideAnimation;
    private AlphaAnimation mShowAnimation;

    /**
     * View渐隐动画效果
     */
    private void setHideAnimation(View view, int duration) {
        if (null == view || duration < 0) {
            return;
        }
        if (null != mHideAnimation) {
            mHideAnimation.cancel();
        }
        mHideAnimation = new AlphaAnimation(1.0f, 0.0f);
        mHideAnimation.setDuration(duration);
        mHideAnimation.setFillAfter(true);
        view.startAnimation(mHideAnimation);
    }

    /**
     * View渐现动画效果
     */
    private void setShowAnimation(View view, int duration) {
        if (null == view || duration < 0) {
            return;
        }
        if (null != mShowAnimation) {
            mShowAnimation.cancel();
        }
        mShowAnimation = new AlphaAnimation(0.0f, 1.0f);
        mShowAnimation.setDuration(duration);
        mShowAnimation.setFillAfter(true);
        view.startAnimation(mShowAnimation);
    }

    protected class LockScreenTouchEvent implements View.OnTouchListener {

        private long mLastTouch = 0;
        private long mTouchThreshold = 2000;
        private GestureDetectorCompat mDetector;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Implement a long touch action by comparing
            // time between action up and action down
            long currentTime = System.currentTimeMillis();
            if ((event.getAction() == MotionEvent.ACTION_UP)
                    && (Math.abs(currentTime - mLastTouch) > mTouchThreshold)) {

            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mLastTouch = currentTime;
            }

            /* Added by Xiaopeng. Detect touch gestures using onTouchEvent and collect touch data. */
            if (mDetector != null) {
                mDetector.onTouchEvent(event);
            }

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
                    data[3] = Float.toString(getResources().getConfiguration().orientation);
                    ;
                    data[4] = Float.toString(event.getOrientation(0));
                    data[5] = Float.toString(event.getX(0));
                    data[6] = Float.toString(event.getY(0));
                    data[7] = Float.toString(event.getPressure(0));
                    data[8] = Float.toString(event.getSize(0));
                    data[11] = Long.toString(id);
                    data[12] = Long.toString(currentTime);
                    break;

                case MotionEvent.ACTION_CANCEL:

                    break;
            }
            /* End. Detect touch gestures using onTouchEvent and collect touch data. */

            // return so that the event isn't consumed but used
            // by the webview as well
            return false;
        }
    }


}
