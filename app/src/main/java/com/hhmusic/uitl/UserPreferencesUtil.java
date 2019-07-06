package com.hhmusic.uitl;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class UserPreferencesUtil {

    public static final String USER_NAME = "user_name";
    public static final String PASSWORD = "password";


    private static UserPreferencesUtil sInstance;

    private static SharedPreferences mPreferences;

    private UserPreferencesUtil(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static UserPreferencesUtil getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new UserPreferencesUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    public String getUsername(){
        return mPreferences.getString(USER_NAME, "");
    }


    public String getPassword(){
        return mPreferences.getString(PASSWORD, "");
    }


    public void setUserName(String userName){
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(USER_NAME, userName);
        editor.apply();
    }

    public void setPassword(String password){
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PASSWORD, password);
        editor.apply();
    }

}