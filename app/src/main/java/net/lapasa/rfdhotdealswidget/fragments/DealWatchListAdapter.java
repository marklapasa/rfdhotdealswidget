package net.lapasa.rfdhotdealswidget.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.lapasa.rfdhotdealswidget.R;
import net.lapasa.rfdhotdealswidget.Utils;
import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;
import net.lapasa.rfdhotdealswidget.model.entities.TermSpanRecord;
import net.lapasa.rfdhotdealswidget.services.DispatchNotificationCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DealWatchListAdapter extends BaseExpandableListAdapter
{
    private static final String HAS_OPENED_BEFORE = "firstExpansion";
    private int targetLayoutId = Utils.getNewsItemLayout();
    private final Context context;
    List<DealWatchRecord> records;
    private List<NewsItem> cachedNewsItems = new ArrayList<NewsItem>();
    final SharedPreferences sharedPreferences;

    public DealWatchListAdapter(Context context)
    {
        this.records = new ArrayList<DealWatchRecord>();
        this.context = context;
        sharedPreferences = context.getSharedPreferences(DealWatchListFragment.class.getName(), Context.MODE_PRIVATE);
    }

    public void addCachedNewsRecords(List<NewsItem> items)
    {
        cachedNewsItems.clear();
        cachedNewsItems.addAll(deduplicate(items));
    }

    /**
     * This is required because it's possible to have multiple widgets. Normalize this to a unique set of news items
     * @param cachedNewsItems
     * @return
     */
    private List<NewsItem> deduplicate(List<NewsItem> cachedNewsItems)
    {
        List<NewsItem> blacklist = new ArrayList<>();
        Set<String> existingUrls = new HashSet<String>();
        for (NewsItem newsItem : cachedNewsItems)
        {
            if (existingUrls.contains(newsItem.getUrl()))
            {
                blacklist.add(newsItem);
            }
            else
            {
                existingUrls.add(newsItem.getUrl());
            }
        }

        for (NewsItem newsItem : blacklist)
        {
            cachedNewsItems.remove(newsItem);
        }

        return cachedNewsItems;
    }


    /**
     * A DealWatchAdapter displays a list of DealWatchRecords
     * @param records
     */
    public void setRecords(List<DealWatchRecord> records)
    {
        this.records.clear();
        this.records.addAll(records);

        // TODO: Take the cached news item records and assign them to each of the DealWatchRecords


        notifyDataSetInvalidated();
    }

    @Override
    public int getGroupCount()
    {
        return records.size();
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        DealWatchRecord dealWatchRecord = records.get(groupPosition);
        if (dealWatchRecord == null || dealWatchRecord.filteredNewsItems == null || dealWatchRecord.filteredNewsItems.size() == 0)
        {
            dealWatchRecord.filteredNewsItems = dealWatchRecord.filter(cachedNewsItems);
        }
        return dealWatchRecord.filteredNewsItems.size();
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return records.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        DealWatchRecord record = records.get(groupPosition);
        if (record.filteredNewsItems != null)
        {
            return record.filteredNewsItems.get(childPosition);
        }
        else
        {
            return null;
        }
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        TextView keywordsTextView = null;

        View v = convertView;
        if (v == null)
        {
            v = View.inflate(context, R.layout.filter_group_row_item, null);
        }


        DealWatchRecord record = records.get(groupPosition);

        // Keywords
        keywordsTextView = (TextView) v.findViewById(R.id.keywords);
        keywordsTextView.setText(null);
        keywordsTextView.setText(record.keywords);

        // Expiration Date
        TextView expirationDate = (TextView) v.findViewById(R.id.expirationDate);
        expirationDate.setText(null);
        expirationDate.setText(record.getExpirationStr());

        // Count
        record.filteredNewsItems = record.filter(cachedNewsItems);

        TextView count = (TextView) v.findViewById(R.id.count);
        count.setText(null);
        int size = record.filteredNewsItems.size();
        if (size > 0)
        {
            count.setText(String.valueOf(size));
        }


/*
        boolean hasOpenedGroup = sharedPreferences.getBoolean(HAS_OPENED_BEFORE, false);
        if (!hasOpenedGroup)
        {
            v.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(final View v)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Tip");
                    builder.setMessage("Long press on filter to edit");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(HAS_OPENED_BEFORE, true);
                            editor.apply();
                            v.setOnClickListener(null);
                        }
                    });
                    builder.show();
                }
            });
        }
*/


        // Clear
        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        DealWatchRecord record = records.get(groupPosition);

        NewsItem newsItem = record.filteredNewsItems.get(childPosition);

        // List row item layout
        View v = convertView;
        if (v == null)
        {
            v = View.inflate(context, targetLayoutId, null);
        }

        TextView titleTextView = (TextView) v.findViewById(R.id.title);
        TextView bodyTextView = (TextView) v.findViewById(R.id.body);
        TextView dateTextView = (TextView) v.findViewById(R.id.date);

        titleTextView.setText(null);
        bodyTextView.setText(null);
        dateTextView.setText(null);

        String formattedTitle = newsItem.getTitle(); //applySpans(newsItem.getTitle(), record.getResults());
        titleTextView.setText(formattedTitle);
        bodyTextView.setMaxLines(1000);
//        bodyTextView.setFilters(new InputFilter[]{});
        bodyTextView.setText(newsItem.getBody());
        dateTextView.setText(newsItem.getFormattedDate(context));

        if (targetLayoutId == R.layout.news_item)
        {
//            setThumbnailOnNewsItem(newsItem.getThumbnail(), (ImageView) v.findViewById(R.id.image));
        }

        // Clear
        return v;
    }

    private String applySpans(String title, List<TermSpanRecord> results)
    {
        Spannable sb = new SpannableString(title);

        for (TermSpanRecord termSpanRecord : results)
        {
            DispatchNotificationCommand.setStyle(sb, termSpanRecord.start, termSpanRecord.end, DispatchNotificationCommand.HIGHLIGHT);
        }
        return sb.toString();
    }

    private void setThumbnailOnNewsItem(String thumbnailUrl, ImageView imageView)
    {
        if (thumbnailUrl != null)
        {
            Bitmap bitmap;
            try
            {
                // TODO: This 200, 200 stuff needs to put into dimens file
                bitmap = Picasso.with(context).load(thumbnailUrl).resize(200, 200).centerInside().get();
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            }
            catch (IOException e)
            {
                imageView.setImageBitmap(null);
                imageView.setVisibility(View.GONE);
            }
        }
        else
        {
            imageView.setImageBitmap(null);
            imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer)
    {
        super.registerDataSetObserver(observer);
    }

}
