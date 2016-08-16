package xyz.hanks.nestedwebview.hscroll;

import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.OverScroller;

/**
 * Created by hanks on 16/8/15.
 */
public class NestedLinearLayout extends ViewGroup implements NestedScrollingParent {
    private static final int INVALID_POINTER = -1;
    private static final String TAG = "xxxxxxx";
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final OverScroller mScroller;
    private final int mTouchSlop;
    private final int mMaximumVelocity;
    private final int mMinimumVelocity;
    private VelocityTracker mVelocityTracker;
    private int mNestedYOffset;
    private boolean mIsBeingDragged;
    private int mLastMotionY;
    private int mActivePointerId;
    private int[] mScrollOffset;

    ScrollerCompat scrollerCompat;
    private int currentSwapLine;

    public NestedLinearLayout(Context context) {
        this(context, null);
    }

    public NestedLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 通过 NestedScrolling 机制来处理嵌套滚动
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mScroller = new OverScroller(context);
        scrollerCompat = ScrollerCompat.create(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();
        final int parentLeft = getPaddingLeft();
        final int parentTop = getPaddingTop();

        int lastBottom = parentTop;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft = parentLeft + lp.leftMargin;
                int childTop = lastBottom + lp.topMargin;
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
                lastBottom = childTop + height + lp.bottomMargin;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        boolean canScrollUp = (dy < 0 && canChildScrollUp(target));
        boolean canScrollDown = dy > 0 && ViewCompat.canScrollVertically(target, 1);
        Log.e("xxxxxx", canScrollUp + ":::::" + ViewCompat.canScrollVertically(target, 1) + ",getscrollY:" + getScrollY());

        boolean consume = !canScrollDown && !canScrollUp;
        if (target instanceof RecyclerView) {
            if (dy > 0) {
                consume = getScrollY() > 0 && getScrollY() < getChildAt(0).getMeasuredHeight() + getChildAt(1).getMeasuredHeight();
            } else {
                consume = !canChildScrollUp(target);
            }
        }

        if (dy < 0 && getScrollY() < 0) {
            consume = false;
        }
        // webView 还没到顶部继续上拉
        if (target instanceof WebView && getScrollY() >= 0) {
            consume = true;
        }

        if (consume) {
            consumed[1] = dy;
            scrollBy(0, dy);
        } else {
            // 不消费
            consumed[1] = 0;
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        this.scrollBy(dxUnconsumed, dyUnconsumed);
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.i(TAG, "onNestedFling : velocityY = " + velocityY + " " + consumed);
        if (!consumed) {
            fling((int) velocityY);
            return true;
        }
        return false;
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
//        Log.i(TAG, "onNestedPreFling :  direction = " + this.direction + " currentSwapLine = " + this.currentSwapLine + " velocityY = " + velocityY + " scrollY = " + getScrollY());

        fling((int) velocityY);
        return true;
    }

    /**
     * Fling the scroll view
     *
     * @param velocityY The initial velocity in the Y direction. Positive
     *                  numbers mean that the finger/cursor is moving down the screen,
     *                  which means we want to scroll towards the top.
     */
    public void fling(int velocityY) {
        if (getChildCount() > 0) {
            int height = getHeight();
            int bottom = getChildAt(0).getHeight();
            mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, 0,
                    Math.max(0, bottom - height), 0, height/2);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {


            if (!awakenScrollBars()) {
                // Keep on drawing until the animation has finished.
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }

    @Override
    public void onStopNestedScroll(View child) {
        mNestedScrollingParentHelper.onStopNestedScroll(child);
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is getDirection custom view.
     */
    public boolean canChildScrollUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            // 判断 AbsListView 的子类 ListView 或者 GridView 等
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(view, -1) || view.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(view, -1);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }
}
