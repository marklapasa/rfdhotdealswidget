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
import java.util.Set;
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
    private NotificationRecord notificationRecord;

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
        // Pull in all available Deal Watch Records
        setFilters(DealWatchRecord.getAllRecords());

        for (NewsItem newsItem : newsItems)
        {
            // Evaluate how what news items belong with a deal watch filter
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
                        // Existing DealWatchRecord maps to brand new NewsItems
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

    private void generateNewNotificationRecords()
    {
        Set<DealWatchRecord> dealWatchRecords = map.keySet();
        if(dealWatchRecords.size() == 1)
        {
            DealWatchRecord[] oneItemArray = new DealWatchRecord[1];
            dealWatchRecords.toArray(oneItemArray);
            DealWatchRecord dealWatchRecord = oneItemArray[0];

            List<NewsItem> newsItems = map.get(dealWatchRecord);
            if (newsItems.size() == 1)
            {
                // Scenario 1: One Deal Watch Record match to 1 News Item Record
                processSingleNotification(dealWatchRecord, newsItems.get(0));
            }
            else
            {
                // Scenario 2: One Deal Watch Record matches many News Item Records
                processDealDigestNotification();
            }
        }
        else if (dealWatchRecords.size() > 1)
        {
            // Scenario 3: Many Deal Watch Records matches many News Item Records
            // Scenario 4: Many Deal Watch Records matches one News Item Record
            processDealDigestNotification();
        }


        if (notificationRecord != null)
        {
            new DispatchNotificationCommand(context, notificationRecord).execute();
        }
    }


    /**
     * Create a single Notification that represents multiple DealWatch and NewsItem records
     */
    private void processDealDigestNotification()
    {
        NotificationRecord mergedRecord = new NotificationRecord();
        mergedRecord.save();

        // Set title - 42 deals found for "a", "b", and "c"
        StringBuilder keywordsSuffixStringBuilder = new StringBuilder();
        int dealSum = 0;
        int loopCounter = 0;

        int size = map.keySet().size();
        for (DealWatchRecord dealWatchRecord : map.keySet())
        {

            List<NewsItem> newsItems = map.get(dealWatchRecord);

            // Append to deal digest
            dealSum += newsItems.size();
            createNotificationNewsItems(mergedRecord, dealWatchRecord, newsItems);

            keywordsSuffixStringBuilder.append("\"" + dealWatchRecord.keywords + "\"");

            if (size != 1)
            {
                if (size == 2)
                {
                    if (loopCounter == 0)
                    {
                        keywordsSuffixStringBuilder.append(" and ");
                    }
                }
                else if (loopCounter < size)
                {
                    keywordsSuffixStringBuilder.append(", ");

                    if (loopCounter == size - 2)
                    {
                        keywordsSuffixStringBuilder.append("and ");
                    }
                }
            }
            else
            {
                mergedRecord.setOwner(dealWatchRecord);
            }

            loopCounter++;

        }

        mergedRecord.setTitle(dealSum + " deals found for " + keywordsSuffixStringBuilder.toString());
        mergedRecord.setBody("Tap and drag to expand");
        mergedRecord.save();

        notificationRecord = mergedRecord;
    }

    /**
     * Append DealWatchRecord and NewsItemRecords in to this one notification
     *
     * @param mergedRecord
     * @param dealWatchRecord
     * @param newsItems
     */
    private void createNotificationNewsItems(NotificationRecord mergedRecord, DealWatchRecord dealWatchRecord, List<NewsItem> newsItems)
    {
        List<NotificationNewsItemRecord> recentNewsItems = new ArrayList<NotificationNewsItemRecord>();

        for (NewsItem newsItem : newsItems)
        {
            NotificationNewsItemRecord notificationNewsItemRecord = new NotificationNewsItemRecord();
            notificationNewsItemRecord.setOwner(mergedRecord);
            notificationNewsItemRecord.setNewsItemId(newsItem.getId());
            notificationNewsItemRecord.setTitle(newsItem.getTitle());
            notificationNewsItemRecord.setBody(newsItem.getBody());
            notificationNewsItemRecord.setKeywords(dealWatchRecord.keywords);
            notificationNewsItemRecord.save();

            recentNewsItems.add(notificationNewsItemRecord);
        }
        mergedRecord.setRecentNewsItems(recentNewsItems);


    }

    private void processSingleNotification(DealWatchRecord dealWatchRecord, NewsItem singleNewsItem)
    {
        // Check to see if this notification already exits
        NotificationRecord existingNotification = NotificationRecord.getByNewsItem(singleNewsItem);

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
        notificationRecord = new NotificationRecord();
        notificationRecord.setId(dealWatchRecord.getId());
        notificationRecord.setTitle(composeTitle(singleNewsItem.getTitle(), dealWatchRecord));
        notificationRecord.setSubTitle(singleNewsItem.getTitle());
        notificationRecord.setBody(singleNewsItem.getBody());
        notificationRecord.setUrl(singleNewsItem.getUrl());
        notificationRecord.setOwner(dealWatchRecord);
        notificationRecord.setReadFlag(NotificationRecord.UNREAD);
        notificationRecord.save();

        NotificationNewsItemRecord singleNotificationNewsItemRecord = new NotificationNewsItemRecord();
        singleNotificationNewsItemRecord.setOwner(notificationRecord);
        singleNotificationNewsItemRecord.setNewsItemId(singleNewsItem.getId());
        singleNotificationNewsItemRecord.setTitle(singleNewsItem.getTitle());
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
        String keywordStr = "\"" + dealWatchRecord.keywords + "\"";

        String newTitle = "Latest deal on " + keywordStr;
        // Primary - Is there a dollar amount
        Matcher m = DispatchNotificationCommand.pricePattern.matcher(title);
        List<String> amts = new ArrayList<>();
        while (m.find())
        {
            amts.add(m.group());
        }

        if (amts.size() > 0)
        {

            newTitle = keywordStr + " for " + amts.get(0);
        }
        else
        {
            // Secondary - Is there  a percentage amount?
            m = discountPattern.matcher(title);
            while (m.find())
            {
                amts.add(m.group());
            }
            if (amts.size() > 0)
            {
                newTitle = keywordStr + " at " + amts.get(0) + " off";
            }
        }

        return newTitle;
    }
}
