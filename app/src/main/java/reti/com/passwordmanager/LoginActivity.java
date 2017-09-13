package reti.com.passwordmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private final String LOGIN_FILE = "LOGIN_FILE";
    private final String PIN_KEY = "PIN_KEY";
    private boolean firstAccess = false;
    private EditText et_pin;
    private TextView tv_title;
    private Button bt_login;
    private SharedPreferences.Editor she;
    private SharedPreferences sp;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Password Manager");

        sp = getSharedPreferences(LOGIN_FILE, Context.MODE_PRIVATE);
        she = sp.edit();

        et_pin = (EditText) findViewById(R.id.et_pin);
        tv_title = (TextView) findViewById(R.id.tv_title);
        bt_login = (Button) findViewById(R.id.bt_login);

        if(sp.contains(PIN_KEY)){
            tv_title.setText("Enter Pin");
            tv_title.setTextColor(Color.BLACK);
            bt_login.setText("Login");
            firstAccess = false;
        }else {
            tv_title.setText("Choose Your Pin");
            tv_title.setTextColor(Color.RED);
            bt_login.setText("Confirm Pin");
            firstAccess = true;
        }

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login(true);
                String pinEntered = et_pin.getText().toString();
                if(firstAccess){
                    she.putString(PIN_KEY,pinEntered);
                    she.commit();
                    finish();
                    startActivity(getIntent());
                }else if(sp.contains(PIN_KEY)){
                    if(sp.getString(PIN_KEY,null).equals(pinEntered)){
                        Intent homeIntent = new Intent(getApplicationContext(),HomeActivity.class);
                        startActivity(homeIntent);
                        finish();
                    }else {
                        Login(false);
                        Toast.makeText(getApplicationContext(),"Error: invalid PIN",Toast.LENGTH_LONG).show();
                        //Snackbar.make(v,"Error: invalid PIN",Snackbar.LENGTH_LONG).show();
                    }
                }else {
                    Log.d("ERROR-LOGIN", "something was wrong");
                }
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
}
