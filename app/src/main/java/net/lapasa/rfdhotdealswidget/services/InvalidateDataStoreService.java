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
import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.Item;

import net.lapasa.rfdhotdealswidget.DealsWidgetProvider;
import net.lapasa.rfdhotdealswidget.R;
import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.model.NewsItemsDTO;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;

public class InvalidateDataStoreService extends IntentService
{
	private static final String RSS_HOT_DEALS = "http://forums.redflagdeals.com/feed/forum/9";
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

		updateFooter(getString(R.string.checking_rss_feed));

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


				// mlapasa: There are better ways of parsing atom feed, this is not one of them but this is quite the hack
				// to get around parsing \n, \r, and \t
				InputStream inputStream = url.openConnection().getInputStream();
				BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
				StringBuilder total = new StringBuilder();
				String line;
				while ((line = r.readLine()) != null) {
					total.append(line).append('\n');
				}
				String feedStr = total.toString();
				feedStr = feedStr.replaceAll("\n","").replaceAll("\r","").replaceAll("\t","").replaceAll("<t>","").replaceAll("</t>", "");

				InputStream stream = new ByteArrayInputStream(feedStr.getBytes(StandardCharsets.UTF_8));



				Feed feed = EarlParser.parseOrThrow(stream, 0);
				Log.i(TAG, "Processing feed: " + feed.getTitle());
				List<Item> rssItems = (List<Item>) feed.getItems();

				/*
				 * // Set the feed title Editor editor = prefs.edit();
				 * editor.putString
				 * (context.getString(R.string.key_edittext_bigtitle),
				 * feed.getTitle()); editor.commit();
				 */

				// These are the items that have been downloaded

				for (Item rssItem : rssItems)
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
		catch (IOException e)
		{
			e.printStackTrace();
			footerMsg = "Check Network Connection (" + e.getClass().getName() + ")";
			isDataAvailable = false;
		}
		catch (DataFormatException e)
		{
			e.printStackTrace();
			footerMsg = "Cannot parse RSS Feed (" + e.getClass().getName() + ")";
			isDataAvailable = false;
		}
		catch (XmlPullParserException e)
		{
			e.printStackTrace();
			footerMsg = "Cannot parse RSS Feed (" + e.getClass().getName() + ")";
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
			updateSyncIcon(DealsWidgetProvider.ERROR);
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

	private void updateSyncIcon(int error)
	{
		DealsWidgetProvider.sendPendingIntent(
				this.getApplicationContext(),
				DealsWidgetProvider.UPDATE_SYNC_ICON, widgetId, String.valueOf(error));
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
