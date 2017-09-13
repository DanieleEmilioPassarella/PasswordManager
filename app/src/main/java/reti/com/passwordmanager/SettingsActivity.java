package reti.com.passwordmanager;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Debug;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import reti.com.passwordmanager.models.CategoryEntry;
import reti.com.passwordmanager.models.CategoryEntryDao;
import reti.com.passwordmanager.models.DaoMaster;
import reti.com.passwordmanager.models.DaoSession;
import reti.com.passwordmanager.models.PasswordEntry;
import reti.com.passwordmanager.models.PasswordEntryDao;
import reti.com.passwordmanager.utility.Utility;

public class SettingsActivity extends AppCompatActivity {

    //MARK: properties
    private Button buttonResetPin;
    private Button buttonConfirmPin;
    private Button buttonChangeTheme;
    private Button buttonExportPassword;
    private TextView tv_theme;
    private EditText et_oldPin;
    private EditText et_newPin1;
    private EditText et_newPin2;
    private ImageView iv_oldPin;
    private ImageView iv_newPin1;
    private ImageView iv_newPin2;
    private Boolean validatorOldPin = false;
    private Boolean validatorNewPin1 = false;
    private Boolean validatorNewPin2 = false;
    private Context selfContext;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        selfContext = this;
        Utility.setCurrentTheme(selfContext);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //MARK: outlets
        buttonResetPin = (Button) findViewById(R.id.buttonResetPin);
        buttonConfirmPin = (Button) findViewById(R.id.buttonConfirmPin);
        buttonChangeTheme = (Button) findViewById(R.id.bt_changeTheme);
        buttonExportPassword = (Button) findViewById(R.id.bt_esportaPassword);
        tv_theme = (TextView) findViewById(R.id.tv_theme);
        et_oldPin = (EditText) findViewById(R.id.oldPinET);
        et_newPin1 = (EditText) findViewById(R.id.newPinET1);
        et_newPin2 = (EditText) findViewById(R.id.newPinET2);
        iv_oldPin = (ImageView) findViewById(R.id.iv_oldPin);
        iv_newPin1 = (ImageView) findViewById(R.id.iv_newPin1);
        iv_newPin2 = (ImageView) findViewById(R.id.iv_newPin2);

        hideResetPinView();
        setTitleTheme();
        disableConfirmResetPinButton();
        setEditTextAction();

