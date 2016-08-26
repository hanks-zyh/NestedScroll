package xyz.hanks.nestedwebview.nestedscroll;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

import xyz.hanks.nestedwebview.ScrollUtils;

/**
 * NestedScrollView
 * Created by hanks on 16/8/22.
 */
public class NScrollView extends ScrollView implements NestedScrollingParent {

    private static final String TAG = "NScrollView";
    private NestedScrollingParentHelper mParentHelper;
    private int[] childrenHeight = new int[3];
    private List<View> nestedScrollingChildList = new ArrayList<>();

    public NScrollView(Context context) {
        this(context, null);
    }

    public NScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public NScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        this.mParentHelper = new NestedScrollingParentHelper(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View view = getChildAt(0);
        if (view == null || !(view instanceof ViewGroup)) {
            throw new IllegalArgumentException("must have one child ViewGroup");
        }
        ViewGroup viewGroup = (ViewGroup) view;
        nestedScrollingChildList.clear();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof NestedScrollingChild) {
                nestedScrollingChildList.add(child);
                int measuredHeight = child.getMeasuredHeight();
                ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
                layoutParams.height = measuredHeight;
                child.setLayoutParams(layoutParams);
            }
        }
    }

    private void consumeEvent(int dx, int dy, int[] consumed) {
        scrollBy(dx, dy);
        consumed[0] = 0;
        consumed[1] = dy;
        log("parent consumed pre : " + consumed[1]);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        int scrollY = getScrollY();
        boolean targetScrollDown = ScrollUtils.canChildScrollDown(target);
        boolean targetScrollUp = ScrollUtils.canChildScrollUp(target);
        log("onNestedPreScroll: target = [" + target.getClass().getName() + "], dx = [" + dx + "], dy = [" + dy + "], consumed = [" + consumed + "]");
        log("scrollY:" + scrollY + ",lien:" + (childrenHeight[0] + childrenHeight[1]) + ",scrollDown:" + targetScrollDown + "scrollUp:" + targetScrollUp);

        if (dy > 0) {
            if (target instanceof WebView && (scrollY < childrenHeight[0] + childrenHeight[1] && !targetScrollDown)) {
                consumeEvent(0, dy, consumed);
                return;
            }

            if (target instanceof RecyclerView && scrollY < childrenHeight[0] + childrenHeight[1]) {
                consumeEvent(0, dy, consumed);
                return;
            }
        } else {
            if (target instanceof WebView && scrollY > 0) {
                consumeEvent(0, dy, consumed);
                return;
            }

            if (target instanceof RecyclerView && scrollY < childrenHeight[0] + childrenHeight[1]) {
                consumeEvent(0, dy, consumed);
                return;
            }
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        scrollBy(dxUnconsumed, dyUnconsumed);
        log("onNestedScroll: target = [" + target.getClass().getName() + "], dxConsumed = [" + dxConsumed + "], dyConsumed = [" + dyConsumed + "], dxUnconsumed = [" + dxUnconsumed + "], dyUnconsumed = [" + dyUnconsumed + "]");
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        log("onNestedPreFling: target = [" + target.getClass().getName() + "], velocityX = [" + velocityX + "], velocityY = [" + velocityY + "]");
        int scrollY = getScrollY();
        if (scrollY < childrenHeight[0] + childrenHeight[1]) {
            fling((int) velocityY);
            return true;
        }
        return false;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        log("onNestedFling: target = [" + target.getClass().getName() + "], velocityX = [" + velocityX + "], velocityY = [" + velocityY + "], consumed = [" + consumed + "]");
        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    @Override
    public void onStopNestedScroll(View target) {
        log("======== onStopNestedScroll =======");
        mParentHelper.onStopNestedScroll(target);
    }

    public void log(String s) {
        Log.e(TAG, s);
    }

}
