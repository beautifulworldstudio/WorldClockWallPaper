package app.util.livewalllpaper;

import android.graphics.Point;
import android.content.Context;
import android.view.SurfaceHolder;

public interface MapRenderer
 {
  public void initialize(Context clock, SurfaceHolder holder,Point p);
  public void suspendTask();
  public void resumeTask();
  public void releaseSurface(SurfaceHolder holder);
 }
