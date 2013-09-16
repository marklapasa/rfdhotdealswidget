package net.lapasa.rfdhotdealswidget;

import java.util.Arrays;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RemoteViews;

/**
 * Broadcast receiver responsible for initiating the startup of the Deals Widget (onUpdate)
 * and any other post launch behavior (onReceive)
 * 
 * @author mlapasa
 * 
 */
public class DealsWidgetProvider extends AppWidgetProvider
{
	private static final String TAG = DealsWidgetProvider.class.getName();
	public static final String NAMESPACE = TAG + ":";
	
	private static final String PLEASE_WAIT = "Please Wait...";
	private static final String AUTOMATIC = " (Automatic)";
	private static final String MANUAL = " (Manual)";
	public static SparseArray<String> suffix = new SparseArray<String>();
	public static final String UPDATE_STATUS_FOOTER = "net.lapasa.rfdhotdealswidget.UPDATE_STATUS_FOOTER";			
	public static final int EVERY_FIVE_MINUTES = 300000;
	public static final int EVERY_MINUTE = 60000;
	private static final int DEFAULT_REFRESH_FREQUENCY = 900000;// 15 minutes
	public static final String SELECTED_URL = "net.lapasa.rfdhotdealswidget.SELECTED_ITEM";
	public static final String LAUNCH_URL_ACTION = "net.lapasa.rfdhotdealswidget.LAUNCH_URL_ACTION";
	private static final String REFRESH_ACTION_PREFIX = "net.lapasa.rfdhotdealswidget.REFRESH";
	public static final String REFRESH_ACTION_AUTOMATIC = "net.lapasa.rfdhotdealswidget.REFRESH_ACTION";
	public static final String REFRESH_ACTION_MANUAL = "net.lapasa.rfdhotdealswidget.REFRESH_MANUAL";
	
	private static final String METADATA = "net.lapasa.rfdhotdealswidget";
	private AlarmManager alarms;
	private RemoteViews rv;
	private SharedPreferences prefs;
	private PendingIntent collectionPendingIntent;
	
		

	/**
	 * Whenever the user selects an item from the list, open the browser
	 * 
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		
		int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		Log.d(TAG, "onReceive("+ intent.getAction() + ") on widgetId=" + widgetId);
		String action = intent.getAction();
		
		if (action == null)
		{
			Log.e(TAG, "There is no action on this intent");
			return;
		}
		
		/**
		 * When user selects a row in the list, it will launch the link to the RFD website
		 */
		if (action.equals(LAUNCH_URL_ACTION))
		{
			String targetUrl = intent.getStringExtra(SELECTED_URL);

			Intent i = new Intent(Intent.ACTION_VIEW);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setData(Uri.parse(targetUrl));
			context.startActivity(i);
		}
		else if(action.contains(REFRESH_ACTION_PREFIX))
		{			
			refreshWidget(context, action, widgetId);
		}
		else if(action.equals(UPDATE_STATUS_FOOTER))
		{
			String msg = intent.getStringExtra(METADATA);
			Log.d(TAG, "msg = " + msg);
			updateStatusFooterOnly(context, widgetId, msg);
		}

