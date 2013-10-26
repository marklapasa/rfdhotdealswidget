package net.lapasa.rfdhotdealswidget.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NewsItemSQLHelper extends SQLiteOpenHelper
{

	private static final String DATABASE_NAME = "newsItems.db";

	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_NEWSITEMS = "newsItems";

	public static final String ID = "_id";

	public static final String TITLE = "title";

	public static final String LINK = "link";

	public static final String PUB_DATE = "pubDate";

	public static final String DESCRIPTION = "description";

	public static final String IS_UNREAD = "isUnread";

	public static final String THUMBNAIL = "thumbnail";

	private static final String DATABASE_CREATE = "create table " + TABLE_NEWSITEMS + "(" + ID + " integer primary key autoincrement, " + TITLE + " text," + LINK + " text," + PUB_DATE + " long,"
			+ DESCRIPTION + " text," + IS_UNREAD + " long, " + THUMBNAIL + " blob" + ");";

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
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWSITEMS);
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
			THUMBNAIL 
		};
	}

}
