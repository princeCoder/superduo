package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import barqsoft.footballscores.R;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by Prinzly Ngotoum on 10/1/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetScoreDataProvider implements RemoteViewsService.RemoteViewsFactory {

    Cursor mCursor = null;

    Context mContext = null;

    public WidgetScoreDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (mCursor.moveToPosition(position))
            return mCursor.getLong(mCursor.getColumnIndex(DatabaseContract.scores_table._ID));
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.score_widget_list_item);
    }

    @Override
    public RemoteViews getViewAt(int position) {

        if (position == AdapterView.INVALID_POSITION || mCursor == null || !mCursor.moveToPosition(position)) {
            return null;
        }

        RemoteViews mView = new RemoteViews(mContext.getPackageName(),
                R.layout.score_widget_list_item);
        if(mCursor!=null){
            if (mCursor.moveToPosition(position)) {
                mView.setTextViewText(R.id.home_name, mCursor.getString(mCursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL)));
                mView.setTextViewText(R.id.away_name, mCursor.getString(mCursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL)));
                String away_goal=mCursor.getString(mCursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL));
                String home_goal=mCursor.getString(mCursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL));
                mView.setTextViewText(R.id.away_score,Integer.parseInt(away_goal)!=-1?away_goal:"");
                mView.setTextViewText(R.id.home_score, Integer.parseInt(home_goal)!=-1?home_goal:"");
                Bitmap homeCrestImage = null;
                Bitmap awayCrestImage = null;
                String homeCrestUrl = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.scores_table.HOME_CREST_COL));
                String awayCrestUrl = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.scores_table.AWAY_CREST_COL));
                try {
                    homeCrestImage = Glide.with(mContext)
                            .load(homeCrestUrl)
                            .asBitmap()
                            .error(R.drawable.ic_launcher)
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();

                    awayCrestImage = Glide.with(mContext)
                            .load(awayCrestUrl)
                            .asBitmap()
                            .error(R.drawable.ic_launcher)
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                } catch (InterruptedException | ExecutionException e) {
                   // Log.e(LOG_TAG, "Error retrieving large icon from " + homeCrestUrl, e);
                }

                if (homeCrestImage != null) {
                    mView.setImageViewBitmap(R.id.home_crest, homeCrestImage);
                }

                if (awayCrestImage != null) {
                    mView.setImageViewBitmap(R.id.away_crest, awayCrestImage);
                }

                //Handle the click event

                final Intent fillInIntent = new Intent();
                fillInIntent.setAction(WidgetProvider.ACTION_START_ATIVITY);
                mView.setOnClickFillInIntent(R.id.item, fillInIntent);

            }
        }

        return mView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {

        if (mCursor != null) {
            mCursor.close();
        }
        final long identityToken = Binder.clearCallingIdentity();
        initData();
        Binder.restoreCallingIdentity(identityToken);
    }

    private void initData() {
        //1day=86400000 milliseconds
        Date fragmentdate = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat(mContext.getString(R.string.date_format));
        String[] date=new  String[]{mformat.format(fragmentdate)};
        mCursor = mContext.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, DatabaseContract.scores_table.DATE_COL,date , null);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

}