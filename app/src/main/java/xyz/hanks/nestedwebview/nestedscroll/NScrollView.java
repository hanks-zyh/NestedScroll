package xyz.hanks.nestedwebview.nestedscroll;

import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import xyz.hanks.nestedwebview.ScrollUtils;

/**
 * NestedScrollView
 * Created by hanks on 16/8/22.
 */
public class NScrollView extends ViewGroup implements NestedScrollingParent {

    private static final String TAG = "NScrollView";
    private ScrollListener scrollListener;
    private int maxDistance = 0;
    private int totalHeight = 0;
    private NestedScrollingParentHelper mParentHelper;
    private List<View> nestedScrollingChildList = new ArrayList<>();
    private ScrollerCompat scroller;

    public NScrollView(Context context) {
        this(context, null);
    }

    public NScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        mParentHelper = new NestedScrollingParentHelper(this);
        scroller = ScrollerCompat.create(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int measureHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams layoutParams = child.getLayoutParams();
            if (layoutParams.height == LayoutParams.WRAP_CONTENT) {
                measureChild(child, widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
            } else if (layoutParams.height == LayoutParams.MATCH_PARENT) {
                measureChild(child, widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            } else {
                measureChild(child, widthMeasureSpec, height);
            }

            int childMeasuredHeight = child.getMeasuredHeight();
            measureHeight += childMeasuredHeight;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int top = t;
        int lastChildHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childMeasuredHeight = child.getMeasuredHeight();
            child.layout(l, top, r,  top + childMeasuredHeight);
            top += childMeasuredHeight;
            lastChildHeight = childMeasuredHeight;
        }
        maxDistance = top - lastChildHeight;
        totalHeight = top;
        //viewGroup.layout(l, t, r, top);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        nestedScrollingChildList.clear();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ScrollListener) {
                scrollListener = (ScrollListener) child;
            }
            nestedScrollingChildList.add(child);
        }
    }

    private void consumeEvent(int dx, int dy, int[] consumed) {
        scrollBy(dx, dy);
        consumed[0] = 0;
        consumed[1] = dy;
        log("parent consumed pre : " + consumed[1]);
    }

    public int getCurrentWrapline(View target) {
        int line = 0;
        for (View view : nestedScrollingChildList) {
            if (view == target) {
                return line;
            }
            line += view.getHeight();
        }
        return line;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (scrollListener != null) {
            scrollListener.onScroll(l, t, oldl, oldt);
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        logi("======== onStartNestedScroll =======");

        mParentHelper.onNestedScrollAccepted(child, target, axes);
        return;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        int scrollY = getScrollY();
        int line = getCurrentWrapline(target);
        boolean targetScrollDown = ScrollUtils.canChildScrollDown(target);
        boolean targetScrollUp = ScrollUtils.canChildScrollUp(target);
        log("onNestedPreScroll: target = [" + target.getClass().getName() + "], dx = [" + dx + "], dy = [" + dy + "], consumed = [" + consumed + "]");
        log("scrollY:" + scrollY + ",lien:" + line + ",scrollDown:" + targetScrollDown + "scrollUp:" + targetScrollUp + ",maxDistance" + maxDistance);

        if (scrollY == line
                && ((dy > 0 && targetScrollDown) || (dy < 0 && targetScrollUp))) {
            return;
        }

        if (scrollY + dy < 0 || scrollY + dy > maxDistance) {
            return;
        }
        consumeEvent(0, dy, consumed);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int scrollY = getScrollY();
        if (scrollY + dyUnconsumed < 0 || scrollY + dyUnconsumed > maxDistance) {
            return;
        }
        scrollBy(dxUnconsumed, dyUnconsumed);
        log("onNestedScroll: target = [" + target.getClass().getName() + "], dxConsumed = [" + dxConsumed + "], dyConsumed = [" + dyConsumed + "], dxUnconsumed = [" + dxUnconsumed + "], dyUnconsumed = [" + dyUnconsumed + "]");
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        int scrollY = getScrollY();
        log("onNestedPreFling: target = [" + target.getClass().getName() + "], velocityX = [" + velocityX + "], velocityY = [" + velocityY + "]" + ",scrollY:" + scrollY);
        int line = getCurrentWrapline(target);
        if (scrollY != line) {
            fling((int) velocityY, scrollY);
            return true;
        }
        return false;
    }

    private void fling(int velocityY, int scrollY) {
        if (getChildCount() > 0) {
            int height = getHeight() - getPaddingTop() - getPaddingBottom();
            int bottom = totalHeight;
            scroller.fling(0, scrollY, 0, velocityY, 0, 0, 0,
                    Math.max(0, bottom - height));
            invalidate();
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            int y = scroller.getCurrY();
            scrollTo(0, y);
            invalidate();
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        log("onNestedFling: target = [" + target.getClass().getName() + "], velocityX = [" + velocityX + "], velocityY = [" + velocityY + "], consumed = [" + consumed + "]");
        return false;
    }

    @Override
    public void onStopNestedScroll(View target) {
        logi("======== onStopNestedScroll =======");
        mParentHelper.onStopNestedScroll(target);
    }

    public void log(String s) {
        Log.e(TAG, s);
    }

    public void logi(String s) {
        Log.i(TAG, s);
    }

    public void logw(String s) {
        Log.w(TAG, s);
    }

}
