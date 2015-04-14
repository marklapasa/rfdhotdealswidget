package net.lapasa.rfdhotdealswidget.model.entities;

import com.orm.SugarRecord;
import com.orm.dsl.Table;

import java.util.Date;
import java.util.List;

/*
    Keywords
    Expire Date
    And/Or
    Enabled: Default is true

    Match(String):boolean
 */


@Table(name = "DEAL_WATCH_RECORD")
public class DealWatchRecord extends SugarRecord
{
    public static final String FILTER_AND = "FILTER_AND";
    public static final String FILTER_OR= "FILTER_OR";
    public static final String FILTER_EXACT = "FILTER_EXACT";

    private static final String TAG = DealWatchRecord.class.getName();

    public String keywords;
    public Date expiration;
    public boolean enabled;
    public String type;

    public DealWatchRecord()
    {
    }

    public boolean match(String srcStr, String targetStr)
    {
        return srcStr.toLowerCase().contains(targetStr.toLowerCase());
    }

    public static DealWatchRecord get(long id)
    {
        return (DealWatchRecord) find(DealWatchRecord.class, "id == " + id, null, null, null, "1");
    }

    public static List<DealWatchRecord> getAllRecords()
    {
        return DealWatchRecord.find(DealWatchRecord.class, null);
    }
}
