package net.lapasa.rfdhotdealswidget;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import net.lapasa.rfdhotdealswidget.fragments.DealWatchListFragment;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;


public class DealWatchActivity extends ActionBarActivity
{

    public static final String RECORD_ID = "RECORD_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_watch);
        DealWatchRecord.purgeExpired();

        Fragment frag = new DealWatchListFragment();
        long existingDealWatchFilterId = getIntent().getLongExtra(DealWatchActivity.RECORD_ID, -1L);
        if (existingDealWatchFilterId >= 0)
        {
            Bundle bundle = new Bundle();
            bundle.putLong(DealWatchActivity.RECORD_ID, existingDealWatchFilterId);
            frag.setArguments(bundle);
        }

        launchFragment(frag);
    }

    public void launchFragment(Fragment fragment)
    {
        String backStateName = fragment.getClass().getName();
        FragmentManager mgr = getFragmentManager();

        boolean fragmentPopped = mgr.popBackStackImmediate(backStateName, 0);
        if (!fragmentPopped)
        {
            FragmentTransaction ft = mgr.beginTransaction();
            ft.replace(R.id.container, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    @Override
    public void onBackPressed()
    {
        if (getFragmentManager().getBackStackEntryCount() == 1)
        {
            finish();
        }
        else
        {
            getFragmentManager().popBackStack();
        }
    }

    public void setTitle(String title)
    {
        getActionBar().setTitle(title);
    }


}
