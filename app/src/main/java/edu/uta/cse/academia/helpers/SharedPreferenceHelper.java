package edu.uta.cse.academia.helpers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Arun on 4/20/2016.
 */
public class SharedPreferenceHelper {
    private SharedPreferences sharedPreferences;
    public SharedPreferenceHelper(Context context,String filename){
        sharedPreferences  = context.getSharedPreferences(filename,Context.MODE_PRIVATE);
    }
    public void addKeyValueToSharedPreferences(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public String getValueFromSharedPreferencesForKey(String key, String defaultString){
        String value="";
        value = sharedPreferences.getString(key,defaultString);
        return value;
    }
    public void clearSharedPreferences(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }
}
