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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lapasa.rfdhotdealswidget.NewsItem;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class NewsItemsDTO
{
	private static final String TAG = NewsItemsDTO.class.getName();
	private SQLiteDatabase database;
	private NewsItemSQLHelper dbHelper;
	private Context context;
	
	public NewsItemsDTO(Context context)
	{
		dbHelper = new NewsItemSQLHelper(context);
		this.context = context;
	}
	
	public void open() throws SQLException
	{
		database = dbHelper.getWritableDatabase();
	}	
	
	public void close()
	{
		dbHelper.close();
	}
	
	
	public NewsItem create(NewsItem newsItem)
	{
		open();
			
		SQLiteStatement insertStatment = NewsItemSQLHelper.getInsertSQL(database);
		
		long id = persistNewsItem(insertStatment, newsItem);
		
		Cursor cursor = getCursorById(id);
		
		NewsItem persistedNewsItem = cursorToNewsItem(cursor);
		cursor.close();
		
		close();
		return persistedNewsItem;
	}
	
	/**
	 * Inject the fields of the newsItem object into the SQLiteStatement 
	 * 
	 * @param insertStatment
	 * @param newsItem
	 * @return
	 */
	private long persistNewsItem(SQLiteStatement insertStatment, NewsItem newsItem)
	{		
		insertStatment.clearBindings();
		insertStatment.bindString(1, newsItem.getTitle());
		insertStatment.bindString(2, newsItem.getUrl());
		insertStatment.bindLong(3, newsItem.getLongDate());
		insertStatment.bindString(4, newsItem.getBody());
		insertStatment.bindLong(5, newsItem.getUnreadFlag());
		if (newsItem.getImgAsByteArray() != null)
		{
			insertStatment.bindBlob(6, newsItem.getImgAsByteArray());
		}
		else
		{
			insertStatment.bindNull(6);
		}
		insertStatment.bindLong(7, newsItem.getWidgetId());
		
		return insertStatment.executeInsert();
	}

	private Cursor getCursorById(long id)
	{
		Cursor cursor = database.query(
				NewsItemSQLHelper.TABLE_NEWS_ITEMS,
				NewsItemSQLHelper.getAllColumns(),
				NewsItemSQLHelper.ID + " = " + id, null, null, null, null);
		return cursor;
	}

	private NewsItem cursorToNewsItem(Cursor cursor)
	{
		NewsItem newsItem = new NewsItem(cursor.getLong(7));
		
		newsItem.setId(cursor.getLong(0));
		
		newsItem.setTitle(cursor.getString(1));
		newsItem.setUrl(cursor.getString(2));
		long long1 = cursor.getLong(3);
		newsItem.setDate(long1);
		String body = cursor.getString(4);
		Log.i(TAG, "Body == " + body);
		newsItem.setBody(body);
		newsItem.setUnreadFlag(cursor.getLong(5));
		newsItem.setThumbnail(cursor.getBlob(6));
		
		return newsItem;
	}

	/**
	 * This method will query the DB for all news items that match the widget ID
	 * @param widgetId
	 * @return
	 */
	public List<NewsItem> getAll(long widgetId)
	{
		Log.d(TAG, "getAll(" + widgetId + "): Trying to retrieve news items from DB ");
		open();
		
		List<NewsItem> newsItems = new ArrayList<NewsItem>();
		
		Cursor cursor = database.query(
				NewsItemSQLHelper.TABLE_NEWS_ITEMS,
				NewsItemSQLHelper.getAllColumns(),
				NewsItemSQLHelper.WIDGET_ID + " = " + widgetId, null, null, null, NewsItemSQLHelper.PUB_DATE + " ASC");
	
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast())
		{
			NewsItem newsItem = cursorToNewsItem(cursor);
			newsItems.add(newsItem);
			cursor.moveToNext();
		}
		
		cursor.close();		
		close();
		
		Collections.sort(newsItems);
		
		return newsItems;
	}
	
	public void delete(NewsItem newsItem)
	{
		open();
		database.delete(NewsItemSQLHelper.TABLE_NEWS_ITEMS, NewsItemSQLHelper.ID + " = " + newsItem.getId(), null);
		close();
	}
	
	public NewsItem update(NewsItem newsItem)
	{
		open();
		
		String sql = "UPDATE " + NewsItemSQLHelper.TABLE_NEWS_ITEMS + " SET "  +
				NewsItemSQLHelper.TITLE + " = ?," + 
				NewsItemSQLHelper.LINK + " = ?," + 
				NewsItemSQLHelper.PUB_DATE + " = ?," + 
				NewsItemSQLHelper.DESCRIPTION + " = ?," +
				NewsItemSQLHelper.IS_UNREAD + " = ?," +
				NewsItemSQLHelper.THUMBNAIL + " = ?," + 
				NewsItemSQLHelper.WIDGET_ID + " = ?" +
				" WHERE _id = " + newsItem.getId();
		
		SQLiteStatement updateStatment = database.compileStatement(sql);
		updateStatment.clearBindings();		
		updateStatment.bindString(1, newsItem.getTitle());
		updateStatment.bindString(2, newsItem.getUrl());
		long longDate = newsItem.getLongDate();
		updateStatment.bindLong(3, longDate);
		updateStatment.bindString(4, newsItem.getBody());
		updateStatment.bindLong(5, newsItem.getUnreadFlag());
		if (newsItem.getImgAsByteArray() != null)
		{
			updateStatment.bindBlob(6, newsItem.getImgAsByteArray());
		}
		else
		{
			updateStatment.bindNull(6);
		}
		updateStatment.bindLong(7, newsItem.getWidgetId());
		
		long id = updateStatment.executeUpdateDelete();
		
		Cursor cursor = getCursorById(id);
		
		cursor.moveToFirst();
		NewsItem updatedNewsItem = cursorToNewsItem(cursor);
		
		cursor.close();
		close();
		
		return updatedNewsItem;
	}
	
	
	public NewsItem getByWidgetIdAndNewsItemId(int widgetId, long newsItemId)
	{
		Log.d(TAG, "getByWidgetIdAndNewsItemId(" + widgetId + "): Trying to retrieve news items Id = " + newsItemId);
		
		open();
		
		List<NewsItem> newsItems = new ArrayList<NewsItem>();
		
		String whereClause = 
				NewsItemSQLHelper.WIDGET_ID + " = " + widgetId
				+ " AND "
				+ NewsItemSQLHelper.ID + " = " + newsItemId;
		Cursor cursor = database.query(
				NewsItemSQLHelper.TABLE_NEWS_ITEMS,
				NewsItemSQLHelper.getAllColumns(),
				whereClause, null, null, null, null);
	
		cursor.moveToFirst();
		
		NewsItem newsItem = null;
		while(!cursor.isAfterLast())
		{
			newsItem = cursorToNewsItem(cursor);
			break;
		}
		
		cursor.close();		
		close();
		
		return newsItem;
	}
	
	

	/**
	 * Bulk save a collection of NewsItem's
	 * 
	 * @param downloadedNewsItems
	 * @param widgetId 
	 */
	public void save(List<NewsItem> downloadedNewsItems, int widgetId)
	{	
		// Don't persist items that have already been persisted
		List<NewsItem> bulkNewsItems = removeDuplicates(downloadedNewsItems, widgetId);
		
		int newItemsCount = bulkNewsItems.size();
		if (newItemsCount > 0)
		{
			
			
			Log.d(TAG, "Persisting " + newItemsCount + " items for widgetId = " + widgetId);

			open();
			SQLiteStatement insertStatment = NewsItemSQLHelper.getInsertSQL(database);

			for (NewsItem newsItem : bulkNewsItems)
			{
				insertStatment.clearBindings();
				newsItem.setUnreadFlag(NewsItem.NEW_AND_UNREAD);
				long insertId = persistNewsItem(insertStatment, newsItem);
				Log.d(TAG, "News Item Record created, id = " + insertId + " for widgetId = " + newsItem.getWidgetId());
			}
			close();
		}
		else
		{
			Log.d(TAG, "There are no new items to persist");
		}
	}
	
	
	/**
	 * Exclude NewsItem that have already been persisted, based on 
	 * publication date (pubDate)
	 * 
	 * @param newNewsItems
	 * @return
	 */
	private List<NewsItem> removeDuplicates(List<NewsItem> newNewsItems, int widgetId)
	{
		int originalSize = newNewsItems.size();
		List<NewsItem> persistedItems = getAll(widgetId);
		
		
		
		if(persistedItems.size() > 0)
		{
			markAllAsNotNew(persistedItems);
			NewsItem newNewsItem = null;
			
			for (int i = originalSize - 1; i >= 0; i--)
			{
				newNewsItem = newNewsItems.get(i);
				String rawTitle = newNewsItem.getTitle();
				
				
				String pTitleRemove = null;
				
				for (NewsItem nItem : persistedItems)
				{
					String persistedTitle = nItem.getTitle();
					if (persistedTitle.equals(rawTitle))
					{
						pTitleRemove = persistedTitle;
						Log.e(TAG, "persistedTitle->" + newNewsItem.toString(context));
						newNewsItems.remove(i);
						break;
					}
				}
				
				if (pTitleRemove != null)
				{
					persistedItems.remove(pTitleRemove);
				}
			}
			
			Log.d(TAG, "There were " + (originalSize - newNewsItems.size()) + " removed from RSS Response");			
		}
		else
		{
			Log.d(TAG, "No duplicates, these news items are unique");
		}		
			
		return newNewsItems;
		
	}
	

	private void markAllAsNotNew(List<NewsItem> persistedItems)
	{
		long[] idsList = getIdsFromList(persistedItems);
		String idsListStr = Arrays.toString(idsList);
		idsListStr = idsListStr.substring(1, idsListStr.length() - 1);
		
		String sql = "UPDATE " + NewsItemSQLHelper.TABLE_NEWS_ITEMS + " SET "  +
				NewsItemSQLHelper.IS_UNREAD + " = " + NewsItem.UNREAD+
				" WHERE _id IN (" + idsListStr + ") AND " + NewsItemSQLHelper.IS_UNREAD + " = " + NewsItem.NEW_AND_UNREAD;

		open();
		SQLiteStatement updateStatment = database.compileStatement(sql);
		
		long count = updateStatment.executeUpdateDelete();
		Log.d(TAG, "markAllAsNotNew(): Marked " + count + " rows were affected");
		close();
	}

	private long[] getIdsFromList(List<NewsItem> persistedItems)
	{
		long[] idsList = new long[persistedItems.size()];
		for (int i = 0; i < idsList.length; i++)
		{
			idsList[i] = persistedItems.get(i).getId();
		}

		return idsList;
	}

	/**
	 * Exclude NewsItem that have already been persisted, based on 
	 * publication date (pubDate)
	 * 
	 * @param newNewsItems
	 * @return
	 */
	private List<NewsItem> removeDuplicatesOLD(List<NewsItem> newNewsItems, int widgetId)
	{
		int originalSize = newNewsItems.size();
	
		open();		
				
		// Query for a list of cached news items by titles for target WidgetId
		Cursor cursor = database.query(
				NewsItemSQLHelper.TABLE_NEWS_ITEMS,
				new String[]
						{ 
							NewsItemSQLHelper.TITLE,
							NewsItemSQLHelper.WIDGET_ID
						},
				NewsItemSQLHelper.WIDGET_ID + " = " + widgetId, null, null, null, null);
		
		cursor.moveToFirst();
		
		// Store titles into the set
		Set<String> persistedTitles = new HashSet<String>();
		while(!cursor.isAfterLast())
		{
			persistedTitles.add(cursor.getString(0));
			Log.w(TAG, "PERSISTED==> " + cursor.getLong(1) + ":" + cursor.getString(0));
			cursor.moveToNext();
		}
		
		
		
		cursor.close();		
		close();
				
		// Using the persisted title, cross reference them 
		// with the downloaded news items
		
		
		if (persistedTitles.size() > 0)
		{
			NewsItem newNewsItem;
			
			for (int i = originalSize - 1; i >= 0; i--)
			{
				newNewsItem = newNewsItems.get(i);
				String rawTitle = newNewsItem.getTitle();
				
				
				String pTitleRemove = null;
				
				for (String pTitle : persistedTitles)
				{
					if (pTitle.equals(rawTitle))
					{
						pTitleRemove = pTitle;
						Log.e(TAG, "REMOVE->" + newNewsItem.toString(context));
						newNewsItems.remove(i);
						break;
					}
				}
				
				if (pTitleRemove != null)
				{
					persistedTitles.remove(pTitleRemove);
				}
			}
			
			Log.d(TAG, "There were " + (originalSize - newNewsItems.size()) + " removed from RSS Response");			
		}
		else
		{
			Log.d(TAG, "No duplicates, these news items are unique");
		}
		
		return newNewsItems;
	}

	/**
	 * Open conenction with DB to remove news items for target widget that are older
	 * than target threshold
	 * 
	 * @param widgetId
	 * @param threadhold	1d/2d/5d/1w/2w/1m
	 */
	public void removeStale(int widgetId, long threshold)
	{
		long now = new Date().getTime();
		long thresholdDate = now - threshold;
		
		
		Log.d(TAG, "Removing stale items for widgetID = " + widgetId);
		String whereClause = 
				NewsItemSQLHelper.WIDGET_ID + " = " + widgetId
				+ " AND "
				+ NewsItemSQLHelper.PUB_DATE + " <= " + thresholdDate;
		
		open();
		int rowsDeleted = database.delete(NewsItemSQLHelper.TABLE_NEWS_ITEMS, whereClause, null);
		close();
		
		Log.d(TAG, "removeStale(widgetId = " +widgetId + ", threshold=" + threshold + "): " + rowsDeleted + " items were deleted from the DB");
		
		
	}
    
}
