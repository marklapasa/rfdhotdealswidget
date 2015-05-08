package net.lapasa.rfdhotdealswidget.fragments;

import android.app.Fragment;
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
import net.lapasa.rfdhotdealswidget.dealwatch.commands.SortAlphabeticallyCommand;
import net.lapasa.rfdhotdealswidget.dealwatch.commands.SortByExpirationCommand;
import net.lapasa.rfdhotdealswidget.model.NewsItemsDTO;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;


public class DealWatchListFragment extends Fragment
{

    private ExpandableListView expandibleListView;
    private DealWatchListAdapter adapter;
    private int lastExpandedPosition;
    private boolean hasOpenedGroup;

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
                new SortAlphabeticallyCommand().execute();
                break;
            case R.id.action_sort_by_expiration:
                new SortByExpirationCommand().execute();
                break;
//            case R.id.action_group_by_enabled:
//                new GroupByEnabledCommand().execute();
//                break;
            default:
                return true;
        }


        return super.onOptionsItemSelected(item);
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
        ((DealWatchActivity) getActivity()).launchFragment(frag);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().setTitle(getActivity().getString(R.string.deal_watch));
    }


    private void refresh()
    {
        if (adapter == null)
        {
            adapter = new DealWatchListAdapter(getActivity());
        }
        adapter.setRecords(DealWatchRecord.getAllRecords());
    }


    @Override
    public void registerForContextMenu(View view)
    {
        super.registerForContextMenu(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        refresh();

        // Cache news Items
        NewsItemsDTO dto = new NewsItemsDTO(getActivity());
        adapter.addCachedNewsRecords(dto.find(null));

        expandibleListView.setAdapter(adapter);

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
            }
        });

        if (getArguments() != null)
        {
            long targetDealWatchToBeOpened = getArguments().getLong(DealWatchActivity.RECORD_ID, -1L);
            if (targetDealWatchToBeOpened >= 0)
            {
                openById(targetDealWatchToBeOpened);
            }
        }

    }

    public void openById(long targetDealWatchToBeOpened)
    {
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
            expandibleListView.expandGroup(targetIndex);
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
