package app.util.livewalllpaper;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.TimeZone;

/**
 * Created by Kentaro on 2018/02/23.
 */

public class City
 {
  private Bitmap clock;
  private int x;
  private int y;
  private TimeZone zone;

  public City(Bitmap bitmap, int positionX, int positionY, String str)
   {
    x = positionX;
    y = positionY;
    clock = bitmap;
    zone = TimeZone.getTimeZone(str);
    if(zone==null) Log.d("Worldclock", "zone=null:" + str);
   }

  public int getX(){ return x;}
  public int getY(){ return y;}
  public Bitmap getClockImage(){ return clock; }
  public TimeZone getTimeZone(){ return zone;}
 }
