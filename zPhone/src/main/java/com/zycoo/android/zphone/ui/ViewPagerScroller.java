package com.zycoo.android.zphone.ui;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by tqcenglish on 15-1-7.
 */
public class ViewPagerScroller extends Scroller {

    private final static int sDuration = 100;

    public ViewPagerScroller(Context context) {
        super(context);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, sDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, sDuration);
    }
}
