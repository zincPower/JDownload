package com.zinc.libdownload.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import com.zinc.libdownload.R;

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
    private int currentProgress = 0;       //当前进度（包括变动的进度）

    //进行绘制的参数（不是特殊情况不要改动）
    private int _changingDraw = 1;  //变动的进度（当次变动增加的进度）

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
        if (_changingDraw != 0) {
            //画蓝线
            canvas.drawArc(oval, START_POINT, _changingDraw, false, paint);
        }

    }

    /**
     * 动画入口
     *
     * @param currentProgress 当前进度
     */
    public void display(int currentProgress) {

        this.currentProgress = currentProgress;
        _changingDraw = sweepRound * currentProgress / total;

        postInvalidate();

    }

    public RoundChart setTotal(int total) {
        this.total = total;
        return this;
    }

    public int dip2px(Context context, float dipValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * density + 0.5f);
    }
}
