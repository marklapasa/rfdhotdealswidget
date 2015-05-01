package net.lapasa.rfdhotdealswidget.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DealWatchListAdapter extends BaseExpandableListAdapter implements View.OnClickListener
{
    private int targetLayoutId = Utils.getNewsItemLayout();
    private final Context context;
    List<DealWatchRecord> records;
    private List<NewsItem> cachedNewsItems = new ArrayList<NewsItem>();

    public DealWatchListAdapter(Context context)
    {
        this.records = new ArrayList<DealWatchRecord>();
        this.context = context;
    }

    public void addCachedNewsRecords(List<NewsItem> items)
    {
        cachedNewsItems.clear();
        cachedNewsItems.addAll(items);
    }

    public void setRecords(List<DealWatchRecord> records)
    {
        this.records.clear();
        this.records.addAll(records);
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

        Editable formattedTitle = applySpans(newsItem.getTitle(), record.getResults());
        titleTextView.setText(formattedTitle);
        bodyTextView.setMaxLines(1000);
        bodyTextView.setFilters(new InputFilter[]{});
        bodyTextView.setText(newsItem.getBody());
        dateTextView.setText(newsItem.getFormattedDate(context));

        if (targetLayoutId == R.layout.news_item)
        {
            setThumbnailOnNewsItem(newsItem.getThumbnail(), (ImageView) v.findViewById(R.id.image));
        }

        // Data for when the user clicks on this row
        v.setTag(newsItem);
        v.setOnClickListener(this);

        // Clear
        return v;
    }

    private Editable applySpans(String title, List<TermSpanRecord> results)
    {
        Editable e = new SpannableStringBuilder();
        return e;
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

    // Called when the user clicks on a child view
    @Override
    public void onClick(View v)
    {
        NewsItem newsItem = (NewsItem) v.getTag();

        String targetUrl = newsItem.getUrl();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setData(Uri.parse(targetUrl));
        context.startActivity(i);
    }
}