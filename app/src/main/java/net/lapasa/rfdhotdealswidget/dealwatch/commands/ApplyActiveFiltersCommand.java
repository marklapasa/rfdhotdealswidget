package net.lapasa.rfdhotdealswidget.dealwatch.commands;


import android.content.Context;

import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;

import java.util.List;

/**
 * Trigger this
 */
public class ApplyActiveFiltersCommand implements ICommand
{
    private final List<NewsItem> newsItems;
    private Context context;

    public ApplyActiveFiltersCommand(List<NewsItem> newsItems)
    {
        this.newsItems = newsItems;
        this.context = context;
    }

    @Override
    public void execute()
    {
        // Retrive a list of active Filters/DealWatchRecords
        String whereClause = "enabled = true";
        List<DealWatchRecord> filters = DealWatchRecord.find(DealWatchRecord.class, whereClause, new String[]{});

        for (NewsItem newsItem : newsItems)
        {
            for(DealWatchRecord filter : filters)
            {
                filter.match(newsItem);
            }
        }
    }
}
