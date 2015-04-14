package net.lapasa.rfdhotdealswidget;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

import net.lapasa.rfdhotdealswidget.fragments.CreateEditDealWatchFragment;


public class DealWatchListFragment extends Fragment
{

    private ExpandableListView expandibleListView;

    public DealWatchListFragment()
    {
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
        View v = inflater.inflate(R.layout.fragment_deal_watch_list, container, false);
        expandibleListView = (ExpandableListView) v.findViewById(R.id.expandableListView);
        View emptyView = inflater.inflate(R.layout.deal_watch_empty, null);
        getActivity().addContentView(emptyView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
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
                launchEditor();
                break;
            case R.id.action_expand_all:
                break;
            case R.id.action_collapse_all:
                break;
            case R.id.action_sort_alphabetically:
                break;
            case R.id.action_sort_by_expiration:
                break;
            case R.id.action_group_by_enabled:
                break;
            default:
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void launchEditor()
    {
        ((DealWatchActivity) getActivity()).launchFragment(new CreateEditDealWatchFragment());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().setTitle(getActivity().getString(R.string.deal_watch));
    }
}
