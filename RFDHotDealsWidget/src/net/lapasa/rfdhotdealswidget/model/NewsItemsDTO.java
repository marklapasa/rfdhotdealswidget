package net.lapasa.rfdhotdealswidget.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.lapasa.rfdhotdealswidget.NewsItem;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class NewsItemsDTO
{
	private SQLiteDatabase database;
	private NewsItemSQLHelper dbHelper;
	
	public NewsItemsDTO(Context context)
	{
		dbHelper = new NewsItemSQLHelper(context);
	}
	
	public void open() throws SQLException
	{
		database = dbHelper.getWritableDatabase();
	}	
	
	public void close()
	{
		dbHelper.close();
	}
	
	
	//	{ ID, TITLE, LINK, PUB_DATE, DESCRIPTION, CONTENT, THUMBNAIL };
	public NewsItem create(NewsItem newsItem)
	{
		open();
		
		/*
		ContentValues cv = new ContentValues();
		cv.put(NewsItemSQLHelper.TITLE, newsItem.getTitle());
		cv.put(NewsItemSQLHelper.LINK, newsItem.getUrl());
		cv.put(NewsItemSQLHelper.PUB_DATE, newsItem.getDateAsLong());
		cv.put(NewsItemSQLHelper.DESCRIPTION, newsItem.getBody());
//		cv.put(NewsItemSQLHelper.CONTENT, newsItem.getContent());
		cv.put(NewsItemSQLHelper.THUMBNAIL, newsItem.getImage());
		
		long id = database.insert(NewsItemSQLHelper.TABLE_NEWSITEMS, null, cv);
		
		Cursor cursor = database.query(
				NewsItemSQLHelper.TABLE_NEWSITEMS,
				NewsItemSQLHelper.getAllColumns(),
				NewsItemSQLHelper.ID + " = " + id, null, null, null, null);
		
		*/
		
		String sql = "INSERT INTO " + NewsItemSQLHelper.TABLE_NEWSITEMS + 
				"(" + 
				NewsItemSQLHelper.TITLE + "," + 
				NewsItemSQLHelper.LINK + "," + 
				NewsItemSQLHelper.PUB_DATE + "," + 
				NewsItemSQLHelper.DESCRIPTION + "," +
				NewsItemSQLHelper.IS_UNREAD + "," +
				NewsItemSQLHelper.THUMBNAIL + ")" + 
				" VALUES(?,?,?,?,?,?)";
		
		SQLiteStatement insertStatment = database.compileStatement(sql);
		insertStatment.clearBindings();
		insertStatment.bindString(1, newsItem.getTitle());
		insertStatment.bindString(2, newsItem.getUrl());
		insertStatment.bindLong(3, newsItem.getDateAsLong());
		insertStatment.bindString(4, newsItem.getBody());
		insertStatment.bindLong(5, newsItem.getUnreadFlag());
		insertStatment.bindBlob(6, newsItem.getImgAsByteArray());
		
		
		long id = insertStatment.executeInsert();
		
		Cursor cursor = getCursorById(id);
		
		NewsItem persistedNewsItem = cursorToNewsItem(cursor);
		cursor.close();
		
		close();
		return persistedNewsItem;
		
	}

	private Cursor getCursorById(long id)
	{
		Cursor cursor = database.query(
				NewsItemSQLHelper.TABLE_NEWSITEMS,
				NewsItemSQLHelper.getAllColumns(),
				NewsItemSQLHelper.ID + " = " + id, null, null, null, null);
		return cursor;
	}

	private NewsItem cursorToNewsItem(Cursor cursor)
	{
		NewsItem newsItem = new NewsItem();
		newsItem.setId(cursor.getLong(0));
		
		newsItem.setTitle(cursor.getString(1));
		newsItem.setUrl(cursor.getString(2));
		newsItem.setDate(cursor.getLong(3));
		newsItem.setBody(cursor.getString(4));
		newsItem.setUnreadFlag(cursor.getLong(5));
		newsItem.setThumbnail(cursor.getBlob(6));
		
		return newsItem;
	}
	
	public List<NewsItem> getAll()
	{
		open();
		
		List<NewsItem> newsItems = new ArrayList<NewsItem>();
		
		Cursor cursor = database.query(
				NewsItemSQLHelper.TABLE_NEWSITEMS,
				NewsItemSQLHelper.getAllColumns(),
				null, null, null, null, NewsItemSQLHelper.PUB_DATE);
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast())
		{
			NewsItem newsItem = cursorToNewsItem(cursor);
			newsItems.add(newsItem);
			cursor.moveToNext();
		}
		
		cursor.close();
		
		Collections.sort(newsItems);
		
		return newsItems;
	}
	
	public void delete(NewsItem newsItem)
	{
		database.delete(NewsItemSQLHelper.TABLE_NEWSITEMS, NewsItemSQLHelper.ID + " = " + newsItem.getId(), null);
	}
	
	public NewsItem update(NewsItem newsItem)
	{
		open();
		
		String sql = "UPDATE " + NewsItemSQLHelper.TABLE_NEWSITEMS + " SET "  +
				NewsItemSQLHelper.TITLE + " = ?," + 
				NewsItemSQLHelper.LINK + " = ?," + 
				NewsItemSQLHelper.PUB_DATE + " = ?," + 
				NewsItemSQLHelper.DESCRIPTION + " = ?," +
				NewsItemSQLHelper.IS_UNREAD + " = ?," +
				NewsItemSQLHelper.THUMBNAIL + " = ?" + 
				" WHERE ID = " + newsItem.getId();
		
		SQLiteStatement updateStatment = database.compileStatement(sql);
		updateStatment.clearBindings();		
		updateStatment.bindString(1, newsItem.getTitle());
		updateStatment.bindString(2, newsItem.getUrl());
		updateStatment.bindLong(3, newsItem.getDateAsLong());
		updateStatment.bindString(4, newsItem.getBody());
		updateStatment.bindLong(5, newsItem.getUnreadFlag());
		updateStatment.bindBlob(6, newsItem.getImgAsByteArray());
		
		long id = updateStatment.executeUpdateDelete();
		
		Cursor cursor = getCursorById(id);
		
		cursor.moveToFirst();
		NewsItem updatedNewsItem = cursorToNewsItem(cursor);
		cursor.close();
		
		close();
		
		return updatedNewsItem;
		
	}
}