        //MARK: button action
        buttonResetPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleResetPinView();
            }
        });

        buttonConfirmPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String newPin;
                if((newPin = validateResetPin()) != null){
                    // all is valid try to set new pin
                    disableConfirmResetPinButton();
                    hideResetPinView();
                    if(LoginActivity.setNewPin(newPin)){
                        Toast.makeText(getApplicationContext(),"Pin Updated",Toast.LENGTH_LONG).show();
                        enableResetPinButton();
                    }else{
                        Log.d("NewPasswordSetter", "writing new pin failed");
                        Toast.makeText(getApplicationContext(),"Pin Updated",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Log.d("VAIDATOR", "validation failed");
                }
            }
        });

        buttonConfirmPin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    hideKeyboard();
                    String newPin;
                    if((newPin = validateResetPin()) != null){
                        // all is valid try to set new pin
                        disableConfirmResetPinButton();
                        hideResetPinView();
                        if(LoginActivity.setNewPin(newPin)){
                            Toast.makeText(getApplicationContext(),"Pin Updated",Toast.LENGTH_LONG).show();
                            enableResetPinButton();
                        }else{
                            Log.d("NewPasswordSetter", "writing new pin failed");
                            Toast.makeText(getApplicationContext(),"Pin Updated",Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Log.d("VALIDATOR", "validation failed");
                    }
                }

            }
        });

        buttonChangeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.toggleTheme(getApplicationContext());
                recreate();
            }
        });

        buttonExportPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(getApplicationContext(),HomeActivity.DB_FILE);
                Database db = helper.getWritableDb();
                DaoSession daoSession = new DaoMaster(db).newSession();

                List<PasswordEntry> passwordEntryList = daoSession.getPasswordEntryDao().loadAll();

                try {
                    FileOutputStream fos = openFileOutput(Utility.PASSWORD_MANAGER_FILE,MODE_PRIVATE);
                    for(PasswordEntry password: passwordEntryList){
                        String row = "Dominio: "+password.dominio+" - Username: "+password.username+" - Password: "+password.password+"\r\n";
                        fos.write(row.getBytes());
                    }
                    fos.flush();
                    fos.close();
                    Toast.makeText(getApplicationContext(), "File PasswordManager saved:\r\n"+passwordEntryList.size()+" password.",Toast.LENGTH_LONG).show();

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateUpTo(new Intent(this,HomeActivity.class));
    }

    private void toggleResetPinView(){
        if(buttonConfirmPin.getVisibility() == View.GONE) {
            showResetPinView();
        }else {
            hideResetPinView();
        }
    }

    private void hideResetPinView(){
        buttonConfirmPin.setVisibility(View.GONE);
        disableConfirmResetPinButton();
        et_oldPin.setVisibility(View.GONE);
        et_newPin1.setVisibility(View.GONE);
        et_newPin2.setVisibility(View.GONE);
        iv_oldPin.setVisibility(View.GONE);
        iv_newPin1.setVisibility(View.GONE);
        iv_newPin2.setVisibility(View.GONE);
        et_oldPin.setText("");
        et_newPin1.setText("");
        et_newPin2.setText("");
    }

    private void showResetPinView(){
        buttonConfirmPin.setVisibility(View.VISIBLE);
        et_oldPin.setVisibility(View.VISIBLE);
        et_newPin1.setVisibility(View.VISIBLE);
        et_newPin2.setVisibility(View.VISIBLE);
        iv_oldPin.setVisibility(View.VISIBLE);
        iv_newPin1.setVisibility(View.VISIBLE);
        iv_newPin2.setVisibility(View.VISIBLE);
    }

    private void disableResetPinButton(){
        buttonResetPin.setAlpha(0.5f);
        buttonResetPin.setFocusable(false);
    }

    private void enableResetPinButton(){
        buttonResetPin.setAlpha(1f);
        buttonResetPin.setFocusable(true);
        buttonResetPin.setFocusableInTouchMode(true);
    }

    private void disableConfirmResetPinButton(){
        buttonConfirmPin.setAlpha(0.5f);
        buttonConfirmPin.setFocusable(false);
    }

    private void enableConfirmResetPinButton(){
        buttonConfirmPin.setAlpha(1f);
        buttonConfirmPin.setFocusable(true);
        buttonConfirmPin.setFocusableInTouchMode(true);
    }

    private void setTitleTheme(){
        String currentTheme = Utility.getTitleCurrentTheme(this);
        tv_theme.setText("\""+currentTheme+"\""+" Theme");
    }

    private void setEditTextAction(){

        et_oldPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0) {
                    iv_oldPin.setImageResource(R.drawable.ic_unchecked);
                    validatorOldPin = false;
                    disableConfirmResetPinButton();
                }
                else{
                    if(s.toString().equals(LoginActivity.getPin())) {
                        iv_oldPin.setImageResource(R.drawable.ic_check);
                        validatorOldPin = true;
                        validateResetPin();
                    }else {
                        iv_oldPin.setImageResource(R.drawable.ic_unchecked);
                        validatorOldPin = false;
                        disableConfirmResetPinButton();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        et_newPin1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0) {
                    iv_newPin1.setImageResource(R.drawable.ic_unchecked);
                    validatorNewPin1 = false;
                    disableConfirmResetPinButton();
                }
                else{
                    if(!et_newPin2.getText().toString().isEmpty()){
                        // second editText not empty
                        if(s.toString().equals(et_newPin2.getText().toString())){
                            // second editText equal to this
                            iv_newPin1.setImageResource(R.drawable.ic_check);
                            validatorNewPin1 = true;
                            validateResetPin();
                        }else{
                            // second EditText not empty & not equal to this
                            iv_newPin1.setImageResource(R.drawable.ic_unchecked);
                            validatorNewPin1 = false;
                            disableConfirmResetPinButton();
                        }
                    }else{
                        iv_newPin1.setImageResource(R.drawable.ic_check);
                        validatorNewPin1 = true;
                        validateResetPin();
                    }
                }
            }
        });

        et_newPin2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0) {
                    iv_newPin2.setImageResource(R.drawable.ic_unchecked);
                    validatorNewPin2 = false;
                    disableConfirmResetPinButton();
                }
                else{
                    if(s.toString().equals(et_newPin1.getText().toString())) {
                        iv_newPin2.setImageResource(R.drawable.ic_check);
                        validatorNewPin2 = true;
                        validateResetPin();
                    }else {
                        iv_newPin2.setImageResource(R.drawable.ic_unchecked);
                        validatorNewPin2 = false;
                        disableConfirmResetPinButton();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        et_newPin2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    buttonConfirmPin.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(),0);
    }

    private String validateResetPin(){
        if(validatorOldPin && validatorNewPin1 && validatorNewPin2){
            enableConfirmResetPinButton();
            return et_newPin2.getText().toString();
        }else {
            disableConfirmResetPinButton();
            return null;
        }
    }

}
