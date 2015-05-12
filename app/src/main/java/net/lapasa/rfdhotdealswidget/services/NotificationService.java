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
    /**
     * A single DealWatchRecord can represent multiple NewsItem records
     */
    private Map<DealWatchRecord, List<NewsItem>> map = new HashMap<DealWatchRecord, List<NewsItem>>();
    public List<NotificationRecord> notificationRecords;

    private final List<DealWatchRecord> filters = new ArrayList<DealWatchRecord>();
    private final List<NotificationRecord> pendingNotificationRecords;
    private final Context context;
    public static Pattern discountPattern = Pattern.compile("d+%");

    public void setFilters(List<DealWatchRecord> filters)
    {
        this.filters.clear();
        this.filters.addAll(filters);
    }


    /**
     * Constructor
     *
     * @param context
     */
    public NotificationService(Context context)
    {
        this.context = context;
        pendingNotificationRecords = NotificationRecord.getAllRecords();
        notificationRecords = new ArrayList<NotificationRecord>();
    }

    /**
     * Iterate through each NewsItem and run it through all the DealWatch filters. It is highly likely that an existing
     * Notification has been generated.
     *
     * @param newsItems Assumption is that these are newsItems that not have been persisted before
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
                        // Existing DealWatchRecord maps to brand news NewsItems
                        map.put(key, difference);
                    }
                }
            }

            generateNewNotificationRecords();
        }
    }


    private String getFormattedTitle(int size, String keywords)
    {
        return String.valueOf(size) + " deals found for \"" + keywords + "\"";
    }

    /**
     * In order to generate a NotificationRecord, you need a DealWatchRecord and at least one NewsItem record
     * If a persisted record doesn't exist, then create a corresponding NotificationRecord
     * If it does already exist, check to see if it needs to be updated
     */
    private void generateNewNotificationRecords()
    {
        // Iterate through each DealWatchRecord (key)
        for (DealWatchRecord dealWatchRecord : map.keySet())
        {
            // Retrieve the list of brand new NewsItems (value)
            List<NewsItem> newsItems = map.get(dealWatchRecord);

            if (newsItems.size() == 1)
            {
                // Create notification that would showcase a single news item
                processSingleNotification(dealWatchRecord, newsItems.get(0));
            }
            else if (newsItems.size() >= 2)
            {
                processMultipleNotifications(dealWatchRecord, newsItems);
            }
        }

        // Launch all archived notification items
        launchNotifications();
    }


    private void generateMultipleNotifications(DealWatchRecord dealWatchRecord, List<NewsItem> newsItems)
    {
        // Create notification data that would reflect multiple records
        NotificationRecord newMultiNotificationRecord = new NotificationRecord();
        newMultiNotificationRecord.setId(dealWatchRecord.getId());
        newMultiNotificationRecord.setTitle(getFormattedTitle(newsItems.size(), dealWatchRecord.keywords)); // "4 deals found for 'ssd'"
        newMultiNotificationRecord.setOwner(dealWatchRecord);
        newMultiNotificationRecord.setReadFlag(NotificationRecord.UNREAD);
        newMultiNotificationRecord.save();

        for (NewsItem newsItem : newsItems)
        {
            NotificationNewsItemRecord notificationNewsItemRecord = new NotificationNewsItemRecord();
            notificationNewsItemRecord.setOwner(newMultiNotificationRecord);
            notificationNewsItemRecord.setNewsItemId(newsItem.getId());
            notificationNewsItemRecord.save();
        }
    }


    private void processMultipleNotifications(DealWatchRecord dealWatchRecord, List<NewsItem> newsItems)
    {
        // Get the notification record associated to this dealWatchRecord
        NotificationRecord existingNotification = NotificationRecord.getByDealWatchRecord(dealWatchRecord);

        if (existingNotification != null)
        {

            if (existingNotification.getReadFlag() == NotificationRecord.READ)
            {
                existingNotification.setReadFlag(NotificationRecord.UNREAD);
                existingNotification.save();
            }

            // If the existing notification has the same news item, leave it alone and do not generate a new notification
            if (isNewsItemsTheSame(existingNotification, newsItems))
            {
                return;
            }
            else
            {
                // If the unread notification has a different news item, delete the existing and generate a new notification as the replacement
                existingNotification.delete();
                generateMultipleNotifications(dealWatchRecord, newsItems);
            }


        }
        else
        {
            // Create a brand new notification
            generateMultipleNotifications(dealWatchRecord, newsItems);
        }
    }

    /**
     * Return true if the News items in associated to the notification are the same as the ones
     * provided in the newsItems parameter
     *
     * @param notificationRecord
     * @param newsItems
     * @return
     */
    private boolean isNewsItemsTheSame(NotificationRecord notificationRecord, List<NewsItem> newsItems)
    {
        boolean isSame = true;
        List<NotificationNewsItemRecord> notificationNewsItemRecords = notificationRecord.fetchNewsItemsIds();

        int persistedNewsItemIdSize = notificationNewsItemRecords.size();
        int newsItemIdsSize = newsItems.size();

        if (persistedNewsItemIdSize == newsItemIdsSize)
        {
            // Assumption: Both collections are sorted
            long[] persistedNewsItemIds = new long[persistedNewsItemIdSize];
            for (int i = 0; i < persistedNewsItemIds.length; i++)
            {
                long lhs = notificationNewsItemRecords.get(i).getNewsItemId();
                long rhs = newsItems.get(i).getId();
                if (lhs != rhs)
                {
                    isSame = false;
                    break;
                }
            }
        }
        else
        {
            isSame = false;
        }
        return isSame;
    }

    private void processSingleNotification(DealWatchRecord dealWatchRecord, NewsItem singleNewsItem)
    {
        // Check to see if this notification already exits
        NotificationRecord existingNotification = NotificationRecord.getById(singleNewsItem.getLongDate());

        if (existingNotification != null)
        {
            if (existingNotification.getReadFlag() == NotificationRecord.UNREAD)
            {
                return; // Do nothing because we already have a notification for this filter displayed
            }
            else
            {
                // An existing Notification exists but the filters say there may or may not be new news item data
                String whereClause = "owner = ?";
                String[] whereArgs = new String[]{String.valueOf(existingNotification.getId())};
                List<NotificationNewsItemRecord> existingNewsItems = NotificationNewsItemRecord.find(NotificationNewsItemRecord.class, whereClause, whereArgs);

                if (existingNewsItems != null && existingNewsItems.size() == 1)
                {
                    NotificationNewsItemRecord existingNewsItem = existingNewsItems.get(0);
                    // Assumption: Should never be existingNewsItem = null
                    if (singleNewsItem.getId() == existingNewsItem.getNewsItemId())
                    {
                        // It is the case the persisted notification and it's news item is the same as this would-be notification and it's accompanying news item
                        return;
                    }
                    else
                    {
                        // replace the existing one with a new one
                        existingNotification.delete();
                        generateSingleNotification(dealWatchRecord, singleNewsItem);
                    }
                }
                else
                {
                    generateSingleNotification(dealWatchRecord, singleNewsItem);
                }
            }
        }
        else
        {
            generateSingleNotification(dealWatchRecord, singleNewsItem);
        }
    }

    private void generateSingleNotification(DealWatchRecord dealWatchRecord, NewsItem singleNewsItem)
    {
        // At this point, the news item has been vetted as unique, so proceed with creation
        NotificationRecord newSingleNotificationRecord = new NotificationRecord();
        newSingleNotificationRecord.setId(dealWatchRecord.getId());
        newSingleNotificationRecord.setTitle(composeTitle(singleNewsItem.getTitle(), dealWatchRecord));
        newSingleNotificationRecord.setSubTitle(singleNewsItem.getTitle());
        newSingleNotificationRecord.setBody(singleNewsItem.getBody());
        newSingleNotificationRecord.setUrl(singleNewsItem.getUrl());
        newSingleNotificationRecord.setOwner(dealWatchRecord);
        newSingleNotificationRecord.setReadFlag(NotificationRecord.UNREAD);
        newSingleNotificationRecord.save();

        NotificationNewsItemRecord singleNotificationNewsItemRecord = new NotificationNewsItemRecord();
        singleNotificationNewsItemRecord.setOwner(newSingleNotificationRecord);
        singleNotificationNewsItemRecord.setNewsItemId(singleNewsItem.getId());
        singleNotificationNewsItemRecord.save();
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
        String whereClause = "read_flag = ?";
        String[] whereArgs = new String[]{String.valueOf(NotificationRecord.UNREAD)};
        notificationRecords = NotificationRecord.find(NotificationRecord.class, whereClause, whereArgs);
        new DispatchNotificationCommand(context, notificationRecords).execute();

    }

}
