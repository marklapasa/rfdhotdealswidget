package net.lapasa.rfdhotdealswidget;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/**
 * 
 * @author mlapasa
 * 
 */
class DealsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
	private static final String RSS_HOT_DEALS = "http://forums.redflagdeals.com/external.php?type=RSS2&forumids=9";
	private static final String TAG = DealsRemoteViewsFactory.class.getName();
	private Context context;
	private int widgetId;
	private List<NewsItem> list = new ArrayList<NewsItem>();
	private SharedPreferences prefs;

	public DealsRemoteViewsFactory(Context context, Intent intent)
	{
		this.context = context;
		widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		prefs = context.getSharedPreferences(DealsWidgetProvider.NAMESPACE + widgetId, Context.MODE_PRIVATE);
		Log.d(TAG, "Created DealsRemoteViewsFactory for widgetId = " + widgetId);

	}

	/**
	 * Setup any connections/cursors to data source.
	 */
	@Override
	public void onCreate()
	{
		// In onCreate() you setup any connections / cursors to your data
		// source. Heavy lifting,
		// for example downloading or creating content etc, should be deferred
		// to onDataSetChanged()
		// or getViewAt(). Taking more than 20 seconds in this call will result
		// in an ANR.

		// list.add(new NewsItem("#Title0", "#Body0", Calendar.getInstance()));
		// list.add(new NewsItem("#Title1", "#Body1", Calendar.getInstance()));

		// We sleep for 3 seconds here to show how the empty view appears in the
		// interim.
		// The empty view is set in the StackWidgetProvider and should be a
		// sibling of the
		// collection view.
		
		/*
		try
		{
			Thread.sleep(3000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		*/

	}

	public int getCount()
	{
		return list.size();
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	/**
	 * 
	 */
	@Override
	public RemoteViews getLoadingView()
	{
		return null; // Returning null will get the default loading view
	}

	/**
	 * 
	 */
	@Override
	public RemoteViews getViewAt(int position)
	{
		// position will always range from 0 to getCount() - 1.

		// We construct a remote views item based on our widget item xml file,
		// and set the
		// text based on the position.
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.news_item);
		NewsItem newsItem = list.get(position);
		String dateStrCurrent = newsItem.getFormattedDate(context);

		Bitmap bitmap = newsItem.getImage();
		if (bitmap != null)
		{
			rv.setImageViewBitmap(R.id.image, bitmap);
		}
		else
		{
			rv.setImageViewBitmap(R.id.image, null);
		}
		
		
		rv.setTextViewText(R.id.title, newsItem.getTitle());
		rv.setTextViewText(R.id.body, newsItem.getBody());

		rv.setTextViewText(R.id.date, dateStrCurrent);

		// Create illusion of grouping by time
		if (position > 0)
		{
			NewsItem prevNewsItem = list.get(position - 1);
			String dateStrPrevious = prevNewsItem.getFormattedDate(context);

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
		extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		Intent fillInIntent = new Intent();
		fillInIntent.putExtras(extras);
		fillInIntent.setData(Uri.parse(fillInIntent.toUri(Intent.URI_INTENT_SCHEME)));
		rv.setOnClickFillInIntent(R.id.newsItem, fillInIntent);

		// Return the remote views object.
		return rv;
	}

	public int getViewTypeCount()
	{
		return 1;
	}

	public boolean hasStableIds()
	{
		return true;
	}

	/**
	 * Triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
	 * 
	 * This allows a RemoteViewsFactory to respond to data changes by updating
	 * any internal references. Note: expensive tasks can be safely performed
	 * synchronously within this method.
	 * 
	 * In the interim, the old data will be displayed within the widget.
	 */
	public void onDataSetChanged()
	{
		Log.d(TAG, "onDataSetChanged(): Retriving RSS News Items");
		// if offline,
		// Open a connection to the local database
		// Query for all persisted news items and store it into a collection
		// Get

		// else if online
		// Open a connection to the RSS feed
		// Download the RSS Feed
		// Parse the RSS feed into a NewsItem[]
		// Delete all items in database
		// Persist results:List<NewsItem> into database

		// If data is retrived either live online or from local database
		// Clear collection, clear list
		// Update timestamp

		list.clear();
		
		String footerMsg = "Last Updated: " + DealsService.sdfFull.format(new Date())
				+ DealsWidgetProvider.suffix.get(widgetId);

		URL url;
		try
		{
			url = getUrl();

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
				list.add(ni);
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
		

		/*
		 * 
		 * TODO:
		 * List<NewsItem> tmplist = new ArrayList<NewsItem>();
		 * 
		 * // Check online status
		 * 
		 * // If offline, read from DB // parse results
		 * 
		 * // If online, query RSS // parse results // store results in DB
		 * 
		 * // Display results list.clear(); list.addAll(tmplist);
		 */
		
		Log.d(TAG, footerMsg);
		updateFooter(footerMsg);
		
	}
	
	private void updateFooter(String msg)
	{
		DealsWidgetProvider.sendPendingIntent(
				context, 
				DealsWidgetProvider.UPDATE_STATUS_FOOTER, 
				widgetId, msg);		
	}

	private URL getUrl() throws MalformedURLException
	{
		String url = prefs.getString(context.getString(R.string.key_rssfeedurl), RSS_HOT_DEALS);		
		return new URL(url);
	}

	@Override
	public void onDestroy()
	{
		list.clear();
	}
	


}
