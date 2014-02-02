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

import java.io.ByteArrayOutputStream;
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
public class NewsItem implements Comparable<NewsItem>
{
	public static final long READ = 0;
	public static final long UNREAD = 1;
	public static final long NEW_AND_UNREAD = 2;
	
	
	
	private long id;
	private static final String TAG = NewsItem.class.getName();
	private RssItem rssItem;
	private static SimpleDateFormat sdfDate = new SimpleDateFormat("MMM d");
	private static SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm aa");
	private Date date;
	private Bitmap bitmap;
	private String imgFileName;
	private String title;
	private String url;
	private String body;
	private long unreadFlag = -1;
	private long longDate;
	private long widgetId;
	
	public NewsItem(long widgetId)
	{
		this(null, widgetId);
	}	

	public NewsItem(RssItem rssItem, long widgetId)
	{
		this.rssItem = rssItem;
		this.widgetId = widgetId;
		
		if (rssItem != null)
		{
			title = rssItem.getTitle();
			url = rssItem.getLink();
			setDate(rssItem.getPubDate().getTime());
			body = parseBody(rssItem.getDescription());
			unreadFlag = UNREAD;
//			Log.d(TAG, getTimeOnly());
	
			if (getImageUrl() != null)
			{
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
				{
					bitmap = loadBitmap(getImageUrl());	
				}
			}
		}
	}



	public String getFormattedDate(Context c)
	{
		String str = DateUtils.getRelativeTimeSpanString(getDate().getTime(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS).toString();
		return str.toUpperCase();
	}

	public String getDayOnly()
	{
		return sdfDate.format(getDate());
	}

	public String getTimeOnly()
	{
		return sdfTime.format(getDate());
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setBody(String body)
	{
		this.body = body;
	}

	public String getBody()
	{
		return body;
	}
	
	private String parseBody(String desc)
	{
		String workingText = desc.replaceAll("http.*?\\s", " ");
		workingText = workingText.replace("\n", "").replace("\r", "");
		workingText = workingText.trim();
		return workingText;
	}

	public String getUrl()
	{
		return url;
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
	
	
	/**
	 * Take the URL to an image, download the data to an internal directory
	 * Persist the name associated the File object.
	 * @param urlStr
	 * @return
	 
	public static Uri cacheBitmap(String urlStr)
	{
		Uri uri = null;
		
		try
		{
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			
//			File file = conte
//			OutputStream ouput = new FileOutputStream(file);
			
			Bitmap bmp = BitmapFactory.decodeStream(input);
			if (bmp != null)
			{
				bmp = Bitmap.createScaledBitmap(bmp, 50, 50, true);			
			}
			
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	*/

	public static Bitmap loadBitmap(String urlStr)
	{
		try
		{
			URL url = new URL(urlStr);
			Log.d(TAG, "Loading bitmap from: " + urlStr);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			
			
			Bitmap bmp = BitmapFactory.decodeStream(input);
			bmp.copy(Bitmap.Config.ARGB_4444, false);
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

	public void setImgFileName(String filename)
	{
		this.imgFileName = filename;
	}
	
	public String getImgFileName()
	{
		return this.imgFileName;
	}
	
	public byte[] getImgAsByteArray()
	{
		if (bitmap == null)
		{
			return null;
		}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return stream.toByteArray();
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public void setUrl(String url)
	{
		this.url = url;
	}

	public Date getDate()
	{
		return this.date;
	}

	public void setDate(long date)
	{
		this.date = new Date(date);
		this.setLongDate(date);
	}

	public void setThumbnail(byte[] blob)
	{
		if (blob != null)
		{
			this.bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
		}
		else
		{
			this.bitmap = null;
		}
	}

	@Override
	public int compareTo(NewsItem another)
	{
		return another.getDate().compareTo(getDate());
	}
	
	public void setUnreadFlag(long flag)
	{
		this.unreadFlag = flag;
	}

	public long getUnreadFlag()
	{
		return unreadFlag;
	}

	public long getLongDate()
	{
		return longDate;
	}

	public void setLongDate(long longDate)
	{
		this.longDate = longDate;
	}

	public long getWidgetId()
	{
		return widgetId;
	}

	public void setWidgetId(long widgetId)
	{
		this.widgetId = widgetId;
	}
	
	public String toString(Context context)
	{
		return this.getTitle() + " | " + this.getFormattedDate(context);
	}
}
