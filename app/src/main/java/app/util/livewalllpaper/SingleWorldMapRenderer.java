package app.util.livewalllpaper;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.FileOutputStream;
import java.util.Calendar;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class SingleWorldMapRenderer extends BroadcastReceiver implements Runnable
 {
  public static final String GLOBEVIEW = "GLOBEVIEW";
  public static final String MAPVIEW = "MAPVIEW";

  private static final String xhdpipostfix ="_xhdpi";
  private static final String xxhdpipostfix ="_xxhdpi";
  private static final String clockpositionjson = "clockposition";
  private static final String initialset = "initialsetting";
  private static final String clockselectionfilename = "clockselection.json";
  private static final String modesettingfilename = "modeselection.json";
  private static final String timezoneidJST = "Asia/Tokyo";

  private static final long interval = 300000L;
  private static final long TURNON = 1600L;
  private static final long TURNOFF = 1000L;
  private static final int character1_xhdpi = 13;
  private static final int character2_xhdpi = 40;
  private static final int character3_xhdpi = 65;
  private static final int character4_xhdpi = 91;
  private static final int character5_xhdpi = 121;
  private static final int characterline_xhdpi = 54;

  private static final int character1_xxhdpi = 18;
  private static final int character2_xxhdpi = 60;
  private static final int character3_xxhdpi = 101;
  private static final int character4_xxhdpi = 141;
  private static final int character5_xxhdpi = 183;
  private static final int characterline_xxhdpi = 81;

  public static final int MAPMODE = 5;
  public static final int GLOBEMODE = 7;
  private static final int clockwidth_xhdpi = 160;
  private static final int clockwidth_xxhdpi = 240;

     //画面関連
  private int wholewidth; //
  private int wholeheight; //
  private int viewwidth;
  private int viewheight;
  private int Xbase; //
  private int Xoffset; //
  private int Yequator; //
  private int shiftnumber;
  private int shiftwidth;
  private int shiftindex;
  private Paint p;
  //太陽位置計算関連変数
  private double timevalue; //
  private double elon;//
  private double e;//
  private double asc ;//
  private double dec;//
  private double phai0 ;//
  private double dist; //
  private double parallax ;//
  private double k ;
  private double twilightk;
  private double time; //
  //描画関連
  private Bitmap screen;
  private Canvas screencanvas;
  private Rect mapsource, view;
  private Bitmap worldmap;//
  private Bitmap nightmap;//
  private Bitmap[] characters;
  private Calendar calendar;
  private Context context;
  private String sizeIdentifier="";
  private SurfaceHolder surface;
  private long lastUpdated;
  private long lastTicked;
  private long blinkinterval;
  private long timestamp_clockselection;
  private long timestamp_displaymode;
  private int characterline;
  private int[] characterpos;
  private int prefferedsize;
  private int paintmode;
  private int clockwidth;
  //時計選択データ
  private ArrayList<City> clockpositions;
  //フラグ
  private boolean loop;//ループ
  private boolean update; //画面更新許可
  private boolean clockticked; //時間更新
  private boolean clockinitialized; //時計が初期化されたか
  private boolean isSurfaceAccessable;//Surfaceが利用可能か示す
  private boolean isColonVisible; //時計のコロンが点灯しているか？
  private boolean suspend;

  public SingleWorldMapRenderer(){}
  //
  public synchronized void initialize(Context c, SurfaceHolder holder, Point point)
   {
    context = c;
    Resources res = context.getResources();
    surface = holder;
    p = new Paint();
    isSurfaceAccessable = true;
    int screenWidth = point.x;
    int screenHeight = point.y;
    StringBuilder worldmapname = new StringBuilder("worldmap");
    StringBuilder nightmapname = new StringBuilder("nightmap");

    //表示モードを決定する
    paintmode = initDisplayMode();

    //Log.d("WorldClock", "width=" + screenWidth + "height=" + screenHeight);
    if(screenHeight < 1920) //In the case of xhdpi resolution
     {
      sizeIdentifier = xhdpipostfix;
      characterpos = new int[]{ character1_xhdpi, character2_xhdpi, character3_xhdpi, character4_xhdpi, character5_xhdpi};
      characterline = characterline_xhdpi;
      prefferedsize= 1280;
      clockwidth = clockwidth_xhdpi;
     }
    else if(screenHeight >= 1920)
     {
      sizeIdentifier = xxhdpipostfix;
      characterpos = new int[]{ character1_xxhdpi, character2_xxhdpi, character3_xxhdpi, character4_xxhdpi, character5_xxhdpi};
      characterline = characterline_xxhdpi;
      prefferedsize= 1920;
      clockwidth = clockwidth_xxhdpi;
     }

    worldmapname.append(sizeIdentifier);//サイズ識別の文字列を付加
    nightmapname.append(sizeIdentifier);//サイズ識別の文字列を付加
    String packagename = context.getPackageName();
    worldmap =  BitmapFactory.decodeResource(res, res.getIdentifier(worldmapname.toString(),"drawable", packagename));
    nightmap =  BitmapFactory.decodeResource(res, res.getIdentifier(nightmapname.toString(),"drawable", packagename));

    //時計の文字を読み込む
    characters = new Bitmap[11];
    for(int i= 0; i < 10; i++)
     {
      characters[i] = BitmapFactory.decodeResource(res, res.getIdentifier("num"+ String.valueOf(i)+ sizeIdentifier,"drawable", packagename));
//      if(characters[i] == null) Log.d("Worldclock", "characters["+ i+ "]=null");
     }
    characters[10] = BitmapFactory.decodeResource(res, res.getIdentifier("colon" + sizeIdentifier,"drawable", packagename));

    //時計の初期化
    initClock();

    //点滅は消灯からスタートする
    isColonVisible = false;

    Xbase = 0;
    Xoffset = 0;
    wholewidth = worldmap.getWidth();
    wholeheight = worldmap.getHeight();
    Yequator = wholeheight / 2;
    viewwidth = screenWidth;
    viewheight = screenHeight;

    //縮尺地図上の画面の幅を計算する
    int scaledwidth = viewwidth /(viewheight / wholeheight);
    //表示に必要なページ数
    int pages =  wholewidth / scaledwidth;
    if (wholewidth % scaledwidth > 0) pages++;
    //シフト回数
    shiftnumber = pages - 1;

    if (shiftnumber != 0)
     {
      int difference =  scaledwidth * pages - wholewidth;
      int shiftoverlap = difference/ shiftnumber;
      if (difference % shiftnumber > 0 ) shiftoverlap++;
      shiftwidth = scaledwidth - shiftoverlap;
     }
    //Log.d("WorldClock", "mapwidth=" + worldmap.getWidth() + "mapheight=" + worldmap.getHeight());

    screen = Bitmap.createBitmap(wholewidth, wholeheight, Bitmap.Config.ARGB_8888);//
    screencanvas = new Canvas(screen);
    mapsource = new Rect(0, 0, wholeheight * screenWidth / screenHeight, wholeheight);
    view = new Rect(0, 0, viewwidth, viewheight);
    calendar = Calendar.getInstance();//

    update = true;
    loop = true;
    clockticked = true;//初回は無条件で実行する
    suspend = false;
   }

  //モード設定
  private int initDisplayMode()
   {
    String jsonstring = null;
    File file = new File(context.getFilesDir(), modesettingfilename);

    //設定ファイルが存在する場合
    if (file.exists())
     {
      timestamp_displaymode = file.lastModified();

      try
       {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer= new byte[1024];
        int read = 0;
        while((read = fis.read(buffer)) > 0)
         {
          baos.write(buffer, 0, read);
         }

        jsonstring = new String(baos.toByteArray());
        fis.close();
        baos.close();
       }
      catch(IOException e){}
     }
    else//設定ファイルが存在しない場合は地図モードの設定ファイルを作る
     {
      try
       {
        JSONObject result = new JSONObject();
        result.put("mapmode", WorldClock.MODE_SINGLE);
        result.put("displaymode", MAPVIEW);

        FileOutputStream fos =new FileOutputStream(file);
        fos.write(result.toString().getBytes("UTF-8"));
       }
      catch(Exception e){ /*Log.d("Worldclock", e.toString());*/ }
     }

    //設定ファイルが存在する場合
    if(jsonstring != null)
     {
      JSONObject mode = null;
      try
       {
        mode = new JSONObject(jsonstring);
        String map = mode.getString("mapmode");
        String modename = mode.getString("displaymode");

        if(!map.equals(WorldClock.MODE_SINGLE)) return MAPMODE;//WorldClockにエラーを通知
        switch(modename)
         {
          case GLOBEVIEW : return GLOBEMODE;
          case MAPVIEW   : return MAPMODE;
         }
       }
      catch(Exception e){}
     }

    //エラーが起きた場合はここから地図モードの値を返す
    return MAPMODE;
   }

  //都市の読み込み
  private void initClock()
   {
    Resources res = context.getResources();
    String packagename= context.getPackageName();

    String selectionjsonstring = null;
    clockinitialized = false;

    File file = new File(context.getFilesDir(), clockselectionfilename);
    //設定ファイルが存在する場合
    if (file.exists())
     {
      //更新時間を記録
      timestamp_clockselection = file.lastModified();
      try
       {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer= new byte[1024];
        int read = 0;
        while((read = fis.read(buffer)) > 0)
         {
          baos.write(buffer, 0, read);
         }
        selectionjsonstring = new String(baos.toByteArray());
        fis.close();
        baos.close();
       }
      catch(IOException e){ /*Log.d("Worldclock", "Error initclock : " + e.toString());*/}
     }

    //設定ファイルが存在しない場合、または読み込みに失敗した場合
    if(selectionjsonstring == null)
     {
      timestamp_clockselection = System.currentTimeMillis();
      final int id = res.getIdentifier(initialset, "raw", packagename);

      if (id == 0)
       {    //エラーにはならない
        clockinitialized = false;
        return;
       }

      try
       {
        InputStream is = res.openRawResource(id);
        selectionjsonstring = loadText(is);
       }
      catch(Exception e){ /*Log.d("Worldclock","error initclock " + e.toString() );*/clockinitialized = false; return;}
     }

    //読み込んだjsonファイルを基に使われる時計のデータを読み込む
    if (selectionjsonstring != null)
     {
      //時計の位置を記述したjsonファイルを読み込む
      final int id = res.getIdentifier(clockpositionjson + sizeIdentifier, "raw", packagename);
      if (id == 0)
       {    //エラーにはならない
        clockinitialized = false;
        return;
       }
      JSONArray dataarray = null;
      JSONArray selection = null;
      try
       {
        InputStream is = res.openRawResource(id);
        dataarray =  new JSONObject(loadText(is)).getJSONArray("datas");//位置データ
        selection = new JSONObject(selectionjsonstring).getJSONArray("selection");//選択データ
       }
      catch(Exception e){ /*Log.d("Worldclock","error initclock " + e.toString() );*/clockinitialized = false; return;}

      clockpositions = new ArrayList<City>();
      for(int j = 0; j < selection.length();j++)
       {
        try
         {
          JSONObject item = selection.getJSONObject(j);
          String selectedcityname = item.getString("cityname");//選択された都市名

          for (int i = 0; i < dataarray.length(); i++)
           {
            try
             {
              JSONObject entry = dataarray.getJSONObject(i);
              String definedcityname = entry.getString("cityname");//定義された都市名

              if(selectedcityname.equals(definedcityname))
               {
                Bitmap bmap = BitmapFactory.decodeResource(res, res.getIdentifier(definedcityname + sizeIdentifier, "drawable", packagename));
                int X = Integer.parseInt(entry.getString("positionX"));
                int Y = Integer.parseInt(entry.getString("positionY"));
                String str = entry.getString("zone");
                if (bmap != null) clockpositions.add(new City(bmap, X, Y, str));

                //Log.d("Worldclock", "cityname=" + selectedcityname + " X=" + X + " Y=" + Y + "width=" + bmap.getWidth());
               }
             }
            catch (Exception e) { continue; }
           }
         }
        catch(JSONException e){ continue;}
       }
      clockinitialized = true;
     }
   }


  //年の選択が変更されたか調べる
  private void checkClockSelection()
   {
    File file = new File(context.getFilesDir(), clockselectionfilename);

    //設定ファイルが存在する場合
    if (file.exists())
     {
      long updatetime = file.lastModified();
      if (updatetime > timestamp_clockselection) {initClock(); }
     }
   }

  //表示モードが変更されたか調べる
  private void checkModeSetting()
   {
    File file = new File(context.getFilesDir(), modesettingfilename);

    //設定ファイルが存在する場合
    if (file.exists())
     {
      long updatetime = file.lastModified();
      if (updatetime > timestamp_displaymode)
       {
        int newmode = initDisplayMode();

        if(paintmode != newmode)
         {
          //地球儀モードから地図モードの変更は気を遣う
          if (paintmode == GLOBEMODE)
           {
            //縮尺地図上の画面の幅を計算する
            int scaledwidth = viewwidth /(viewheight / wholeheight);
            //表示に必要なページ数
            int pages =  wholewidth / scaledwidth;
            if (wholewidth % scaledwidth > 0) pages++;
            //スクリーン×ページ数の大きさと全体の長さの差からオーバーラップの幅を得る
            int difference =  scaledwidth * pages - wholewidth;
            int shiftoverlap = difference/ (pages - 1);

            int pagenumber = 0;
            for(int i = pages - 1; i > 0; i--)
             {
              int xMin = (scaledwidth - shiftoverlap) * i;
              int xMax = xMin + scaledwidth;
              if (xMin <= Xoffset && Xoffset < xMax){ pagenumber = i; break; }
             }
            shiftindex = pagenumber + 1;
            if (shiftindex >= pages) shiftindex = 0;
           }
          paintmode = newmode;
         }
       }
     }
   }


  @Override
  public void run()
   {
    long lastShifted = System.currentTimeMillis();
    long lastLoop = lastShifted;

	while( loop )
     {
      if(clockticked)
       {
        //時間更新
        long millisec = System.currentTimeMillis();
        calendar.setTimeInMillis(millisec);
        lastTicked = millisec;
        //画面書き換えのタイミング
        clockticked = false;

        if(update)
         {
          updateTime();
          updateScreen();
          lastUpdated = System.currentTimeMillis();
          update = false;
         }
        if (surface != null && paintmode == MAPMODE) shiftMap();
       }

      if(suspend)
       {
        try
         {
          synchronized(this)
           {
            wait();
           }
         }
        catch(InterruptedException e){}

        //スレッドが停止された状態から復帰した場合。前回の時間更新から１分以上の場合は時間更新。
        if (System.currentTimeMillis() - lastTicked > 60000L)
         {
          clockticked = true;
         }
       }

      long currentSecond = System.currentTimeMillis();//現在の時間

      //点滅コントロール
      blinkinterval += (currentSecond - lastLoop);
      if(isColonVisible & blinkinterval > TURNON)
       {
        isColonVisible = false;
        blinkinterval = 0L;
       }
      else if(!isColonVisible & blinkinterval > TURNOFF)
       {
        isColonVisible = true;
        blinkinterval = 0L;
       }

      if(paintmode == MAPMODE)
       {
        //地図をシフトする
        if (currentSecond - lastShifted > 8000L)
         {
          if (shiftnumber != 0)
           {
            Xoffset = shiftwidth * shiftindex;
            shiftMap();
            shiftindex++;
            if (shiftindex > shiftnumber) shiftindex = 0;
           }
          lastShifted = currentSecond;
         }
        else if (blinkinterval == 0L)//切り替わった直後は必ず０であることを利用する
         {
          shiftMap();//時計再描画のための地図描画
         }
       }
      else if(paintmode == GLOBEMODE)
       {
        if(currentSecond > (lastShifted + 50L))
         {
          Xoffset++;
          if (Xoffset >= wholewidth) Xoffset-= wholewidth;
          shiftMap();
          lastShifted = currentSecond;
         }
       }
      lastLoop = currentSecond;
	 }
   }
  
  //
  @Override
  public synchronized void onReceive(Context contex, Intent i)
   {
    clockticked = true;
    if(System.currentTimeMillis() - lastUpdated >= interval)
     {
      update = true;  
     }
   }


  public void updateTime()
   {
    //日本以外のマシンで実行された場合を想定する
	TimeZone local = calendar.getTimeZone();
    TimeZone JST = TimeZone.getTimeZone(timezoneidJST);
    calendar.setTimeZone(JST);

    int y = calendar.get(Calendar.YEAR);
    int m = calendar.get(Calendar.MONTH) + 1;
    int d = calendar.get(Calendar.DAY_OF_MONTH);
    int h = calendar.get(Calendar.HOUR_OF_DAY);
    int min = calendar.get(Calendar.MINUTE);

    calculateSunPosition((double)y, (double)m, (double)d, (double)h, (double)min);
    calendar.setTimeZone(local);
   }
  
  //retrieve the position of the sun
  public void calculateSunPosition(double year, double month, double day, double hour, double minute)
   {
    time = minute / 60.0 + hour;
    timevalue = StarPosition.getTime(year, month, day, time);
	elon = StarPosition.getSunEclipticLongitude(timevalue);//
	e = StarPosition.getInclination(timevalue);//
	asc = StarPosition.getRightAscension(elon, e);//
	dec = StarPosition.getDeclination(elon, e);//
	phai0 =  StarPosition.getSidereal(timevalue, time / 24.0, 0);//
	dist = StarPosition.getSunDistance(timevalue);
	parallax = StarPosition.getParallax(dist);//
	k = StarPosition.getSunriseAltitude(StarPosition.getSunDiameter(dist), 0.0, StarPosition.refraction, parallax);
	twilightk = StarPosition.getTwilightAltitude(0.0, parallax);
   }


  private void shiftMap()
   {
    if(!isSurfaceAccessable) return;
    checkClockSelection();
    checkModeSetting();
    Canvas c = null;

    synchronized(surface)
     {
      try
       {
        c = surface.lockCanvas();
        if (c != null)
         {
          int right = wholeheight * viewwidth / viewheight + Xoffset;

          if (right > wholewidth)//右端がイメージ外に出る
           {
            int srclength = wholewidth - Xoffset;
            int dstlength = srclength * viewheight / wholeheight;
            mapsource.left = Xoffset;
            mapsource.right = wholewidth - 1;
            view.left = 0;
            view.right = dstlength - 1;
            c.drawBitmap(screen, mapsource, view, p);

            mapsource.left = 0;
            mapsource.right = right - wholewidth - 1;
            view.left = dstlength;
            view.right = viewwidth - 1;
            c.drawBitmap(screen, mapsource, view, p);
           }
          else //
           {
            mapsource.left = Xoffset;
            mapsource.right = right;
            view.left = 0;
            view.right = viewwidth;
            c.drawBitmap(screen, mapsource, view, p);
           }
          //時計の描画
          if(clockinitialized)
           {
            TimeZone localzone = calendar.getTimeZone();
            calendar.setTimeInMillis(System.currentTimeMillis());

            int left = Xoffset * 4;
            if (paintmode == GLOBEMODE) left -= clockwidth;//地球儀モードの場合は突然消える現象を防ぐ

            int mapwidth = wholewidth * 4;//拡大後のサイズ
            int r = right * 4;

            for (City city:clockpositions )
             {
              int clockMinX = city.getX() * viewheight / prefferedsize; //実際のスクリーン高さと想定の高さの比率を求めて補正する
              if(left < mapwidth & r < mapwidth)
               {
                if(left < clockMinX & clockMinX < r)
                 {
                  drawClock(c, city, clockMinX);
                 }
               }
              else if(r >= mapwidth)
               {
                if(left < clockMinX & clockMinX < mapwidth){ drawClock(c, city, clockMinX); }
                else if(0 <= clockMinX & clockMinX < r - mapwidth) { drawClock(c, city, clockMinX + mapwidth); }
               }
             }
            calendar.setTimeZone(localzone);
           }
         }
       }
      catch (java.lang.IllegalStateException e) {}
      finally
       {
        if (c != null)
         {
          surface.unlockCanvasAndPost(c);
         }
       }
     }
   }

  //時計をキャンバスに描く
  private void drawClock(Canvas c, City city, int clockMinX)
   {
    int clockpositionY = city.getY() * viewheight / prefferedsize;//時計位置を補正する
    int Xbase = Xoffset * 4;
    c.drawBitmap(city.getClockImage(), clockMinX - Xbase, clockpositionY, p);
    calendar.setTimeZone(city.getTimeZone());
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);
    int order10 = hour / 10;

    //それぞれの文字位置も補正する
    int baseline = clockpositionY + characterline * viewheight / prefferedsize;
    //時（10の位）
    if(order10 != 0) { c.drawBitmap(characters[order10], (clockMinX + characterpos[0] * viewheight / prefferedsize) - Xbase, baseline, p);}
    //時（1の位）
    c.drawBitmap(characters[hour % 10], (clockMinX + characterpos[1] * viewheight / prefferedsize) - Xbase, baseline, p);
    //コロン
    if (isColonVisible) c.drawBitmap(characters[10], (clockMinX + characterpos[2] * viewheight / prefferedsize) - Xbase, baseline, p);
    //分(10の位)
    c.drawBitmap(characters[minute / 10], (clockMinX + characterpos[3] * viewheight / prefferedsize) - Xbase, baseline, p);
    //分（1の位）
    c.drawBitmap(characters[minute % 10], (clockMinX + characterpos[4] * viewheight / prefferedsize) - Xbase, baseline, p);
   }


  public void releaseSurface(SurfaceHolder holder)
   {
    if(!holder.equals(surface))
     {
      return;
     }

    isSurfaceAccessable = false;//Surfaceが破棄された
    loop = false;
   }


  public void previewScreen()
   {
    new Thread()
     {
      @Override
      public void run()
       {
        Canvas c = null;
        int count =0;
        while(count< 3) {
            try {
                c = surface.lockCanvas();
                if (c != null) {
                    mapsource.left = 0;
                    mapsource.right = wholeheight * viewwidth / viewheight;
                    view.left = 0;
                    view.top = 0;
                    view.right = viewwidth;
                    c.drawBitmap(worldmap, mapsource, view, p);
                     break;
                }
               else
                {
                 Thread.sleep(300);
                }
            }
            catch(InterruptedException e){}
            finally {
                if (c != null) {
                    surface.unlockCanvasAndPost(c);
                }
            }
         count++;
        }
       }
     }.start();
   }


  public void terminateApplication()
   {
    loop = false;
   }

  //
  public void updateScreen()
   {
	double hinode_keido, hinoiri_keido, asayake, higure;
	int hinoiriX=0, hinodeX=0;
    int asayakeX=0, higureX=0;

    //screen.eraseColor(0xff000000);

    screencanvas.drawBitmap(worldmap,0.0f, 0.0f, p);//世界地図で初期化

    for(int i = 0;i < wholeheight; i++)
     {
      //
      double latitude = getLatitudeFromY(Yequator - i);
      //
      double jikaku = StarPosition.getTimeAngle(k, dec, latitude);
      double jikaku_twi =  StarPosition.getTimeAngle(twilightk, dec, latitude);
       	 
      if(!Double.isNaN(jikaku))//
       {
        hinode_keido = StarPosition.reviseAngle(-jikaku + asc - phai0);
        hinoiri_keido = StarPosition.reviseAngle(jikaku + asc - phai0);
        hinodeX =(int)getXfromLongitude(hinode_keido);
        hinoiriX = (int)getXfromLongitude(hinoiri_keido);//

        //drawDayLightSide(hinodeX, hinoiriX, i);//

        if (!Double.isNaN(jikaku_twi))//
         {
          asayake = StarPosition.reviseAngle(-jikaku_twi + asc - phai0);
          higure = StarPosition.reviseAngle(jikaku_twi + asc - phai0);
          asayakeX = (int)getXfromLongitude(asayake);
          higureX = (int)getXfromLongitude(higure);

          drawNightSide(higureX, asayakeX, i);
          //
          if (asayakeX < hinodeX )
           {
          	drawTwilight(latitude, asayakeX, hinodeX, i);
           }
          else
           {
      		drawTwilight(latitude, asayakeX, wholewidth -1, i);
      		drawTwilight(latitude, 0, hinodeX, i);
           }
          //
          if (hinoiriX < higureX )
           {
       		drawTwilight(latitude, hinoiriX, higureX, i);
           }
          else
           {
    		drawTwilight(latitude, hinoiriX, wholewidth -1, i);
     		drawTwilight(latitude, 0, higureX, i);
           }
         }
        else//
         {
          if(hinodeX <= hinoiriX)
           {
          	drawTwilight(latitude, hinoiriX, wholewidth - 1, i);
        	drawTwilight(latitude, 0, hinodeX, i);
           }
          else
           {
        	drawTwilight(latitude, hinoiriX, hinodeX, i);
           }
         }
       }
      else //
       {
        if (!Double.isNaN(jikaku_twi))//
         {
          asayake = StarPosition.reviseAngle(-jikaku_twi + asc - phai0);
          higure = StarPosition.reviseAngle(jikaku_twi + asc - phai0);
          asayakeX = (int)getXfromLongitude(asayake);
          higureX = (int)getXfromLongitude(higure);

          if (asayakeX < higureX)
           {
          	drawTwilight(latitude, asayakeX, higureX, i);
            drawNightSide(higureX, wholewidth -1 , i);
            drawNightSide(0, asayakeX , i);
           }
          else
           {
        	drawTwilight(latitude, asayakeX, wholewidth - 1, i);
          	drawTwilight(latitude, 0, higureX, i);
            drawNightSide(higureX, asayakeX , i);
           }
         }
        else //
         {
//          double altitude = StarPosition.getSunAltitude(asc, dec, latitude, StarPosition.getSidereal(timevalue, time / 24.0, 0.0));
//          drawTwilight(0, wholewidth -1, i, altitude);
          drawTwilight(latitude, 0, wholewidth - 1,  i);
         }
       }
     }
   }

  //
  private void drawNightSide(int higure, int asayake, int y)
   {
    if (higure <= asayake)
	 {
      for(int i = higure; i <= asayake; i++)
       {
    	screen.setPixel(i, y, nightmap.getPixel(i, y));
        //p.setColor(nightmap.getPixel(i, y));
       // screencanvas.drawPoint(i, y,p);
       }
     }
    else
     {
      for(int i = higure; i < wholewidth; i++)
       {
        screen.setPixel(i, y, nightmap.getPixel(i, y));
        //p.setColor(nightmap.getPixel(i, y));
        //screencanvas.drawPoint(i, y, p);
       }
      for(int i = 0; i <= asayake; i++)
       {
        screen.setPixel(i, y, nightmap.getPixel(i, y));
        //p.setColor(nightmap.getPixel(i, y));
//        screencanvas.drawPoint(i, y, p);
       }
     }
   }

  //
  private void drawDayLightSide(int hinode, int hinoiri, int y)
   {
    if (hinode <= hinoiri)
     {
      for(int i = hinode; i <= hinoiri; i++)
       {
    	screen.setPixel(i, y, worldmap.getPixel(i, y));
       }
     }
    else
     {
      for(int i = hinode; i < wholewidth; i++)
       {
        screen.setPixel(i, y, worldmap.getPixel(i, y));
       }
      for(int i = 0; i <= hinoiri; i++)
       {
        screen.setPixel(i, y, worldmap.getPixel(i, y));
       }
     }
   }  

  //
  private void drawTwilight(double latitude, int startx, int endx, int y)
   {
    int addition = startx <= endx ? 1 : -1;
    double longitude = 0.0;
    if(startx < 0 || startx >= wholewidth || endx < 0 || endx >= wholewidth) return;
    
    for(int i = startx; i != endx; i += addition)
     {
      longitude = (double)i / (double)wholewidth * 360.0;

      double phai = StarPosition.getSidereal(timevalue, time / 24.0, longitude);//�P����
      double altitude = StarPosition.getSunAltitude(asc, dec, latitude, phai);//���x
      if(altitude > 0.0) continue;

      if(!Double.isNaN(altitude))
       {
        double ratio = (8.0 + Math.floor(altitude)) / 8.0;

        if(ratio < 0.0) screen.setPixel(i, y, nightmap.getPixel(i, y));
        //else if(ratio > 1.0) screen.setPixel(i, y, worldmap.getPixel(i, y));
        else screen.setPixel(i, y, composeColors(nightmap.getPixel(i, y), worldmap.getPixel(i, y),ratio));
       }
     }
   }

