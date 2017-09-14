package reti.com.passwordmanager;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import reti.com.passwordmanager.utility.Utility;

public class LoginActivity extends AppCompatActivity {

    // MARK: FINGERPRINT PROPERTIES
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    // END FINGERPRINT PROPERTIES


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
    private ImageView iv_fingerprint;
    private static SharedPreferences.Editor she;
    private static SharedPreferences sp;


    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }

    //Create a new method that we’ll use to initialize our cipher//
    public boolean initCipher() {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {

            //Return false if cipher initialization failed//
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    //Create the generateKey method that we’ll use to gain access to the Android keystore and generate the encryption key//
    private void generateKey() throws FingerprintException {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            keyGenerator.init(new

                    //Specify the operation(s) this key can be used for//
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            //Generate the key//
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    private void initalizeFingerprintRecognition() {
        // MARK: FINGERPRINT
        // If you’ve set your app’s minSdkVersion to anything lower than 23, then you’ll need to verify that the device is running Marshmallow
        // or higher before executing any fingerprint-related code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Get an instance of KeyguardManager and FingerprintManager//
            keyguardManager =
                    (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager =
                    (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);


            //Check whether the device has a fingerprint sensor//
            if (!fingerprintManager.isHardwareDetected()) {
                // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable to use your app’s fingerprint functionality//
                tv_title.setText("Your device doesn't support fingerprint authentication");
            }
            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // If your app doesn't have this permission, then display the following text//
                tv_title.setText("Please enable the fingerprint permission");
            }

            //Check that the user has registered at least one fingerprint//
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                // If the user hasn’t configured any fingerprints, then display the following message//
                tv_title.setText("No fingerprint configured. Please register at least one fingerprint in your device's Settings");
            }

            //Check that the lockscreen is secured//
            if (!keyguardManager.isKeyguardSecure()) {
                // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
                tv_title.setText("Please enable lockscreen security in your device's Settings");
            } else {
                try {
                    generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }

                if (initCipher()) {
                    //If the cipher is initialized successfully, then create a CryptoObject instance//
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                    // for starting the authentication process (via the startAuth method) and processing the authentication process events//
                    FingerprintHandler helper = new FingerprintHandler(this);
                    helper.setLoginActivity(this);
                    helper.startAuth(fingerprintManager, cryptoObject);
                }
            }
        }

        // END FINGERPRINT
    }

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
        iv_fingerprint = (ImageView) findViewById(R.id.iv_fingerprint);
        // END OUTLETS


        // check if first access
        if(sp.contains(PIN_KEY)){
            // not first access
            bt_recuperaPin.setVisibility(View.VISIBLE);
            sp_secretQuestion.setVisibility(View.GONE);
            et_secretAnsware.setVisibility(View.GONE);
            tv_title.setText("");
            bt_login.setText("Login");
            firstAccess = false;
            et_pin.requestFocusFromTouch();
            iv_fingerprint.setVisibility(View.VISIBLE);
            initalizeFingerprintRecognition();
        }else {
            // is first access
            bt_recuperaPin.setVisibility(View.GONE);
            sp_secretQuestion.setVisibility(View.VISIBLE);
            et_secretAnsware.setVisibility(View.VISIBLE);
            tv_title.setText("Secret Question");
            bt_login.setText("Confirm Pin");
            et_pin.requestFocusFromTouch();
            iv_fingerprint.setVisibility(View.GONE);
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
