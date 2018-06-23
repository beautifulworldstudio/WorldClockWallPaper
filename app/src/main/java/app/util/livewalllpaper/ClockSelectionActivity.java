package app.util.livewalllpaper;

//import android.preference.PreferenceActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.view.View;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Kentaro on 2018/02/26.
 */

 public class ClockSelectionActivity extends Activity implements CompoundButton.OnCheckedChangeListener
 {
  private static final String clockpositionjson ="clockposition";
  private static final String defaultpostfix ="_small";
  private static final String xhdpipostfix ="_xhdpi";
  private static final String xxhdpipostfix ="_xxhdpi";
  private static final String clocksettingfile = "clockselection.json";
  private static final String modesettingfilename="modeselection.json";
  private static final String initialset = "initialsetting";
  private ArrayList<String> selectlist;
  private String[] citynames;
  private String[] displaynames;
  private String sizeIdentifier="";
  private String maptype;
  private String displaymode;

  static class ViewHolder
   {
    CheckBox checkBox;
    TextView textView;
   }


  @Override
  public void onCreate(Bundle savedInstanceState)
   {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.selecter);

    WindowManager winmanager = (WindowManager) getSystemService(WINDOW_SERVICE);
    Display disp = winmanager.getDefaultDisplay();
    Point realsize = new Point();
    disp.getRealSize(realsize);

    if(realsize.y > 960 & realsize.y < 1920) //In the case of xhdpi resolution
     {
      sizeIdentifier = xhdpipostfix;
     }
    else if(realsize.y > 1280) //In the case of xhdpi resolution
     {
      sizeIdentifier= xxhdpipostfix;
     }
    else
     {
      sizeIdentifier = defaultpostfix;
     }
    selectlist = getSelectedItems();

    String[][] clockstrings = getAllItems();
    citynames = clockstrings[0];
    displaynames = clockstrings[1];

    Button btn =(Button)findViewById(R.id.selectbutton);
    btn.setOnClickListener(new View.OnClickListener()
     {
      @Override
      public void onClick(View view)
       {
        dialog3(ClockSelectionActivity.this);
       }
     });

    RadioButton globemode = (RadioButton)findViewById(R.id.RadioButton2);
    RadioButton mapmode = (RadioButton)findViewById(R.id.RadioButton1);

    globemode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
     {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
       {
        if(isChecked)
         {
          maptype = WorldClock.MODE_SINGLE;
          displaymode = SingleWorldMapRenderer.GLOBEVIEW;
          writeDisplayMode();
         }
       }
     });

    mapmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
     {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
       {
        if(isChecked)
         {
          maptype = WorldClock.MODE_SINGLE;
          displaymode = SingleWorldMapRenderer.MAPVIEW;
          writeDisplayMode();
         }
       }
     });

    //ロケールによる表示テキストの変更を加える
    if(Locale.getDefault().getLanguage().equals("ja"))
     {
      setTitle("ワールドクロックマップの設定");

      globemode.setText(R.string.roundglobe_ja);
      globemode.setTextSize(realsize.y / 50);
      mapmode.setText(R.string.slidemap_ja);
      mapmode.setTextSize(realsize.y / 50);

      TextView globemodetext = (TextView)findViewById(R.id.globemodetext);
      globemodetext.setText(R.string.roundglobe_explain_ja);
      globemodetext.setTextSize(realsize.y / 70);

      TextView slidemodetext = (TextView)findViewById(R.id.slidemodetext);
      slidemodetext.setText(R.string.slidemap_explain_ja);
      slidemodetext.setTextSize(realsize.y / 70);

      btn.setText(R.string.selectcity_ja);
      btn.setTextSize(realsize.y / 50);
     }
    else
     {
      setTitle("Settings of World Clock Map");

      globemode.setText(R.string.roundglobe_en);
      globemode.setTextSize(realsize.y / 50);
      mapmode.setText(R.string.slidemap_en);
      mapmode.setTextSize(realsize.y / 50);
      TextView globemodetext = (TextView)findViewById(R.id.globemodetext);
      globemodetext.setText(R.string.roundglobe_explain_en);
      globemodetext.setTextSize(realsize.y / 70);

      TextView slidemodetext = (TextView)findViewById(R.id.slidemodetext);
      slidemodetext.setText(R.string.slidemap_explain_en);
      slidemodetext.setTextSize(realsize.y / 70);

      btn.setText(R.string.selectcity_en);
      btn.setTextSize(realsize.y / 50);
     }

    //ラジオボタンをセットする
    getDisplayMode(this);
    if(maptype.equals(WorldClock.MODE_SINGLE) && displaymode.equals(SingleWorldMapRenderer.GLOBEVIEW))
     {
      globemode.setChecked(true);
     }
    else if(maptype.equals(WorldClock.MODE_SINGLE) && displaymode.equals(SingleWorldMapRenderer.MAPVIEW))
     {
      mapmode.setChecked(true);
     }
    else//エラー。単一地図・スライド方式にチェック
     {
      mapmode.setChecked(true);
     }
   }

  @Override
  public void onStop()
   {
    super.onStop();
//    writeJsonData();
//    writeDisplayMode();
   }

  @Override
  public void onDestroy()
   {
    super.onDestroy();
   }
