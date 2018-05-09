package app.util.livewalllpaper;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.service.wallpaper.WallpaperService.Engine;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.content.res.Resources;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.view.Surface;
import android.content.IntentFilter;
import android.view.WindowManager;

public class WorldClock extends WallpaperService
 {
  private int counter;   //
  public static String msg ="message";

  @Override
  public void onCreate()
   {
	//Log.d("WorldClock", "Service onCreate ");
   }

  @Override
  public Engine onCreateEngine()
   {
    return new WorldClockEngine();
   }

  @Override
  public void onDestroy() { }
  
  class WorldClockEngine extends Engine
   {
    private MapRenderer renderer; //
    private Thread renderthread;

    @Override
    public void onCreate(SurfaceHolder surfaceHolder)
     {
      WindowManager winmanager = (WindowManager) getSystemService(WINDOW_SERVICE);
      Display disp = winmanager.getDefaultDisplay();
      Point realsize = new Point();
      disp.getRealSize(realsize);
      renderer = new MapRenderer();

      renderer.initialize(WorldClock.this, surfaceHolder, realsize.x, realsize.y);

      registerReceiver(renderer, new IntentFilter("android.intent.action.TIME_TICK"));
      renderthread = new Thread(renderer);
      renderthread.start();
     }

    @Override
    public void onDestroy()
     {
      try
       {
        unregisterReceiver(renderer);
       }
      catch (Exception e){}
     }

    @Override
    public void onSurfaceCreated(SurfaceHolder holder){ }

    @Override
    public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void onSurfaceDestroyed(SurfaceHolder holder)
     {
      renderer.releaseSurface(holder);
     }

    @Override
    public void onDesiredSizeChanged(int desiredWidth, int desiredHeight){ }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep, int xPixels, int yPixels) {}

    @Override
    public void onVisibilityChanged(boolean visible)
     {
      if (!visible)
       {
        if (renderthread != null) renderer.suspendTask();
       }
      else
       {
        if (renderthread != null) renderer.resumeTask();
       }
     }

    @Override
    public void onTouchEvent(MotionEvent event) {}
    }
   //the end of WorldClockEngine
  }