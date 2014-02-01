package net.lapasa.rfdhotdealswidget.services;

import java.text.SimpleDateFormat;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class RefreshUIService extends RemoteViewsService
{
	private static final String TAG = RefreshUIService.class.getName();
	public static SimpleDateFormat sdfFull = new SimpleDateFormat("MMM d h:mm aa");

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		return new DealsRemoteViewsFactory(this.getApplicationContext(), intent);
	}
}