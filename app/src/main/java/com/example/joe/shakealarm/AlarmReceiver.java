package com.example.joe.shakealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by joe on 2017/3/1.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent){
        intent=new Intent(context,Shakephone.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //表示启动的目标组件在新的task中打开
        context.startActivity(intent);
    }
}
