package com.gurenn.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gurenn.coolweather.R;
import com.gurenn.coolweather.service.AutoUpdateService;
import com.gurenn.coolweather.util.HttpCallbackListener;
import com.gurenn.coolweather.util.HttpUtil;
import com.gurenn.coolweather.util.Utility;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class WeatherActivity extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout mWeatherInfoLayout;

    /**
     * 用于显示城市名
     */
    private TextView mCityNameText;
    /**
     * 用于显示发布时间
     */
    private TextView mPublishText;
    /**
     * 用于显示天气描述信息
     */
    private TextView mWeatherDespText;
    /**
     * 用于显示气温1
     */
    private TextView mTemp1Text;
    /**
     * 用于显示气温2
     */
    private TextView mTemp2Text;
    /**
     * 用于显示当前日期
     */
    private TextView mCurrentDateText;
    /**
     * 切换城市按钮
     */
    private Button mSwitchCityBtn;
    /**
     * 更新天气按钮
     */
    private Button mRefreshWeatherBtn;
    /**
     * 用于显示当查询天气失败时的错误信息
     */
    private TextView mErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_weather);

        // 初始化各控件
        initView();

        String countyName = getIntent().getStringExtra("county_name");
        if (!TextUtils.isEmpty(countyName)) {
            mPublishText.setText("同步中");
            mWeatherInfoLayout.setVisibility(View.INVISIBLE);
        } else {
            showWeather();
        }
        queryWeatherInfo(countyName);
    }

    /**
     * 初始化各控件
     */
    private void initView() {
        mWeatherInfoLayout = (LinearLayout) findViewById(R.id.ll_weather_info);
        mCityNameText = (TextView) findViewById(R.id.tv_city_name);
        mPublishText  = (TextView) findViewById(R.id.tv_publish_text);
        mWeatherDespText = (TextView) findViewById(R.id.tv_weather_desp);
        mTemp1Text = (TextView) findViewById(R.id.tv_temp1);
        mTemp2Text = (TextView) findViewById(R.id.tv_temp2);
        mCurrentDateText = (TextView) findViewById(R.id.tv_current_date);
        mSwitchCityBtn = (Button) findViewById(R.id.btn_switch_city);
        mSwitchCityBtn.setOnClickListener(this);
        mRefreshWeatherBtn = (Button) findViewById(R.id.btn_refresh_weather);
        mRefreshWeatherBtn.setOnClickListener(this);
        mErrorText = (TextView) findViewById(R.id.tv_error);
    }

    /**
     * 根据传入的城市名查询天气信息
     * @param cityName
     */
    private void queryWeatherInfo(String cityName) {

        String address = null;
//        try {
//            address = "http://v.juhe.cn/weather/index?"
//                    + "cityname=" + URLEncoder.encode(cityName, "utf-8")
//                    + "&key=2e04850980b6304c62bb397daa208764";
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        try {
            address = "http://ali-weather.showapi.com/area-to-weather"
                    + "?area=" + URLEncoder.encode(cityName, "utf-8")
                    + "&needAlarm=1"
                    + "&needIndex=1"
                    + "&needMoreDay=1";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleWeatherResponse(WeatherActivity.this, response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showWeather();
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPublishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上
     */
    private void showWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mCityNameText.setText(sp.getString("city_name", ""));
        mTemp1Text.setText(sp.getString("temp1", ""));
        mTemp2Text.setText(sp.getString("temp2", ""));
        mWeatherDespText.setText(sp.getString("weather_desp", ""));
        mPublishText.setText(sp.getString("publish_time", ""));
        mCurrentDateText.setText(sp.getString("current_date", ""));
        mWeatherInfoLayout.setVisibility(View.VISIBLE);
        mErrorText.setVisibility(View.GONE);
//        if (sp.getString("city_name", "").equals("")) {
//            mErrorText.setVisibility(View.VISIBLE);
//            mWeatherInfoLayout.setVisibility(View.GONE);
//        } else {
//            mCityNameText.setText(sp.getString("city_name", ""));
//            mTemp1Text.setText(sp.getString("temp1", ""));
//            mTemp2Text.setText(sp.getString("temp2", ""));
//            mWeatherDespText.setText(sp.getString("weather_desp", ""));
//            mPublishText.setText(sp.getString("publish_time", ""));
//            mCurrentDateText.setText(sp.getString("current_date", ""));
//            mWeatherInfoLayout.setVisibility(View.VISIBLE);
//            mErrorText.setVisibility(View.GONE);
//        }
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_switch_city:
                Intent intent = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_refresh_weather:
                mPublishText.setText("同步中...");
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCity = sp.getString("city_name", "");
                if (!TextUtils.isEmpty(weatherCity)) {
                    queryWeatherInfo(weatherCity);
                }
                break;
            default:break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
        intent.putExtra("from_weather_activity", true);
        intent.putExtra("pos", getIntent().getIntArrayExtra("position"));
        startActivity(intent);
        finish();
    }
}
