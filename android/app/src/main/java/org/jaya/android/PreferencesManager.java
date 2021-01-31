package org.jaya.android;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jaya.scriptconverter.ScriptType;
import org.jaya.search.JayaQueryParser;

/**
 * Created by murthy on 12/03/17.
 */

public class PreferencesManager {

    public static final String PREF_INDEXING_NEEDED = "com.org.jaya.PREF_INDEXING_NEEDED";
    public static final String PREF_OUTPUT_SCRIPT_TYPE = "com.org.jaya.PREF_OUTPUT_SCRIPT_TYPE";
    public static final String PREF_FONT_SIZE = "com.org.jaya.PREF_FONT_SIZE";
    public static final String PREF_LAST_VIEWED_DOC_ID = "com.org.jaya.PREF_LAST_VIEWED_DOC_ID";
    public static final String PREF_ACCURATE_SUBSTRING_SEARCH_ENABLED = "com.org.jaya.PREF_ACCURATE_SUBSTRING_SEARCH_ENABLED";
    public static final String PREF_NUM_OF_ADJACENT_DOCS_TO_COPY = "com.org.jaya.PREF_NUM_OF_ADJACENT_DOCS_TO_COPY";

    public static final float MIN_FONT_SIZE = 12.f;
    public static final float MAX_FONT_SIZE = 72.f;
    public static final int DFAULT_NUM_OF_ADJACENT_DOCS_TO_COPY = 10;

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
        ScriptType defaultScript = ScriptType.KANNADA;
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
            String scrTypeStr = preferences.getString(PREF_OUTPUT_SCRIPT_TYPE, defaultScript.name());
            return ScriptType.valueOf(scrTypeStr);
        }catch (Exception ex){
            ex.printStackTrace();
            setPreferredOutputScriptType(defaultScript);
        }
        return defaultScript;
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

    public static boolean isAccurateSubstringSearchEnabled() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
        boolean retVal = preferences.getBoolean(PREF_ACCURATE_SUBSTRING_SEARCH_ENABLED, false);
        return retVal;
    }

    public static void setAccurateSubstringSearchEnabled(boolean bEnabled){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_ACCURATE_SUBSTRING_SEARCH_ENABLED, bEnabled);
        editor.apply();
        JayaQueryParser.setAccurateSubstringSearchEnabled(bEnabled);
    }

    public static int getNumberOfAdjacentDocsToCopy() {
        int defaultValue = DFAULT_NUM_OF_ADJACENT_DOCS_TO_COPY;
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
            return Integer.parseInt(preferences.getString(PREF_NUM_OF_ADJACENT_DOCS_TO_COPY, String.valueOf(defaultValue)));
        } catch(Exception ex) {
            ex.printStackTrace();
            setNumberOfAdjacentDocsToCopy(defaultValue);
        }
        return defaultValue;
    }

    public static void setNumberOfAdjacentDocsToCopy(int numberOfAdjacentDocsToCopy){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JayaApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_NUM_OF_ADJACENT_DOCS_TO_COPY, String.valueOf(numberOfAdjacentDocsToCopy));
        editor.apply();
    }
}
