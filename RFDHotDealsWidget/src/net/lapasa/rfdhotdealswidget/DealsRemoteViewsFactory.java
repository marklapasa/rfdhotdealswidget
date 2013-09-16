package net.lapasa.rfdhotdealswidget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
	private File directory;

	public DealsRemoteViewsFactory(Context context, Intent intent)
	{
		this.context = context;
		widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		prefs = context.getSharedPreferences(DealsWidgetProvider.NAMESPACE + widgetId, Context.MODE_PRIVATE);
		Log.d(TAG, "Created DealsRemoteViewsFactory for widgetId = " + widgetId);
		
		directory = context.getFilesDir();

	}

	/**
	 * Setup any connections/cursors to data source.
	 */
	public void onCreate(){}

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
		NewsItem newsItem = list.get(position);

		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.news_item);
		
				 
		setIconOnNewsItem(newsItem, rv);
		 
		// Set the title
		rv.setTextViewText(R.id.title, newsItem.getTitle());
		
		// Set the first couple of lines of the news item description
		rv.setTextViewText(R.id.body, newsItem.getBody());

		// Set the date
		String dateStrCurrent = newsItem.getFormattedDate(context);
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

	private void setIconOnNewsItem(NewsItem newsItem, RemoteViews rv)
	{
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
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
		}
		else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
//			String imgUrlStr = newsItem.getImageUrl();
//			
//			if (imgUrlStr != null)
//			{
//				rv.setViewVisibility(R.id.image, View.VISIBLE);
//				newsItem = cacheImgDisk(newsItem);
//				
//				String imgFileName = newsItem.getImgFileName();
//				Log.d(TAG, "imgFileName = " + imgFileName);
//				if (imgFileName != null)
//				{
//					File f = new File(directory, imgFileName);
//					Uri uriFromFile = Uri.fromFile(f);
//					rv.setUri(R.id.image, "setImageURI", uriFromFile);
//				}
//				else
//				{
//					rv.setUri(R.id.image, "setImageURI", null);
//				}			
//			}
//			else
//			{
//				rv.setViewVisibility(R.id.image, View.GONE);
//			}			
		}
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
		List<NewsItem> tmplist = new ArrayList<NewsItem>();
		

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

		
		boolean isDataAvailable = true;
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
				tmplist.add(ni);
			}

		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			footerMsg = "RSS Feed URL Invalid (" + e.getClass().getName() + ")";
			isDataAvailable = false;
		}
		catch (SAXException e)
		{
			e.printStackTrace();
			footerMsg = "Cannot parse RSS Feed (" + e.getClass().getName() + ")";
			isDataAvailable = false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			footerMsg = "Check Network Connection (" + e.getClass().getName() + ")";
			isDataAvailable = false;
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
		
		if (isDataAvailable)
		{
			Log.d(TAG, "Data is available, clearing list");
			list.clear();
			list.addAll(tmplist);
		}
		
		Log.d(TAG, footerMsg);
		updateFooter(footerMsg);
		
		
		
	}
	
	/**
	 * Take the URL out of the NewsItem, download the img, store it as a local
	 * file; Remember the file handler at the in the newsitem
	 * 
	 * @param ni
	 * @return
	 */
	private NewsItem cacheImgDisk(NewsItem ni)
	{
		String urlStr = ni.getImageUrl();

		if (ni.getImgFileName() == null && urlStr != null)
		{
			FileOutputStream fos = null;
			HttpURLConnection connection;
			Bitmap bmp = null;
			try
			{
				URL url = new URL(urlStr);

				connection = (HttpURLConnection) url.openConnection();
				connection.setDoInput(true);
				connection.connect();
				InputStream input = connection.getInputStream();
								
				bmp = BitmapFactory.decodeStream(input);
				bmp.copy(Bitmap.Config.ARGB_4444, false);
				bmp = Bitmap.createScaledBitmap(bmp, 200, 200, false);
				String filename = ni.getImgFileName(); //getLastBitFromUrl(urlStr);
				fos = context.openFileOutput(filename, Context.MODE_WORLD_READABLE);
				bmp.compress(Bitmap.CompressFormat.JPEG, 10, fos);

				ni.setImgFileName(filename);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					fos.close();
					bmp = null;
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return ni;
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
	
	// http://stackoverflow.com/questions/4050087/how-to-obtain-the-last-path-segment-of-an-uri
	public static String getLastBitFromUrl(final String url)
	{
	    return url.replaceFirst(".*/([^/?]+).*", "$1");
	}	


}
