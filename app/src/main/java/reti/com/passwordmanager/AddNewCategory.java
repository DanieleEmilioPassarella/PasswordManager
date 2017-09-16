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

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                getAllCategory());
        spinnerCategory.setAdapter(spinnerAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = spinnerCategory.getItemAtPosition(position).toString();
                if(!category.equals("Default")){
                    iv_category_delete.setImageResource(R.drawable.ic_check);
                    bt_deleteCategory.setFocusable(true);
                }else{
                    iv_category_delete.setImageResource(R.drawable.ic_unchecked);
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
                            .setTitle(R.string.category_title)
                            .setMessage(getString(R.string.category_message1) +categorySelected +"\r\n"+ getString(R.string.category_message2))
                            .setPositiveButton("Si, Elimina", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    removeCategoryFromDB(categorySelected);
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            })
                            .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
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
                if(category.equals("")) Toast.makeText(getApplicationContext(),"Nome categoria non valido.",Toast.LENGTH_LONG).show();
                else {
                    createCategory(category);
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });


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
