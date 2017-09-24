package reti.com.passwordmanager;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import reti.com.passwordmanager.utility.Utility;

public class FilterActivity extends AppCompatActivity {

    // Properties
    private Button applyFilter;
    private Button resetFilter;
    private Spinner orderBySpinner;
    private Spinner sortBySpinner;
    private SharedPreferences shared;
    private SharedPreferences.Editor sharedEditor;
    private String[] orderArraySharedPreference;
    private String[] sortArraySharedPreference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set current theme
        Utility.setCurrentTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Outlets
        applyFilter = (Button) findViewById(R.id.filter_applyButton);
        resetFilter = (Button) findViewById(R.id.filter_resetButton);
        orderBySpinner = (Spinner) findViewById(R.id.filter_orderBySpinner);
        sortBySpinner = (Spinner) findViewById(R.id.filter_sortBySpinner);

        // Shared Preferences
        shared = getSharedPreferences(Utility.FILTER_SHAREDPREFERENCES_KEY,MODE_PRIVATE);
        sharedEditor = shared.edit();

        // get string array from resource
        orderArraySharedPreference = getResources().getStringArray(R.array.filter_order);
        sortArraySharedPreference = getResources().getStringArray(R.array.filter_sorting);


        // check and set from shared preferences the spinners
        checkAndSetSharedPreferences();

        // Actions
        applyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save in shared preferences the choosed filter
                saveChoosedFilter();
                setResult(RESULT_OK);
                finish();
            }
        });

        resetFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // make alert and reset filter
                AlertDialog.Builder alertResetDefaultFilter = new AlertDialog.Builder(FilterActivity.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
                alertResetDefaultFilter
                        .setTitle(R.string.filter_alert_title)
                        .setMessage(R.string.filter_alert_message)
                        .setPositiveButton(R.string.generic_confirmMessage, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                writeDefaultFilter();
                                setDefaultSpinner();
                                setResult(RESULT_OK);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.generic_cancelMessage, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });

        orderBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    } // END ON_CREATE METHOD

    private void checkAndSetSharedPreferences(){

        if(shared.getString(Utility.ORDERBY_FILTER_KEY,null) != null) {

            if(shared.getString(Utility.SORTBY_FILTER_KEY,null) != null) {

                // if order and sort isn't null set spinner from shared preferences
                setSpinnerBySharedPreferences();

            }else {
                writeDefaultFilter();
                setDefaultSpinner();
            }
        }else {
            writeDefaultFilter();
            setDefaultSpinner();
        }
    }

    private void setDefaultSpinner() {
        orderBySpinner.setSelection(0);
        sortBySpinner.setSelection(0);
    }

    private void setSpinnerBySharedPreferences() {
        String orderPreference = shared.getString(Utility.ORDERBY_FILTER_KEY,null);
        String sortPreference = shared.getString(Utility.SORTBY_FILTER_KEY,null);

        if(orderPreference.equals(orderArraySharedPreference[0])) {
            orderBySpinner.setSelection(0);
        }else {
            orderBySpinner.setSelection(1);
        }

        if(sortPreference.equals(sortArraySharedPreference[0])) {
            sortBySpinner.setSelection(0);
        }else {
            sortBySpinner.setSelection(1);
        }
    }

    private void writeDefaultFilter() {
        // write in shared preferences first item of array string from strings.xml
        sharedEditor.putString(Utility.ORDERBY_FILTER_KEY, orderArraySharedPreference[0]);

        sharedEditor.putString(Utility.SORTBY_FILTER_KEY,sortArraySharedPreference[0]);

        sharedEditor.commit();
    }

    private void saveChoosedFilter() {
        String orderChoosed = orderArraySharedPreference[orderBySpinner.getSelectedItemPosition()];
        String sortChoosed = sortArraySharedPreference[sortBySpinner.getSelectedItemPosition()];
        sharedEditor.putString(Utility.ORDERBY_FILTER_KEY, orderChoosed);
        sharedEditor.putString(Utility.SORTBY_FILTER_KEY, sortChoosed);
        sharedEditor.commit();
    }
}
