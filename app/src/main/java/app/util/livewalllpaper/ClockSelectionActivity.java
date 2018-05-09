package app.util.livewalllpaper;

//import android.preference.PreferenceActivity;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.view.View;
import android.util.Log;
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
  private static final String settingfilename = "clockselection.json";
  private static final String initialset = "initialsetting";
  private ArrayList<String> selectlist;
  private String[] citynames;
  private String sizeIdentifier="";

  static class ViewHolder
   {
    CheckBox checkBox;
    TextView textView;
   }


  @Override
  public void onCreate(Bundle savedInstanceState)
   {
    super.onCreate(savedInstanceState);

    //ロケールを取得
    if(!Locale.getDefault().getLanguage().equals("ja")){ setTitle("The list of selectable cities");}
    else setTitle("選択できる都市のリスト");

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
    citynames = getAllItems();

    createCityList();
   }

  @Override
  public void onStop()
   {
    super.onStop();
    writeJsonData();
   }

  @Override
  public void onDestroy()
   {
    super.onDestroy();
   }

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

  private ArrayList<String> getSelectedItems()
   {
    Context context = this;
    String selectionjsonstring = null;
    File file = new File(context.getFilesDir(), settingfilename);

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

  private String[] getAllItems()
   {
    Context context = this;
    Resources res = context.getResources();
    String packagename = context.getPackageName();

    //時計の位置を記述したjsonファイルを読み込む
    final int id = res.getIdentifier(clockpositionjson + sizeIdentifier, "raw", packagename);

    if (id == 0)
     {    //エラーにはならない
      return new String[0];
     }

    JSONArray dataarray = null;
    try
     {
      InputStream is = res.openRawResource(id);
      dataarray =  new JSONObject(loadText(is)).getJSONArray("datas");
     }
    catch(Exception e){ /*Log.d("Worldclock","error initspinner " + e.toString() );*/ return new String[0];}

    ArrayList<String> citynames= new ArrayList<String>();

    for (int i = 0; i < dataarray.length(); i++)
     {
      try
       {
        JSONObject entry = dataarray.getJSONObject(i);
        citynames.add(entry.getString("cityname"));
       }
      catch (Exception e){ continue; }
     }
    String[] result = new String[citynames.size()];
    for(int i = 0;i < citynames.size();i++)
     {
      result[i] = citynames.get(i);
     }

    return result;
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

   /*
  public void dialog3(Context context)
   {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle("表示できる都市");

    final String[] items = citynames;
    final boolean[] check = new boolean[items.length];
    for (int i = 0; i < selectlist.size(); i++)
     {
      String selected = selectlist.get(i);
         Log.d("Worldclock", "selected city=" +selected);
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

    builder.setMultiChoiceItems(items, check, mCheckListener);

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
*/
  private void writeJsonData()
   {
    File file = new File(getFilesDir(), settingfilename);

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
    catch(Exception e){Log.d("Worldclock", e.toString());}
   }
 }
