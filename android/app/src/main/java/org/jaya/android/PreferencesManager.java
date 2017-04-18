package org.jaya.android;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jaya.scriptconverter.ScriptType;

/**
 * Created by murthy on 12/03/17.
 */

public class PreferencesManager {

    public static final String PREF_INDEXING_NEEDED = "com.org.jaya.PREF_INDEXING_NEEDED";
    public static final String PREF_OUTPUT_SCRIPT_TYPE = "com.org.jaya.PREF_OUTPUT_SCRIPT_TYPE";

    public static boolean isIndexingRequired() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
        return preferences.getBoolean(PREF_INDEXING_NEEDED, true);
    }

    public static void setIsIndexingRequired(boolean bRequired){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_INDEXING_NEEDED, bRequired);
        editor.apply();
    }

    public static ScriptType getPreferredOutputScriptType(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
        String scrTypeStr = preferences.getString(PREF_OUTPUT_SCRIPT_TYPE, ScriptType.KANNADA.name());
        return ScriptType.valueOf(scrTypeStr);
    }

    public static void setPreferredOutputScriptType(ScriptType type){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_OUTPUT_SCRIPT_TYPE, type.name());
        editor.apply();
    }
}
