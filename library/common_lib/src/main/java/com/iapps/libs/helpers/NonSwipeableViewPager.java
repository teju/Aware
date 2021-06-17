package com.iapps.libs.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.viewpager.widget.ViewPager;

public class NonSwipeableViewPager extends ViewPager {

    private boolean enableSwipe = false;

    public void setEnableSwipe(boolean enableSwipe) {
        this.enableSwipe = enableSwipe;
    }

    public NonSwipeableViewPager(Context context) {
        super(context);
        enableSwipe = false;
    }

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        enableSwipe = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (this.enableSwipe) {
            return super.onInterceptTouchEvent(event);
        }

        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (this.enableSwipe) {
            return super.onTouchEvent(event);
        }
        // Never allow swiping to switch between pages
        return false;
    }


}
