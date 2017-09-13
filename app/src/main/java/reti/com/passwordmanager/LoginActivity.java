package reti.com.passwordmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import reti.com.passwordmanager.utility.Utility;

public class LoginActivity extends AppCompatActivity {

    //MARK: properties
    private static final String LOGIN_FILE = "LOGIN_FILE";
    private static final String PIN_KEY = "PIN_KEY";
    private static final String SECRET_QUESTION = "SECRET_QUESTION";
    private static final String SECRET_ANSWARE = "SECRET_ANSWARE";
    private static boolean firstAccess = false;
    private EditText et_pin;
    private TextView tv_title;
    private Button bt_login;
    private Button bt_recuperaPin;
    private Spinner sp_secretQuestion;
    private EditText et_secretAnsware;
    private static SharedPreferences.Editor she;
    private static SharedPreferences sp;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        Utility.setCurrentTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Password Manager");

        //MARK: outlets
        sp = getSharedPreferences(LOGIN_FILE, Context.MODE_PRIVATE);
        she = sp.edit();

        et_pin = (EditText) findViewById(R.id.et_pin);
        tv_title = (TextView) findViewById(R.id.tv_title);
        bt_login = (Button) findViewById(R.id.bt_login);
        bt_recuperaPin = (Button) findViewById(R.id.bt_recuperaPin);
        sp_secretQuestion = (Spinner) findViewById(R.id.sp_secretQuestion);
        et_secretAnsware = (EditText) findViewById(R.id.et_rispostaSegretaLogin);

        // check if first access
        if(sp.contains(PIN_KEY)){
            bt_recuperaPin.setVisibility(View.VISIBLE);
            sp_secretQuestion.setVisibility(View.GONE);
            et_secretAnsware.setVisibility(View.GONE);
            tv_title.setText("Enter Pin");
            bt_login.setText("Login");
            firstAccess = false;
            et_pin.requestFocusFromTouch();

        }else {
            bt_recuperaPin.setVisibility(View.GONE);
            sp_secretQuestion.setVisibility(View.VISIBLE);
            et_secretAnsware.setVisibility(View.VISIBLE);
            tv_title.setText("Choose Your Pin\r\nSecret Question");
            bt_login.setText("Confirm Pin");
            et_pin.requestFocusFromTouch();
            firstAccess = true;
        }

        setupInitialView();


        //MARK: actions
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login(true);
                String pinEntered = et_pin.getText().toString();
                if(firstAccess){
                    String answare = et_secretAnsware.getText().toString();
                    if(!answare.isEmpty()){
                        she.putString(SECRET_QUESTION, sp_secretQuestion.getSelectedItem().toString());
                        she.putString(SECRET_ANSWARE,answare);
                        she.putString(PIN_KEY,pinEntered);
                        she.commit();
                        finish();
                        startActivity(getIntent());
                    }else{
                        et_secretAnsware.setText("");
                    }
                }else if(sp.contains(PIN_KEY)){
                    if(sp.getString(PIN_KEY,null).equals(pinEntered)){
                        Intent homeIntent = new Intent(getApplicationContext(),HomeActivity.class);
                        startActivity(homeIntent);
                        finish();
                    }else {
                        Login(false);
                        et_pin.setText("");
                        Toast.makeText(getApplicationContext(),"Error: invalid PIN",Toast.LENGTH_LONG).show();
                        //Snackbar.make(v,"Error: invalid PIN",Snackbar.LENGTH_LONG).show();
                    }
                }else {
                    Log.d("ERROR-LOGIN", "something was wrong");
                }
            }
        });

        bt_recuperaPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent recoveryPin = new Intent(getApplicationContext(),RecoveryPin.class);
                startActivity(recoveryPin);
            }
        });

        et_pin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    bt_login.performClick();
                    return true;
                }
                return false;
            }
        });

    }

    public void Login(boolean isLogging){
        if(isLogging){
            et_pin.setFocusable(false);
            bt_login.setEnabled(false);
        }else{
            et_pin.setFocusableInTouchMode(true);
            bt_login.setEnabled(true);
        }
    }

    public static String getPin(){
        if(sp.contains(PIN_KEY)){
            String pin = sp.getString(PIN_KEY,null);
            return pin;
        }else return "";

    }

    public static boolean setNewPin(String pin){
        if(!sp.contains(PIN_KEY)){
            return false;
        }else{
            she.remove(PIN_KEY);
            she.putString(PIN_KEY, pin);
            she.commit();
            boolean contain = sp.contains(PIN_KEY);
            return true;
        }
    }

    public static String getSecretQuestion(){
        return sp.getString(SECRET_QUESTION,null);
    }

    public static String getSecretAnsware(){
        return sp.getString(SECRET_ANSWARE,null);
    }

    private void setEditTextActions(){
        et_pin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0) {
                    disableLoginButton();
                }
                else{
                    if(!et_secretAnsware.getText().toString().isEmpty()){
                        enableLoginButton();
                    }else {
                        disableLoginButton();
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        et_secretAnsware.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0) {
                    disableLoginButton();
                }
                else{
                    enableEditTextPin();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void enableLoginButton(){
        bt_login.setAlpha(1f);
        bt_login.setFocusable(true);
        bt_login.setFocusableInTouchMode(true);
    }

    private void disableLoginButton(){
        bt_login.setAlpha(0.5f);
        bt_login.setFocusable(false);
        bt_login.setFocusableInTouchMode(false);
    }

    private void disableEditTextPin(){
        et_pin.setFocusable(false);
        et_pin.setFocusableInTouchMode(false);
    }

    private void enableEditTextPin(){
        et_pin.setFocusable(true);
        et_pin.setFocusableInTouchMode(true);
    }

    private void setupInitialView(){
        if(et_secretAnsware.getVisibility() == View.VISIBLE) {
            disableEditTextPin();
        }else {
            enableEditTextPin();
        }
        disableLoginButton();
        setEditTextActions();
    }
}