/*
  private void createCityList()
   {
    LinearLayout base = findViewById(R.id.listbase);

    String[] allcities = getAllItems();
    ArrayList<String> selectlist = getSelectedItems();
    LayoutInflater inflater = LayoutInflater.from(this);

    for (int j = 0; j < allcities.length; j++)
     {
      View view = inflater.inflate(R.layout.list, null);
      ViewHolder   holder = new ViewHolder();

      holder.checkBox = view.findViewById(R.id.check_box);
      holder.checkBox.setTag(allcities[j]);
      holder.textView = view.findViewById(R.id.text_view);
      holder.textView.setText(allcities[j]);
      holder.textView.setTextColor(Color.BLACK);
      view.setTag(holder);

      for (int i = 0; i < selectlist.size(); i++)
       {
        String selected = selectlist.get(i);
        //Log.d("Worldclock", "selected city=" +selected);
        if (selected.equals(allcities[j]))
         {
          holder.checkBox.setChecked(true);

          break;
         }
       }
      //状態を設定した後でイベントリスナーを設定する
      holder.checkBox.setOnCheckedChangeListener(this);

      //レイアウトにビュー追加
      base.addView(view);
     }
   }
*/
  private ArrayList<String> getSelectedItems()
   {
    Context context = this;
    String selectionjsonstring = null;
    File file = new File(context.getFilesDir(), clocksettingfile);

    //設定ファイルが存在する場合
    if (file.exists())
     {
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
      catch(IOException e){}
     }

    //設定ファイルが存在しない場合、または読み込みに失敗した場合
    if(selectionjsonstring == null)
     {
      Resources res = context.getResources();

      final int id = res.getIdentifier(initialset, "raw", context.getPackageName());

      if (id == 0)
       {    //エラーにはならない
        return new ArrayList<String>();
       }

      try
       {
        InputStream is = res.openRawResource(id);
        selectionjsonstring = loadText(is);

       }
      catch(Exception e){ /*Log.d("Worldclock","error initclock " + e.toString() );*/ return new ArrayList<String>();}
     }

    JSONArray selection = null;
    try
     {
      selection = new JSONObject(selectionjsonstring).getJSONArray("selection");
     }
    catch(Exception e){ return new ArrayList<String>(); }

    ArrayList<String> citynames = new ArrayList<String>();

    for(int j = 0; j < selection.length();j++)
     {
      try
       {
        JSONObject item = selection.getJSONObject(j);
        citynames.add(item.getString("cityname"));
       }
      catch(Exception e){ continue; }
     }

    return citynames;
   }

  private String[][] getAllItems()
   {
    Context context = this;
    Resources res = context.getResources();
    String packagename = context.getPackageName();
    String[][] result = new String[2][];

    //時計の位置を記述したjsonファイルを読み込む
    final int id = res.getIdentifier(clockpositionjson + sizeIdentifier, "raw", packagename);

    if (id == 0)
     {    //エラーにはならない
      return result;
     }

    JSONArray dataarray = null;
    try
     {
      InputStream is = res.openRawResource(id);
      dataarray =  new JSONObject(loadText(is)).getJSONArray("datas");
     }
    catch(Exception e){ /*Log.d("Worldclock","error initspinner " + e.toString() );*/ return new String[2][0];}

    ArrayList<String> citynamesArray = new ArrayList<String>();
    ArrayList<String> displaynamesArray = new ArrayList<String>();

    for (int i = 0; i < dataarray.length(); i++)
     {
      try
       {
        JSONObject entry = dataarray.getJSONObject(i);
        String citynamedata = entry.getString("cityname");
        String displaynamedata =entry.getString("displayname");

        //例外が発生しなければ代入する
        citynamesArray.add(citynamedata);
        displaynamesArray.add(displaynamedata)         ;
       }
      catch (Exception e){ continue; }
     }

    //数が一致しないときは要素数０で返す
    if(citynamesArray.size() != displaynamesArray.size()) return result;

    //登録名（cityname）を格納する
    result[0] = new String[citynamesArray.size()];
    for(int i = 0;i < citynamesArray.size();i++)
     {
      result[0][i] = citynamesArray.get(i);
     }

    //表示名（displayname）を格納する
    result[1] = new String[displaynamesArray.size()];
    for(int i = 0;i < displaynamesArray.size();i++)
     {
      result[1][i] = displaynamesArray.get(i);
    }

    return result;
   }

  private void getDisplayMode(Context context)
   {
    String jsonstring = null;
    File file = new File(context.getFilesDir(), modesettingfilename);

    //設定ファイルが存在する場合
    if (file.exists())
     {
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

    if(jsonstring != null)
     {
      JSONObject mode = null;
      try
       {
        mode = new JSONObject(jsonstring);
        maptype = mode.getString("mapmode");
        displaymode = mode.getString("displaymode");

        //Log.d("worldclock", "map=" + maptype + " mode=" + displaymode);
       }
      catch(Exception e){}
     }

    //設定ファイルが存在しない場合、または読み込みに失敗した場合
    if(jsonstring == null | maptype == null | displaymode == null)
     {
      //1枚地図・時間ごとにスライドする方式を指定する
      maptype = WorldClock.MODE_SINGLE;
      displaymode = SingleWorldMapRenderer.MAPVIEW;
     }
   }

  private String loadText(InputStream is)throws IOException
   {
    byte[] buffer = new byte[1024];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int read = 0;
    int count = 0;
    while ((read = is.read(buffer)) != -1)
     {
      count+= read;
      baos.write(buffer, 0, read);
     }
    String result = new String(baos.toByteArray());
    baos.close();

    return result;
   }

  //都市を選んだ時のイベントリスナ
  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
   {
    String tag="";
    try
     {
      CheckBox checked = (CheckBox)buttonView;
      tag = (String)checked.getTag();
     }
    catch(ClassCastException e){ return; }

    if (!isChecked)
     {
      for (int j = 0; j < selectlist.size(); j++)
       {
        if (selectlist.get(j).equals(tag)) { selectlist.remove(tag); break; }
       }
     }
    else
     {
      boolean matched = false;
      for (int j = 0; j < selectlist.size(); j++)
       {
        if (selectlist.get(j).equals(tag)) { matched = true; break; }
       }
      if(!matched) selectlist.add(tag);
     }
   }

  //都市名表示のダイアログ
  public void dialog3(Context context)
   {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);

    if(Locale.getDefault().getLanguage().equals("ja")) builder.setTitle(R.string.selectable_ja);
    else builder.setTitle(R.string.selectcity_en);

    final String[] items = citynames;
    final boolean[] check = new boolean[items.length];

    for (int i = 0; i < selectlist.size(); i++)
     {
      String selected = selectlist.get(i);
      //Log.d("Worldclock", "selected city=" +selected);

      for (int j = 0; j < items.length; j++)
       {
        if (selected.equals(items[j]))
         {
          Log.d("Worldclock", "selected city=" + selected + ": item=" + items[j]);
          check[j] = true;
          break;
         }
       }
     }

    DialogInterface.OnMultiChoiceClickListener mCheckListener = new DialogInterface.OnMultiChoiceClickListener()
     {
      public void onClick(DialogInterface dialog, int which, boolean isChecked)
       {
        check[which] = isChecked;
//        String checked = isChecked ? " checked." : " released.";
//        Log.d("Worldclock", "No."+ which + " " + checked);
       }
     };

    builder.setMultiChoiceItems(displaynames, check, mCheckListener);

    // ボタンのリスナー //
    DialogInterface.OnClickListener mButtonListener = new DialogInterface.OnClickListener()
     {
      public void onClick(DialogInterface dialog, int which)
       {
        String btnStr = "";
        switch( which )
         {
          case AlertDialog.BUTTON_POSITIVE:
           btnStr = "OK";
           //データの更新を実行する
           for (int i = 0; i < check.length; i++)
            {
             boolean matched = false;
             for (int j = 0; j < selectlist.size(); j++)
              {
               if (selectlist.get(j).equals(items[i])) matched = true;
              }

             if(check[i] & !matched) selectlist.add(items[i]);
             else if(!check[i] & matched) selectlist.remove(items[i]);
            }
            writeJsonData();
           break;
          case AlertDialog.BUTTON_NEUTRAL:
           btnStr = "Cancel";
           break;
         }
       }
     };

    // 決定・キャンセル用にボタンも配置 //
    builder.setPositiveButton("OK", mButtonListener );
    builder.setNeutralButton ("Cancel", mButtonListener );

    AlertDialog dialog = builder.create();
    dialog.show();
   }

  private void writeJsonData()
   {
    File file = new File(getFilesDir(), clocksettingfile);

    try
     {
      JSONArray array = new JSONArray();
      for(String entry: selectlist)
       {
        JSONObject jobj = new JSONObject();
        jobj.put("cityname",entry);
        array.put(jobj);
       }
      JSONObject result = new JSONObject();
      result.put("selection", array);

      FileOutputStream fos =new FileOutputStream(file);
      fos.write(result.toString().getBytes("UTF-8"));
     }
    catch(Exception e){ /*Log.d("Worldclock", e.toString());*/}
   }

  private void writeDisplayMode()
   {
    File file = new File(getFilesDir(), modesettingfilename);

    try
     {
      JSONObject result = new JSONObject();
      result.put("mapmode", maptype);
      result.put("displaymode", displaymode);

      FileOutputStream fos =new FileOutputStream(file);
      fos.write(result.toString().getBytes("UTF-8"));
     }
    catch(Exception e){ /*Log.d("Worldclock", e.toString());*/ }
   }
 }
