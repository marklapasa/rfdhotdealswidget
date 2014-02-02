package net.lapasa.rfdhotdealswidget.services;

import net.lapasa.rfdhotdealswidget.DealsWidgetProvider;
import net.lapasa.rfdhotdealswidget.NewsItem;
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
