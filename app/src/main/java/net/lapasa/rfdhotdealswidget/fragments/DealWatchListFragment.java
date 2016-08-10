package net.lapasa.rfdhotdealswidget.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import net.lapasa.rfdhotdealswidget.DealWatchActivity;
import net.lapasa.rfdhotdealswidget.R;
import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.model.NewsItemsDTO;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;


public class DealWatchListFragment extends Fragment
{

    private static final String SORT_PREFERENCE = "SORT_PREFERENCE";
    private ExpandableListView expandibleListView;
    private DealWatchListAdapter adapter;
    private int lastExpandedPosition;
    private NewsItemsDTO newsItemDTO;
    private String sortPreference = DealWatchRecord.SORT_ALPHABETICALLY_ASC;
    private DealWatchActivity activity;

    public DealWatchListFragment()
    {
        // Requried empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_deal_watch_list, null);

        expandibleListView = (ExpandableListView) v.findViewById(R.id.expandableListView);

        expandibleListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                launchEditor((DealWatchRecord) adapter.getGroup(position));
                return true;
            }
        });
        View emptyView = v.findViewById(R.id.empty_view);

        expandibleListView.setEmptyView(emptyView);

        // From http://stackoverflow.com/a/17586315/855984
        expandibleListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener()
        {
            @Override
            public void onGroupExpand(int groupPosition)
            {
                if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition)
                {
                    expandibleListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;

                // Update subtitle
                DealWatchRecord record = (DealWatchRecord) adapter.getGroup(groupPosition);
                String whereClause = "\"" + record.keywords + "\"";
                activity.getSupportActionBar().setSubtitle(whereClause);
            }
        });

        expandibleListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener()
        {
            @Override
            public void onGroupCollapse(int groupPosition)
            {
                activity.getSupportActionBar().setSubtitle(R.string.sub_title);
            }
        });


        expandibleListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
        {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
            {
                DealWatchRecord record = (DealWatchRecord) adapter.getGroup(groupPosition);
                NewsItem newsItem = record.filteredNewsItems.get(childPosition);

                String targetUrl = newsItem.getUrl();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setData(Uri.parse(targetUrl));
                activity.startActivity(i);
                return true;
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        menu.clear();
        inflater.inflate(R.menu.menu_deal_watch, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_add_filter:
                launchEditor(null);
                break;
//            case R.id.action_expand_all:
//                new ExpandAllCommand().execute();
//                break;
//            case R.id.action_collapse_all:
//                new CollapseAllCommand().execute();
//                break;
            case R.id.action_sort_alphabetically:
                if (DealWatchRecord.SORT_ALPHABETICALLY_ASC.equals(sortPreference))
                {
                    rememberSortPreference(DealWatchRecord.SORT_ALPHABETICALLY_DESC);
                }
                else
                {
                    rememberSortPreference(DealWatchRecord.SORT_ALPHABETICALLY_ASC);
                }
                refresh();
                break;
            case R.id.action_sort_by_expiration:
                if (DealWatchRecord.SORT_EXPIRATION_ASC.equals(sortPreference))
                {
                    rememberSortPreference(DealWatchRecord.SORT_EXPIRATION_DESC);
                }
                else
                {
                    rememberSortPreference(DealWatchRecord.SORT_EXPIRATION_ASC);
                }

                refresh();
                break;
//            case R.id.action_group_by_enabled:
//                new GroupByEnabledCommand().execute();
//                break;
            default:
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void rememberSortPreference(String targetPref)
    {
        sortPreference = targetPref;
        SharedPreferences sharedPreferences = activity.getSharedPreferences(DealWatchListFragment.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SORT_PREFERENCE, sortPreference);
        editor.apply();
        
    }

    private void launchEditor(DealWatchRecord record)
    {
        Fragment frag = new CreateEditDealWatchFragment();
        if (record != null)
        {
            Bundle bundle = new Bundle();
            bundle.putLong(CreateEditDealWatchFragment.EXISTING_RECORD_ID, record.getId());
            frag.setArguments(bundle);
        }
        activity.launchFragment(frag);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        refresh();
    }

    private void refresh()
    {

        activity = (DealWatchActivity) getActivity();
        activity.enableLoadingAnimation(true);

        android.support.v7.app.ActionBar supportActionBar = activity.getSupportActionBar();
        supportActionBar.setTitle(activity.getString(R.string.deal_watch));
        supportActionBar.setSubtitle(activity.getString(R.string.sub_title));


        if (adapter == null)
        {
            adapter = new DealWatchListAdapter(getActivity());
        }

        if (newsItemDTO == null)
        {
            newsItemDTO = new NewsItemsDTO(getActivity());
        }

        // Inject to the adapter the most up to date news item records
        adapter.addCachedNewsRecords(newsItemDTO.find(null));


        recallSortPreference();
        adapter.setRecords(DealWatchRecord.getAllRecords(sortPreference));

        // Cache news Items
        if (newsItemDTO == null)
        {
            newsItemDTO = new NewsItemsDTO(getActivity());
        }


        expandibleListView.setAdapter(adapter);


        activity.enableLoadingAnimation(false);
    }

    private void recallSortPreference()
    {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(DealWatchListFragment.class.getName(), Context.MODE_PRIVATE);
        sortPreference = sharedPreferences.getString(SORT_PREFERENCE, DealWatchRecord.SORT_ALPHABETICALLY_ASC);
    }


    @Override
    public void registerForContextMenu(View view)
    {
        super.registerForContextMenu(view);
    }


    public void openById(long targetDealWatchToBeOpened)
    {
        refresh();
        // Figure out index of DealWatchFilter record that has this id
        int targetIndex = -1;
        for (int i = 0; i < adapter.getGroupCount(); i++)
        {
            DealWatchRecord record = (DealWatchRecord) adapter.getGroup(i);
            if (record.getId() == targetDealWatchToBeOpened)
            {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex >= 0)
        {

            final int _targetIndex = targetIndex;

            adapter.getChildrenCount(targetIndex); // This will trigger fetching the data for the child views
            android.os.Handler h = new android.os.Handler();
            h.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    expandibleListView.setSelection(_targetIndex);
                    expandibleListView.expandGroup(_targetIndex);
                }
            }, 1000);

        }
    }
    /**
     * Get the DealWatchRecord and query the NewsItem db for matches
     *
     * @param parent
     * @param v
     * @param groupPosition
     * @param id
     * @return

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
    {
        DealWatchRecord record = (DealWatchRecord) adapter.getGroup(groupPosition);
        record.filteredNewsItems = record.filter(cachedNewsItems);
        return false;
    }
     */
}