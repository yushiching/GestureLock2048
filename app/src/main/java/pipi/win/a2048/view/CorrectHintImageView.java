package pipi.win.a2048.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.uberspot.a2048.R;


public class CorrectHintImageView extends AppCompatImageView {
    public CorrectHintImageView(Context context) {
        super(context);
    }

    public CorrectHintImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CorrectHintImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private AlphaAnimation mHideAnimation;
    private AlphaAnimation mShowAnimation;


    public void showCorrectAni(int dur){
        this.setImageResource(R.drawable.ic_done_black_24dp);
        startHideAnimation(dur);
    }
    public void showWrongAni(int dur){
        this.setImageResource(R.drawable.ic_highlight_off_black_24dp);
        startHideAnimation(dur);
    }
    public void showAllOverAni(int dur){
        this.setImageResource(R.drawable.ic_done_all_black_24dp);
        startHideAnimation(dur);
    }

    public void startHideAnimation(int dur){
        setHideAnimation(this,dur);
    }
    /**
     * View渐隐动画效果
     */
    protected void setHideAnimation(View view, int duration) {
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
    protected void setShowAnimation(View view, int duration) {
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


}
