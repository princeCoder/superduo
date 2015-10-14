package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

/**
 * Created by Prinzly Ngotoum on 10/1/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        WidgetScoreDataProvider dataProvider = new WidgetScoreDataProvider(
                getApplicationContext(), intent);
        return dataProvider;
    }
}