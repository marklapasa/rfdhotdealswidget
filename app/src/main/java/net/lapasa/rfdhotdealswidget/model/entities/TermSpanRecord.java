package net.lapasa.rfdhotdealswidget.model.entities;

import com.orm.SugarRecord;
import com.orm.dsl.Table;


/**
 * A DealWatchRecord aggregates many DealWatchResultRecords
 */
@Table(name = "DEAL_WATCH_RESULT_RECORD")
public class TermSpanRecord extends SugarRecord
{
    DealWatchRecord owner;

    public String columnName;
    public long newsItemId;
    public long start;
    public long end;
    public String keywords;

    public TermSpanRecord()
    {
        // Required empty constructor
    }
}
