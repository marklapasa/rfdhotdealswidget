package net.lapasa.rfdhotdealswidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;

public class MainActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);

		// "Your widget is now ready for use! Look for it under widgets and drag it to your home screen!""
		// Intent intent = new Intent();
		// intent.setAction("net.lapasa.rfdhotdealswidget.ACTION_WIDGET_RECEIVER");
		// sendBroadcast(intent);
		sendBroadcast(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
		setContentView(R.layout.welcome);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		// GoogleAnalytics.getInstance(this).setDryRun(true);

		// EasyTracker.getInstance(this).activityStart(this); // Add this
		// method.
	}

	@Override
	public void onStop()
	{
		super.onStop();
//		EasyTracker.getInstance(this).activityStop(this); // Add this method.
	}
}
