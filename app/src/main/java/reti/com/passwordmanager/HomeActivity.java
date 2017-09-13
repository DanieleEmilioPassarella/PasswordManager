package reti.com.passwordmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import reti.com.passwordmanager.models.DaoMaster;
import reti.com.passwordmanager.models.DaoSession;
import reti.com.passwordmanager.models.PasswordEntry;
import reti.com.passwordmanager.models.PasswordEntryDao;

public class HomeActivity extends AppCompatActivity {

    private ListView passwordListView;
    private TextView tv_noEntry;

    private DaoSession daoSession;
    public static final String DB_FILE = "PASSWORD_MANAGER_DB";
    public static final String ITEM_TO_MODIFY = "ITEM_TO_MODIFY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle("Password Manager");


        tv_noEntry = (TextView) findViewById(R.id.tv_noEntry);

        passwordListView = (ListView) findViewById(R.id.listView_password);

        setListView();

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
                        .setTitle("Remove Item")
                        .setMessage("Item: "+itemClicked.dominio+", "+itemClicked.username+ "\r\nDo you want remove it?")
                        .setPositiveButton("Si, Cancella", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeFromDb(itemClicked);
                                setListView();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.password_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.addPassword:{
                //do something
                Intent addNewPassword = new Intent(this,AddPasswordEntry.class);
                startActivityForResult(addNewPassword,1);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            setListView();
        }
    }

    private void setListView(){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,DB_FILE);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        PasswordEntryDao daoPassword = daoSession.getPasswordEntryDao();
        //get all entry order descendent by EntryId
        List<PasswordEntry> entryList = daoPassword.queryBuilder().orderAsc(PasswordEntryDao.Properties.Dominio).list();
        //create new adapter string for ListView
        PasswordAdapter arrayListPassword = new PasswordAdapter(this,R.layout.listview_adapter_passwordentry);
        //populate arrayAdapter
        if(entryList.size()>=1){
            tv_noEntry.setVisibility(View.GONE);
            for(PasswordEntry pw : entryList){
                arrayListPassword.add(pw);
            }
        }else {
            tv_noEntry.setText("No Entry Add Once");
            tv_noEntry.setVisibility(View.VISIBLE);
        }
        passwordListView.setAdapter(arrayListPassword);
    }

    private void removeFromDb(PasswordEntry pe){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,DB_FILE);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

        DeleteQuery<PasswordEntry> tableDeleteQuery = daoSession.queryBuilder(PasswordEntry.class)
                .where(
                PasswordEntryDao.Properties.Dominio.eq(pe.dominio),
                PasswordEntryDao.Properties.Username.eq(pe.username),
                PasswordEntryDao.Properties.Password.eq(pe.password)
        ).buildDelete();
        tableDeleteQuery.executeDeleteWithoutDetachingEntities();
        daoSession.clear();
    }
}
