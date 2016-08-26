package xyz.hanks.nestedwebview.nestedscroll;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.webkit.WebView;

import xyz.hanks.nestedwebview.goldnestscroll.DirectionDetector;
import xyz.hanks.nestedwebview.goldnestscroll.GoldWebView;
import xyz.hanks.nestedwebview.goldnestscroll.ScrollStateChangedListener;

/**
 * Created by hanks on 16/8/22.
 */
public class NWebView extends WebView implements NestedScrollingChild {

    private static final int INVALID_POINTER = -1;
    private static String TAG = GoldWebView.class.getSimpleName();
    private final int[] mScrollConsumed = new int[2];
    private final int[] mScrollOffset = new int[2];
    public int direction = 0;
    public ScrollStateChangedListener.ScrollState position = ScrollStateChangedListener.ScrollState.TOP;
    int preContentHeight = 0;
    private int consumedY;
    private int contentHeight = -1;
    private float density;
    private DirectionDetector directionDetector;
    private NestedScrollingChildHelper mChildHelper;
    private OnLongClickListener longClickListenerFalse;
    private OnLongClickListener longClickListenerTrue;
    private boolean mIsBeingDragged = false;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private int mNestedYOffset;
    private int mNestedOffsetY;
    private int mLastMotionY;
    private int mActivePointerId = -1;
    private VelocityTracker mVelocityTracker;
    private OnScrollChangeListener onScrollChangeListener;
    private int originHeight;
    private float preY;
    private ScrollStateChangedListener scrollStateChangedListener;
    private int mTouchSlop;
    private int webviewHeight = -1;
    private int mInitialTouchY;
    private int mLastMotionYmLastTouchY;

    public NWebView(Context context) {
        this(context, null);
    }

    public NWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
        directionDetector = new DirectionDetector();
        density = getScale();
//        setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        this.consumedY = (y - oldy);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsetY = 0;
        }
        int y = (int) event.getY();
        event.offsetLocation(0, mNestedOffsetY);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = y;
                mNestedOffsetY = 0;
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = mLastMotionY - y;
                int oldY = getScrollY();

                if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset)) {
                    dy -= mScrollConsumed[1];
                    event.offsetLocation(0, mScrollOffset[1]);
                    mNestedOffsetY += mScrollOffset[1];
                }
                mLastMotionY = y + mScrollOffset[1];


                final int scrolledDeltaY = getScrollY() - oldY;
                final int unconsumedY = dy - scrolledDeltaY;
                if (dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, mScrollOffset)) {
                    mLastMotionY -= mScrollOffset[1];
                    event.offsetLocation(0, mScrollOffset[1]);
                    mNestedYOffset += mScrollOffset[1];
                }
                log("dy:" + unconsumedY);
                if (dy < 0) {
                    int newScrollY = Math.max(0, oldY + dy);
                    dy -= newScrollY - oldY;
                    if (dispatchNestedScroll(0, newScrollY - dy, 0, dy, mScrollOffset)) {
                        event.offsetLocation(0, mScrollOffset[1]);
                        mNestedOffsetY += mScrollOffset[1];
                        mLastMotionY -= mScrollOffset[1];
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                stopNestedScroll();
                break;

        }
        return super.onTouchEvent(event);
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

}
