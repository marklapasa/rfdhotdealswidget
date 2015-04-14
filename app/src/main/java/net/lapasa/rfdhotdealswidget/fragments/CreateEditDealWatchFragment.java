package net.lapasa.rfdhotdealswidget.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;

import net.lapasa.rfdhotdealswidget.R;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;

import java.util.Calendar;
import java.util.Date;

public class CreateEditDealWatchFragment extends Fragment
{
    private static final String EXISTING_RECORD_ID = "EXISTING_RECORD_ID";
    private static int CREATE_MODE = 0;
    private static int EDIT_MODE = 1;
    private DealWatchRecord dealWatchRecord;
    private long existingDealWatchRecordId;

    private EditText keywordField;
    private RadioGroup typeRadioGroup;
    private RadioGroup expirationRadioGroup;
    private DatePicker expirationDatePicker;
    private boolean isCreateMode = true;
    private Date expirationDate;

    public CreateEditDealWatchFragment()
    {
        initDealWatchRecord();
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        menu.clear();
        if (isCreateMode)
        {
            inflater.inflate(R.menu.menu_deal_watch_create, menu);
        }
        else
        {
            inflater.inflate(R.menu.menu_deal_watch_edit, menu);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.deal_watch_create_edit, null);

        keywordField = (EditText) v.findViewById(R.id.keywords);
        typeRadioGroup = (RadioGroup) v.findViewById(R.id.typeRadioGroup);
        expirationRadioGroup = (RadioGroup) v.findViewById(R.id.expirationRadioGroup);
        expirationDatePicker = (DatePicker) v.findViewById(R.id.customDatePicker);

        if (!isCreateMode)
        {
            // TODO: Take the values from the existing record and write them to the UI
        }

        if (expirationRadioGroup.getCheckedRadioButtonId() == R.id.customDate)
        {
            expirationDatePicker.setVisibility(View.VISIBLE);
        }

        expirationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if (checkedId != R.id.customDate)
                {
                    expirationDatePicker.setVisibility(View.GONE);
                }
            }
        });

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        int strResId = (isCreateMode) ? R.string.create_filter : R.string.edit_filter;
        getActivity().setTitle(getActivity().getString(strResId));

    }

    public void save()
    {
        // Keywords
        dealWatchRecord.keywords = keywordField.getText().toString();

        // Type
        switch(typeRadioGroup.getCheckedRadioButtonId())
        {
            case R.id.AND_filter:
                dealWatchRecord.type = DealWatchRecord.FILTER_AND;
                break;
            case R.id.OR_filter:
                dealWatchRecord.type = DealWatchRecord.FILTER_OR;
                break;
            case R.id.EXACT_filter:
                dealWatchRecord.type = DealWatchRecord.FILTER_EXACT;
                break;
        }

        // Expiration Date
        switch (typeRadioGroup.getCheckedRadioButtonId())
        {
            case R.id._30days:
                expirationDate = getDateXDaysFromNow(30);
                break;
            case R.id._90days:
                expirationDate = getDateXDaysFromNow(90);
                break;
            case R.id._1year:
                expirationDate = getDateXDaysFromNow(365);
                break;
            case R.id.customDate:
                // Do nothing, this should already be set in the datepicker
                break;
        }

        dealWatchRecord.expiration = expirationDate;

        dealWatchRecord.save();
    }

    private Date getDateXDaysFromNow(int daysInTheFuture)
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, daysInTheFuture);
        return c.getTime();
    }

    private void initDealWatchRecord()
    {
        Bundle arguments = getArguments();
        if (arguments != null)
        {
            long id = (long) arguments.get(EXISTING_RECORD_ID);
            if (id >= 0)
            {
                this.isCreateMode = false;
                dealWatchRecord = DealWatchRecord.get(id);
            }
            else
            {
                dealWatchRecord = new DealWatchRecord();
            }
        }
        else
        {
            dealWatchRecord = new DealWatchRecord();
        }
    }
}
