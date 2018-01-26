package reti.com.passwordmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.List;

import reti.com.passwordmanager.models.CategoryEntry;
import reti.com.passwordmanager.models.CategoryEntryDao;
import reti.com.passwordmanager.models.DaoMaster;
import reti.com.passwordmanager.models.DaoSession;
import reti.com.passwordmanager.models.PasswordEntry;
import reti.com.passwordmanager.models.PasswordEntryDao;
import reti.com.passwordmanager.utility.Utility;

public class AddPasswordEntry extends AppCompatActivity {


    private EditText et_domain, et_username, et_password;
    private ImageView iv_domain,iv_username,iv_password,iv_spinner;
    private Button bt_add;
    private Spinner spinnerCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Utility.setCurrentTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_password_entry);
        setTitle("Home");

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,HomeActivity.DB_FILE);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        final PasswordEntryDao daoPassword = daoSession.getPasswordEntryDao();

        et_domain = (EditText) findViewById(R.id.et_domain);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        iv_domain = (ImageView) findViewById(R.id.iv_domain_addPassword);
        iv_username = (ImageView) findViewById(R.id.iv_username_addPassword);
        iv_password = (ImageView) findViewById(R.id.iv_password_addPassword);
        iv_spinner = (ImageView) findViewById(R.id.iv_spinner_addPassword);
        bt_add = (Button) findViewById(R.id.bt_addPassword);
        spinnerCategory = (Spinner) findViewById(R.id.sp_category_chooser_AddPassword);

        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
                getAllCategory());

        spinnerCategory.setAdapter(spinnerAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = spinnerCategory.getItemAtPosition(position).toString();
                if(category.equals("Default")) iv_spinner.setImageResource(R.drawable.ic_menu);
                else iv_spinner.setImageResource(R.drawable.ic_check);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blockEditText(true);
                PasswordEntry newEntry = getPasswordEntryFromUI();
                if(newEntry!=null) {

                    newEntry.setId(newEntry.getId() + 1 );
                    daoPassword.insert(newEntry);
                    setResult(RESULT_OK);
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(),"Invalid Entry, please check field.",Toast.LENGTH_LONG).show();
                    //Snackbar.make(v,"Invalid Entry, please check field.",Snackbar.LENGTH_LONG).show();
                    blockEditText(false);
                }
            }
        });

        et_domain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0) iv_domain.setImageResource(R.drawable.ic_unchecked);
                else{
                    iv_domain.setImageResource(R.drawable.ic_check);
                }
            }
        });

        et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0) iv_username.setImageResource(R.drawable.ic_unchecked);
                else{
                    iv_username.setImageResource(R.drawable.ic_check);
                }
            }
        });

        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0) iv_password.setImageResource(R.drawable.ic_unchecked);
                else{
                    iv_password.setImageResource(R.drawable.ic_check);
                }
            }
        });

        et_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    bt_add.performClick();
                    return true;
                }
                return false;
            }
        });

        Intent homeIntent = getIntent();
        String category = homeIntent.getStringExtra(HomeActivity.CATEGORY_SAVED_INSTANCE);
        setSpinnerCategory(category);
    }

    private void setSpinnerCategory(String category){
        int numberOfItem = spinnerCategory.getAdapter().getCount();
        int positionCategory = 0;
        for(int i = 0; i < numberOfItem; i++){
            if(spinnerCategory.getItemAtPosition(i).toString().equals(category)){
                positionCategory = i;
                break;
            }
        }
        spinnerCategory.setSelection(positionCategory);
    }

    private PasswordEntry getPasswordEntryFromUI(){
        String domain = et_domain.getText().toString();
        String username = et_username.getText().toString();
        String password = et_password.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        if(!domain.equals("") && !username.equals("") && !password.equals("")){
            return new PasswordEntry(domain,username,password,category);
        }else return null;
    }

    private void blockEditText(boolean block){
        if(block){
            et_domain.setFocusable(false);
            et_username.setFocusable(false);
            et_password.setFocusable(false);
        }else {
            et_domain.setFocusableInTouchMode(true);
            et_username.setFocusableInTouchMode(true);
            et_password.setFocusableInTouchMode(true);
        }
    }

    private ArrayList<String> getAllCategory(){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,HomeActivity.DB_FILE);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        CategoryEntryDao categoryEntryDao = daoSession.getCategoryEntryDao();
        ArrayList<String> result = new ArrayList<>();
        List<CategoryEntry> entryList = categoryEntryDao.loadAll();
        for(CategoryEntry entry: entryList){
            result.add(entry.category);
        }
        return result;
    }
}
