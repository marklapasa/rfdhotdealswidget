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
package net.lapasa.rfdhotdealswidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import com.google.android.gms.analytics.GoogleAnalytics;
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
