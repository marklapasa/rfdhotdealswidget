package net.lapasa.rfdhotdealswidget.services;

import android.content.Context;

import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;
import net.lapasa.rfdhotdealswidget.model.entities.NotificationNewsItemRecord;
import net.lapasa.rfdhotdealswidget.model.entities.NotificationRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationService
{
    private final List<DealWatchRecord> filters;
    private final List<NotificationRecord> pendingNotificationRecords;
    private final Context context;

    /**
     * A single DealWatchRecord can represent multiple NewsItem records
     */
    private Map<DealWatchRecord, List<NewsItem>> map = new HashMap<DealWatchRecord, List<NewsItem>>();

    public NotificationService(Context context)
    {
        this.context = context;
        filters = DealWatchRecord.getAllRecords();
        pendingNotificationRecords = NotificationRecord.getAllRecords();
    }

    /**
     * Iterate through each NewsItem and run it through all the DealWatch filters
     *
     * @param newsItems
     */
    public void prepareNotificationData(List<NewsItem> newsItems)
    {
        for (NewsItem newsItem : newsItems)
        {
            // For each NewsItem, run it through the filters
            for (DealWatchRecord filter : filters)
            {
                // Applying the filter to the newsItem
                if (filter.match(newsItem))
                {
                    List<NewsItem> newsItemValues = map.get(filter);

                    // Initialize this collection if it doesn't already exist
                    if (newsItemValues == null)
                    {
                        newsItemValues = new ArrayList<NewsItem>();
                    }
                    newsItemValues.add(newsItem);
                }
            }
        }

        // Result: Should have a map whose key = DealWatchRecord maps to a list of NewsItems

        if (!map.isEmpty())
        {
            // Iterate through each DealWatchRecord and test for the existance of a NotificationRecord
            for (DealWatchRecord key : map.keySet())
            {
                // For each DealWatchRecord, ensure that we are not about to create a duplicate notification
                List<NewsItem> list = map.get(key);

                for (NotificationRecord notificationRecord : pendingNotificationRecords)
                {
                    List<NewsItem> difference = notificationRecord.evaluateDifference(list);
                    if (difference.size() > 0)
                    {
                        map.put(key, difference);
                    }
                }
            }

            // If a persisted record doesn't exist, then create a corresponding NotificationRecord
            // If it does already exist, check to see if it needs to be updated

            generateNewNotificationRecords();
        }
    }


    private String getFormattedTitle(int size, String keywords)
    {
        return String.valueOf(size) + " deals found for " + keywords;
    }

    /**
     * In order to genreate a NotificationRecord, you need a DealWatchRecord and at least one NewsItem record
     */
    private void generateNewNotificationRecords()
    {
        for (DealWatchRecord dealWatchRecord : map.keySet())
        {
            List<NewsItem> newsItems = map.get(dealWatchRecord);
            if (newsItems.size() == 1)
            {
                // Create notification that would showcase a single news item
                NotificationRecord newSingleNotificationRecord = new NotificationRecord();
                NewsItem singleNewsItem = newsItems.get(0);
                newSingleNotificationRecord.setTitle(singleNewsItem.getTitle());
                newSingleNotificationRecord.setBody(singleNewsItem.getBody());
                newSingleNotificationRecord.save();

                NotificationNewsItemRecord singleNotificationNewsItemRecord = new NotificationNewsItemRecord();
                singleNotificationNewsItemRecord.setOwner(newSingleNotificationRecord);
                singleNotificationNewsItemRecord.setNewsItemId(singleNewsItem.getId());
                singleNotificationNewsItemRecord.save();
            }
            else if (newsItems.size() >= 2)
            {
                // Create notification data that would reflect multiple records
                NotificationRecord newMultiNotificationRecord = new NotificationRecord();
                newMultiNotificationRecord.setTitle(getFormattedTitle(newsItems.size(), dealWatchRecord.keywords)); // "4 deals found for 'ssd'"
                newMultiNotificationRecord.save();

                for (NewsItem newsItem : newsItems)
                {
                    NotificationNewsItemRecord notificationNewsItemRecord = new NotificationNewsItemRecord();
                    notificationNewsItemRecord.setOwner(newMultiNotificationRecord);
                    notificationNewsItemRecord.setNewsItemId(newsItem.getId());
                    notificationNewsItemRecord.save();
                }
            }
        }

        // Launch all archived notification items
        launchNotifications();
    }

    private void launchNotifications()
    {
        List<NotificationRecord> archivedNotifications = NotificationRecord.find(NotificationRecord.class, null, null);
        DispatchNotificationCommand cmd = new DispatchNotificationCommand(context, archivedNotifications);

    }


    public void createUpdateNotifications()
    {

    }

}
