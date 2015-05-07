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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationService
{
    private Pattern discountPattern = Pattern.compile("\\d+%?");

    public List<DealWatchRecord> getFilters()
    {
        return filters;
    }

    public void setFilters(List<DealWatchRecord> filters)
    {
        this.filters.clear();
        this.filters.addAll(filters);
    }

    private final List<DealWatchRecord> filters = new ArrayList<DealWatchRecord>();
    private final List<NotificationRecord> pendingNotificationRecords;
    private final Context context;

    /**
     * A single DealWatchRecord can represent multiple NewsItem records
     */
    private Map<DealWatchRecord, List<NewsItem>> map = new HashMap<DealWatchRecord, List<NewsItem>>();
    public List<NotificationRecord> notificationRecords;

    public NotificationService(Context context)
    {
        this.context = context;
        pendingNotificationRecords = NotificationRecord.getAllRecords();
        notificationRecords = new ArrayList<NotificationRecord>();
    }

    /**
     * Iterate through each NewsItem and run it through all the DealWatch filters
     *
     * @param newsItems
     */
    public void runNotifications(List<NewsItem> newsItems)
    {
        setFilters(DealWatchRecord.getAllRecords());

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
                        map.put(filter, newsItemValues);
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
        return String.valueOf(size) + " deals found for \"" + keywords + "\"";
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
                newSingleNotificationRecord.setTitle(composeTitle(singleNewsItem.getTitle(), dealWatchRecord));
                newSingleNotificationRecord.setBody(singleNewsItem.getBody());
                newSingleNotificationRecord.setUrl(singleNewsItem.getUrl());
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
                newMultiNotificationRecord.setBody("Tap to open Deals Watch List");
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

    /**
     * "ssd" for $100
     * "ssd" at 10% off
     *
     * @param title
     * @param dealWatchRecord
     * @return
     */
    private String composeTitle(String title, DealWatchRecord dealWatchRecord)
    {
        String newTitle = title;
        // Primary - Is there a dollar amount
        Matcher m = DispatchNotificationCommand.pricePattern.matcher(title);
        List<String> amts = new ArrayList<>();
        while(m.find())
        {
            amts.add(m.group());
        }

        if (amts.size() > 0)
        {
            newTitle = "\"" + dealWatchRecord.keywords + "\" for " + amts.get(0);
        }
        else
        {
            // Secondary - Is there  a percentage amount?
            m = discountPattern.matcher(title);
            while(m.find())
            {
                amts.add(m.group());
            }
            if (amts.size() > 0)
            {
                newTitle = "\"" + dealWatchRecord.keywords + "\" at " + amts.get(0) + " off";
            }
        }

        return newTitle;
    }

    private void launchNotifications()
    {
        notificationRecords = NotificationRecord.find(NotificationRecord.class, null, null);
        new DispatchNotificationCommand(context, notificationRecords).execute();

    }

}
