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
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }


    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (!consumed) {
            return false;
        }
        scrollerCompat.fling(0, 0, 0, (int) velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        ViewCompat.postInvalidateOnAnimation(this);
        return true;
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (oldX != x || oldY != y) {
                int dy = adjustScrollY(y - oldY);
                if (dy != 0) {
                    scrollBy(x - oldX, dy);
                    onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
                } else {
                    mScroller.forceFinished(true);
                }
            }

            if (!awakenScrollBars()) {
                ViewCompat.postInvalidateOnAnimation(this);
            }

        }
        super.computeScroll();
    }

    private int adjustScrollY(int delta) {
        int dy = 0;
        int distance = Math.abs(delta);
        if (delta > 0) { // Scroll To Bottom
            View second = getChildAt(1);
            if (second != null) {
                int max = second.getTop() - getScrollY(); // 最多滚动到第二个View的顶部和Container顶部对齐
                max = Math.min(max, second.getBottom() - getScrollY() - getBottom()); // 最多滚动到第二个View的底部和Container对齐
                dy = Math.min(max, distance);
            }
        } else if (delta < 0) { // Scroll To Top
            dy = -Math.min(distance, getScrollY());
        }
        return dy;
    }
    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
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
