package net.lapasa.rfdhotdealswidget.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;

import net.lapasa.rfdhotdealswidget.R;
import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;

import java.util.Calendar;
import java.util.Date;

public class CreateEditDealWatchFragment extends Fragment implements DialogInterface.OnClickListener
{
    public static final String EXISTING_RECORD_ID = "EXISTING_RECORD_ID";
    private static int CREATE_MODE = 0;
    private static int EDIT_MODE = 1;
    private DealWatchRecord dealWatchRecord;
    private long existingDealWatchRecordId;

    private EditText keywordField;
    private RadioGroup typeRadioGroup;
    private RadioGroup expirationRadioGroup;
    private DatePicker expirationDatePicker;
    private boolean isCreateMode = true;

    public CreateEditDealWatchFragment()
    {
        // Required empty constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle arguments = getArguments();
        if (arguments != null)
        {
            long id = arguments.getLong(EXISTING_RECORD_ID);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_save)
        {
            if (isValidForm())
            {
                save();
                getFragmentManager().popBackStack();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isValidForm()
    {
        boolean isValid = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("OK", this);

        // Check if keywords exists
        builder.setTitle("Error");
        String keywordsText = keywordField.getText().toString();
        if (keywordsText == null || keywordsText.isEmpty() || keywordsText.length() < 2)
        {
            isValid = false;
            builder.setMessage("Please provide a valid keyword(s)");
            builder.show();
        }
        else if (DealWatchRecord.isExistingFilter(keywordsText))
        {
            isValid = false;
            builder.setMessage("This filter already exists");
            builder.show();
        }

        return isValid;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.deal_watch_create_edit, null);

        keywordField = (EditText) v.findViewById(R.id.keywords);
        typeRadioGroup = (RadioGroup) v.findViewById(R.id.typeRadioGroup);
        typeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                onTypeCheckedChanged(group, checkedId);
            }
        });
        expirationRadioGroup = (RadioGroup) v.findViewById(R.id.expirationRadioGroup);
        expirationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                onExpirationCheckedChange(group, checkedId);
            }
        });

        expirationDatePicker = (DatePicker) v.findViewById(R.id.customDatePicker);

        if (!isCreateMode)
        {
            populateExistingValues();
        }
        else
        {
            typeRadioGroup.check(R.id.OR_filter);
        }

        if (expirationRadioGroup.getCheckedRadioButtonId() == R.id.customDate)
        {
            expirationDatePicker.setVisibility(View.VISIBLE);
        }
        else
        {
            expirationRadioGroup.check(R.id._90days);
            expirationDatePicker.setVisibility(View.GONE);
        }


        return v;
    }

    private void onTypeCheckedChanged(RadioGroup group, int checkedId)
    {
        // Type
        switch (checkedId)
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
    }


    /**
     * Take the values from the existing record and write them to the UI
     */
    private void populateExistingValues()
    {
        // Keywords
        keywordField.setText(dealWatchRecord.keywords);

        // Filter type
        populateType();

        // Expiration - Always Custom date when updating
        expirationRadioGroup.check(R.id.customDate);
        expirationDatePicker.setVisibility(View.VISIBLE);
    }

    private void onExpirationCheckedChange(RadioGroup group, int checkedId)
    {
        // Expiration Date
        switch (checkedId)
        {
            case R.id._30days:
                dealWatchRecord.expiration = getDateXDaysFromNow(30);
                break;
            case R.id._90days:
                dealWatchRecord.expiration = getDateXDaysFromNow(90);
                break;
            case R.id._1year:
                dealWatchRecord.expiration = getDateXDaysFromNow(365);
                break;
            case R.id.customDate:
                // Do nothing, this should already be set in the datepicker
                expirationDatePicker.setVisibility(View.VISIBLE);
                break;
        }

        if (checkedId != R.id.customDate)
        {
            expirationDatePicker.setVisibility(View.GONE);
        }
    }

    private void populateType()
    {
        long existingType = dealWatchRecord.type;
        if (existingType == DealWatchRecord.FILTER_AND)
        {
            typeRadioGroup.check(R.id.AND_filter);
        }
        else if (existingType == DealWatchRecord.FILTER_EXACT)
        {
            typeRadioGroup.check(R.id.EXACT_filter);
        }
        else if (existingType == DealWatchRecord.FILTER_OR)
        {
            typeRadioGroup.check(R.id.OR_filter);
        }
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
        dealWatchRecord.save();
    }

    private Date getDateXDaysFromNow(int daysInTheFuture)
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, daysInTheFuture);
        return c.getTime();
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        keywordField.requestFocus();
    }
}
