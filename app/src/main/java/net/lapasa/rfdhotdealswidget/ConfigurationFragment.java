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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

/**
 * 
 * @author mlapasa
 * 
 */
public class ConfigurationFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
{
	private static String nameSpace = null;
	private int widgetId;

	public void setWidgetId(int widgetId)
	{
		this.widgetId = widgetId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Need to hash the prefences by their widget ID. We do not
		// want to have one preference namespace for many widgets
		String sharedPrefName = DealsWidgetProvider.NAMESPACE + widgetId;

		getPreferenceManager().setSharedPreferencesName(sharedPrefName);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.widget_config);

		configureShareOption();
	}

	private void configureShareOption()
	{
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT,
				"Using the RFD Hot Deals widget on my Android device(s), I am able to get the latest deals on my homescreen!\n\nhttps://play.google.com/store/apps/details?id=net.lapasa.rfdhotdealswidget");
		sendIntent.setType("text/plain");
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "RFD Hot Deals Widget For Android");
		sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		PreferenceScreen preferenceScreen = getPreferenceScreen();
		Preference preference = preferenceScreen.getPreference(0);
		preference.setIntent(sendIntent);
	}

	private String getDefaultSummaryText(String key)
	{
		String key_refresh_frequency = getString(R.string.key_refresh_frequency);
		String key_big_title = getString(R.string.key_edittext_bigtitle);
		String key_small_title = getString(R.string.key_edittext_smalltitle);
		String key_rss_feed = getString(R.string.key_rssfeedurl);

		String summary_refresh_frequency = getString(R.string.summary_refresh_frequency);
		String summary_big_title = getString(R.string.summary_big_title);
		String summary_small_title = getString(R.string.summary_small_title);
		String summary_rss_feed = getString(R.string.summary_rss_feed_url);

		String defaultSummaryText = null;

		if (key.equals(key_refresh_frequency))
		{
			defaultSummaryText = summary_refresh_frequency;
		}
		else if (key.equals(key_big_title))
		{
			defaultSummaryText = summary_big_title;
		}
		else if (key.equals(key_small_title))
		{
			defaultSummaryText = summary_small_title;
		}
		else if (key.equals(key_rss_feed))
		{
			defaultSummaryText = summary_rss_feed;
		}

		return defaultSummaryText;

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
	{
		
		/*
		String value = prefs.getString(key, null);
		Preference targetPref = getPreferenceScreen().findPreference(key);

		StringBuilder b = new StringBuilder();
		b.append(getDefaultSummaryText(key));
		b.append("\n\n");
		b.append("Current Value: " + value);

		targetPref.setSummary(b.toString());
		*/
	}

	@Override
	public void onDestroy()
	{
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

}
