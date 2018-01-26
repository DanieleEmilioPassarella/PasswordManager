package reti.com.passwordmanager.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.graphics.ColorUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;

import reti.com.passwordmanager.R;

public class Utility {

    public static String PASSWORD_MANAGER_FILE = "PasswordManager.csv";
    public static String THEME_SHARED_PREFERENCES = "THEME";
    public static String CURRENT_THEME_KEY = "CURRENT_THEME";

    public static String FILTER_SHAREDPREFERENCES_KEY = "FILTER_SHARED_PREFERENCES";
    public static String ORDERBY_FILTER_KEY = "ORDERBY_FILTER";
    public static String SORTBY_FILTER_KEY = "SORTBY_FILTER";

    public static int DEFAULT_THEME_KEY = R.style.AppTheme;
    public static int DRACULA_THEME_KEY = R.style.DraculaTheme;
    public static int GREEN_THEME_KEY = R.style.GreenTheme;

    public static final int ACTIVITY_RESULT_IMPORT_FILE_INTENT = 4444;

    public static int[] themes = new int[]{DEFAULT_THEME_KEY,DRACULA_THEME_KEY,GREEN_THEME_KEY};


    public static void toggleTheme(Context context) {
        SharedPreferences sh = context.getSharedPreferences(THEME_SHARED_PREFERENCES,Context.MODE_PRIVATE);
        SharedPreferences.Editor she = sh.edit();
        int current_theme_key = sh.getInt(CURRENT_THEME_KEY,-1);
        if(current_theme_key != -1) {
            current_theme_key = current_theme_key+1 == themes.length ? 0 : current_theme_key+1;
            she.putInt(CURRENT_THEME_KEY,current_theme_key);
            she.commit();
        }else {
            Log.d("TOGGLE-THEME", "setCurrentTheme: Current Theme not set");
        }
    }

    public static void setCurrentTheme(Context context) {
        SharedPreferences sh = context.getSharedPreferences(THEME_SHARED_PREFERENCES,Context.MODE_PRIVATE);
        int current_theme_key = sh.getInt(CURRENT_THEME_KEY,-1);
        if(current_theme_key != -1) {
            int current_theme = themes[current_theme_key];
            context.setTheme(current_theme);
        }else {
            Log.d("GET-THEME", "setCurrentTheme: Current Theme not set");
            SharedPreferences.Editor she = sh.edit();
            she.putInt(CURRENT_THEME_KEY,0);
            she.commit();
            context.setTheme(DEFAULT_THEME_KEY);
        }
    }

    public static String getTitleCurrentTheme(Context context){
        SharedPreferences sh = context.getSharedPreferences(THEME_SHARED_PREFERENCES,Context.MODE_PRIVATE);
        int current_theme_key = sh.getInt(CURRENT_THEME_KEY,-1);
        switch (current_theme_key) {
            case 0: return "Blue";
            case 1: return "Fuchsia";
            case 2: return "Green";
            default: return "Default";
        }
    }

    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

    static public void writeToFile(String data, Context context){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

}
