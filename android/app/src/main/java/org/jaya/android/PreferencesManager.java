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
    public static final String PREF_FONT_SIZE = "com.org.jaya.PREF_FONT_SIZE";
    public static final String PREF_LAST_VIEWED_DOC_ID = "com.org.jaya.PREF_LAST_VIEWED_DOC_ID";

    public static final float MIN_FONT_SIZE = 12.f;
    public static final float MAX_FONT_SIZE = 72.f;

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

    public static float getFontSize(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
        float fontSize = preferences.getFloat(PREF_FONT_SIZE, MIN_FONT_SIZE);
        return fontSize;
    }

    public static void setFontSize(float fontSize){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(PREF_FONT_SIZE, fontSize);
        editor.apply();
    }

    public static int getLastViewedDocId(){
        int randomDocId =  (int)(JayaApp.getSearcher().numDocs() * Math.random());
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
        int docId = randomDocId;//preferences.getInt(PREF_LAST_VIEWED_DOC_ID, randomDocId);
        return docId;
    }

    public static void setLastViewedDocId(int docId){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_LAST_VIEWED_DOC_ID, docId);
        editor.apply();
    }
}
