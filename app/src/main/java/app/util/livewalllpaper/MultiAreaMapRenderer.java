package app.util.livewalllpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.SurfaceHolder;

public class MultiAreaMapRenderer extends BroadcastReceiver implements Runnable, MapRenderer
 {
  private static final long interval = 300000; //更新間隔

  private long lastUpdated;
  //フラグ
  private boolean clockticked;
  private boolean update;

  @Override
  public synchronized void initialize(Context c, SurfaceHolder holder, Point point){}
  @Override
  public void suspendTask(){}
  @Override
  public void resumeTask(){}
  @Override
  public void releaseSurface(SurfaceHolder holder){}

  @Override
  public void run()
   {
   }

  @Override
  public synchronized void onReceive(Context contex, Intent i)
   {
    clockticked = true;
    if(System.currentTimeMillis() - lastUpdated >= interval)
     {
      update = true;
     }
   }
 }
