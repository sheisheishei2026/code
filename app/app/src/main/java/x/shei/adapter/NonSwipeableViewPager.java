package x.shei.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

public class NonSwipeableViewPager extends ViewPager {

    private boolean isSwipeEnabled = false;

    public NonSwipeableViewPager(Context context) {
        super(context);
    }

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSwipeEnabled(boolean enabled) {
        isSwipeEnabled = enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // 如果禁止滑动，不拦截触摸事件，让子视图处理
        if (!isSwipeEnabled) {
            return false;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 如果禁止滑动，不处理触摸事件
        if (!isSwipeEnabled) {
            return false;
        }
        return super.onTouchEvent(event);
    }
}
