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

import com.squareup.picasso.Picasso;

import net.lapasa.rfdhotdealswidget.DealsWidgetProvider;
import net.lapasa.rfdhotdealswidget.R;
import net.lapasa.rfdhotdealswidget.Utils;
import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.model.NewsItemsDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mlapasa
 *
 */
class DealsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
	private int targetLayoutId = Utils.getNewsItemLayout();
	private static final String TAG = DealsRemoteViewsFactory.class.getName();
	private Context context;
	private int widgetId;
	private List<NewsItem> list = new ArrayList<NewsItem>();
	private SharedPreferences prefs;
	private NewsItemsDTO dto;


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
	public void onCreate()
	{
		dto = new NewsItemsDTO(context);
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
	 * Returning null will get the default loading view
	 */
	@Override
	public RemoteViews getLoadingView()
	{
		return null;
	}

	/**
	 * Populate the data for each news item
	 */
	@Override
	public RemoteViews getViewAt(int position)
	{
		// Data item
		NewsItem newsItem = list.get(position);

		// List row item layout
		RemoteViews rv = new RemoteViews(context.getPackageName(), targetLayoutId);

		// Change the new indicator tag on the upper right side
		setIndicator(newsItem, rv);

		if (targetLayoutId == R.layout.news_item)
		{
			setThumbnailOnNewsItem(newsItem.getThumbnail(), rv);
		}

		// Set the first couple of lines of the news item description
		rv.setTextViewText(R.id.body, newsItem.getBody());


		// Set the title
		rv.setTextViewText(R.id.title, newsItem.getTitle());


		// Set the date
		String dateStrCurrent = newsItem.getFormattedDate(context);
		rv.setTextViewText(R.id.date, dateStrCurrent);

		// Create illusion of grouping by time
		groupByTime(position, rv, dateStrCurrent);


		// Configure Selected Item in a Fill-Intent //
		setOnItemClickHandler(newsItem, rv);

		// Return the remote views object.
		return rv;
	}

	private void setIndicator(NewsItem newsItem, RemoteViews rv)
	{
		long unreadFlag = newsItem.getUnreadFlag();
		if (unreadFlag == NewsItem.NEW_AND_UNREAD)
		{
			rv.setViewVisibility(R.id.newAndUnreadIndicator, View.VISIBLE);
			rv.setViewVisibility(R.id.readIndicator, View.GONE);
		}
		else if (unreadFlag == NewsItem.READ)
		{
			rv.setViewVisibility(R.id.newAndUnreadIndicator, View.GONE);
			rv.setViewVisibility(R.id.readIndicator, View.VISIBLE);
		}
		else
		{
			rv.setViewVisibility(R.id.newAndUnreadIndicator, View.GONE);
			rv.setViewVisibility(R.id.readIndicator, View.GONE);
		}
	}

	/**
	 * Create illusion of grouping by time
	 *
	 * @param position
	 * @param rv
	 * @param dateStrCurrent
	 */
	private void groupByTime(int position, RemoteViews rv, String dateStrCurrent)
	{
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
	}

	/**
	 * Next, we set a fill-intent which will be used to fill-in the pending
	 * intent template which is set on the collection view in StackWidgetProvider.
	 *
	 * @param newsItem
	 * @param rv
	 */
	private void setOnItemClickHandler(NewsItem newsItem, RemoteViews rv)
	{
		Bundle extras = new Bundle();

		// Assign the current position associated to this view
		extras.putString(DealsWidgetProvider.SELECTED_URL, newsItem.getUrl());
		extras.putLong(DealsWidgetProvider.NEWS_ITEM_ID, newsItem.getId());
		extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

		Intent fillInIntent = new Intent();
		fillInIntent.putExtras(extras);
		fillInIntent.setData(Uri.parse(fillInIntent.toUri(Intent.URI_INTENT_SCHEME)));

		rv.setOnClickFillInIntent(R.id.newsItem, fillInIntent);
	}

	private void setThumbnailOnNewsItem(String thumbnailUrl, RemoteViews rv)
	{
		if (thumbnailUrl != null)
		{
			Bitmap bitmap;
			try
			{
				// TODO: This 200, 200 stuff needs to put into dimens file
				bitmap = Picasso.with(context).load(thumbnailUrl).resize(200, 200).centerInside().get();
				rv.setImageViewBitmap(R.id.image, bitmap);
				rv.setViewVisibility(R.id.image, View.VISIBLE);
			}
			catch (IOException e)
			{
				rv.setImageViewBitmap(R.id.image, null);
				rv.setViewVisibility(R.id.image, View.GONE);
			}
		}
		else
		{
			rv.setImageViewBitmap(R.id.image, null);
			rv.setViewVisibility(R.id.image, View.GONE);
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

	public void onDataSetChanged()
	{
		Log.d(TAG, "onDataSetChanged()...");


		deleteStaleNewsItems();


		// Query for all cached news items that have the targetWidgetId
		List<NewsItem> newsItems = dto.getAllByWidgetId(widgetId);
		if (newsItems.size() > 0)
		{
			Log.i(TAG, "onDataSetChanged(): Persisted data is available, clearing list");
			list.clear();
			list.addAll(newsItems);
		}
		else
		{
			Log.i(TAG, "onDataSetChanged(): No data has been persited; no need to clear list");
		}
		
		
		/*
		Log.i(TAG, "Here are the " + list.size() + " items that will be displayed to the user:");
		
		for (int i = 0; i < list.size() - 1; i++)
		{
			NewsItem tmp = list.get(i);
			Log.d(TAG, list.get(i).getTitle());
		}
		*/
	}


	/**
	 * Initiate call to remove older items
	 */
	private void deleteStaleNewsItems()
	{
		String key = context.getString(R.string.key_purge_threshold);
		String thresholdStr = prefs.getString(key, "604800000"); // 1 Week Default
		long threshold = Long.parseLong(thresholdStr);
		if (threshold > 0)
		{
			dto.removeStale(widgetId, threshold);
		}
	}

	private void updateFooter(String msg)
	{
		DealsWidgetProvider.sendPendingIntent(
				context,
				DealsWidgetProvider.UPDATE_STATUS_FOOTER,
				widgetId, msg);
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
