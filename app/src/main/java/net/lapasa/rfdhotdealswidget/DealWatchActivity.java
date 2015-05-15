package net.lapasa.rfdhotdealswidget;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import net.lapasa.rfdhotdealswidget.fragments.DealWatchListFragment;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;


public class DealWatchActivity extends ActionBarActivity
{

    public static final String RECORD_ID = "RECORD_ID";
    private AlertDialog loadingDialog;
    private DealWatchListFragment frag;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_deal_watch);
        DealWatchRecord.purgeExpired();

        frag = new DealWatchListFragment();


        launchFragment(frag);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher);
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



    public void enableLoadingAnimation(boolean b)
    {
        if (loadingDialog == null)
        {
            ProgressDialog.Builder builder = new ProgressDialog.Builder(this);
            builder.setTitle("Please wait");
            loadingDialog = builder.create();
        }

        if (b)
        {
            loadingDialog.show();
        }
        else
        {
            loadingDialog.hide();
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        long targetRecord = intent.getLongExtra(RECORD_ID, -1L);
        if (frag != null)
        {
            frag.openById(targetRecord);
        }
    }
}
