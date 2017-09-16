package reti.com.passwordmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import reti.com.passwordmanager.models.CategoryEntry;
import reti.com.passwordmanager.models.CategoryEntryDao;
import reti.com.passwordmanager.models.DaoMaster;
import reti.com.passwordmanager.models.DaoSession;
import reti.com.passwordmanager.models.PasswordEntry;
import reti.com.passwordmanager.models.PasswordEntryDao;
import reti.com.passwordmanager.utility.Utility;

import static android.R.attr.category;

public class HomeActivity extends AppCompatActivity {

    private ListView passwordListView;
    private TextView tv_noEntry;
    private Spinner spinnerCategory;
    private EditText findEditText;
    private PasswordAdapter arrayListPassword;

    private DaoSession daoSession;
    public static final String DB_FILE = "PASSWORD_MANAGER_DB";
    public static final String ITEM_TO_MODIFY = "ITEM_TO_MODIFY";
    public static final String CATEGORY_SAVED_INSTANCE = "CURRENT_CATEGORY";
    private String currentCategory;
    private SharedPreferences.Editor sharedEditor;
    private SharedPreferences shared;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        Utility.setCurrentTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle("");

        // remove focus from searchText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        shared = getSharedPreferences(CATEGORY_SAVED_INSTANCE,MODE_PRIVATE);
        sharedEditor = shared.edit();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,HomeActivity.DB_FILE);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

        // MARK: FINDBYVIEW ID ASSIGNMENT
        tv_noEntry = (TextView) findViewById(R.id.tv_noEntry);
        passwordListView = (ListView) findViewById(R.id.listView_password);
        spinnerCategory = (Spinner) findViewById(R.id.sp_choose_category);
        findEditText = (EditText) findViewById(R.id.findEditTextComponent);

        setSpinnerCategory();

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(CATEGORY_SAVED_INSTANCE)){
                String category = savedInstanceState.getString(CATEGORY_SAVED_INSTANCE);
                if(category!=null){
                    currentCategory = category;
                    setListViewOfCategory(category);
                }
            }

        }else{
            currentCategory = spinnerCategory.getSelectedItem().toString();
        }

        // override onItemSelected of spinnerCategoryChooser to set correct list view of password
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String categorySelected = spinnerCategory.getItemAtPosition(position).toString();
                setListViewOfCategory(categorySelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //show info of item
        passwordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final PasswordEntry itemClicked = (PasswordEntry) passwordListView.getItemAtPosition(position);

                AlertDialog.Builder alertShowPassword = new AlertDialog.Builder(HomeActivity.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
                alertShowPassword
                        .setTitle(itemClicked.getDominio())
                        .setMessage("Username: "+itemClicked.getUsername()+"\r\nPassword: "+itemClicked.getPassword())
                        .setNeutralButton("Fatto", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Modifica", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent modifyItem = new Intent(HomeActivity.this,ModifyPasswordActivity.class);
                                modifyItem.putExtra(ITEM_TO_MODIFY,itemClicked);
                                sharedEditor.putString(CATEGORY_SAVED_INSTANCE,getCurrentCategory());
                                sharedEditor.commit();
                                startActivityForResult(modifyItem,2);
                            }
                        })
                        .show();
            }
        });

        //remove item from list
        passwordListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final PasswordEntry itemClicked = (PasswordEntry) passwordListView.getItemAtPosition(position);
                AlertDialog.Builder alerRemoveItem = new AlertDialog.Builder(HomeActivity.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
                alerRemoveItem
                        .setTitle(R.string.login_logClick_title)
                        .setMessage("Dominio: " + itemClicked.dominio + "\r\nUsername: "+itemClicked.username)
                        .setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeFromDb(itemClicked);
                                setListViewOfCategory(spinnerCategory.getSelectedItem().toString());
                            }
                        })
                        .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

                return true;
            }
        });

        setCategoryFromShared();
        setEditTextActions();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedEditor.putString(CATEGORY_SAVED_INSTANCE,getCurrentCategory());
        sharedEditor.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outBundle){
        super.onSaveInstanceState(outBundle);
        outBundle.putString(CATEGORY_SAVED_INSTANCE,currentCategory);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.password_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String category = getCurrentCategory();
        switch (item.getItemId()){
            case R.id.addPassword:{
                Intent addNewPassword = new Intent(this,AddPasswordEntry.class);
                addNewPassword.putExtra(HomeActivity.CATEGORY_SAVED_INSTANCE,category);
                sharedEditor.putString(CATEGORY_SAVED_INSTANCE,category);
                sharedEditor.commit();
                startActivityForResult(addNewPassword,1);
                break;
            }
            case R.id.addCategory:{
                Intent addNewCategory = new Intent(this,AddNewCategory.class);
                addNewCategory.putExtra(HomeActivity.CATEGORY_SAVED_INSTANCE,category);
                sharedEditor.putString(CATEGORY_SAVED_INSTANCE,category);
                sharedEditor.commit();
                startActivityForResult(addNewCategory,1);
                break;
            }
            case R.id.settings:{
                Intent settingsIntent = new Intent(this,SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setSpinnerCategory();
            setCategoryFromShared();
        }
    }

    private void setSelectedItemSpinner(String category){
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

    private String getCurrentCategory(){
        return spinnerCategory.getSelectedItem().toString();
    }

    private void setSpinnerCategory(){
        //adapter for spinner category chooser
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                getAllCategory());

        //set adapter for spinner
        spinnerCategory.setAdapter(spinnerAdapter);
    }

    private void setCategoryFromShared(){
        if(shared.contains(CATEGORY_SAVED_INSTANCE)){
            String categoryFromShared = shared.getString(CATEGORY_SAVED_INSTANCE,null);
            if(categoryFromShared!=null){
                setSelectedItemSpinner(categoryFromShared);
                setListViewOfCategory(categoryFromShared);
            }
        }
    }

    private void setListViewOfCategory(String category){
        PasswordEntryDao daoPassword = daoSession.getPasswordEntryDao();
        //get all entry order descendent by EntryId
        List<PasswordEntry> entryList = daoPassword.queryBuilder().where(PasswordEntryDao.Properties.Category.eq(category)).orderAsc(PasswordEntryDao.Properties.Dominio).list();
        //create new adapter string for ListView
        arrayListPassword = new PasswordAdapter(this,R.layout.listview_adapter_passwordentry);
        //populate arrayAdapter
        if(entryList.size()>=1){
            tv_noEntry.setVisibility(View.GONE);
            for(PasswordEntry pw : entryList){
                arrayListPassword.add(pw);
            }
        }else {
            tv_noEntry.setText(R.string.no_password);
            tv_noEntry.setVisibility(View.VISIBLE);
        }
        passwordListView.setAdapter(arrayListPassword);
    }

    private void setListViewOfFinderInCategory(String keyword) {
        if(keyword.isEmpty()) {
            setListViewOfCategory(currentCategory);
        }else {
            // query database with keyword
            QueryBuilder<PasswordEntry> query = daoSession.getPasswordEntryDao().queryBuilder();
            query.where(PasswordEntryDao.Properties.Dominio.like(keyword+"%"));
            // make query and get the list of result
            List<PasswordEntry> passwordFiltered = query.orderAsc().list();

            //create new adapter string for ListView
            PasswordAdapter arrayListPassword = new PasswordAdapter(this,R.layout.listview_adapter_passwordentry);
            //populate arrayAdapter
            if(passwordFiltered.size()>=1){
                tv_noEntry.setVisibility(View.GONE);
                for(PasswordEntry pw : passwordFiltered){
                    arrayListPassword.add(pw);
                }
                passwordListView.setAdapter(arrayListPassword);
            }else {
                passwordListView.setAdapter(arrayListPassword);
                tv_noEntry.setText(R.string.no_find_result);
                tv_noEntry.setVisibility(View.VISIBLE);
                Log.d("FINDER LISTVIEW", "setListViewOfFinderInCategory: NESSUN RISCONTRO CON RICERCA");
            }
        }
    }

    private void removeFromDb(PasswordEntry pe){
        DeleteQuery<PasswordEntry> tableDeleteQuery = daoSession.queryBuilder(PasswordEntry.class)
                .where(
                PasswordEntryDao.Properties.Dominio.eq(pe.dominio),
                PasswordEntryDao.Properties.Username.eq(pe.username),
                PasswordEntryDao.Properties.Password.eq(pe.password)
        ).buildDelete();
        tableDeleteQuery.executeDeleteWithoutDetachingEntities();
        daoSession.clear();
    }

    private ArrayList<String> getAllCategory(){
        ArrayList<String> result = new ArrayList<>();
        CategoryEntryDao categoryEntryDao = daoSession.getCategoryEntryDao();
        if(categoryEntryDao.loadAll().size() != 0){
            List<CategoryEntry> entryList = categoryEntryDao.loadAll();
            for(CategoryEntry entry: entryList){
                result.add(entry.category);
            }
            return result;
        }else{
            categoryEntryDao.insert(new CategoryEntry("Default"));
            result.add("Default");
            return result;
        }
    }

    private void setEditTextActions() {

        findEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // get keyword insered
                String keyWord = s.toString();
                // set list view filtered by keyword
                setListViewOfFinderInCategory(keyWord);
            }
        });
    }
}
