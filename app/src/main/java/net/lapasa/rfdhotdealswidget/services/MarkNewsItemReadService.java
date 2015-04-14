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

import net.lapasa.rfdhotdealswidget.DealsWidgetProvider;
import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.model.NewsItemsDTO;
import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class MarkNewsItemReadService extends IntentService
{
	public static final String ORIGINAL_INTENT = "original_intent";
	private static String TAG = MarkNewsItemReadService.class.getName();
	private Context context;
	private NewsItemsDTO dto;
	private int widgetId;
	private NewsItem newsItem;
	private long newsItemId;

	public MarkNewsItemReadService(String name)
	{
		super(name);
	}

	public MarkNewsItemReadService()
	{
		super(TAG);
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		context = this.getApplicationContext();
		dto = new NewsItemsDTO(context);
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		newsItemId = intent.getLongExtra(DealsWidgetProvider.NEWS_ITEM_ID, -1);

		NewsItem targetNewsItem = dto.getByWidgetIdAndNewsItemId(widgetId, newsItemId);

		if (targetNewsItem.getUnreadFlag() == NewsItem.UNREAD || targetNewsItem.getUnreadFlag() == NewsItem.NEW_AND_UNREAD)
		{
			targetNewsItem.setUnreadFlag(NewsItem.READ);
			dto.update(targetNewsItem);

			// At this point, the service has completed performing it's
			// job. Need to notify the widget to query the datastore
			// and render the widget
			Intent refreshUIintent = new Intent(this.getApplicationContext(), DealsWidgetProvider.class);
			refreshUIintent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			refreshUIintent.putExtra(DealsWidgetProvider.IS_DATA_AVAILABLE, true);
			refreshUIintent.setAction(DealsWidgetProvider.UPDATE_UI);
			refreshUIintent.setData(Uri.parse(refreshUIintent.toUri(Intent.URI_INTENT_SCHEME)));
			sendBroadcast(refreshUIintent);
		}
	}
}
