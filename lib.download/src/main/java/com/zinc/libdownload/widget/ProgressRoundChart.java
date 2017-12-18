package com.zinc.libdownload.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.zinc.libdownload.R;


/**
 * Created by zinc on 16/10/9.
 */

public class ProgressRoundChart extends android.support.v7.widget.AppCompatImageView {

    private static final int LINE_WIDTH = 10;   //圆弧的宽度【单位：dp】

    private Context mContext;
    public int radius;       //半径
    public int lineWidth;    //弧宽
    private Paint paint;

    private int count = 100;        //总人数
    private int checkCount = 40;    //打卡人数
    private int sweepRound = 293;   //总圆弧扫过的面积
    private int lineSweepRound = 0;

    public ProgressRoundChart(Context context) {
        super(context);
        mContext = context;

        init();
    }

    public ProgressRoundChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        init();
    }

    //这是一个直径82dp,线宽10dp的空心圆
    private void init() {
        lineWidth = dip2px(mContext, LINE_WIDTH);
        radius = (getWidth() - lineWidth) / 2;

        //设置背景
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.jdcolorWhite));

        paint = new Paint();
        paint.setAntiAlias(true); //去锯齿
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(getResources().getColor(R.color.jdroundNoneColor));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);

        int padding = dip2px(mContext, 4);
        RectF oval = new RectF(padding, padding, padding + radius * 2, padding + radius * 2);
        canvas.drawArc(oval, -235, sweepRound, false, paint);

        paint.setColor(getResources().getColor(R.color.jdroundHaveLightColor));

        if (lineSweepRound != 0) {
            canvas.drawArc(oval, -235, lineSweepRound, false, paint);
        }

    }

    public void display(int checkCount, int count) {
        this.count = count;
        this.checkCount = checkCount;
        calculateSweepRound();
        postInvalidate();
    }

    public int dip2px(Context context, float dipValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * density + 0.5f);
    }

    private void calculateSweepRound() {
        if(count == 0){
            lineSweepRound = 0;
        }else{
            lineSweepRound = sweepRound * checkCount / count;
        }
    }

}
