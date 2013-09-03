package net.lapasa.rfdhotdealswidget;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.matshofman.saxrssreader.RssItem;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.util.Log;

/**
 * A wrapper for RssItem
 * 
 * @author mlapasa
 * 
 */
public class NewsItem
{
	private static final String TAG = NewsItem.class.getName();
	private RssItem rssItem;
	private static SimpleDateFormat sdfDate = new SimpleDateFormat("MMM d");
	private static SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm aa");
	private Date date;
	private Bitmap bitmap;

	public NewsItem(RssItem rssItem)
	{
		this.rssItem = rssItem;
		date = rssItem.getPubDate();
		Log.d(TAG, getTimeOnly());

		if (getImageUrl() != null)
		{
			bitmap = loadBitmap(getImageUrl());
		}
	}

	public String getFormattedDate(Context c)
	{
		String str = null;
		str = DateUtils.getRelativeTimeSpanString(date.getTime(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS).toString();
		return str.toUpperCase();
	}

	public String getDayOnly()
	{
		return sdfDate.format(date);
	}

	public String getTimeOnly()
	{
		return sdfTime.format(date);
	}

	public String getTitle()
	{
		return rssItem.getTitle();
	}

	public String getBody()
	{
		String workingText = rssItem.getDescription().replaceAll("http.*?\\s", " ");
		workingText = workingText.replace("\n", "").replace("\r", "");
		workingText = workingText.trim();
		return workingText;
	}

	public String getUrl()
	{
		return rssItem.getLink();
	}

	public Bitmap getImage()
	{
		return bitmap;
	}

	public String getImageUrl()
	{
		String imgUrl = null;
		Pattern p = Pattern.compile("(http://)+[\\d\\w[-./]]*(.jpg)+");
		Matcher m = p.matcher(rssItem.getDescription());

		if (m.find())
		{
			imgUrl = m.group(0);
		}

		return imgUrl;
	}

	public static Bitmap loadBitmap(String urlStr)
	{
		try
		{
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			
			
			Bitmap bmp = BitmapFactory.decodeStream(input);
			if (bmp != null)
			{
				bmp = Bitmap.createScaledBitmap(bmp, 200, 200, true);			
			}
			return bmp;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
