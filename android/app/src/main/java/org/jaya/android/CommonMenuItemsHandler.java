package org.jaya.android;

import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by murthy on 12/05/17.
 */

public class CommonMenuItemsHandler {

    public static boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.accurate_substring_search);
        item.setChecked(PreferencesManager.isAccurateSubstringSearchEnabled());
        return true;
    }

    public static boolean onOptionsItemSelected(MenuItem item) {

        if( item.getItemId() == R.id.accurate_substring_search ){
            item.setChecked(!item.isChecked());
            PreferencesManager.setAccurateSubstringSearchEnabled(item.isChecked());
        }
        return true;
    }

    public static boolean onMenuOpened(int featureId, Menu menu) {
        if( menu != null ) {
            MenuItem item = menu.findItem(R.id.accurate_substring_search);
            item.setChecked(PreferencesManager.isAccurateSubstringSearchEnabled());
        }
        return true;
    }
}
