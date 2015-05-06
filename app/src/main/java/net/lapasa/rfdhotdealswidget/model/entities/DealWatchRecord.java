package net.lapasa.rfdhotdealswidget.model.entities;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.dsl.Table;

import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.model.SpanPair;
import net.lapasa.rfdhotdealswidget.model.Spans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    public static final long FILTER_AND     = 0;
    public static final long FILTER_OR      = 1;
    public static final long FILTER_EXACT   = 2;

    @Ignore
    private static String[] columnNames = new String[]{"title","body"};

    private static final String TAG = DealWatchRecord.class.getName();

    public String keywords;
    public Date expiration;
    public boolean enabled;
    public long type = FILTER_OR;

    @Ignore
    public List<NewsItem> filteredNewsItems;

    public DealWatchRecord()
    {
        // Required empty constructor
    }


    /**
     *
     * @param id
     * @return
     */
    public static DealWatchRecord get(long id)
    {
        List<DealWatchRecord> dealWatchRecords = find(DealWatchRecord.class, "id = ?", new String[]{String.valueOf(id)}, null, null, "1");
        if (dealWatchRecords != null && dealWatchRecords.size() == 1)
        {
            return dealWatchRecords.get(0);
        }
        else
        {
            return null;
        }
    }

    public static List<DealWatchRecord> getAllRecords()
    {
        return DealWatchRecord.find(DealWatchRecord.class, null, null);
    }

    public static void update(DealWatchRecord record)
    {
        record.save();
    }

    public static void delete(DealWatchRecord record)
    {
        DealWatchRecord.deleteAll(DealWatchRecord.class, "id = ?", String.valueOf(record.getId()));
    }

    public boolean match(NewsItem newsItem)
    {
        boolean found = false;

        Map<String, Spans> map = new HashMap<String, Spans>();

        // Check for matches in the title
        Spans spans = new Spans(newsItem.getTitle(), keywords, type);
        if (spans.pairs.size() > 0)
        {
            found = true;
            persistSpanInfo("title", spans.pairs, newsItem, keywords);
        }

        spans = null;

        // Check for matches in the descripiton
        spans = new Spans(newsItem.getBody(), keywords, type);

        if (spans.pairs.size() > 0)
        {
            found = true;
            persistSpanInfo("body", spans.pairs, newsItem, keywords);
        }

        return found;
    }

    private void persistSpanInfo(String colName, List<SpanPair> pairs, NewsItem newsItem, String keywords)
    {
        for (SpanPair spanPair : pairs)
        {
            TermSpanRecord resultRecord = new TermSpanRecord();
            resultRecord.owner = this;
            resultRecord.columnName = colName;
            resultRecord.newsItemId = newsItem.getId();
            resultRecord.start = spanPair.start;
            resultRecord.end = spanPair.end;
            resultRecord.keywords = keywords;
            resultRecord.save();
        }
    }

    /**
     * Iterate through all the records and evict those records that remain
     * persisted after their defined expiration date
     */
    public static void purgeExpired()
    {
        Date now = new Date();
        List<DealWatchRecord> allRecords = getAllRecords();
        for (int i = 0; i < allRecords.size(); i++)
        {
            DealWatchRecord record = allRecords.get(i);
            if (record.expiration.getTime() < now.getTime())
            {
                record.delete();
            }
        }
    }

    public String getWhereClause()
    {
        String whereClause = null;
        if (type == FILTER_OR)
        {
            whereClause = getORClause(keywords, columnNames);
        }
        else if (type == FILTER_AND)
        {
            whereClause = getANDClause();
        }
        else
        {
            whereClause = columnNames[0] + " LIKE '%"+ keywords +"%' OR " + columnNames[1] + " LIKE '%"+ keywords +"%'";
        }

        return whereClause;
    }

    public static String getORClause(String keywordsStr, String[] colNames)
    {
        StringBuilder builder = new StringBuilder();

        String colName;
        String term;
        for (int i = 0; i < colNames.length; i++)
        {
            colName = colNames[i];

            String[] terms = Spans.prepareStrings(keywordsStr);

            for(int j = 0; j < terms.length; j++)
            {
                term = terms[j];
                builder.append(colName + " LIKE '%"+ term + "%'");

                if (j < terms.length - 1)
                {
                    builder.append(" OR ");
                }
            }

            if (i < colNames.length - 1)
            {
                builder.append(" OR ");
            }
        }

        return builder.toString();
    }

    // Return records that have both keywords present in either title or body
    // true = all keywords in title only
    // true = all keywords in body only
    // true = all keywords in title and body
    // false = some keywords in title but not in body
    // false = some keywords in body but not in title
    // false = no keywords in title or body
    public String getANDClause()
    {
        StringBuilder builder = new StringBuilder();

        String colName;
        String term;

        for (int i = 0; i < columnNames.length; i++)
        {
            colName = columnNames[i];

            String[] terms = Spans.prepareStrings(keywords);

            builder.append("(");
            for(int j = 0; j < terms.length; j++)
            {
                term = terms[j];
                builder.append(colName + " LIKE '%"+ term + "%'" );

                if (j != terms.length - 1)
                {
                    builder.append(" AND ");
                }
            }
            builder.append(")");

            if (i != columnNames.length - 1)
            {
                builder.append(" OR ");
            }
        }

        return builder.toString();
    }

    /**
     * Return a list of Span data that corresponds to a single DealWatchRecord
     *
     * @return
     */
    public List<TermSpanRecord> getResults()
    {
        String whereClause = "owner = ?";
        List<TermSpanRecord> results = TermSpanRecord.find(TermSpanRecord.class, whereClause, new String[]{String.valueOf(getId())});
        return results;
    }

    public String getExpirationStr()
    {
        return "Expires " + sdf.format(expiration);
    }

    public static boolean isExistingFilter(String keywordsText)
    {
        String whereClause = getORClause(keywordsText, new String[]{"keywords"});
        List<DealWatchRecord> dealWatchRecords = DealWatchRecord.find(DealWatchRecord.class, whereClause, null);
        return dealWatchRecords.size() > 0;
    }

    /**
     * Return a list of NewsItem that pass this DealWatchRecord's criteria
     *
     * @param newsItems
     * @return
     */
    public List<NewsItem> filter(List<NewsItem> newsItems)
    {
        List<NewsItem> results = new ArrayList<NewsItem>();
        for (NewsItem newsItem : newsItems)
        {
            if (match(newsItem))
            {
                results.add(newsItem);
            }
        }
        return results;
    }
}