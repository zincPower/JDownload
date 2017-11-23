package com.zinc.libdownload.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import co.zinc.jdownload.R;

import static com.zinc.libdownload.config.TagConfig.ANIM_DURATION;

/**
 * @author Jiang zinc
 * @date 创建时间：2017/11/15
 * @description 跑动的数字
 */


public class RunningTextview extends android.support.v7.widget.AppCompatTextView {

    private int number = 0;             //上次数值（不包括变动中）
    private int changingNumber = 0;     //变动中的数值
    private int currentNumber = 0;      //当前数值

    private ObjectAnimator animator;

    public RunningTextview(Context context) {
        super(context);
        init();
    }

    public RunningTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.jdcolorWhite));

        animator = ObjectAnimator.ofFloat(this, "changingNum", 0f, 1f);
        animator.setDuration(ANIM_DURATION);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                number = currentNumber;
                changingNumber = 0;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }

    public void display(int currentNumber) {

        this.currentNumber = currentNumber;
        this.changingNumber = this.currentNumber - number;

        if(animator.isRunning()){
            animator.cancel();
        }

        animator.start();
    }

    private void setChangingNum(float factor) {
        calculateNum(factor);
        postInvalidate();
    }

    private void calculateNum(float factor) {
        setText("" + (int) (changingNumber * factor + number));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(animator != null){
            animator.cancel();
        }
    }

}
