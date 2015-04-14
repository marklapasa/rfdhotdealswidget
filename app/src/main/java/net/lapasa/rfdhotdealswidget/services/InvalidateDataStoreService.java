/*******************************************************************************
 * Copyright 2014 Mark Lapasa
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.lapasa.rfdhotdealswidget.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.lapasa.rfdhotdealswidget.DealsWidgetProvider;
import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.R;
import net.lapasa.rfdhotdealswidget.model.NewsItemsDTO;
import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;

import org.xml.sax.SAXException;

import android.app.IntentService;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.androidbook.salbcr.LightedGreenRoom;

public class InvalidateDataStoreService extends IntentService
{
	private static final String RSS_HOT_DEALS = "http://forums.redflagdeals.com/external.php?type=RSS2&forumids=9";
	private static final String TAG = InvalidateDataStoreService.class.getName();
	private int widgetId;
	private NewsItemsDTO dto;
	private SharedPreferences prefs;
	private Context context;

	/**
	 * Default construtor
	 * 
	 * @param name
	 */
	public InvalidateDataStoreService(String name)
	{
		super(name);

	}

	/**
	 * Constructor used when invoked via an intent
	 */
	public InvalidateDataStoreService()
	{
		this(TAG);
	}

	/**
	 * Initialize references to the Lighted Green Room and DTO
	 */
	@Override
	public void onCreate()
	{
		super.onCreate();
		context = this.getApplicationContext();
		LightedGreenRoom.setup(context);
		LightedGreenRoom.s_registerClient();
		dto = new NewsItemsDTO(context);
	}

	/**
	 * Announce that this service is in progress, otherwise must wait
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStart(intent, startId);
		LightedGreenRoom.s_enter();
		return Service.START_NOT_STICKY;
	}

	/**
	 * Initiate remote data fetch workflow
	 */
	@Override
	protected void onHandleIntent(Intent intent)
	{
		try
		{
			Intent broadcastIntent = intent.getParcelableExtra("original_intent");
			handleBroadcastIntent(broadcastIntent);
		}
		finally
		{
			LightedGreenRoom.s_leave();
		}
	}

	/**
	 * Perform long running operation in this method. This will be execute in a
	 * seperate thread
	 * 
	 * @param broadcastIntent
	 */
	protected void handleBroadcastIntent(Intent broadcastIntent)
	{
		widgetId = broadcastIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		prefs = context.getSharedPreferences(DealsWidgetProvider.NAMESPACE + widgetId, Context.MODE_PRIVATE);

		updateFooter("Checking RSS Feed..");

		// Invalidate datastore here
		Log.d(TAG, "handleBroadcastIntent(): Retriving RSS News Items to be cached");

		List<NewsItem> downloadedNewsItems = new ArrayList<NewsItem>();

		String footerMsg = "Last Updated: " + RefreshUIService.sdfFull.format(new Date()) + " " + DealsWidgetProvider.suffix.get(widgetId);

		boolean isDataAvailable = true;

		try
		{

			if (isOnline())
			{

				URL url = getUrl();
				RssFeed feed = RssReader.read(url);

				/*
				 * // Set the feed title Editor editor = prefs.edit();
				 * editor.putString
				 * (context.getString(R.string.key_edittext_bigtitle),
				 * feed.getTitle()); editor.commit();
				 */

				// These are the items that have been downloaded
				ArrayList<RssItem> rssItems = feed.getRssItems();
				for (RssItem rssItem : rssItems)
				{
					NewsItem ni = new NewsItem(rssItem, widgetId);
					Log.i(TAG, ni.toString(context));
					downloadedNewsItems.add(ni);
				}
			}
			else
			{
				throw new IOException();
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

		Log.d(TAG, footerMsg);

		if (isDataAvailable)
		{

			String persistingStr = "Persisting...";
			Log.d(TAG, persistingStr);
			footerMsg = persistingStr;
			updateFooter(footerMsg);

			dto.save(downloadedNewsItems, widgetId);
		}
		else
		{
			// Display Error msg
			updateFooter(footerMsg);
		}

		// At this point, the service has completed performing it's job. Need to
		// notify the widget to query the datastore and render the widget
		Intent refreshUIintent = new Intent(this.getApplicationContext(), DealsWidgetProvider.class);
		refreshUIintent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		refreshUIintent.putExtra(DealsWidgetProvider.IS_DATA_AVAILABLE, isDataAvailable);
		refreshUIintent.setAction(DealsWidgetProvider.UPDATE_UI);
		refreshUIintent.setData(Uri.parse(refreshUIintent.toUri(Intent.URI_INTENT_SCHEME)));
		sendBroadcast(refreshUIintent);

	}

	/**
	 * Once this service has completed it's work, indicate the next process can
	 * take over by shutting down this one
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		LightedGreenRoom.s_unRegisterClient();
	}

	private URL getUrl() throws MalformedURLException
	{
		String url = prefs.getString(this.getApplicationContext().getString(R.string.key_rssfeedurl), RSS_HOT_DEALS);
		return new URL(url);
	}

	private void updateFooter(String msg)
	{
		DealsWidgetProvider.sendPendingIntent(this.getApplicationContext(), DealsWidgetProvider.UPDATE_STATUS_FOOTER, widgetId, msg);
	}

	/**
	 * This service does not do bind
	 */
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	/**
	 * Return true if network is available
	 * 
	 * @return
	 */
	private boolean isOnline()
	{
		try
		{
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		}
		catch (Exception e)
		{
			return false;
		}
	}

}
