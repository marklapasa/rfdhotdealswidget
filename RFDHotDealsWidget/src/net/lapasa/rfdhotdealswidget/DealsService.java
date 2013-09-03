package net.lapasa.rfdhotdealswidget;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;

import org.xml.sax.SAXException;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class DealsService extends RemoteViewsService
{
	private static final String TAG = DealsService.class.getName();
	public static SimpleDateFormat sdfFull = new SimpleDateFormat("MMM d h:mm aa");

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "Created DealsService");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		DealsRemoteViewsFactory factory = new DealsRemoteViewsFactory(this.getApplicationContext(), intent);		
		return factory;
//		return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
	}
	
	class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
	{
		private static final String RSS_HOT_DEALS = "http://forums.redflagdeals.com/external.php?type=RSS2&forumids=9";

		private static final int mCount = 10;
		private List<NewsItem> mWidgetItems = new ArrayList<NewsItem>();
		private Context mContext;
		private int mAppWidgetId;

		public StackRemoteViewsFactory(Context context, Intent intent)
		{
			mContext = context;
			mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		public void onCreate()
		{
		}

		public void onDestroy()
		{
			// In onDestroy() you should tear down anything that was setup for your
			// data source,
			// eg. cursors, connections, etc.
			mWidgetItems.clear();
		}

		public int getCount()
		{
			return mCount;
		}
		
		public RemoteViews getViewAtORIGINAL(int position)
		{
			NewsItem newsItem = mWidgetItems.get(position);

			RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.news_item);
			
			

			// Set the cached thumbnail
			Bitmap bitmap = newsItem.getImage();
			if (bitmap != null)
			{
				rv.setImageViewBitmap(R.id.image, bitmap);
			}
			else
			{
				rv.setImageViewBitmap(R.id.image, null);
			}
			
			
			// Set the title
			rv.setTextViewText(R.id.title, newsItem.getTitle());
			
			// Set the first couple of lines of the news item description
			rv.setTextViewText(R.id.body, newsItem.getBody());

			// Set the date
			String dateStrCurrent = newsItem.getFormattedDate(mContext);
			rv.setTextViewText(R.id.date, dateStrCurrent);
			
			

			// Create illusion of grouping by time
			if (position > 0)
			{
				NewsItem prevNewsItem = mWidgetItems.get(position - 1);
				String dateStrPrevious = prevNewsItem.getFormattedDate(mContext);

				if (dateStrCurrent.equals(dateStrPrevious))
				{
					rv.setViewVisibility(R.id.date, View.GONE);
				}
				else
				{
					rv.setViewVisibility(R.id.date, View.VISIBLE);
				}
			}
			else
			{
				rv.setViewVisibility(R.id.date, View.VISIBLE);	
			}

			// Next, we set a fill-intent which will be used to fill-in the pending
			// intent template which is set on the collection view in
			// StackWidgetProvider.

			// ////////////////////////////////////////////
			// Configure Selected Item in a Fill-Intent //
			// ////////////////////////////////////////////
			Bundle extras = new Bundle();

			// Assign the current position associated to this view
			extras.putString(DealsWidgetProvider.SELECTED_URL, newsItem.getUrl());
			extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			Intent fillInIntent = new Intent();
			fillInIntent.putExtras(extras);
			fillInIntent.setData(Uri.parse(fillInIntent.toUri(Intent.URI_INTENT_SCHEME)));
			rv.setOnClickFillInIntent(R.id.newsItem, fillInIntent);

			// Return the remote views object.
			return rv;
		}		

		public RemoteViews getViewAt(int position)
		{
			NewsItem newsItem = mWidgetItems.get(position);
			// position will always range from 0 to getCount() - 1.

			// We construct a remote views item based on our widget item xml file,
			// and set the
			// text based on the position.
			RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.news_item);
			
			// Set the date
			String dateStrCurrent = newsItem.getFormattedDate(mContext);
			rv.setTextViewText(R.id.date, dateStrCurrent);
			
			// Set the first couple of lines of the news item description
			rv.setTextViewText(R.id.body, newsItem.getBody());

			
			
			
			rv.setTextViewText(R.id.title, newsItem.getTitle());
			
//			// Set the cached thumbnail
//			Bitmap bitmap = newsItem.getImage();
//			if (bitmap != null)
//			{
//				rv.setImageViewBitmap(R.id.image, bitmap);
//			}
//			else
//			{
//				rv.setImageViewBitmap(R.id.image, null);
//			}			
			
			// Create illusion of grouping by time
			if (position > 0)
			{
				NewsItem prevNewsItem = mWidgetItems.get(position - 1);
				String dateStrPrevious = prevNewsItem.getFormattedDate(mContext);

				if (dateStrCurrent.equals(dateStrPrevious))
				{
					rv.setViewVisibility(R.id.date, View.GONE);
				}
				else
				{
					rv.setViewVisibility(R.id.date, View.VISIBLE);
				}
			}
			else
			{
				rv.setViewVisibility(R.id.date, View.VISIBLE);	
			}
			

			// Next, we set a fill-intent which will be used to fill-in the pending
			// intent template
			// which is set on the collection view in StackWidgetProvider.
			Bundle extras = new Bundle();
			extras.putString(DealsWidgetProvider.SELECTED_URL, newsItem.getUrl());
			extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			
			
			Intent fillInIntent = new Intent();
			fillInIntent.putExtras(extras);
			rv.setOnClickFillInIntent(R.id.newsItem, fillInIntent);

			// Return the remote views object.
			return rv;
		}

		public RemoteViews getLoadingView()
		{
			// You can create a custom loading view (for instance when getViewAt()
			// is slow.) If you
			// return null here, you will get the default loading view.
			return null;
		}

		public int getViewTypeCount()
		{
			return 1;
		}

		public long getItemId(int position)
		{
			return position;
		}

		public boolean hasStableIds()
		{
			return true;
		}

		public void onDataSetChanged()
		{
			Log.d(TAG, "onDataSetChanged(): Retriving RSS News Items");
			mWidgetItems.clear();
			
			String footerMsg = "Last Updated: " + DealsService.sdfFull.format(new Date())
					+ DealsWidgetProvider.suffix.get(mAppWidgetId);

			URL url;
			try
			{

				SharedPreferences prefs = mContext.getSharedPreferences(DealsWidgetProvider.NAMESPACE + mAppWidgetId, Context.MODE_PRIVATE);
				url = new URL(prefs.getString(mContext.getString(R.string.key_rssfeedurl), RSS_HOT_DEALS));

				RssFeed feed = RssReader.read(url);
				
				/*
				Editor editor = prefs.edit();
				editor.putString(context.getString(R.string.key_edittext_bigtitle), feed.getTitle());
				editor.commit();
				*/

				ArrayList<RssItem> rssItems = feed.getRssItems();
				for (RssItem rssItem : rssItems)
				{
					NewsItem ni = new NewsItem(rssItem);
					mWidgetItems.add(ni);
				}

			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
				footerMsg = "RSS Feed URL Invalid (" + e.getClass().getName() + ")";
			}
			catch (SAXException e)
			{
				e.printStackTrace();
				footerMsg = "Cannot parse RSS Feed (" + e.getClass().getName() + ")";
			}
			catch (IOException e)
			{
				e.printStackTrace();
				footerMsg = "Check Network Connection (" + e.getClass().getName() + ")";
			}

			
			Log.d(TAG, footerMsg);
			updateFooter(footerMsg);
		}
		private void updateFooter(String msg)
		{
			DealsWidgetProvider.sendPendingIntent(
					mContext, 
					DealsWidgetProvider.UPDATE_STATUS_FOOTER, 
					mAppWidgetId, msg);		
		}
		
	}	
}
