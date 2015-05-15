package net.lapasa.rfdhotdealswidget.model.entities;


import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.dsl.Table;

import net.lapasa.rfdhotdealswidget.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Persist the notification for a DealWatchRecordMatch. Delete when the user
 * has dealt with this record.
 */
@Table(name = "NOTIFICATION_RECORD")
public class NotificationRecord extends SugarRecord
{
    public static final long UNREAD = 0L;
    public static final long READ = 1L;
    private long readFlag = UNREAD;
    public static final String _ID = "id";
    private DealWatchRecord owner;
    private String subTitle;
    private String title;
    private String body;
    private String url;
    private long count;

    public long getCount()
    {
        return count;
    }

    public void setCount(long count)
    {
        this.count = count;
    }



    @Ignore
    List<NotificationNewsItemRecord> recentNewsItems;









    public List<NotificationNewsItemRecord> getRecentNewsItems()
    {
        return recentNewsItems;
    }

    public void setRecentNewsItems(List<NotificationNewsItemRecord> recentNewsItems)
    {
        this.recentNewsItems = recentNewsItems;
    }








    public DealWatchRecord getOwner()
    {
        return owner;
    }

    public void setOwner(DealWatchRecord owner)
    {
        this.owner = owner;
    }


    public String getSubTitle()
    {
        return subTitle;
    }

    public void setSubTitle(String subTitle)
    {
        this.subTitle = subTitle;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public NotificationRecord()
    {
        // Required empty constructor
    }

    public static List<NotificationRecord> getAllRecords()
    {
        return NotificationRecord.find(NotificationRecord.class, null, new String[]{});
    }

    /**
     * Filter news items whose notificationRecords have not been previously persisted
     *
     * @param tmpList  original list
     * @return  modified list that removes items that have been previously persisted
     */
    public List<NewsItem> evaluateDifference(List<NewsItem> tmpList)
    {
        // Fetch all NotificationNewsItems associated to this NotificationRecord
        String whereClause = "owner = ?";
        String[] whereArgs = new String[]{String.valueOf(this.getId())};

        List<NotificationNewsItemRecord> existingNotificationNewsItemRecords = NotificationNewsItemRecord.find(NotificationNewsItemRecord.class, whereClause, new String[]{});

        List<NewsItem> blacklist = new ArrayList<NewsItem>();
        if (existingNotificationNewsItemRecords != null && existingNotificationNewsItemRecords.size() > 0)
        {
            // Make note of news items that already exist for this notification

            for (NewsItem newsItem : tmpList)
            {
                for (NotificationNewsItemRecord newsItemNotificationRecord : existingNotificationNewsItemRecords)
                {
                    // If this holds true, this news item has already been persisted
                    if (newsItem.getTitle().equals(newsItemNotificationRecord.getTitle()))
                    {
                        blacklist.add(newsItem);
                        break;
                    }
                }
            }
        }
        else
        {
            whereClause = "body = ?";
            String bodyTxt = getBody() == null ? "" : getBody();
            whereArgs = new String[]{bodyTxt};
            List<NotificationRecord> existingNotificationRecords = NotificationRecord.find(NotificationRecord.class, whereClause, whereArgs);

            for (NewsItem newsItem : tmpList)
            {
                for (NotificationRecord existingNotificationRecord : existingNotificationRecords)
                {
                    // If this holds true, this news item has already been persisted
                    if (newsItem.getBody().equals(existingNotificationRecord.getBody()))
                    {
                        blacklist.add(newsItem);
                        break;
                    }
                }
            }
        }

        for (NewsItem itemToBeRemoved : blacklist)
        {
            tmpList.remove(itemToBeRemoved);
        }

        // The remaining news items in this list have not yet been persisted
        return tmpList;
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

    public List<NotificationNewsItemRecord> fetchNewsItemsIds()
    {
        String whereClause = "owner = ?";
        String[] whereArgs = new String[]{String.valueOf(getId())};
        return NotificationNewsItemRecord.find(NotificationNewsItemRecord.class, whereClause, whereArgs, null, "owner", null);
    }

    public static NotificationRecord getById(long id)
    {
        String whereClause = "id = ?";

        String whereArgs = String.valueOf(id);
        List<NotificationRecord> notificationRecords = find(NotificationRecord.class, whereClause, whereArgs);
        boolean a = notificationRecords != null;
        boolean b = notificationRecords.size() == 1;
        if (a && b)
        {
            return notificationRecords.get(0);
        }
        else
        {
            return null;
        }
    }

    public long getReadFlag()
    {
        return readFlag;
    }

    public void setReadFlag(long readFlag)
    {
        this.readFlag = readFlag;
    }

    public static NotificationRecord getByDealWatchRecord(DealWatchRecord dealWatchRecord)
    {

        String whereClause = "owner = ?";
        String[] whereArgs = new String[]{String.valueOf(dealWatchRecord.getId())};
        List<NotificationRecord> notificationRecords = NotificationRecord.find(NotificationRecord.class, whereClause, whereArgs);
        if (notificationRecords.size() == 1)
        {
            return notificationRecords.get(0);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void delete()
    {
        deleteNewsItems();
        super.delete();
    }

    public void deleteNewsItems()
    {
        String whereClause = "owner = ?";
        String[] whereArgs = new String[]{String.valueOf(getId())};
        NotificationNewsItemRecord.deleteAll(NotificationNewsItemRecord.class, whereClause, whereArgs);
    }

    public void updateFilterResultsCount()
    {
        String whereClause = "owner = ?";
        String[] whereArgs = new String[]{String.valueOf(owner.getId())};
        setCount(NotificationNewsItemRecord.count(NotificationNewsItemRecord.class, whereClause, whereArgs));
    }
}