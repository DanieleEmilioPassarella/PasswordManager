package reti.com.passwordmanager;

import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.DeleteQuery;

import reti.com.passwordmanager.models.DaoMaster;
import reti.com.passwordmanager.models.DaoSession;
import reti.com.passwordmanager.models.PasswordEntry;
import reti.com.passwordmanager.models.PasswordEntryDao;

public class ModifyPasswordActivity extends AppCompatActivity {

    private EditText et_domain_modify,et_username_modify,et_password_modify;
    private ImageView check_domain,check_username,check_password;
    private PasswordEntry itemToModify;
    private Button bt_update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);

        setTitle("Home");

        Intent homeIntent = getIntent();
        itemToModify = (PasswordEntry) homeIntent.getSerializableExtra(HomeActivity.ITEM_TO_MODIFY);

        et_domain_modify = (EditText) findViewById(R.id.et_domain_modify);
        et_username_modify = (EditText) findViewById(R.id.et_username_modify);
        et_password_modify = (EditText) findViewById(R.id.et_password_modify);
        check_domain = (ImageView) findViewById(R.id.iv_domain_check);
        check_username = (ImageView)findViewById(R.id.iv_username_check);
        check_password = (ImageView) findViewById(R.id.iv_password_check);
        bt_update = (Button) findViewById(R.id.bt_update);

        initializeEditText();

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
                PasswordEntry newItem = getModifiedItem();
                updatePasswordInDB(itemToModify,newItem);
                setResult(RESULT_OK);
            }
        });

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
                        PasswordEntryDao.Properties.Dominio.eq(oldItem.dominio),
                        PasswordEntryDao.Properties.Username.eq(oldItem.username),
                        PasswordEntryDao.Properties.Password.eq(oldItem.password)
                ).buildDelete();
        tableDeleteQuery.executeDeleteWithoutDetachingEntities();
        daoSession.clear();
    }

    private PasswordEntry getModifiedItem(){
        String dominio = et_domain_modify.getText().toString();
        String username = et_username_modify.getText().toString();
        String password = et_password_modify.getText().toString();
        return new PasswordEntry(dominio,username,password);
    }

    private void initializeEditText(){
        et_domain_modify.setText(itemToModify.getDominio());
        et_username_modify.setText(itemToModify.getUsername());
        et_password_modify.setText(itemToModify.getPassword());
    }

    private void setCheckedField(){
        check_domain.setImageResource(R.drawable.ic_check);
        check_username.setImageResource(R.drawable.ic_check);
        check_password.setImageResource(R.drawable.ic_check);
    }
}
