package xyz.hanks.nestedwebview.goldscroll;

/**
 * Created by hanks on 16/8/16.
 */
public interface ScrollStateChangedListener {
    void onChildDirectionChange(int direction);
    void onChildPositionChange(ScrollState param);
    enum ScrollState {a, b, c, d}
}
