package net.lapasa.rfdhotdealswidget.model.entities;


import com.orm.SugarRecord;
import com.orm.dsl.Table;

import net.lapasa.rfdhotdealswidget.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Persist the notification for a DealWatchRecordMatch. Delete when the user
 * has dealt with this record.
 */
@Table(name = "PENDING_NOTIFICATIO_RECORD")
public class NotificationRecord extends SugarRecord
{
    public DealWatchRecord getOwner()
    {
        return owner;
    }

    public void setOwner(DealWatchRecord owner)
    {
        this.owner = owner;
    }

    public DealWatchRecord owner;
    private String title;
    private String body;

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    private String url;

    public NotificationRecord()
    {
        // Required empty constructor
    }

    public static List<NotificationRecord> getAllRecords()
    {
        return NotificationRecord.find(NotificationRecord.class, null, null);
    }

    /**
     * Filter news items whose notifications have not been previously persisted
     *
     * @param list  original list
     * @return  modified list that removes items that have been previously persisted
     */
    public List<NewsItem> evaluateDifference(List<NewsItem> list)
    {
        // Fetch all NotificationNewsItems associated to this NotificationRecord
        String whereClause = "owner = ?";
        String[] whereArgs = new String[]{String.valueOf(this.getId())};

        List<NotificationNewsItemRecord> newsItemNotificationRecords = NotificationNewsItemRecord.find(NotificationNewsItemRecord.class, whereClause, null);

        // Make note of news items that already exist for this notification
        List<NewsItem> blacklist = new ArrayList<NewsItem>();
        for (NewsItem newsItem : list)
        {
            for (NotificationNewsItemRecord newsItemNotificationRecord : newsItemNotificationRecords)
            {
                if (newsItem.getId() == newsItemNotificationRecord.getId())
                {
                    blacklist.add(newsItem);
                }
            }
        }

        for (NewsItem itemToBeRemoved : blacklist)
        {
            list.remove(itemToBeRemoved);
        }

        return list;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public List<NotificationNewsItemRecord> fetchNewsItems()
    {
        String whereClause = "owner = ?";
        String[] whereArgs = new String[]{String.valueOf(getId())};
        return NotificationNewsItemRecord.find(NotificationNewsItemRecord.class, whereClause, whereArgs);
    }
}