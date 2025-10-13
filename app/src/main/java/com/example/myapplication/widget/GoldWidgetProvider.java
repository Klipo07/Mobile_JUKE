package com.example.myapplication.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.api.CbrService;

public class GoldWidgetProvider extends AppWidgetProvider {
    
    private static final String ACTION_UPDATE = "com.example.myapplication.widget.UPDATE";
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new android.content.ComponentName(context, GoldWidgetProvider.class));
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }
    
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Создаем RemoteViews для виджета
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_gold);
        
        // Добавляем клик по виджету для открытия приложения
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
        
        // Загружаем курс золота
        CbrService.getInstance().getGoldRate(new CbrService.GoldRateCallback() {
            @Override
            public void onSuccess(double goldRate) {
                views.setTextViewText(R.id.widget_gold_rate, String.format("₽%.2f", goldRate));
                views.setTextViewText(R.id.widget_update_time, 
                        java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(new java.util.Date()));
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
            
            @Override
            public void onError(String error) {
                // Показываем фиксированное значение при ошибке
                views.setTextViewText(R.id.widget_gold_rate, "₽10,491");
                views.setTextViewText(R.id.widget_update_time, "Офлайн");
                appWidgetManager.updateAppWidget(appWidgetId, views);
                android.util.Log.e("GoldWidget", "Error loading gold rate: " + error);
            }
        });
        
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
