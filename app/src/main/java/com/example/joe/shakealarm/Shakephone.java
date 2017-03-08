package com.example.joe.shakealarm;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.spec.ECParameterSpec;
import java.util.List;

/**
 * Created by joe on 2017/3/1.
 */
public class Shakephone extends Activity{

    private SensorManager sensorManager; //传感器管理器
    private Vibrator vibrator; //振动器
    private SensorEventListener sensorEventListener; //传感器事件监听器
    private TextView textView;
    private Chronometer chronometer; //定时器
    private MediaPlayer mediaPlayer;

    private int alertValue=0;
    private boolean wakeup=false; //清醒标志

    private ExitApp exit=new ExitApp();

    private void getWidget(){
        textView=(TextView) findViewById(R.id.shake_sence_value);
        chronometer=(Chronometer) findViewById(R.id.chronometer);
        chronometer.setFormat("从混沌到清醒一共用了：%s秒");
    }

    private void playAlarm() {
        mediaPlayer = new MediaPlayer();

        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);//获得系统默认闹铃资源
        try {
            mediaPlayer.setDataSource(this, alarmUri);
        } catch (IOException E) {
            E.printStackTrace();
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM); //将铃声设置为闹铃
        mediaPlayer.setLooping(true); //闹铃重复

        try {
            mediaPlayer.prepare(); //准备声音
        } catch (IOException E) {
            E.printStackTrace();
        }
        mediaPlayer.start();
    }

    private void startVibrate(){

        long[] pattern={400,800,1200,1600};
        vibrator.vibrate(pattern,2);
    }

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //设置屏幕
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                             | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                             | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                             | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        getWidget();
        chronometer.start();
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);//获得传感器管理对象
        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);

        if(MainActivity.getAlarmstyle())
        {
            playAlarm();
        }
        else {
            startVibrate();
        }
        //如果设备不存在加速度感应器就直接返回
        List<Sensor> sensors=sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors==null || sensors.size()==0)
            return ;

        //传感器监听事件
        sensorEventListener=new SensorEventListener() {
            @Override

            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
            //检测到传感器改变
            public void onSensorChanged(SensorEvent event) {

                int shakeSenseValue=Integer.parseInt(MainActivity.shakeValue);
                float[] values=event.values;

                if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                    //计算与设定的摇晃敏感值的差,values[0]为x轴上的加速度,values[1]为y轴上的加速度,values[2]为z轴上的加速度。
                    int value=(int) Math.max(Math.abs(values[0]),Math.max(Math.abs(values[1]),Math.abs(values[2])))-shakeSenseValue;
                    if(value>0){
                        alertValue+=value;
                        if (alertValue>=100){
                            if (MainActivity.getAlarmstyle()){
                                mediaPlayer.stop();
                            }
                            else {
                                vibrator.cancel();
                            }
                        sensorManager.unregisterListener(sensorEventListener);
                        textView.setTextColor(Color.GRAY);
                        textView.setText("清醒值\n100%\n\n成功起床啦\n");

                        chronometer.stop();
                        chronometer.setVisibility(View.VISIBLE);

                        wakeup=true;
                        }
                        else {
                            textView.setText("清醒值: "+alertValue);
                        }
                    }
                }
            }


        };
        //注册该传感器监听器
        sensorManager.registerListener(sensorEventListener,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),sensorManager.SENSOR_DELAY_NORMAL);
    }

    public boolean onKeyDown(int keycode, KeyEvent keyEvent){
        if(keyEvent.getKeyCode()==KeyEvent.KEYCODE_BACK){
            if(wakeup){
                pressAgainExit();
            }
            else {
                Toast.makeText(this,R.string.unablePressBack,Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    public void pressAgainExit(){
        if (exit.isExit){
            finish();
            MainActivity.instance.finish();
        }
        else {
            Toast.makeText(this,R.string.pressBackAgain,Toast.LENGTH_SHORT).show();
            exit.doExitInOneSecond();
        }
    }
    class ExitApp{
        private boolean isExit=false;
        private Runnable task=new Runnable() {
            @Override
            public void run() {
                isExit=false;
            }
        };
        private void doExitInOneSecond(){
            isExit=true;
            Handler handler=new Handler();
            handler.postDelayed(task,2000); //延迟两秒跳转到task线程中

        }
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

}
