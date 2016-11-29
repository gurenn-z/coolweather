package com.gurenn.coolweather.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.gurenn.coolweather.db.CoolWeatherDB;
import com.gurenn.coolweather.model.City;
import com.gurenn.coolweather.model.County;
import com.gurenn.coolweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     * @param coolWeatherDB
     * @param response
     * @return
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,
                                                               String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] allProvinces = response.split(",");
            if (allProvinces != null && allProvinces.length > 0) {
                for (String p : allProvinces) {
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    // 将解析出来的数据存储到Province表
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     * @param coolWeatherDB
     * @param response
     * @param provinceId
     * @return
     */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,
                                               String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProcinceId(provinceId);
                    // 将解析出来的数据存储到City表
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     * @param coolWeatherDB
     * @param response
     * @param cityId
     * @return
     */
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,
                                                 String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0) {
                for (String c : allCounties) {
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    // 将解析出来的数据存储到County表
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的JSON数据，并将解析出的数据存储到本地
     * @param context
     * @param response
     */
    public static void handleWeatherResponse(Context context, String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject resBody = jsonObject.getJSONObject("showapi_res_body");
            // 解析城市名
            String cityName = resBody.getJSONObject("cityInfo").getString("c3");
            // 解析预报发布时间
            Date time = new SimpleDateFormat("yyyyMMddHHmmss").parse(resBody.getString("time"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日  HH:mm:ss");
            String publishTime = "预报发布时间: " + sdf.format(time);
            // 解析当前时间
            Date currentTime = new SimpleDateFormat("yyyyMMdd").parse(resBody.getJSONObject("f1").getString("day"));
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日");
            String week = resBody.getJSONObject("f1").getString("weekday");
            String weekDay="";
            switch (week) {
                case "1":weekDay = "一";break;
                case "2":weekDay = "二";break;
                case "3":weekDay = "三";break;
                case "4":weekDay = "四";break;
                case "5":weekDay = "五";break;
                case "6":weekDay = "六";break;
                case "7":weekDay = "日";break;
                default:break;
            }
            String currentDate = sdf2.format(currentTime) + "\t星期" + weekDay;
            // 解析天气描述
            String weatherDesp = resBody.getJSONObject("now").getString("weather");
            // 解析今天温度
            String dayTemp = /*"日间气温：" + */resBody.getJSONObject("f1").getString("day_air_temperature") + "℃";
            String nightTemp = /*"夜晚气温：" + */resBody.getJSONObject("f1").getString("night_air_temperature") + "℃";
            saveWeatherInfo(context, cityName, publishTime, currentDate, weatherDesp, dayTemp, nightTemp);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
      /*  // 使用聚合数据：全国天气预报API
        try {
            JSONObject jsonObject = new JSONObject(response);
            String resultCode = jsonObject.getString("resultcode");
            int errorCode = jsonObject.getInt("error_code");
            if (resultCode != null && resultCode.equals("200")) {
                JSONObject todayWeather = jsonObject.getJSONObject("result").getJSONObject("today");
                JSONObject skWeather = jsonObject.getJSONObject("result").getJSONObject("sk");
                String cityName = todayWeather.getString("city");
                String publishTime = "今日" + skWeather.getString("time") + "发布";
                String currentDate = todayWeather.getString("date_y")
                        + "\t" + todayWeather.getString("week");
                String weatherDesp = todayWeather.getString("weather");

                String[] tempArr = todayWeather.getString("temperature").split("~");
                String temp1 = tempArr[0].substring(0, tempArr[0].indexOf("℃")) + "℃";
                String temp2 = tempArr[1].substring(0, tempArr[1].indexOf("℃")) + "℃";

                saveWeatherInfo(context, cityName, publishTime, currentDate,
                        weatherDesp, temp1, temp2);
            } else {
                saveWeatherInfo(context, "", "", "", "", "", "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * 将服务器返回的所有天气信息存储到SharedPreferences文件中
     * @param context
     * @param cityName
     * @param publishTime
     * @param currentDate
     * @param weatherDesp
     * @param temp1
     * @param temp2
     */
    private static void saveWeatherInfo(Context context, String cityName, String publishTime,
                                        String currentDate, String weatherDesp,
                                        String temp1, String temp2) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                .edit();
        editor.putString("city_name", cityName);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", currentDate);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.commit();
    }
}
