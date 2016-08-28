package xyz.hanks.nestedwebview.scroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

public class MyViewScroller extends View {

    private Paint paint;
    private VelocityTracker velocityTracker;
    private Scroller scroller;
    private float lastX;
    private float currentX = 200;

    public MyViewScroller(Context context) {
        super(context);
        scroller = new Scroller(context);
        // TODO Auto-generated constructor stub
    }

    public MyViewScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        scroller = new Scroller(context);
    }

    public MyViewScroller(Context context, AttributeSet attrs,
                          int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scroller = new Scroller(context);
        // TODO Auto-generated constructor stub
    }

// public HelloScrollerView(Context context, AttributeSet attrs,
//         int defStyleAttr, int defStyleRes) {
//     super(context, attrs, defStyleAttr, defStyleRes);
//     // TODO Auto-generated constructor stub
// }

    @Override
    public void onDraw(Canvas canvas) {
        if (paint == null) {
            paint = new Paint();
            paint.setTextSize(20);
        }
        canvas.drawText("测试", currentX, 50, paint);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("KeyEventView", " " + keyCode);
//     textView.setText(KeyEvent.keyCodeToString(keyCode) + "");
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.e("KeyEventView", " " + keyCode);
//     textView.setText(KeyEvent.keyCodeToString(keyCode) + "");
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                lastX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                currentX = (currentX + (ev.getX() - lastX));
                lastX = ev.getX();
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                scroller.fling((int) currentX, 0, (int) velocityTracker.getXVelocity(), 0, 0, 99999999, 0, 0);
                velocityTracker.recycle();
                velocityTracker = null;
                if (!scroller.computeScrollOffset()) {

                }
                break;
        }
        invalidate();
        return true;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            currentX = scroller.getCurrX();
            invalidate();
        } else {
        }
    }
}