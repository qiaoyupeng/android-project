package com.example.joe.shakealarm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button btn_setclick; //设置时间按钮
    private ToggleButton btn_enclick; //闹钟开关按钮
    private ToggleButton btn_alarmstyle; //设置闹铃方式

    private SharedPreferences sharedPreferences; //此类为保存应用基本设置的基类
    private SharedPreferences.Editor editor;
    private static boolean alarmstyle=true; // 设置响铃状态，true：铃声,false:震动

    Calendar c=Calendar.getInstance();

    SimpleDateFormat sdf=new SimpleDateFormat("HH:mm"); //格式化日期成字符串，显示比如为"20:20";

    static MainActivity instance;
    static String shakeValue;


    public static void setAlarmstyle(boolean style){
       alarmstyle=style;
    }
    public static boolean getAlarmstyle(){
        return alarmstyle;
    }
    public void loaddata(){
        sharedPreferences=getSharedPreferences("main_activity",MODE_PRIVATE); //"MODE_PRIVATE"常量为应用配置信息仅本应用可见
        editor=sharedPreferences.edit();
        btn_setclick.setText(sharedPreferences.getString("time",sdf.format(new Date(c.getTimeInMillis())))); //从sharedpreferences中读取配置信息
        btn_enclick.setChecked(sharedPreferences.getBoolean("on_off",false));
    }
    private void savedata(){
        editor.putString("time",btn_setclick.getText().toString());
        editor.putBoolean("on_off",btn_enclick.isChecked());
        editor.commit();//提交事务保存数据
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance=this; //用于在shakeAlarm窗口中关闭此activity
        shakeValue=getResources().getString(R.string.shakeSenseValue_2);

        ButtonListener buttonListener=new ButtonListener(); //时间监听器事件

        btn_setclick=(Button)findViewById(R.id.btn_setClock);
        btn_setclick.setOnClickListener(buttonListener);

        btn_enclick=(ToggleButton)findViewById(R.id.btn_enClk);
        btn_enclick.setOnClickListener(buttonListener);

        loaddata(); //加载sharedpreference中保存的数据
    }
    protected void onPause(){ //当mainactivity被暂停失去焦点时保存数据以免丢失
        super.onPause();
        savedata();
    }
    class ButtonListener implements View.OnClickListener {

        private TimePicker timePicker; //选择时间控件

        private PendingIntent pendingIntent;
        private Intent intent;
        AlarmManager alarmManager;//android中的一个全局定时器
        LayoutInflater layoutInflater;
        LinearLayout setAlarmLayout;

        //在类构造方法中加载选择时间对话框的布局
        public ButtonListener(){
            layoutInflater=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            alarmManager=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
            //setAlarmLayout=layoutInflater.inflate(R.layout.alarmDialog,null);
        }

        private void enableClk(){
            timePicker=(TimePicker) findViewById(R.id.timepicker);
            c.set(Calendar.HOUR_OF_DAY,timePicker.getHour()); //设置闹钟hour
            c.set(Calendar.MINUTE,timePicker.getMinute()); //设置闹钟minute
            c.set(Calendar.SECOND,0);  //设置闹钟秒数
            c.set(Calendar.MILLISECOND,0); //设置闹钟毫秒数

            btn_setclick.setText(sdf.format(new Date(c.getTimeInMillis()))); //显示设置的闹钟时间
            intent=new Intent(MainActivity.this,AlarmReceiver.class);
            pendingIntent=PendingIntent.getBroadcast(MainActivity.this,0,intent,0);
            alarmManager.setRepeating(AlarmManager.RTC,c.getTimeInMillis(),24*60*60*1000,pendingIntent);//system.getTimeInMillis得到的时间与c.getTimeInMillis相等时启动pendingIntent对应的组件

        }

        private void disableClk(){
            alarmManager.cancel(pendingIntent);//取消闹钟
        }
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.btn_setClock:
                    layoutInflater.inflate(R.layout.alarmdialog,null);
                    btn_alarmstyle=(ToggleButton)findViewById(R.id.btn_alarmstyle);
                    btn_alarmstyle.setChecked(sharedPreferences.getBoolean("style",false));
                    timePicker=(TimePicker) findViewById(R.id.timepicker);
                    timePicker.setIs24HourView(true);

                    new AlertDialog.Builder(MainActivity.this).setTitle("设置闹钟时间").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            disableClk();
                            enableClk();
                            if (btn_alarmstyle.isChecked())
                            {
                                MainActivity.setAlarmstyle(true);
                            }
                            else {
                                MainActivity.setAlarmstyle(false);
                            }
                            editor.putBoolean("style",btn_alarmstyle.isChecked()); //保存用户设置数据
                            btn_enclick.setChecked(true);
                            Toast.makeText(MainActivity.this,"闹钟设置成功",Toast.LENGTH_LONG).show();
                        }
                    }).setNegativeButton("取消",null).show();
                    break;
                case R.id.btn_enClk:
                    if (btn_enclick.isChecked())
                        enableClk();
                    else disableClk();
                    break;

            }
        }
    }
    //optionsMenu为选项菜单， 该菜单在点击 menu 按键 后会在对应的Activity底部显示出来。
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        SubMenu subMenu=menu.addSubMenu("摇晃灵敏度");//创建选项菜单下的submenu子菜单
        subMenu.add(1,1,1,"温柔");
        subMenu.add(1,1,1,"正常");
        subMenu.add(1,1,1,"用力");
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem menuItem){
            switch (menuItem.getItemId())
            {
                case 1: shakeValue="10";
                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_LONG).show();
                    break;
                case 2:shakeValue="13";
                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_LONG).show();
                    break;
                case 3:shakeValue="15";
                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_LONG).show();
                    break;
                case R.id.menu_about:
                    new AlertDialog.Builder(MainActivity.this).setTitle("关于").setPositiveButton("确定",null).show();
                    break;
                default:
                    break;
            }
        return super.onOptionsItemSelected(menuItem);
    }
}
