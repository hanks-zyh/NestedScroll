package xyz.hanks.nestedwebview.nestedscroll;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.webkit.WebView;

import xyz.hanks.nestedwebview.ScrollUtils;

/**
 * Created by hanks on 16/8/30.
 */
public class CodeWebView extends WebView implements NestedScrollingChild, ScrollListener {

    public static final String TAG = CodeWebView.class.getSimpleName();
    public static final int UP = 1;
    public static final int DOWN = -1;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private final int mTouchSlop;
    private final int mMinimumVelocity;
    private final int mMaximumVelocity;
    private int direction = DOWN; // TODO 还需要同步到父布局的方向
    private int mLastMotionY;
    private int mNestedYOffset;
    private NestedScrollingChildHelper mChildHelper;
    private VelocityTracker mVelocityTracker;
    private boolean allowFly;
    private int downY;
    private float mDownY = -1;


    public CodeWebView(Context context) {
        this(context, null);
    }

    public CodeWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CodeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (oldt < t) {
            direction = DOWN;
        } else {
            direction = UP;
        }
    }

    @Override
    public void onScroll(int l, int t, int oldl, int oldt) {
        if (oldt < t) {
            direction = DOWN;
        } else {
            direction = UP;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean canChildScrollDown = ScrollUtils.canChildScrollDown(this);
        boolean canChildScrollUp = ScrollUtils.canChildScrollUp(this);
        logi("canChildScrollUp = " + canChildScrollUp + ",canChildScrollDown = " + canChildScrollDown + ",direction = " + direction);
        int scrollY = -1;
        if (getParent() instanceof NScrollView) {
            NScrollView parent = (NScrollView) getParent();
            scrollY = parent.getScrollY();
            logi("NScrollView scrollY = " + scrollY);
        }
        if (((direction == UP && canChildScrollUp) || (direction == DOWN && canChildScrollDown)) && scrollY == 0) {
            Log.e(TAG, "webView self consumed event");
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                    mDownY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float y = event.getRawY();
                    if (y <= mDownY) {
                        direction = DOWN;
                    } else {
                        direction = UP;
                    }
                    mDownY = y;
                    downY = (int) y;
                    break;
            }
            super.onTouchEvent(event);
            return true;
        }

        boolean eventAddedToVelocityTracker = false;

        final int action = MotionEventCompat.getActionMasked(event);
        final int actionIndex = MotionEventCompat.getActionIndex(event);

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                allowFly = false;
                downY = (int) event.getRawY();
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) event.getRawY();

                if (moveY <= downY) {
                    direction = DOWN;
                } else {
                    direction = UP;
                }

                int dy = -(moveY - downY);//滚动方法的方向跟坐标是相反的，所以这里要加一个负号
                downY = moveY;
                //在consumed中就是父类滑动后剩下的距离，
                if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset)) {
                    dy -= mScrollConsumed[1];
                    scrollBy(0, dy);
                    allowFly = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.addMovement(event);
                eventAddedToVelocityTracker = true;
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int mScrollPointerId = MotionEventCompat.getPointerId(event, actionIndex);
                float vY = -VelocityTrackerCompat.getYVelocity(mVelocityTracker, mScrollPointerId);
                logi("mMinimumVelocity=" + mMinimumVelocity + ",pointId:" + mScrollPointerId + ",vy=" + vY);
                if (Math.abs(vY) > mMinimumVelocity && !dispatchNestedPreFling(0, vY)) {
                    dispatchNestedFling(0, vY, true);
                    logi("dispatchNestedFling");
                }
                resetTouch();
                break;
        }
        if (!eventAddedToVelocityTracker) {
            mVelocityTracker.addMovement(event);
        }
        return true;

    }

    private void resetTouch() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
        }
        stopNestedScroll();
    }


    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
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
