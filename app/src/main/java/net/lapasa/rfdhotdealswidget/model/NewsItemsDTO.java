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
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import net.lapasa.rfdhotdealswidget.services.NotificationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NewsItemsDTO
{
	private static final String TAG = NewsItemsDTO.class.getName();
	private SQLiteDatabase database;
	private NewsItemSQLHelper dbHelper;
	private Context context;
	private NotificationService notificationService;

	public NewsItemsDTO(Context context)
	{
		dbHelper = new NewsItemSQLHelper(context);
		this.context = context;
		notificationService = new NotificationService(context);
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
	 * @param insertStatement
	 * @param newsItem
	 * @return
	 */
	private long persistNewsItem(SQLiteStatement insertStatement, NewsItem newsItem)
	{		
		insertStatement.clearBindings();
		insertStatement.bindString(1, newsItem.getTitle());
		insertStatement.bindString(2, newsItem.getUrl());
		insertStatement.bindLong(3, newsItem.getLongDate());
		insertStatement.bindString(4, newsItem.getBody());
		insertStatement.bindLong(5, newsItem.getUnreadFlag());
		if (newsItem.getThumbnail() != null)
		{
			insertStatement.bindString(6, newsItem.getThumbnail());
		}
		else
		{
			insertStatement.bindNull(6);
		}
		insertStatement.bindLong(7, newsItem.getWidgetId());
		
		return insertStatement.executeInsert();
	}

	private Cursor getCursorById(long id)
	{
		Cursor cursor = database.query(
				NewsItemSQLHelper.TABLE_NEWS_ITEMS,
				NewsItemSQLHelper.getAllColumns(),
				NewsItemSQLHelper.ID + " = " + id, null, null, null, null);
		boolean b = cursor.moveToNext();
		return cursor;
	}

	private NewsItem cursorToNewsItem(Cursor cursor)
	{
		NewsItem newsItem = new NewsItem(cursor.getLong(7));
		
		newsItem.setId(cursor.getLong(0));
		
		newsItem.setTitle(cursor.getString(1));
		newsItem.setUrl(cursor.getString(2));
		long long1 = cursor.getLong(3);
		newsItem.setPubDate(long1);
		String body = cursor.getString(4);
		Log.i(TAG, "Body == " + body);
		newsItem.setBody(body);
		newsItem.setUnreadFlag(cursor.getLong(5));
		newsItem.setThumbnail(cursor.getString(6));
		
		return newsItem;
	}

	/**
	 * This method will query the DB for all news items that match the widget ID
	 * @param widgetId
	 * @return
	 */
	public List<NewsItem> getAllByWidgetId(long widgetId)
	{
		Log.d(TAG, "getAllByWidgetId(" + widgetId + "): Trying to retrieve news items from DB ");
		return find(NewsItemSQLHelper.WIDGET_ID + " = " + widgetId);
	}

	public List<NewsItem> find(String queryStr)
	{
		List<NewsItem> newsItems = new ArrayList<NewsItem>();
		open();

		Cursor cursor = database.query(NewsItemSQLHelper.TABLE_NEWS_ITEMS, NewsItemSQLHelper.getAllColumns(), queryStr, null, null, null, NewsItemSQLHelper.PUB_DATE + " ASC");

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
		if (newsItem.getThumbnail() != null)
		{
			updateStatment.bindString(6, newsItem.getThumbnail());
		}
		else
		{
			updateStatment.bindNull(6);
		}
		updateStatment.bindLong(7, newsItem.getWidgetId());
		
		long id = updateStatment.executeUpdateDelete();
		
		Cursor cursor = getCursorById(id);

		NewsItem updatedNewsItem = null;
		cursor.moveToFirst();

		if (cursor.getCount() > 0)
		{
			updatedNewsItem = cursorToNewsItem(cursor);
		}

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
		Cursor cursor = database.query(NewsItemSQLHelper.TABLE_NEWS_ITEMS, NewsItemSQLHelper.getAllColumns(), whereClause, null, null, null, null);
	
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
			SQLiteStatement insertStatement = NewsItemSQLHelper.getInsertSQL(database);

			for (NewsItem newsItem : bulkNewsItems)
			{
				insertStatement.clearBindings();
				newsItem.setUnreadFlag(NewsItem.NEW_AND_UNREAD);
				long insertId = persistNewsItem(insertStatement, newsItem);
				newsItem.setId(insertId);
				Log.d(TAG, "News Item Record created, id = " + insertId + " for widgetId = " + newsItem.getWidgetId());
			}
			close();

			notificationService.runNotifications(bulkNewsItems);
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
	 * @param recentNewsItems
	 * @return
	 */
	private List<NewsItem> removeDuplicates(List<NewsItem> recentNewsItems, int widgetId)
	{
		int originalSize = recentNewsItems.size();
		List<NewsItem> persistedItems = getAllByWidgetId(widgetId); // Get all old records
		List<NewsItem> flaggedForRemoval = new ArrayList<>();
		

		if(persistedItems.size() > 0)
		{
			markAllAsNotNew(persistedItems);
			NewsItem recentNewsItem = null;
			
			for (int i = originalSize - 1; i >= 0; i--)
			{
				recentNewsItem = recentNewsItems.get(i);
				long recentPubDate = recentNewsItem.getLongDate();
				
				for (NewsItem persistedItem : persistedItems)
				{
					long persistedPubDate = persistedItem.getLongDate();
					if (recentPubDate == persistedPubDate)
					{
						if (recentNewsItem.getUpdateDate() > persistedItem.getUpdateDate())
						{
							flaggedForRemoval.add(persistedItem);
							break;
						}
					}
				}
				
			}
		}

		if (flaggedForRemoval.size() > 0)
		{
			removeFromDB(flaggedForRemoval);
		}
		return recentNewsItems;
		
	}

	private void removeFromDB(List<NewsItem> itemsToRemove)
	{
		long[] idsList = getIdsFromList(itemsToRemove);
		String idsListStr = Arrays.toString(idsList);
		idsListStr = idsListStr.substring(1, idsListStr.length() - 1);

		final String sql = "DELETE FROM " + NewsItemSQLHelper.TABLE_NEWS_ITEMS + " WHERE _id IN (" + idsListStr + ")";
		open();
		SQLiteStatement updateStatment = database.compileStatement(sql);
		long count = updateStatment.executeUpdateDelete();
		close();


	}
	

	private void markAllAsNotNew(List<NewsItem> persistedItems)
	{
		long[] idsList = getIdsFromList(persistedItems);
		String idsListStr = Arrays.toString(idsList);
		idsListStr = idsListStr.substring(1, idsListStr.length() - 1);
		
		final String sql = "UPDATE " + NewsItemSQLHelper.TABLE_NEWS_ITEMS + " SET "  +
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
	 * Open conenction with DB to remove news items for target widget that are older
	 * than target threshold
	 * 
	 * @param widgetId
	 * @param threshold	1d/2d/5d/1w/2w/1m
	 */
	public void removeStale(int widgetId, long threshold)
	{
		long now = new Date().getTime();
		long thresholdDate = now - threshold; // Higher number vs. Lower number
		
		
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

	public void clearAll()
	{
		open();
		database.execSQL("delete from " + NewsItemSQLHelper.TABLE_NEWS_ITEMS);
		close();
	}

	public NewsItem getById(long newsItemId)
	{
		return null;
	}

	public void deleteAllRecordsForWidget(int appWidgetId)
	{
		open();
		String whereClause = NewsItemSQLHelper.WIDGET_ID + " = " + appWidgetId;
		int delete = database.delete(NewsItemSQLHelper.TABLE_NEWS_ITEMS, whereClause, null);
		close();
	}
}
