package xyz.hanks.nestedwebview.jianshuScroll;

public class DirectionDetector {
    public static int a(int paramInt, boolean paramBoolean) {
        int i=0;
        if (paramInt > 0) {
            i = 1;
        }
        if (paramBoolean && paramInt < 0) {
            i = 2;
        }
        return i;
    }


    public int a(int paramInt, boolean paramBoolean, ScrollStateChangedListener paramc) {
        int direction = a(paramInt, paramBoolean);
        if (paramc != null)
            paramc.onChildDirectionChange(direction);
        return direction;
    }
}

/* Location:           /Users/likang/Desktop/classes_dex2jar.jar
 * Qualified Name:     com.baiji.jianshu.b.j
 * JD-Core Version:    0.6.2
 */