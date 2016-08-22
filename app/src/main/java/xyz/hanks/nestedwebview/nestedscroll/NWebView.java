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
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
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
    private int mLastTouchY;

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
        setOverScrollMode(View.OVER_SCROLL_NEVER);

    }

    private void endTouch() {
        mIsBeingDragged = false;
        mActivePointerId = -1;
        recycleVelocityTracker();
        stopNestedScroll();
    }

    private void flingWithNestedDispatch(int velocityY) {
        if (!dispatchNestedPreFling(0.0F, velocityY)) {
            Log.i(TAG, "dispatchNestedPreFling : velocityY : " + velocityY);
            dispatchNestedFling(0, velocityY, true);
        }
    }

    private void setScrollStateChangedListener(ScrollStateChangedListener paramc) {
        scrollStateChangedListener = paramc;
    }

    @Override
    public void computeScroll() {
        if (position == ScrollStateChangedListener.ScrollState.MIDDLE) {
            super.computeScroll();
        }
    }


    @Override
    public void invalidate() {
        super.invalidate();
        contentHeight = ((int) (getContentHeight() * getScale()));
        if (contentHeight != preContentHeight) {
            loadUrl("javascript:window.InjectedObject.getContentHeight(document.getElementsByTagName('div')[0].scrollHeight)");
            preContentHeight = contentHeight;
        }
    }


    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        consumedY = (y - oldy);
        Log.i(TAG, "consumedYconsumedYconsumedY====" + consumedY);
        if (y <= 0) {
            position = ScrollStateChangedListener.ScrollState.TOP;
            return;
        }
        if (null != scrollStateChangedListener) {
            scrollStateChangedListener.onChildPositionChange(position);
        }
        if (onScrollChangeListener != null) {
            onScrollChangeListener.onScrollChanged(x, y, oldx, oldy, position);
        } else {
            if (y + webviewHeight >= contentHeight) {
                if (contentHeight > 0) {
                    position = ScrollStateChangedListener.ScrollState.BOTTOM;
                }
            } else {
                position = ScrollStateChangedListener.ScrollState.MIDDLE;
            }
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        webviewHeight = h + 1;
        if (contentHeight < 1) {
            setContentHeight(webviewHeight);
        }
    }

    private void calcPosition(){
        if (computeVerticalScrollOffset() == 0) {
            position = ScrollStateChangedListener.ScrollState.TOP;
        }else if(computeVerticalScrollOffset()+computeVerticalScrollExtent()>=computeVerticalScrollRange()){
            position = ScrollStateChangedListener.ScrollState.MIDDLE;
        }else {
            position = ScrollStateChangedListener.ScrollState.BOTTOM;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        log("position:"+position);
        if (position == ScrollStateChangedListener.ScrollState.MIDDLE) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    mIsBeingDragged = false;
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    endTouch();
                    break;
                }
            }
            super.onTouchEvent(ev);
            return true;
        }

        initVelocityTrackerIfNotExists();
        MotionEvent vtev = MotionEvent.obtain(ev);
        final int actionMasked = MotionEventCompat.getActionMasked(ev);
        final int index = MotionEventCompat.getActionIndex(ev);
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mNestedYOffset = 0;
        }
        vtev.offsetLocation(0, mNestedYOffset);
        consumedY = 0;
        direction = 0;
        boolean onTouchEvent = false;
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                // Remember where the motion event started
                onTouchEvent = super.onTouchEvent(ev);
                mLastMotionY = (int) (ev.getY() + 0.5f);
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                preY = vtev.getY();
                mIsBeingDragged = false;
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                onTouchEvent = super.onTouchEvent(ev);
                mLastMotionY = (int) (MotionEventCompat.getY(ev, index) + 0.5f);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev,
                        mActivePointerId);
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                    break;
                }
                if (!mIsBeingDragged && Math.abs(vtev.getY() - preY) > mTouchSlop) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mIsBeingDragged = true;
                }
                final int y = (int) (MotionEventCompat.getY(ev, activePointerIndex) + 0.5f);
                Log.i(TAG, "mLastMotionY=====" + mLastMotionY);
                Log.i(TAG, "YYYYYYY=====" + y);
                int deltaY = mLastMotionY - y;

                if (deltaY != 0) {
                    direction = directionDetector.getDirection(deltaY, true, scrollStateChangedListener);
                }
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1];
                    vtev.offsetLocation(0, mScrollOffset[1]);
                    mNestedYOffset += mScrollOffset[1];
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    mLastMotionY = y - mScrollOffset[1];
                    Log.i(TAG, "deltaY===" + deltaY);
                    Log.i(TAG, "consumedY===" + consumedY);
                    final int unconsumedY = deltaY - consumedY;

                    Log.i(TAG, " child consumed = " + mScrollConsumed[1] + " un_consumed = " + unconsumedY + " position = " + position + " direction = " + direction);
                    onTouchEvent = super.onTouchEvent(ev);
                    if (position == ScrollStateChangedListener.ScrollState.MIDDLE) {
                        return true;
                    }
                    switch (direction) {
                        case 1: {
                            if ((position != ScrollStateChangedListener.ScrollState.BOTTOM) && (contentHeight != webviewHeight)) {
                                scrollBy(0, unconsumedY);
                                break;
                            }
                            Log.i(TAG, "1111111consumedY===" + consumedY + "  unconsumedY==" + unconsumedY);
                            if (dispatchNestedScroll(0, consumedY, 0, unconsumedY, mScrollOffset)) {
                                vtev.offsetLocation(0.0F, mScrollOffset[1]);
                                mNestedYOffset += mScrollOffset[1];
                                mLastMotionY -= mScrollOffset[1];
                            }
                        }
                        break;
                        case 2:
                            if ((position == ScrollStateChangedListener.ScrollState.TOP) || (contentHeight == webviewHeight)) {
                                Log.i(TAG, "2222222consumedY===" + consumedY + "  unconsumedY==" + unconsumedY);
                                if (dispatchNestedScroll(0, consumedY, 0, unconsumedY, mScrollOffset)) {
                                    vtev.offsetLocation(0.0F, mScrollOffset[1]);
                                    mNestedYOffset += mScrollOffset[1];
                                    mLastMotionY -= mScrollOffset[1];
                                }
                            } else {
                                scrollBy(0, unconsumedY);
                            }
                            break;
                        default:
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                onTouchEvent = super.onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_UP:
                onTouchEvent = super.onTouchEvent(ev);
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) VelocityTrackerCompat.getYVelocity(velocityTracker, mActivePointerId);
                    if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                        flingWithNestedDispatch(-initialVelocity);
                    }
                }
                mActivePointerId = INVALID_POINTER;
                endTouch();
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onTouchEvent = super.onTouchEvent(ev);
                onSecondaryPointerUp(ev);
                mLastMotionY = (int) (MotionEventCompat.getY(ev,
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId)) + 0.5F);
                break;
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return onTouchEvent;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >>
                MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose getDirection new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY,
                                   int scrollX, int scrollY,
                                   int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY,
                                   boolean isTouchEvent) {
        if (position != ScrollStateChangedListener.ScrollState.MIDDLE) {
            deltaY = 0;
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    public void scrollToBottom() {
        scrollTo(getScrollX(), contentHeight - webviewHeight);
    }

    public void scrollToTop() {
        scrollTo(getScrollX(), 0);
    }

    public void setContentHeight(int contentHeight) {
        contentHeight = contentHeight;
        Log.i(TAG, "contentHeight = " + contentHeight + " -  webviewHeight = " + webviewHeight + " = " + (contentHeight - webviewHeight));
    }

    public void setOnLongClickListener(OnLongClickListener mOnLongClickListener) {
        longClickListenerFalse = mOnLongClickListener;
        super.setOnLongClickListener(mOnLongClickListener);
    }

    public void setOnScrollChangeListener(OnScrollChangeListener paramOnScrollChangeListener) {
        onScrollChangeListener = paramOnScrollChangeListener;
    }


    @Override
    public boolean startNestedScroll(int paramInt) {
        return mChildHelper.startNestedScroll(paramInt);
    }

    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public void setNestedScrollingEnabled(boolean paramBoolean) {
        mChildHelper.setNestedScrollingEnabled(paramBoolean);
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    protected void onDetachedFromWindow() {
        mChildHelper.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    public void log(String s) {
        Log.e(TAG, s);
    }

    public interface OnScrollChangeListener {
        void onScrollChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4, ScrollStateChangedListener.ScrollState parama);
    }
}
