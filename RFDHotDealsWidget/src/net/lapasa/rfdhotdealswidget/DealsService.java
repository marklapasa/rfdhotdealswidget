package net.lapasa.rfdhotdealswidget;

import java.text.SimpleDateFormat;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

public class DealsService extends RemoteViewsService
{
	private static final String TAG = DealsService.class.getName();
	public static SimpleDateFormat sdfFull = new SimpleDateFormat("MMM d h:mm aa");

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "Created DealsService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		DealsRemoteViewsFactory factory 
			= new DealsRemoteViewsFactory(this.getApplicationContext(), intent);
		
		return factory;
	}
}
