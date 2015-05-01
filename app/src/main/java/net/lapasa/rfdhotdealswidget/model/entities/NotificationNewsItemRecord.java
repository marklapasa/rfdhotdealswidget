package net.lapasa.rfdhotdealswidget.model.entities;

import com.orm.SugarRecord;

/**
 * A NotificationRecord is made of many of these NewsItemNotificationRecords
 */
public class NotificationNewsItemRecord extends SugarRecord
{
    public NotificationRecord getOwner()
    {
        return owner;
    }

    public void setOwner(NotificationRecord owner)
    {
        this.owner = owner;
    }

    NotificationRecord owner;

    public long getNewsItemId()
    {
        return newsItemId;
    }

    public void setNewsItemId(long newsItemId)
    {
        this.newsItemId = newsItemId;
    }

    long newsItemId;

    public NotificationNewsItemRecord()
    {
        // Required empty constructor
    }

}
