package org.asdtm.goodweather.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;

import org.asdtm.goodweather.ConnectionDetector;
import org.asdtm.goodweather.R;
import org.asdtm.goodweather.WeatherJSONParser;
import org.asdtm.goodweather.WeatherRequest;
import org.asdtm.goodweather.model.Weather;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.LanguageUtil;
import org.asdtm.goodweather.utils.PreferenceUtil;
import org.asdtm.goodweather.utils.Utils;
import org.json.JSONException;

import java.io.IOException;
import java.util.Locale;

public class MoreWidgetService extends IntentService {

    private static final String TAG = "UpdateMoreWidgetService";

    private Weather weather;
    private Context context;
    
    public MoreWidgetService() {
        super(TAG);
    }
    
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
                        
            if (weather == null) {
                return;
            }
            
            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Constants.APP_SETTINGS_UPDATE_SOURCE, "L");
            editor.apply();
            
            String lastUpdate = Utils.setLastUpdateTime(context, AppPreference
                    .saveLastUpdateTimeMillis(context));
            
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            ComponentName widgetComponent = new ComponentName(context,
                                                              LessWidgetProvider.class);

            int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);

            for (int appWidgetId : widgetIds) {
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                                                          R.layout.widget_less_3x1);
                
                remoteViews.setTextViewText(R.id.widget_city, Utils.getCityAndCountry(context));
                remoteViews.setTextViewText(R.id.widget_last_update, lastUpdate);
                widgetManager.updateAppWidget(appWidgetId, remoteViews);
            }
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {
        context = this;
        ConnectionDetector checkNetwork = new ConnectionDetector(this);
        if (!checkNetwork.isNetworkAvailableAndConnected()) {
            return;
        }

        SharedPreferences preferences = getSharedPreferences(Constants.APP_SETTINGS_NAME, 0);
        String latitude = preferences.getString(Constants.APP_SETTINGS_LATITUDE, "51.51");
        String longitude = preferences.getString(Constants.APP_SETTINGS_LONGITUDE, "-0.13");
        String locale = LanguageUtil.getLanguageName(PreferenceUtil.getLanguage(this));
        String units = AppPreference.getTemperatureUnit(this);

        try {
            timerHandler.postDelayed(timerRunnable, 20000);
            String weatherRaw = new WeatherRequest().getItems(latitude, longitude, units,
                                                              locale);
            weather = WeatherJSONParser.getWeather(weatherRaw);
            AppPreference.saveWeather(this, weather);
            timerHandler.removeCallbacks(timerRunnable);
            updateWidget();
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error get weather", e);
            stopSelf();
        }
    }
    
    private void updateWidget() {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this, MoreWidgetProvider.class);

        int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
        for (int appWidgetId : widgetIds) {
            String temperatureScale = Utils.getTemperatureScale(this);
            String speedScale = Utils.getSpeedScale(this);
            String percentSign = getString(R.string.percent_sign);
            String pressureMeasurement = getString(R.string.pressure_measurement);

            String temperature = String.format(Locale.getDefault(), "%.0f",
                                               weather.temperature.getTemp());
            String wind = getString(R.string.wind_label, String.format(Locale.getDefault(),
                                                                       "%.1f",
                                                                       weather.wind.getSpeed()),
                                    speedScale);
            String humidity = getString(R.string.humidity_label,
                                        String.valueOf(weather.currentCondition.getHumidity()),
                                        percentSign);
            String pressure = getString(R.string.pressure_label,
                                        String.format(Locale.getDefault(), "%.1f",
                                                      weather.currentCondition.getPressure()),
                                        pressureMeasurement);
            String cloudiness = getString(R.string.cloudiness_label,
                                          String.valueOf(weather.cloud.getClouds()),
                                          percentSign);
            String iconId = weather.currentWeather.getIdIcon();
            String weatherIcon = Utils.getStrIcon(this, iconId);
            String lastUpdate = Utils.setLastUpdateTime(this, AppPreference
                    .saveLastUpdateTimeMillis(this));

            RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
                                                      R.layout.widget_more_3x3);
            remoteViews.setTextViewText(R.id.widget_city, Utils.getCityAndCountry(this));
            remoteViews.setTextViewText(R.id.widget_temperature, temperature + temperatureScale);
            if(!AppPreference.hideDescription(this))
                remoteViews.setTextViewText(R.id.widget_description,
                                        weather.currentWeather.getDescription());
            else remoteViews.setTextViewText(R.id.widget_description, " ");
            remoteViews.setTextViewText(R.id.widget_wind, wind);
            remoteViews.setTextViewText(R.id.widget_humidity, humidity);
            remoteViews.setTextViewText(R.id.widget_pressure, pressure);
            remoteViews.setTextViewText(R.id.widget_clouds, cloudiness);
            remoteViews.setImageViewBitmap(R.id.widget_icon,
                                           Utils.createWeatherIcon(this, weatherIcon));
            remoteViews.setTextViewText(R.id.widget_last_update, lastUpdate);

            widgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        weather = null;
    }
}
