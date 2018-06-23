package app.util.livewalllpaper;

import android.graphics.Point;
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.content.IntentFilter;
import android.view.WindowManager;

public class WorldClock extends WallpaperService
 {
  private int counter;   //
  public static String msg ="message";
  private static WorldClockEngine engine;
  //モードの文字列定数
  public static final String MODE_SINGLE = "Singlemap";
  public static final String MODE_MULTIPLE = "Multimap";

  @Override
  public void onCreate()
   {
	//Log.d("WorldClock", "Service onCreate ");
   }

  @Override
  public Engine onCreateEngine()
   {
    engine = new WorldClockEngine();
    return engine;
   }

  @Override
  public void onDestroy() { }

/*
  public static int getDisplayMode()
   {
    return engine.getMapRenderer().getMode();
   }

  public static void setDisplayMode(int value)
   {
    engine.getMapRenderer().setMode(value);
   }
*/
  class WorldClockEngine extends Engine
   {
    private SingleWorldMapRenderer renderer; //
    private Thread renderthread;

    public SingleWorldMapRenderer getMapRenderer(){ return renderer; }

    @Override
    public void onCreate(SurfaceHolder surfaceHolder)
     {
      WindowManager winmanager = (WindowManager) getSystemService(WINDOW_SERVICE);
      //Display disp = winmanager.getDefaultDisplay();
      Point realsize = new Point();
      winmanager.getDefaultDisplay().getRealSize(realsize);
      renderer = new SingleWorldMapRenderer();

      renderer.initialize(WorldClock.this, surfaceHolder, realsize);

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