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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NewsItemSQLHelper extends SQLiteOpenHelper
{

	private static final String DATABASE_NAME = "newsItems.db";

	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_NEWS_ITEMS = "newsItems";

	public static final String ID = "_id";

	public static final String TITLE = "title";

	public static final String LINK = "link";

	public static final String PUB_DATE = "pubDate";

	public static final String DESCRIPTION = "description";

	public static final String IS_UNREAD = "isUnread";

	public static final String THUMBNAIL = "thumbnail";
	
	public static final String WIDGET_ID = "widgetId";

	private static final String DATABASE_CREATE = 
			"create table " 
					+ TABLE_NEWS_ITEMS 
					+ "(" 
						+ ID + " integer primary key autoincrement, " 
						+ TITLE + " text," 
						+ LINK + " text," 
						+ PUB_DATE + " long,"
						+ DESCRIPTION + " text," 
						+ IS_UNREAD + " long, " 
						+ THUMBNAIL + " text,"
						+ WIDGET_ID + " long"
					+ ");";

	public NewsItemSQLHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w(NewsItemSQLHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS_ITEMS);
		onCreate(db);
	}

	public static String[] getAllColumns()
	{
		return new String[]
		{ 
			ID, 
			TITLE, 
			LINK, 
			PUB_DATE, 
			DESCRIPTION, 
			IS_UNREAD, 
			THUMBNAIL,
			WIDGET_ID
		};
	}
	
	public static SQLiteStatement getInsertSQL(SQLiteDatabase database)
	{
		String sql = "INSERT INTO " + NewsItemSQLHelper.TABLE_NEWS_ITEMS + 
				"(" + 
				NewsItemSQLHelper.TITLE + "," + 
				NewsItemSQLHelper.LINK + "," + 
				NewsItemSQLHelper.PUB_DATE + "," + 
				NewsItemSQLHelper.DESCRIPTION + "," +
				NewsItemSQLHelper.IS_UNREAD + "," +
				NewsItemSQLHelper.THUMBNAIL + ", " +
				NewsItemSQLHelper.WIDGET_ID + ")" + 
				" VALUES(?,?,?,?,?,?,?)";
		
		return database.compileStatement(sql);
	}

}
