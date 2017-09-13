package reti.com.passwordmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.greendao.database.Database;

import reti.com.passwordmanager.models.DaoMaster;
import reti.com.passwordmanager.models.DaoSession;
import reti.com.passwordmanager.models.PasswordEntry;
import reti.com.passwordmanager.models.PasswordEntryDao;

public class AddPasswordEntry extends AppCompatActivity {


    private EditText et_domain, et_username, et_password;
    private Button bt_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        bt_add = (Button) findViewById(R.id.bt_addPassword);

        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blockEditText(true);
                PasswordEntry newEntry = getPasswordEntryFromUI();
                if(newEntry!=null) {
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

    }

    private void resetUI(){
        et_domain.setText("");
        et_password.setText("");
        et_username.setText("");
    }

    private PasswordEntry getPasswordEntryFromUI(){
        String domain = et_domain.getText().toString();
        String username = et_username.getText().toString();
        String password = et_password.getText().toString();
        if(!domain.equals("") && !username.equals("") && !password.equals("")){
            return new PasswordEntry(domain,username,password);
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
}
