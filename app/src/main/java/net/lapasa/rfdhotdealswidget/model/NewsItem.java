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
package net.lapasa.rfdhotdealswidget.model;

import android.content.Context;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.matshofman.saxrssreader.RssItem;

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
	private String title;
	private String url;
	private String body;
	private long unreadFlag = -1;
	private long longDate;
	private long widgetId;
	private String thumbnailUrl;
	
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
			String imgUrlStr = extractImgUrlStr();
			if (imgUrlStr != null)
			{
				setThumbnail(imgUrlStr);
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

	

	public String extractImgUrlStr()
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

	public void setThumbnail(String urlStr)
	{
		this.thumbnailUrl = urlStr;
	}
	
	public String getThumbnail()
	{
		return thumbnailUrl;
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