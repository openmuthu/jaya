package org.jaya.android;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by murthy on 12/03/17.
 */

public class PreferencesManager {

    public static final String PREF_INDEXING_NEEDED = "PREF_INDEXING_NEEDED";

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
}
