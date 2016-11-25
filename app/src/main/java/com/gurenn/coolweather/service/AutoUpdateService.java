package com.gurenn.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.gurenn.coolweather.receiver.AutoUpdateReceiver;
import com.gurenn.coolweather.util.HttpCallbackListener;
import com.gurenn.coolweather.util.HttpUtil;
import com.gurenn.coolweather.util.Utility;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // 这是8小时的毫秒数
//        int anHour = 8 * 60 * 60 * 1000;
        // 3秒的毫秒数,测试用
        int anHour = 3 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String cityName = sp.getString("city_name", "");
        String address = null;
        try {
            address = "http://v.juhe.cn/weather/index?"
                    + "cityname=" + URLEncoder.encode(cityName, "utf-8")
                    + "&key=2e04850980b6304c62bb397daa208764";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ;
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleWeatherResponse(AutoUpdateService.this, response);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