		super.onReceive(context, intent);
	}




	/**
	 * Refresh the list of RSS news items and timestamp
	 * 
	 * @param context
	 * @param action
	 */
	private void refreshWidget(Context context, String action, int widgetId)
	{
		Log.d(TAG, "refreshWidget("+widgetId+")");
		updateAlarm(context, widgetId);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);		
			
		rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		
		updateStatusFooterOnly(context, widgetId, PLEASE_WAIT);
				
		// Titles in the top left
		updateTitles(widgetId, context);		
		
		// Refresh collection only
		Log.d(TAG, "refreshWidget("+widgetId+")");

		appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.newsfeed);
		
		// Indicator if this refresh was done auto or manually
		updateSuffix(widgetId, action);
				
		appWidgetManager.partiallyUpdateAppWidget(widgetId, rv);
		
		Log.d(TAG, "partiallyUpdateAppWidget("+widgetId +")");
	}


	/**
	 * Update status footer located on the bottom right
	 * @param rv2
	 * @param widgetId
	 */
	private void updateSuffix(int widgetId, String action)
	{
		if (action.equals(REFRESH_ACTION_AUTOMATIC))
		{
			suffix.put(widgetId, AUTOMATIC);
		}
		else if (action.equals(REFRESH_ACTION_MANUAL))
		{
			suffix.put(widgetId, MANUAL);
		}		
	}
	
	
	/**
	 * Generic update status footer
	 * @param context
	 * @param widgetId
	 * @param intent
	 */
	private void updateStatusFooterOnly(Context context, int widgetId, String msg)
	{
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		rv.setTextViewText(R.id.footer, msg);
		appWidgetManager.partiallyUpdateAppWidget(widgetId, rv);
		
	}
	


	/**
	 * Used preferred refresh frequency settings and apply them
	 * @param context
	 * @param widgetId
	 */
	private void updateAlarm(Context context, int widgetId)
	{
		prefs = context.getSharedPreferences(NAMESPACE + widgetId, Context.MODE_PRIVATE);
		String key = context.getString(R.string.key_refresh_frequency);		
		String refreshFrequencyStr = prefs.getString(key, String.valueOf(DEFAULT_REFRESH_FREQUENCY));
		int refreshFrequency = Integer.valueOf(refreshFrequencyStr);
		setRepeatingRefreshAlarm(context, widgetId, refreshFrequency);
	}


	private void updateTitles(int widgetId, Context context)
	{
		prefs = context.getSharedPreferences(NAMESPACE + widgetId, Context.MODE_PRIVATE);

		// Update small text
		String smallTextStr = prefs.getString(
				context.getString(R.string.key_edittext_smalltitle), 
				context.getString(R.string.small_text_default));		
		rv.setTextViewText(R.id.smallText, smallTextStr);
		
		// Update small text
		String bigTextStr = prefs.getString(
				context.getString(R.string.key_edittext_bigtitle), 
				context.getString(R.string.big_text_default));		
		rv.setTextViewText(R.id.bigText, bigTextStr);
	}

	/**
	 * "...called when each App Widget is added to a host (unless you use a
	 * configuration Activity). If your App Widget accepts any user interaction
	 * events, then you need to register the event handlers in this callback
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		ComponentName THIS_WIDGET = new ComponentName(context, DealsWidgetProvider.class);
		int[] widgetIds = appWidgetManager.getAppWidgetIds(THIS_WIDGET);

		Log.d(TAG, "onUpdate("+Arrays.toString(widgetIds)+")");
		
		// update each of the widgets with the remote adapter
		for (int i = 0; i < appWidgetIds.length; i++)
		{
			int widgetId = appWidgetIds[i];
			
			// Create a RemoteViews object that will inflate the main layout of
			// the widget if it doesn't already exist
			rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

			//////////////////////////////
			// Configuring the Service //
			/////////////////////////////
			Intent collectionRefreshIntent = getCollectionRefreshIntent(context, widgetId);

			// Associate the view component that can display a collection with
			// the collection service
			rv.setRemoteAdapter(R.id.newsfeed, collectionRefreshIntent);

			// The empty view is displayed when the collection has no items. It
			// should be a sibling of the collection view.
			rv.setEmptyView(R.id.newsfeed, R.id.empty_view);

			// Here we setup the a pending intent template. Individuals items of
			// a collection cannot setup their own pending intents, instead, the
			// collection as a whole can setup a pending intent template, and
			// the individual items can set a fillInIntent to create unique
			// before on an item to item basis.

			PendingIntent launchIntent = getLaunchIntent(context, widgetId);
			rv.setPendingIntentTemplate(R.id.newsfeed, launchIntent);

			// Configure intent to launch SETTINGS Activity
			Intent configIntent = new Intent(context, ConfigurationActivity.class);
			boolean additionalWidgetFlag = widgetIds.length > 1; 
			configIntent.putExtra(ConfigurationActivity.IS_ADDITIONAL_WIDGET, additionalWidgetFlag);
			configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			configIntent.setData(Uri.parse(configIntent.toUri(Intent.URI_INTENT_SCHEME)));
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, configIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			rv.setOnClickPendingIntent(R.id.settings, pendingIntent);
			
			if (widgetIds.length > 1)
			{
				try
				{
					pendingIntent.send();
				}
				catch (CanceledException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			
			// Configure intent to refresh widget manually
			Intent refreshBtnIntent= new Intent(context, DealsWidgetProvider.class);
			refreshBtnIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			refreshBtnIntent.setAction(DealsWidgetProvider.REFRESH_ACTION_MANUAL);
			refreshBtnIntent.setData(Uri.parse(refreshBtnIntent.toUri(Intent.URI_INTENT_SCHEME)));
			PendingIntent refreshIntent= PendingIntent.getBroadcast(context,0, refreshBtnIntent,PendingIntent.FLAG_CANCEL_CURRENT);
			rv.setOnClickPendingIntent(R.id.refresh, refreshIntent);			


			// Set Alarm to update this widget
			setRepeatingRefreshAlarm(context, appWidgetIds[i], DEFAULT_REFRESH_FREQUENCY);
			
			updateTitles(widgetId, context);
			
			suffix.put(widgetId, AUTOMATIC);
			
			// Commit changes to in RemoteViews to AppWidgetManager
			appWidgetManager.updateAppWidget(appWidgetIds[i], null);
			appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
			
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		
	}

	
	/**
	 * Setup onItemSelected on the newsfeed list. This is only done once when
	 * the widget is added to the device homescreen
	 * 
	 * @param context
	 * @param appWidgetId
	 * @return	PendingIntent that instructs widget to go launch URL
	 */
	private PendingIntent getLaunchIntent(Context context, int appWidgetId)
	{
		// Create Launch Intent
		Intent launchIntent = new Intent(context, DealsWidgetProvider.class);

		// Define action for this intent
		launchIntent.setAction(DealsWidgetProvider.LAUNCH_URL_ACTION);
		launchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);		
		launchIntent.setData(Uri.parse(launchIntent.toUri(Intent.URI_INTENT_SCHEME)));
		return PendingIntent.getBroadcast(context, 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);

	}

	/**
	 * The DealsService will provide the remote views for this widget.
	 * This is only done once when the widget is added to the device homescreen
	 * 
	 * @param context
	 * @param appWidgetId
	 * @return
	 */
	private Intent getCollectionRefreshIntent(Context context, int appWidgetId)
	{
		// Create Intent for Service
		Intent intent = new Intent(context, DealsService.class);

		// Associate this widget ID to the Service's metadata		
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		// When intents are compared, the extras are ignored, so we need to
		// embed the extras into the data so that the extras will not be
		// ignored.
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		
		return intent;

	}

	
	/**
	 * Use the alarm manager to dispatch the pending intent
	 * 
	 * @param context
	 * @param appWidgetId
	 * @param updateRate
	 *            Milliseconds when to refresh next
	 */
	private void setRepeatingRefreshAlarm(Context context, int appWidgetId, int updateRate)
	{
		alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (collectionPendingIntent != null)
		{
			alarms.cancel(collectionPendingIntent);
		}
		if (updateRate > 0)
		{
			collectionPendingIntent = createDealsWidgetPendingIntent(context, REFRESH_ACTION_AUTOMATIC, appWidgetId, null);
			Log.d(TAG, "Setting automatic refresh every " + updateRate + " milliseconds for widgetId = " + appWidgetId);
			alarms.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + updateRate, updateRate, collectionPendingIntent);
		}
	}

	
	/**
	 * Return a PendingIntent that will dispatch a single refresh request
	 * 
	 * @param context
	 * @param action
	 * @param appWidgetId
	 * @return
	 
	public static PendingIntent createUpdatePendingIntent(Context context, String action, int appWidgetId)
	{
		Intent refreshIntent = new Intent(context, DealsWidgetProvider.class);
		refreshIntent.setAction(action);
		refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		refreshIntent.setData(Uri.parse(refreshIntent.toUri(Intent.URI_INTENT_SCHEME)));
		return PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	*/
	
	
	/**
	 * Return a Pending that will will dispatch a request as defined by
	 * the action
	 * 
	 * @param context
	 * @param action	The action the deal widget must handle in it's onReceive
	 * @param targetWidgetId
	 * @return
	 */
	public static PendingIntent createDealsWidgetPendingIntent(Context context, String action, int targetWidgetId, String metadata)
	{
		Intent intent = new Intent(context, DealsWidgetProvider.class);
		intent.setAction(action);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, targetWidgetId);
		if (metadata != null)
		{
			intent.putExtra(METADATA, metadata);
		}
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	
	
	public static void sendPendingIntent(Context context, String action, int targetWidgetId, String metadata)
	{
		PendingIntent pi = createDealsWidgetPendingIntent(context, action, targetWidgetId, metadata);
		
		try
		{
			pi.send();
		}
		catch (CanceledException e)
		{
			if(metadata == null)
			{
				metadata = "<Empty>";
			}
			Log.e(TAG, "Failed to dispatch PendingEvent("+action+"," +targetWidgetId + "," + metadata);
			e.printStackTrace();
		}
	}
	
}
