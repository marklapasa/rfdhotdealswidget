package test.net.lapasa.rfdhotdealswidget.services;

import android.test.AndroidTestCase;

import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.model.NewsItemsDTO;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;
import net.lapasa.rfdhotdealswidget.model.entities.NotificationNewsItemRecord;
import net.lapasa.rfdhotdealswidget.model.entities.NotificationRecord;
import net.lapasa.rfdhotdealswidget.model.entities.TermSpanRecord;
import net.lapasa.rfdhotdealswidget.services.NotificationService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.matshofman.saxrssreader.RssItem;

public class TestNotificationService extends AndroidTestCase
{
    private NotificationService notificationService;
    private NewsItemsDTO newsItemsDTO;

    private static final String DUMMY_DESC = "DUMMY DESCRIPTION";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        notificationService = new NotificationService(getContext());
        newsItemsDTO = new NewsItemsDTO(getContext());
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        NotificationRecord.deleteAll(NotificationRecord.class);
        NotificationNewsItemRecord.deleteAll(NotificationNewsItemRecord.class);
        notificationService = null;
        DealWatchRecord.deleteAll(DealWatchRecord.class);
        TermSpanRecord.deleteAll(TermSpanRecord.class);
        newsItemsDTO.clearAll();
    }


    /**
     * Empty list of NewsItems should generate no notificationRecords
     */
    public void testEmptyNewsItemsList()
    {
        notificationService.runNotifications(new ArrayList<NewsItem>());
        assertEquals(0, notificationService.notificationRecords.size());
    }

    /**
     * Single NewsItem with No Filter should have no notifications
     */
    public void testSingleNewsItem_no_filter()
    {
        RssItem rssItem = new RssItem();
        rssItem.setTitle("123456 hello, World 123456");
        //                012345678901234567890123
        rssItem.setDescription(DUMMY_DESC);
        rssItem.setPubDate(new Date());
        rssItem.setLink("http://google.com");

        // Create a single NewsItem
        NewsItem newsItem = new NewsItem(rssItem, 2015L);
        newsItem.setId(2015L);
        NewsItemsDTO dto = new NewsItemsDTO(getContext());
        dto.create(newsItem);
        assertTrue(newsItem.getId() > 0);


        List<NewsItem> singleItemList = new ArrayList<NewsItem>();
        singleItemList.add(newsItem);
        notificationService.runNotifications(singleItemList);
        assertTrue(notificationService.notificationRecords.size() == 0);
    }

    /**
     * Single NewsItem with 1 matching Filter
     */
    public void testSingleNewsItemWith1Filter()
    {
        // Create news item
        RssItem rssItem = new RssItem();
        rssItem.setTitle("123456 hello, World 123456");
        //                012345678901234567890123
        rssItem.setDescription(DUMMY_DESC);
        rssItem.setPubDate(new Date());
        rssItem.setLink("http://google.com");

        // Create a single NewsItem
        NewsItem newsItem = new NewsItem(rssItem, 2015L);
        newsItem.setId(2015L);
        NewsItemsDTO dto = new NewsItemsDTO(getContext());
        dto.create(newsItem);
        assertTrue(newsItem.getId() > 0);

        List<NewsItem> singleItemList = new ArrayList<NewsItem>();
        singleItemList.add(newsItem);

        // Create filter
        DealWatchRecord dealWatchRecord = new DealWatchRecord();
        assertNull(dealWatchRecord.getId());
        dealWatchRecord.keywords = DUMMY_DESC;
        dealWatchRecord.save();
        assertTrue(dealWatchRecord.getId() > 0);

        List<DealWatchRecord> filters = new ArrayList<DealWatchRecord>();
        filters.add(dealWatchRecord);
        notificationService.setFilters(filters);

        notificationService.runNotifications(singleItemList);
        assertEquals(1, notificationService.notificationRecords.size());

        NotificationRecord notificationRecord = notificationService.notificationRecords.get(0);
        List<NotificationNewsItemRecord> notificationNewsItemRecords = notificationRecord.fetchNewsItems();
        assertEquals(1, notificationNewsItemRecords.size());

    }

    /**
     * Two NewsItem with 1 matching filter
     */
    public void testTwoNewsItemsWith1MatchingFilter()
    {
        List<NewsItem> doubleItemList = new ArrayList<NewsItem>();

        // Create 2 news item
        RssItem rssItem = new RssItem();
        rssItem.setTitle("123456 hello, World 123456");
        //                012345678901234567890123
        rssItem.setDescription(DUMMY_DESC);
        rssItem.setPubDate(new Date());
        rssItem.setLink("http://google.com");

        // Create first NewsItem
        NewsItem newsItem = new NewsItem(rssItem, 2015L);
        newsItem.setId(2015L);
        NewsItemsDTO dto = new NewsItemsDTO(getContext());
        dto.create(newsItem);
        assertTrue(newsItem.getId() > 0);


        doubleItemList.add(newsItem);

        // Create the second news item
        rssItem = new RssItem();
        rssItem.setTitle("____World");
        //                012345678901234567890123
        rssItem.setDescription(DUMMY_DESC);
        rssItem.setPubDate(new Date());
        rssItem.setLink("http://yahoo.com");


        newsItem = new NewsItem(rssItem, 2016L);
        newsItem.setId(2016L);
        dto.create(newsItem);
        assertTrue(newsItem.getId() > 0);

        doubleItemList.add(newsItem);



        // Create filter
        DealWatchRecord dealWatchRecord = new DealWatchRecord();
        assertNull(dealWatchRecord.getId());
        dealWatchRecord.keywords = "DUMMY";
        dealWatchRecord.save();
        assertTrue(dealWatchRecord.getId() > 0);

        List<DealWatchRecord> filters = new ArrayList<DealWatchRecord>();
        filters.add(dealWatchRecord);
        notificationService.setFilters(filters);

        notificationService.runNotifications(doubleItemList);
        assertEquals(1, notificationService.notificationRecords.size());

        NotificationRecord notificationRecord = notificationService.notificationRecords.get(0);
        List<NotificationNewsItemRecord> notificationNewsItemRecords = notificationRecord.fetchNewsItems();
        assertEquals(2, notificationNewsItemRecords.size());
    }





}
