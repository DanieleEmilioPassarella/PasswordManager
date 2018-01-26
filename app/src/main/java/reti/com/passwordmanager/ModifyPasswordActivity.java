package reti.com.passwordmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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

public class ModifyPasswordActivity extends AppCompatActivity {

    private EditText et_domain_modify,et_username_modify,et_password_modify;
    private ImageView check_domain,check_username,check_password,check_category;
    private Spinner categorySpinner;
    private PasswordEntry itemToModify;
    private Button bt_update;
    private static String categoryOfOldItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Utility.setCurrentTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);

        setTitle("Home");

        Intent homeIntent = getIntent();
        itemToModify = (PasswordEntry) homeIntent.getSerializableExtra(HomeActivity.ITEM_TO_MODIFY);
        categoryOfOldItem = itemToModify.getCategory();

        et_domain_modify = (EditText) findViewById(R.id.et_domain_modify);
        et_username_modify = (EditText) findViewById(R.id.et_username_modify);
        et_password_modify = (EditText) findViewById(R.id.et_password_modify);
        check_domain = (ImageView) findViewById(R.id.iv_domain_check);
        check_username = (ImageView)findViewById(R.id.iv_username_check);
        check_password = (ImageView) findViewById(R.id.iv_password_check);
        check_category = (ImageView) findViewById(R.id.iv_category_modifyPassword);
        bt_update = (Button) findViewById(R.id.bt_update);
        categorySpinner = (Spinner) findViewById(R.id.sp_category_modifyPassword);


        initializeEditText();

        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
                getAllCategory());

        categorySpinner.setAdapter(spinnerAdapter);

        et_domain_modify.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0){
                    check_domain.setImageResource(R.drawable.ic_unchecked);
                }else check_domain.setImageResource(R.drawable.ic_update);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et_username_modify.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0){
                    check_username.setImageResource(R.drawable.ic_unchecked);
                }else check_username.setImageResource(R.drawable.ic_update);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et_password_modify.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0){
                    check_password.setImageResource(R.drawable.ic_unchecked);
                }else check_password.setImageResource(R.drawable.ic_update);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        bt_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PasswordEntry newItem = getModifiedItem();

                AlertDialog.Builder alertShowPassword = new AlertDialog.Builder(ModifyPasswordActivity.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
                alertShowPassword
                        .setTitle("Modifica Password")
                        .setMessage("Categoria: "+newItem.getCategory()+"\r\nDominio: "+newItem.getDominio()+"\r\nUsername: "+newItem.getUsername()+"\r\nPassword: "+newItem.getPassword())
                        .setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                blockEditText();
                                updatePasswordInDB(itemToModify,newItem);
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
        });

        et_password_modify.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    bt_update.performClick();
                    return true;
                }
                return false;
            }
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(categorySpinner.getItemAtPosition(position).toString().equals(categoryOfOldItem)){
                    check_category.setImageResource(R.drawable.ic_check);
                }else{
                    check_category.setImageResource(R.drawable.ic_update);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //set category for spinner
        setSpinnerCategory(categoryOfOldItem);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    private void setSpinnerCategory(String category){
        int numberOfItem = categorySpinner.getAdapter().getCount();
        int positionCategory = 0;
        for(int i = 0; i < numberOfItem; i++){
            if(categorySpinner.getItemAtPosition(i).toString().equals(category)){
                positionCategory = i;
                break;
            }
        }
        categorySpinner.setSelection(positionCategory);
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

    private void updatePasswordInDB(PasswordEntry oldItem,PasswordEntry newItem){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,HomeActivity.DB_FILE);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        PasswordEntryDao daoPassword = daoSession.getPasswordEntryDao();
        removeOldItemFromDB(oldItem);
        daoPassword.insert(newItem);
    }

    private void removeOldItemFromDB(PasswordEntry oldItem){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,HomeActivity.DB_FILE);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();

        DeleteQuery<PasswordEntry> tableDeleteQuery = daoSession.queryBuilder(PasswordEntry.class)
                .where(
                        PasswordEntryDao.Properties.Dominio.eq(oldItem.getDominio()),
                        PasswordEntryDao.Properties.Username.eq(oldItem.getUsername()),
                        PasswordEntryDao.Properties.Password.eq(oldItem.getPassword())
                ).buildDelete();
        tableDeleteQuery.executeDeleteWithoutDetachingEntities();
        daoSession.clear();
    }

    private PasswordEntry getModifiedItem(){
        String dominio = et_domain_modify.getText().toString();
        String username = et_username_modify.getText().toString();
        String password = et_password_modify.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();
        return new PasswordEntry(dominio,username,password,category);
    }

    private void initializeEditText(){
        et_domain_modify.setText(itemToModify.getDominio());
        et_username_modify.setText(itemToModify.getUsername());
        et_password_modify.setText(itemToModify.getPassword());
    }

    private void blockEditText(){
        et_domain_modify.setFocusable(false);
        et_username_modify.setFocusable(false);
        et_password_modify.setFocusable(false);
        bt_update.setEnabled(false);
    }
}
