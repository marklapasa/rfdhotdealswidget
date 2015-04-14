package net.lapasa.rfdhotdealswidget.dealwatch;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import net.lapasa.rfdhotdealswidget.R;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;

import java.util.ArrayList;

public class DealWatchListAdapter extends BaseExpandableListAdapter
{
    private final Context context;
    private final LayoutInflater inflater;
    private ArrayList<DealWatchRecord> filters;

    public DealWatchListAdapter(Context context, ArrayList<DealWatchRecord> filters)
    {
        this.context = context;
        this.inflater = ((Activity) context).getLayoutInflater();
        this.filters = filters;
    }

    @Override
    public int getGroupCount()
    {
        return filters.size();
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return filters.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return 0;
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        View v = inflater.inflate(R.layout.filter_group_row_item, null);
        return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return false;
    }
}
