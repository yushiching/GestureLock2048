package pipi.win.a2048.activity;

import android.os.Bundle;
import android.util.Log;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.rxpatternlockview.RxPatternLockView;
import com.andrognito.rxpatternlockview.events.PatternLockCompoundEvent;
import com.uberspot.a2048.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.functions.Consumer;
import pipi.win.a2048.activity.base.BaseActivity;
import static com.andrognito.rxpatternlockview.events.PatternLockCompoundEvent.EventType;

public class LockScreenActivity extends BaseActivity {


    @BindView(R.id.pattern_lock_view)
    PatternLockView mPatternLockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);
        ButterKnife.bind(this);

        RxPatternLockView.patternChanges(mPatternLockView)
                .subscribe(new PatternConsumer());

        mPatternLockView.setTactileFeedbackEnabled(false);//close vibration




    }


    protected class PatternConsumer implements Consumer<PatternLockCompoundEvent>{
        @Override
        public void accept(PatternLockCompoundEvent event) throws Exception {
            int tid= event.getEventType();
            switch (tid){
                case EventType.PATTERN_STARTED:
                    logi("Pattern drawing started");
                    break;
                case EventType.PATTERN_PROGRESS:
                    logi("Pattern progress: " +
                            PatternLockUtils.patternToString(mPatternLockView, event.getPattern()));
                    break;
                case EventType.PATTERN_COMPLETE:
                    logi("Pattern complete: " +
                            PatternLockUtils.patternToString(mPatternLockView, event.getPattern()));
                    mPatternLockView.clearPattern();

                    break;
                case EventType.PATTERN_CLEARED:
                    logi("Pattern has been cleared");
                    break;
            }
        }
    }
}
