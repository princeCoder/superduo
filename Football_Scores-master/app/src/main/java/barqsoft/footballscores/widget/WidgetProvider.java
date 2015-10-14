package barqsoft.footballscores.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

/**
 * Created by Prinzly Ngotoum on 10/1/15.
 */
public class WidgetProvider extends AppWidgetProvider {

    public static final String ACTION_START_ATIVITY="barqsoft.footballscores.widget.ACTION_START_ATIVITY";
    public static final String ACTION_DATA_UPDATED ="barqsoft.footballscores.sync.ACTION_DATA_UPDATED";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widgetCollectionList);
        }
        else if (intent.getAction().equals(ACTION_START_ATIVITY)) {
            //Open the App when clicking on the collection Item
            Intent intents=new Intent(context, MainActivity.class);
            //Display the activity as a new task
            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intents);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews mView = initViews(context, appWidgetManager, widgetId);
            appWidgetManager.updateAppWidget(widgetId, mView);

            // Adding collection list item handler
            final Intent onItemClick = new Intent(context, WidgetProvider.class);
            onItemClick.setAction(ACTION_START_ATIVITY);

            final PendingIntent onClickPendingIntent = PendingIntent
                    .getBroadcast(context, 0, onItemClick,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            mView.setPendingIntentTemplate(R.id.widgetCollectionList,
                    onClickPendingIntent);

            //In case there is no data
            mView.setEmptyView(R.id.widgetCollectionList, R.id.widget_empty);

            appWidgetManager.updateAppWidget(widgetId, mView);
        }
    }


    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private RemoteViews initViews(Context context,
                                  AppWidgetManager widgetManager, int widgetId) {

        RemoteViews mView = new RemoteViews(context.getPackageName(),
                R.layout.widget_provider_layout);

        Intent intent = new Intent(context, WidgetService.class);
        mView.setRemoteAdapter(widgetId, R.id.widgetCollectionList, intent);

        return mView;
    }
}