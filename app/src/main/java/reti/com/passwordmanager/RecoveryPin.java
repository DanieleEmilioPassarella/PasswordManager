package reti.com.passwordmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import reti.com.passwordmanager.utility.Utility;

public class RecoveryPin extends AppCompatActivity {

    //MARK: properties
    private TextView tv_domandaSegreta;
    private EditText et_rispostaSegreta;
    private EditText et_newPin;
    private EditText et_confirmPin;
    private Button bt_resetPin;
    private ImageView iv_newPin;
    private ImageView iv_confirmPin;
    private ImageView iv_secretAnsware;
    private boolean validatorNewPin;
    private boolean validatorConfirmPin;
    private boolean validatorSecretAnsware;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Utility.setCurrentTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_pin);

        tv_domandaSegreta = (TextView) findViewById(R.id.tv_domandaSegreta);
        et_rispostaSegreta = (EditText) findViewById(R.id.et_rispostaSegreta);
        et_newPin = (EditText) findViewById(R.id.et_newPin);
        et_confirmPin = (EditText) findViewById(R.id.et_confirmPin);
        bt_resetPin = (Button) findViewById(R.id.bt_inviaRispostaSegreta);
        iv_confirmPin = (ImageView) findViewById(R.id.iv_confirmPinRecovery);
        iv_newPin = (ImageView) findViewById(R.id.iv_newPinRecovery);
        iv_secretAnsware = (ImageView) findViewById(R.id.iv_secretAnsware);

        setTextActions();

        String secretQuestion = LoginActivity.getSecretQuestion();
        tv_domandaSegreta.setText(secretQuestion);

        bt_resetPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String secretAnsware = LoginActivity.getSecretAnsware();
                String userAnsware = et_rispostaSegreta.getText().toString();
                if( !userAnsware.isEmpty() && userAnsware.toLowerCase().equals(secretAnsware.toLowerCase())){
                    if(et_newPin.getText().toString().equals(et_confirmPin.getText().toString()))
                    LoginActivity.setNewPin(et_newPin.getText().toString());
                    Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Removes other Activities from stack
                    startActivity(loginActivity);
                }else{
                    // risposta errata
                    et_rispostaSegreta.setText("");
                    Toast.makeText(getApplicationContext(),"Risposta Errata",Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void setTextActions(){

        et_rispostaSegreta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0) {
                    disableResetPin();
                    iv_secretAnsware.setImageResource(R.drawable.ic_unchecked);
                    validatorSecretAnsware = false;
                    validateResetPin();
                }
                else{
                    if(s.toString().equals(LoginActivity.getSecretAnsware())){
                        iv_secretAnsware.setImageResource(R.drawable.ic_check);
                        validatorSecretAnsware = true;
                        validateResetPin();
                        enableResetPin();
                    }else {
                        disableResetPin();
                        iv_secretAnsware.setImageResource(R.drawable.ic_unchecked);
                        validatorSecretAnsware = false;
                        validateResetPin();
                    }
                }
            }
        });

        et_newPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0) {
                    iv_newPin.setImageResource(R.drawable.ic_unchecked);
                    validatorNewPin = false;
                    disableResetButton();
                }
                else{
                    if(!et_confirmPin.getText().toString().isEmpty()){
                        // second editText not empty
                        if(s.toString().equals(et_confirmPin.getText().toString())){
                            // second editText equal to this
                            iv_newPin.setImageResource(R.drawable.ic_check);
                            validatorNewPin = true;
                            validateResetPin();
                        }else{
                            // second EditText not empty & not equal to this
                            iv_newPin.setImageResource(R.drawable.ic_unchecked);
                            validatorNewPin = false;
                            disableResetButton();
                        }
                    }else{
                        iv_newPin.setImageResource(R.drawable.ic_check);
                        validatorNewPin = true;
                        validateResetPin();
                    }
                }
            }
        });

        et_confirmPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0) {
                    iv_confirmPin.setImageResource(R.drawable.ic_unchecked);
                    validatorConfirmPin = false;
                    disableResetButton();
                }
                else{
                    if(s.toString().equals(et_newPin.getText().toString())) {
                        iv_confirmPin.setImageResource(R.drawable.ic_check);
                        validatorConfirmPin = true;
                        validateResetPin();
                        enableResetPin();
                    }else {
                        iv_confirmPin.setImageResource(R.drawable.ic_unchecked);
                        validatorConfirmPin = false;
                        disableResetButton();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void enableResetPin(){
        iv_newPin.setAlpha(1f);
        iv_confirmPin.setAlpha(1f);
        et_newPin.setFocusable(true);
        et_newPin.setFocusableInTouchMode(true);
        et_confirmPin.setFocusable(true);
        et_confirmPin.setFocusableInTouchMode(true);
    }

    private void disableResetPin(){
        iv_newPin.setAlpha(0.5f);
        iv_confirmPin.setAlpha(0.5f);
        et_newPin.setFocusable(false);
        et_newPin.setFocusableInTouchMode(false);
        et_confirmPin.setFocusable(false);
        et_confirmPin.setFocusableInTouchMode(false);
    }

    private void enableResetButton(){
        bt_resetPin.setAlpha(1f);
        bt_resetPin.setFocusable(true);
        bt_resetPin.setFocusableInTouchMode(true);
    }

    private void disableResetButton(){
        bt_resetPin.setAlpha(0.5f);
        bt_resetPin.setFocusable(false);
        bt_resetPin.setFocusableInTouchMode(false);
    }

    private void validateResetPin(){
        if(validatorSecretAnsware && validatorNewPin && validatorConfirmPin){
            enableResetButton();
        }else{
            disableResetButton();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LoginActivity.FINGERPRINT_ENABLE = true;
    }
}
