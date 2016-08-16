package xyz.hanks.nestedwebview.jianshuScroll;


public interface ScrollStateChangedListener {
  public void onChildDirectionChange(int position);

  public void onChildPositionChange(ScrollState parama);

  public static enum ScrollState{a,b,c,d}
}



/* Location:           /Users/likang/Desktop/classes_dex2jar.jar
 * Qualified Name:     com.baiji.jianshu.b.c
 * JD-Core Version:    0.6.2
 */