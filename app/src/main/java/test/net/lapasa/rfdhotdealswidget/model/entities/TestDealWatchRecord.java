package test.net.lapasa.rfdhotdealswidget.model.entities;


import android.test.AndroidTestCase;

import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.model.NewsItemsDTO;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;
import net.lapasa.rfdhotdealswidget.model.entities.TermSpanRecord;

import java.util.Date;
import java.util.List;

import nl.matshofman.saxrssreader.RssItem;

public class TestDealWatchRecord extends AndroidTestCase
{
    private static final String HELLO_WORLD = "Hello World";
    private static final String DUMMY_TITLE = "DUMMY_TITLE";
    private static final String DUMMY_DESC = "DUMMY_DESCRIPTION";
    private NewsItemsDTO dto;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        dto = new NewsItemsDTO(getContext());
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        DealWatchRecord.deleteAll(DealWatchRecord.class);
        TermSpanRecord.deleteAll(TermSpanRecord.class);
        dto.clearAll();
    }

    public void testCreate()
    {
        DealWatchRecord record = new DealWatchRecord();
        long id = record.save();

        List<DealWatchRecord> records = DealWatchRecord.getAllRecords();
        long persistedId = records.get(0).getId();
        assertEquals(id, persistedId);
        assertEquals(1, DealWatchRecord.getAllRecords().size());
    }

    /**
     * Read Watch record with no results
     */
    public void testRead()
    {
        // Create watch record
        DealWatchRecord record = new DealWatchRecord();
        record.keywords = "a keyword that does not exist";
        long id = record.save();

        List<DealWatchRecord> allRecords = DealWatchRecord.getAllRecords();

        assertNotNull(allRecords);
        assertTrue(allRecords.size() == 1);


        // Check against NewsItem data store


        // Implementatio note - Easier to iterate through each NewsItem and have that NewsItem's text
        // match against each DeleteWatch record

        RssItem rssItem = new RssItem();
        rssItem.setTitle(DUMMY_TITLE);
        rssItem.setDescription(DUMMY_DESC);
        rssItem.setPubDate(new Date());
        rssItem.setLink("http://google.com");

        NewsItem newsItem = new NewsItem(rssItem, 2015L);
        newsItem.setId(2015L);
        dto.create(newsItem);
        assertTrue(newsItem.getId() > 0);

        record = allRecords.get(0);
        assertFalse(record.match(newsItem)); // Should be no matches


        // Clean up
        dto.delete(newsItem);
    }

    /**
     * Read Watch record with 1 OR result
     */
    public void testReadOneORResultInTitle()
    {
        DealWatchRecord OR_record = new DealWatchRecord();
        OR_record.keywords = "hello2, worldz";
        OR_record.type = DealWatchRecord.FILTER_OR;
        long id = OR_record.save();


        RssItem rssItem = new RssItem();
        rssItem.setTitle("world is hello2 right now");
        //                012345678901234567890123
        rssItem.setDescription(DUMMY_DESC);
        rssItem.setPubDate(new Date());
        rssItem.setLink("http://google.com");

        NewsItem newsItem = new NewsItem(rssItem, 2015L);
        newsItem.setId(2015L);
        NewsItemsDTO dto = new NewsItemsDTO(getContext());
        dto.create(newsItem);
        assertTrue(newsItem.getId() > 0);

        List<DealWatchRecord> allRecords = DealWatchRecord.getAllRecords();
        OR_record = allRecords.get(0);
        assertTrue(OR_record.match(newsItem));


        List<TermSpanRecord> results = OR_record.getResults();
        assertTrue(results.size() == 1);


        TermSpanRecord resultRecord = results.get(0);
        assertEquals(newsItem.getId(), resultRecord.newsItemId);
        assertEquals(9, resultRecord.start);
        assertEquals(14, resultRecord.end);
        assertTrue(OR_record.keywords.contains(resultRecord.keywords));
        assertEquals("title", resultRecord.columnName);

        // Clean up
        dto.delete(newsItem);
    }


    /**
     * Read Watch record with 1 AND result
     */
    public void testReadOneANDResultInTitle()
    {
        DealWatchRecord AND_record = new DealWatchRecord();
        AND_record.keywords = "hello, world";
        AND_record.type = DealWatchRecord.FILTER_AND;
        long id = AND_record.save();

        RssItem rssItem = new RssItem();
        rssItem.setTitle("world is hello right now");
        //                012345678901234567890123
        rssItem.setDescription(DUMMY_DESC);
        rssItem.setPubDate(new Date());
        rssItem.setLink("http://google.com");

        NewsItem newsItem = new NewsItem(rssItem, 2015L);
        newsItem.setId(2015L);
        NewsItemsDTO dto = new NewsItemsDTO(getContext());
        dto.create(newsItem);
        assertTrue(newsItem.getId() > 0);

        List<DealWatchRecord> allRecords = DealWatchRecord.getAllRecords();
        AND_record = allRecords.get(0);


        assertTrue(AND_record.match(newsItem));
        List<TermSpanRecord> results = AND_record.getResults();
        assertEquals(2, results.size());

        TermSpanRecord resultRecord = results.get(0);
        assertEquals("title", resultRecord.columnName);
        assertEquals(newsItem.getId(), resultRecord.newsItemId);
        assertEquals(9, resultRecord.start);
        assertEquals(13, resultRecord.end);
        assertTrue(AND_record.keywords.contains(resultRecord.keywords));

        resultRecord = results.get(1);
        assertEquals("title", resultRecord.columnName);
        assertEquals(newsItem.getId(), resultRecord.newsItemId);
        assertEquals(0, resultRecord.start);
        assertEquals(4, resultRecord.end);
        assertTrue(AND_record.keywords.contains(resultRecord.keywords));



    }

    /**
     * Read Watch record with 1 EXACT result
     */
    public void testReadOneEXACTResultInTitle()
    {
        DealWatchRecord EXACT_record = new DealWatchRecord();
        EXACT_record.keywords = "hello, World";
        EXACT_record.type = DealWatchRecord.FILTER_EXACT;
        long id = EXACT_record.save();

        RssItem rssItem = new RssItem();
        rssItem.setTitle("123456 hello, World 123456");
        //                012345678901234567890123
        rssItem.setDescription(DUMMY_DESC);
        rssItem.setPubDate(new Date());
        rssItem.setLink("http://google.com");

        NewsItem newsItem = new NewsItem(rssItem, 2015L);
        newsItem.setId(2015L);
        NewsItemsDTO dto = new NewsItemsDTO(getContext());
        dto.create(newsItem);
        assertTrue(newsItem.getId() > 0);

        List<DealWatchRecord> allRecords = DealWatchRecord.getAllRecords();
        EXACT_record = allRecords.get(0);

        /*Map<String, Spans> spans = EXACT_record.match(newsItem);
        assertNotNull(spans);
        assertTrue(spans.size() == 1);
        Spans titlePairs = spans.get("title");
        assertTrue(titlePairs.pairs.size() == 1);

        SpanPair pair = titlePairs.pairs.get(0);
        assertEquals(7, pair.start);
        assertEquals(18, pair.end);*/
    }

    /**
     * Confirm all watch records that have expired dates get deleted
     */
    public void testDeleteExpired()
    {
        DealWatchRecord OLD_record = new DealWatchRecord();
        assertNull(OLD_record.getId());
        OLD_record.expiration = new Date();
        OLD_record.expiration.setTime(new Date().getTime() - 60000); // Set time to 1 minute ago
        OLD_record.save();
        assertNotNull(OLD_record.getId());

        DealWatchRecord FUTURE_record = new DealWatchRecord();
        assertNull(FUTURE_record.getId());
        FUTURE_record.expiration = new Date();
        FUTURE_record.expiration.setTime(new Date().getTime() + 60000); // Set time to 1 minute in the future
        FUTURE_record.save();
        assertNotNull(FUTURE_record.getId());


        DealWatchRecord.purgeExpired();

        DealWatchRecord record = DealWatchRecord.get(OLD_record.getId());
        assertNull(record);
        record = DealWatchRecord.get(FUTURE_record.getId());
        assertNotNull(record);

        // Clean up
        OLD_record.delete();
        FUTURE_record.delete();
    }


    public void testUpdate()
    {
        // Add initial record
        DealWatchRecord record = new DealWatchRecord();
        assertNull(record.keywords);
        long id = record.save();

        // Retrive record
        record = null;
        record = DealWatchRecord.get(id);
        // Make modifications
        record.keywords = HELLO_WORLD;
        // Update the record
        record.save();


        // Query the record
        record = null;
        record = DealWatchRecord.get(id);

        // Verify if the changes were persisted
        assertEquals(record.keywords, HELLO_WORLD);
    }

    public void testDelete()
    {
        // Add record
        // Remember the ID
        DealWatchRecord record = new DealWatchRecord();
        long id = record.save();
        DealWatchRecord querriedRecord = DealWatchRecord.get(id);
        assertNotNull(querriedRecord);

        querriedRecord = null;

        // Delete the record
        DealWatchRecord.delete(record);

        // Ensure that the Id no longer exists
        querriedRecord = DealWatchRecord.get(id);
        assertNull(querriedRecord);
    }

    public void testExistingRecord()
    {
        assertFalse(DealWatchRecord.isExistingFilter(HELLO_WORLD));

        DealWatchRecord record = new DealWatchRecord();
        record.keywords = HELLO_WORLD;
        long id = record.save();

        assertTrue(DealWatchRecord.isExistingFilter(HELLO_WORLD));


    }
}