/*
  //
  private void drawTwilight(int startx, int endx, int y, double h)
   {
    double ratio =  (8.0 + Math.floor(h)) / 8.0;
    int addition = startx <= endx ? 1 : -1;

    for(int i = startx; i < endx; i++)
     {
      if(ratio < 0.0) screen.setPixel(i, y, nightmap.getPixel(i, y));
      else if(ratio > 1.0) screen.setPixel(i, y, worldmap.getPixel(i, y));
      else screen.setPixel(i, y, composeColors(nightmap.getPixel(i, y), worldmap.getPixel(i, y),ratio));
     }
   }
*/
  //
/*
  public void setVibible(boolean visible)
   {
    isVisible = visible;
   }
*/
  public void suspendTask(){ suspend = true; }

  public void resumeTask()
   {
    synchronized(this)
     {
      notify();
     }
    suspend = false;
   }

  //
  private int composeColors(int color1, int color2, double ratio) 
   {
    int b1 = (color1 & 0xff) , b2 = (color2 & 0xff); 
    int newBlue = b1 - (int)((double)(b1 - b2) * ratio);
    if (newBlue < 0) newBlue = 0; 
    else if (newBlue > 255) newBlue = 0xff;

    int g1 = (color1 &0xff00) >> 8, g2 = (color2 & 0xff00) >> 8;
    int newGreen = g1 - (int)((double)(g1 - g2) * ratio);
    if (newGreen < 0) newGreen = 0; 
    else if(newGreen > 255) newGreen = 0xff;

    int r1 = (color1 &0xff0000) >> 16, r2 = (color2 & 0xff0000) >> 16;
    int newRed = r1 - (int)((double)(r1 - r2) * ratio);
    if (newRed < 0) newRed = 0; 
    else if(newRed > 255) newRed = 0xff;

    return (newRed << 16) + (newGreen << 8) + newBlue  + 0xff000000;
   }

  //
  private double getXfromLongitude(double longitude)
   {
    double result = longitude;

    if (result <= -360.0) { result += Math.ceil(result / 360.0) * 360.0; }
    else if(result >= 360.0) { result -= Math.floor(result / 360.0) *360.0; }

    result = result / 360.0 * wholewidth + Xbase; //

    if( result > wholewidth) result -= wholewidth;
    else if (result < 0) result += wholewidth;

    return result;
   }

  //
  private double getLatitudeFromY(int y)
   {
    return (double)y / (double)Yequator * 90.0;
   }

  private String loadText(InputStream is)throws IOException
   {
    byte[] buffer = new byte[1024];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int read = 0;
       int count = 0;
    while((read = is.read(buffer))!= -1)
     {
         count+= read;
      baos.write(buffer, 0, read);
     }
    String result = new String(baos.toByteArray());
    baos.close();

    return result;
   }
 }
