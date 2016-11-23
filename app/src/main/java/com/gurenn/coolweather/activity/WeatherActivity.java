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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_weather);

        // 初始化各控件
        initView();

        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            // 有县级代号时就去查询天气
            mPublishText.setText("同步中...");
            mWeatherInfoLayout.setVisibility(View.INVISIBLE);
            mCityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            // 没有县级代号时就直接显示本地天气
            showWeather();
        }
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
    }

    /**
     * 查询县级代号所对应的天气代号
     * @param countyCode
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * 查询天气代号所对应的天气
     * @param weatherCode
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
     * @param address
     * @param type
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        // 从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    // 处理从服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this, response);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
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
        mPublishText.setText("今天" + sp.getString("publish_time", "") + "发布");
        mCurrentDateText.setText(sp.getString("current_date", ""));
        mWeatherInfoLayout.setVisibility(View.VISIBLE);
        mCityNameText.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_refresh_weather:
                mPublishText.setText("同步中...");
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = sp.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:break;
        }
    }
}
