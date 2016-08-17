package xyz.hanks.nestedwebview.goldnestscroll;

public class DirectionDetector {
    public static int getDirection(int paramInt, boolean paramBoolean) {
        int i = 0;
        if (paramInt > 0) {
            i = 1;
        }
        if (paramBoolean && paramInt < 0) {
            i = 2;
        }
        return i;
    }
    public int getDirection(int paramInt, boolean paramBoolean, ScrollStateChangedListener paramc) {
        int direction = getDirection(paramInt, paramBoolean);
        if (paramc != null)
            paramc.onChildDirectionChange(direction);
        return direction;
    }
}