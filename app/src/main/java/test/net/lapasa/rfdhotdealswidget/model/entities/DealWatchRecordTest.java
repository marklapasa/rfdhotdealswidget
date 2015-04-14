package test.net.lapasa.rfdhotdealswidget.model.entities;

import junit.framework.TestCase;

import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;

import java.util.List;

public class DealWatchRecordTest extends TestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        DealWatchRecord.deleteAll(DealWatchRecord.class);
    }

    protected void createTest()
    {
        DealWatchRecord record = new DealWatchRecord();
        long id = record.save();

        List<DealWatchRecord> records = DealWatchRecord.getAllRecords();
        long persistedId = records.get(0).getId();
        assertEquals(id, persistedId);
        assertEquals(1, DealWatchRecord.getAllRecords().size());
    }




}
