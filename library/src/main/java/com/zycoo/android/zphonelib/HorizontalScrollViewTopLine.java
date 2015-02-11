package com.zycoo.android.zphonelib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created by tqcenglish on 15-2-9.
 * <p/>
 * http://stackoverflow.com/questions/8203606/android-how-to-draw-a-border-to-a-linearlayout
 * http://stackoverflow.com/questions/16472529/object-allocation-during-draw-layout
 */
public class HorizontalScrollViewTopLine extends HorizontalScrollView {
    private static final int DARK_MEDIUM_GREY = Color.rgb(96, 96, 96);
    private static final int LIGHT_MEDIUM_GREY = Color.rgb(192, 192, 192);
    private Paint mStrokePaint = new Paint();
    private Rect mRect = new Rect();
    private Rect outline;
    protected boolean drawTopLine = false;

    public HorizontalScrollViewTopLine(Context context) {
        super(context);
        init();
        setWillNotDraw(false);
    }

    public HorizontalScrollViewTopLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        setWillNotDraw(false);
    }

    public HorizontalScrollViewTopLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (outline == null) {
            canvas.getClipBounds(mRect);
            outline = new Rect(0, 0, mRect.right, 2);
        }
        if (drawTopLine) {
            canvas.drawRect(outline, mStrokePaint);
        }
    }

    public void init() {
        mStrokePaint.setColor(LIGHT_MEDIUM_GREY);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(1.5f);
    }
}