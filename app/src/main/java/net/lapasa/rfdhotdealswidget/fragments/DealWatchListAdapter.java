package net.lapasa.rfdhotdealswidget.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.lapasa.rfdhotdealswidget.R;
import net.lapasa.rfdhotdealswidget.Utils;
import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;
import net.lapasa.rfdhotdealswidget.model.entities.TermSpanRecord;
import net.lapasa.rfdhotdealswidget.services.DispatchNotificationCommand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DealWatchListAdapter extends BaseExpandableListAdapter
{
    private static final String HAS_OPENED_BEFORE = "firstExpansion";
    private final Context context;
    private final Utils utils;
    List<DealWatchRecord> records;
    private List<NewsItem> cachedNewsItems = new ArrayList<NewsItem>();
    final SharedPreferences sharedPreferences;

    public DealWatchListAdapter(Context context)
    {
        this.records = new ArrayList<DealWatchRecord>();
        this.context = context;
        sharedPreferences = context.getSharedPreferences(DealWatchListFragment.class.getName(), Context.MODE_PRIVATE);
        this.utils = new Utils();
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
            v = View.inflate(context, R.layout.deal_watch_news_item, null);
        }

        TextView titleTextView = (TextView) v.findViewById(R.id.title);
        TextView bodyTextView = (TextView) v.findViewById(R.id.body);
        TextView dateTextView = (TextView) v.findViewById(R.id.date);
        ImageView imageView = (ImageView) v.findViewById(R.id.image);

        titleTextView.setText(null);
        bodyTextView.setText(null);
        dateTextView.setText(null);
        imageView.setImageDrawable(null);

        String formattedTitle = newsItem.getTitle(); //applySpans(newsItem.getTitle(), record.getResults());
        titleTextView.setText(formattedTitle);
//        bodyTextView.setMaxLines(1000);
//        bodyTextView.setFilters(new InputFilter[]{});
        bodyTextView.setText(newsItem.getBody());



        String formattedDate = newsItem.getFormattedDate(context);
        dateTextView.setText(formattedDate);
        setThumbnailOnNewsItem(newsItem.getThumbnail(), (ImageView) v.findViewById(R.id.image));

        groupByTime(childPosition, dateTextView, formattedDate, record.filteredNewsItems);

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

    private void setThumbnailOnNewsItem(String thumbnailUrl, final ImageView imageView)
    {
        Target t = new Target()
        {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
            {
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable)
            {
                imageView.setImageBitmap(null);
                imageView.setVisibility(View.GONE);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable)
            {

            }
        };


        if (thumbnailUrl != null)
        {
            Picasso.with(context).load(thumbnailUrl).resize(200, 200).centerInside().into(t);
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

    /**
     * Create illusion of grouping by time
     *
     * @param position
     * @param dateHeader
     * @param dateStrCurrent
     */
    private void groupByTime(int position, View dateHeader, String dateStrCurrent, List<NewsItem> list)
    {
        if (position > 0)
        {
            NewsItem prevNewsItem =  list.get(position - 1);
            String dateStrPrevious = prevNewsItem.getFormattedDate(context);


            if (dateStrCurrent.equals(dateStrPrevious))
            {
                dateHeader.setVisibility(View.GONE);
            }
            else
            {
                dateHeader.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            dateHeader.setVisibility(View.VISIBLE);
        }
    }
}
