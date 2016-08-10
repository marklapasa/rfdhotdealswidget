package net.lapasa.rfdhotdealswidget.model.entities;

import com.orm.SugarRecord;
import com.orm.dsl.Table;

/**
 * A NotificationRecord is made of many of these NewsItemNotificationRecords
 */
@Table(name = "NOTIFICATION_NEWS_ITEM_RECORD")
public class NotificationNewsItemRecord extends SugarRecord
{

    String body;
    String title;
    String keywords;
    NotificationRecord owner;
    long newsItemId;

    public NotificationNewsItemRecord()
    {
        // Required empty constructor
    }

    public NotificationRecord getOwner()
    {
        return owner;
    }

    public void setOwner(NotificationRecord owner)
    {
        this.owner = owner;
    }

    public long getNewsItemId()
    {
        return newsItemId;
    }

    public void setNewsItemId(long newsItemId)
    {
        this.newsItemId = newsItemId;
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


    public String getKeywords()
    {
        return keywords;
    }

    public void setKeywords(String keywords)
    {
        this.keywords = keywords;
    }

}