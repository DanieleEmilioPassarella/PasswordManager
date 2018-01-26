package reti.com.passwordmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.DeleteQuery;

import java.util.ArrayList;
import java.util.List;

import reti.com.passwordmanager.models.CategoryEntry;
import reti.com.passwordmanager.models.CategoryEntryDao;
import reti.com.passwordmanager.models.DaoMaster;
import reti.com.passwordmanager.models.DaoSession;
import reti.com.passwordmanager.models.PasswordEntry;
import reti.com.passwordmanager.models.PasswordEntryDao;
import reti.com.passwordmanager.utility.Utility;

public class AddNewCategory extends AppCompatActivity {

    private EditText et_category;
    private ImageView iv_category_create,iv_category_delete;
    private Button bt_createCategory,bt_deleteCategory;
    private Spinner spinnerCategory;

    private Spinner firstCategory;
    private Spinner secondCategory;
    private Button moveButton;
    private ImageView iv_firstCategory;
    private ImageView iv_secondCategory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Utility.setCurrentTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_category);

        et_category = (EditText) findViewById(R.id.et_categoryName_addCategory);
        iv_category_create = (ImageView) findViewById(R.id.iv_category_create);
        iv_category_delete = (ImageView) findViewById(R.id.iv_category_delete);
        bt_createCategory = (Button) findViewById(R.id.bt_addCategory);
        bt_deleteCategory = (Button) findViewById(R.id.bt_deleteCategory);
        spinnerCategory = (Spinner) findViewById(R.id.sp_category_manageCategory);

        firstCategory = (Spinner) findViewById(R.id.sp_firstCategory);
        secondCategory = (Spinner) findViewById(R.id.sp_secondCategory);
        moveButton = (Button) findViewById(R.id.btn_copy);
        iv_firstCategory =  (ImageView) findViewById(R.id.iv_firstCategory);
        iv_firstCategory = (ImageView)  findViewById(R.id.iv_secondCategory);


        // set default button not enabled
        disableCreate();
        disableDelete();
        disableMove();
        disableSecondCategory();

        // set spinner data
        setDeleteSpinner();
        setFirstCategorySpinner();

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = spinnerCategory.getItemAtPosition(position).toString();
                if(!category.equals("Default")){
                    iv_category_delete.setImageResource(R.drawable.ic_check);
                    bt_deleteCategory.setFocusable(true);
                    enableDelete();
                }else{
                    iv_category_delete.setImageResource(R.drawable.ic_unchecked);
                    disableDelete();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        et_category.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                iv_category_create.setImageResource(R.drawable.ic_update);
                enableCreate();
            }
        });

        bt_deleteCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String categorySelected = spinnerCategory.getSelectedItem().toString();
                if(categorySelected.equals("Default")) {
                    bt_deleteCategory.setFocusable(false);
                    Toast.makeText(AddNewCategory.this,"Impossibile eliminare categoria Default",Toast.LENGTH_LONG).show();
                }else{
                    AlertDialog.Builder alerRemoveItem = new AlertDialog.Builder(AddNewCategory.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
                    alerRemoveItem
                            .setTitle("Remove Category")
                            .setMessage("Remove :"+categorySelected+"\r\nAre you sure?")
                            .setPositiveButton("Si, Elimina", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    removeCategoryFromDB(categorySelected);
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
            }
        });

        bt_createCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = et_category.getText().toString();
                if(category.equals("")) Toast.makeText(getApplicationContext(),"Invalid Category",Toast.LENGTH_LONG).show();
                else {
                    createCategory(category);
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        firstCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = firstCategory.getItemAtPosition(position).toString();
                if(! category.equals("Default")){
                    setSecondCategorySpinner();
                    enableSecondCategory();
                }else{
                    disableMove();
                    disableSecondCategory();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        secondCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String firstCategorySelected = firstCategory.getItemAtPosition(firstCategory.getSelectedItemPosition()).toString();
                String secondCategorySelected = secondCategory.getItemAtPosition(position).toString();
                if(firstCategorySelected.equals(secondCategorySelected)){
                    disableMove();
                }else {
                    enableMove();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertShowPassword = new AlertDialog.Builder(AddNewCategory.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
                alertShowPassword
                        .setTitle(R.string.category_alert_move_title)
                        .setMessage(R.string.category_alert_move_message)
                        .setPositiveButton(R.string.generic_yesMessage, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            // TODO: implements move logics
                                movePassword();
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

    } // END CREATE METHOD

    private void setFirstCategorySpinner(){
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                getAllCategory());
        firstCategory.setAdapter(spinnerAdapter);
    }

    private void setSecondCategorySpinner(){
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                getSecondCateogrySpinnerData());
        secondCategory.setAdapter(spinnerAdapter);
    }

    private void setDeleteSpinner(){
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                getAllCategory());
        spinnerCategory.setAdapter(spinnerAdapter);
    }

    private void enableSecondCategory(){
        secondCategory.setEnabled(true);
    }

    private void disableSecondCategory(){
        secondCategory.setEnabled(false);
    }

    private void enableMove(){
        moveButton.setEnabled(true);
    }

    private void disableMove(){
        moveButton.setEnabled(false);
    }

    private void enableCreate(){
        bt_createCategory.setEnabled(true);
    }

    private void disableCreate(){
        bt_createCategory.setEnabled(false);
    }

    private void enableDelete(){
        bt_deleteCategory.setEnabled(true);
    }

    private void disableDelete(){
        bt_deleteCategory.setEnabled(false);
    }

    private void createCategory(String category){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,HomeActivity.DB_FILE);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        CategoryEntryDao categoryEntry = daoSession.getCategoryEntryDao();
        long count = categoryEntry.queryBuilder().where(CategoryEntryDao.Properties.Category.eq(category)).count();
        if(count == 0) categoryEntry.insert(new CategoryEntry(category));
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

    private ArrayList<String> getSecondCateogrySpinnerData(){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,HomeActivity.DB_FILE);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        CategoryEntryDao categoryEntryDao = daoSession.getCategoryEntryDao();
        ArrayList<String> result = new ArrayList<>();
        List<CategoryEntry> entryList = categoryEntryDao.loadAll();
        // get first category selected
        String firstCategorySelected = firstCategory.getItemAtPosition(firstCategory.getSelectedItemPosition()).toString();

        for(CategoryEntry entry: entryList){
            if(! entry.category.equals(firstCategorySelected)) {
                result.add(entry.category);
            }
        }
        return result;
    }

    private void movePassword(){
        String firstCategorySelected = firstCategory.getItemAtPosition(firstCategory.getSelectedItemPosition()).toString();
        String secondCategorySelected = secondCategory.getItemAtPosition(secondCategory.getSelectedItemPosition()).toString();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,HomeActivity.DB_FILE);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        PasswordEntryDao passwordEntryDao = daoSession.getPasswordEntryDao();
        List<PasswordEntry> passwords = passwordEntryDao.queryBuilder().where(PasswordEntryDao.Properties.Category.eq(firstCategorySelected)).list();
        for(PasswordEntry password: passwords){
            password.setCategory(secondCategorySelected);
            passwordEntryDao.update(password);
        }

        //daoSession.clear();
    }

    private void removeCategoryFromDB(String category){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,HomeActivity.DB_FILE);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        DeleteQuery<CategoryEntry> deleteCategory = daoSession.queryBuilder(CategoryEntry.class)
                .where(
                        CategoryEntryDao.Properties.Category.eq(category)
                ).buildDelete();
        deleteCategory.executeDeleteWithoutDetachingEntities();
        DeleteQuery<PasswordEntry> deletePasswordOfCategory = daoSession.queryBuilder(PasswordEntry.class)
                .where(
                        PasswordEntryDao.Properties.Category.eq(category)
                ).buildDelete();
        deletePasswordOfCategory.executeDeleteWithoutDetachingEntities();
        daoSession.clear();
    }

}
