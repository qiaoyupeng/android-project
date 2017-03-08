package com.example.joe.shakealarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by joe on 2017/3/1.
 */
public class SplashActivity extends Activity{
    /*
    该activity实现app启动时的动画效果
     */
    private static long SPLASH_DISPLAY_TIME=2000;
    private Handler handler;
    public Intent intent;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        handler=new Handler();

        /*Handler类消息处理机制
        在新启动的线程中发送消息，在主线程中获取、处理消息。
        这里调用postDelayed方法，2s后调用Runnable对象相当于实现了一个2s的计时器。
        */
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        intent=new Intent(SplashActivity.this,MainActivity.class);
                        startActivity(intent);
                        SplashActivity.this.finish();
                    }
                }
                , SPLASH_DISPLAY_TIME);
    }
}
