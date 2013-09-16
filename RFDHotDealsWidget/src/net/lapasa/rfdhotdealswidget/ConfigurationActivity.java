package net.lapasa.rfdhotdealswidget;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


/**
 * 
 */

/**
 * Configure the widget to do other things other than the default behavior
 * 
 * @author mlapasa
 */
public class ConfigurationActivity extends Activity
{
	private static final String TAG = ConfigurationActivity.class.getName();
	public static String IS_ADDITIONAL_WIDGET = "IS_ADDITIONAL_WIDGET";
	private int widgetId;
	private boolean isAdditionalWidget;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		
		// "First, get the App Widget ID from the Intent that launched the Activity"
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		
		if (extras != null) 
		{
		    widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		    isAdditionalWidget = extras.getBoolean(IS_ADDITIONAL_WIDGET);
		}		
		
		setTitle("Settings");
		
		// Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        ConfigurationFragment f = new ConfigurationFragment();
        
        f.setWidgetId(widgetId);
        mFragmentTransaction.replace(android.R.id.content, f);
        
        mFragmentTransaction.commit();		
        
		// "...then perform App Widget Configuration"
        
	}

	
	
	/**
	 * Tip: When your configuration Activity first opens, set the Activity result to RESULT_CANCELED.
	 * This way, if the user backs-out of the Activity before reaching the end, the App Widget host
	 * is notified that the configuration was cancelled and the App Widget will not be added.
	 */
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();		
		onConfigurationComplete();		
	}
	

	/**
	 * 
	 * "The onUpdate() method will not be called when the App Widget is created (the system will 
	 * not send the ACTION_APPWIDGET_UPDATE broadcast when a configuration Activity is launched). 
	 * It is the responsibility of the configuration Activity to request an update from the 
	 * AppWidgetManager when the App Widget is first created. 	
	 * 
	 * However, onUpdate() will be called for subsequent updates—it is only skipped the first time."

	 */
	private void onConfigurationComplete()
	{
		Log.d(TAG, "onConfigurationComplete()");

		PendingIntent pi = DealsWidgetProvider.createDealsWidgetPendingIntent(this, 
				DealsWidgetProvider.REFRESH_ACTION_AUTOMATIC, widgetId, "Applying Configuration Changes...");
		try
		{
			pi.send();
		}
		catch (CanceledException e)
		{
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
		finish();
	}
}