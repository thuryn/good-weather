package org.asdtm.goodweather.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import org.asdtm.goodweather.R;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;

import java.util.Locale;


public class LessWidgetProvider extends AbstractWidgetProvider {

    private static final String TAG = "WidgetLessInfo";

    private static final String WIDGET_NAME = "MORE_WIDGET";

    @Override
    protected void preLoadWeather(Context context, RemoteViews remoteViews) {
        SharedPreferences weatherPref = context.getSharedPreferences(Constants.PREF_WEATHER_NAME,
                                                                     Context.MODE_PRIVATE);
        String temperatureScale = Utils.getTemperatureScale(context);

        String temperature = String.format(Locale.getDefault(), "%.0f", weatherPref
                .getFloat(Constants.WEATHER_DATA_TEMPERATURE, 0));
        String description = weatherPref.getString(Constants.WEATHER_DATA_DESCRIPTION, "clear sky");
        String iconId = weatherPref.getString(Constants.WEATHER_DATA_ICON, "01d");
        String weatherIcon = Utils.getStrIcon(context, iconId);
        String lastUpdate = Utils.setLastUpdateTime(context,
                                                    AppPreference.getLastUpdateTimeMillis(context));
        remoteViews.setTextViewText(R.id.widget_city, Utils.getCityAndCountry(context));
        remoteViews.setTextViewText(R.id.widget_temperature,
                                    temperature + temperatureScale);
        if(!AppPreference.hideDescription(context))
            remoteViews.setTextViewText(R.id.widget_description, description);
        else remoteViews.setTextViewText(R.id.widget_description, " ");
        remoteViews.setImageViewBitmap(R.id.widget_icon,
                                       Utils.createWeatherIcon(context, weatherIcon));
        remoteViews.setTextViewText(R.id.widget_last_update, lastUpdate);
    }

    @Override
    protected void setWidgetTheme(Context context, RemoteViews remoteViews) {

        int textColorId = AppPreference.getTextColor(context);
        int backgroundColorId = AppPreference.getBackgroundColor(context);
        int windowHeaderBackgroundColorId = AppPreference.getWindowHeaderBackgroundColorId(context);
        
        remoteViews.setInt(R.id.widget_root, "setBackgroundColor", backgroundColorId);
        remoteViews.setTextColor(R.id.widget_temperature, textColorId);
        remoteViews.setTextColor(R.id.widget_description, textColorId);
        remoteViews.setInt(R.id.header_layout, "setBackgroundColor", windowHeaderBackgroundColorId);
    }

    @Override
    protected int getWidgetLayout() {
        return R.layout.widget_less_3x1;
    }

    @Override
    protected Class<?> getWidgetClass() {
        return LessWidgetProvider.class;
    }

    @Override
    protected String getWidgetName() {
        return WIDGET_NAME;
    }
}
