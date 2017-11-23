package com.zinc.libdownload.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;

import com.zinc.libdownload.R;

import static com.zinc.libdownload.config.TagConfig.ANIM_DURATION;
import static com.zinc.libdownload.config.TagConfig.TAG;


/**
 * @author Jiang zinc
 * @date 创建时间：2017/11/15
 * @description 环形进度
 */

public class RoundChart extends android.support.v7.widget.AppCompatImageView {

    private static final int LINE_WIDTH = 10;   //圆弧的宽度【单位：dp】
    private static final int START_POINT = -235;   //圆弧的宽度【单位：dp】

    private Context mContext;
    public int radius;       //半径
    public int lineWidth;    //弧宽
    private Paint paint;

    private int sweepRound = 293;   //圆弧扫过的面积的终点【圆为360】

    private int total = -1;           //进度总数（默认为0／100）
    private int progress = 0;          //当前进度（不包括变动的进度）
    private int changingProgress = 0;  //变动中的进度
    private int currentProgress = 0;       //当前进度（包括变动的进度）

    //进行绘制的参数（不是特殊情况不要改动）
    private int _beforeDraw = 1;  //不动的进度（上一次的进度）
    private int _changingDraw = 1;  //变动的进度（当次变动增加的进度）

    private ObjectAnimator animator = null; //动画

    public RoundChart(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public RoundChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    //这是一个直径82dp,线宽10dp的空心圆
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true); //去锯齿
        paint.setStrokeCap(Paint.Cap.ROUND);

        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.jdcolorWhite));

        animator = ObjectAnimator.ofFloat(this, "changeProgress", 0f, 1f);
        animator.setDuration(ANIM_DURATION);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //动画结束，需要进行一些数值赋值
                RoundChart.this.progress = currentProgress;
                _beforeDraw = sweepRound * progress / total;
                _changingDraw = 0;
                Log.i(TAG, "onAnimationEnd: progress" + RoundChart.this.progress);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        lineWidth = dip2px(mContext, LINE_WIDTH);
        radius = (getWidth() - lineWidth) / 2;

        paint.setColor(ContextCompat.getColor(getContext(), R.color.jdroundNoneColor));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);

        int padding = lineWidth / 2;
        RectF oval = new RectF(padding, padding, padding + radius * 2, padding + radius * 2);
        //画白线
        canvas.drawArc(oval, START_POINT, sweepRound, false, paint);

        paint.setColor(ContextCompat.getColor(getContext(), R.color.jdroundHaveLightColor));
        if (progress != 0 && _beforeDraw != 0) {
            //画蓝线
            canvas.drawArc(oval, START_POINT, _beforeDraw, false, paint);
        }

        Log.i(TAG, "方法【onDraw】；_beforeDraw：" + _beforeDraw);

        if (_changingDraw != 0) {
            canvas.drawArc(oval, START_POINT + _beforeDraw, _changingDraw, false, paint);
        }

    }

    /**
     * 动画入口
     *
     * @param currentProgress 当前进度
     */
    public void display(int currentProgress) {

        Log.i(TAG, "当前线程: "+Thread.currentThread());

        if (total == -1) {
            throw new RuntimeException("缺少总进度，需要先调用setTotal()进行总进度设置");
        }

        this.currentProgress = currentProgress;

        //获取当前增加的进度
        this.changingProgress = currentProgress - this.progress;

        Log.i(TAG, "方法【display】；当前进度： " + currentProgress + ";总进度:" + total + ";增加进度：" + changingProgress + ";上次进度：" + this.progress);

        if (animator.isRunning()) {
            animator.cancel();
        }

        animator.start();

    }

    public void setChangeProgress(float changingNum) {
        calculateSweepRound(changingNum);
        postInvalidate();
    }

    private void calculateSweepRound(float changingNum) {
        _beforeDraw = sweepRound * progress / total;

        if (progress == 100) {
            _changingDraw = 0;
        } else {
            _changingDraw = (int) (changingNum * sweepRound * changingProgress / total);
        }
    }

    public RoundChart setTotal(int total) {
        this.total = total;
        return this;
    }

    public int dip2px(Context context, float dipValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * density + 0.5f);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(animator != null){
            animator.cancel();
        }
    }
}
