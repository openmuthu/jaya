package org.jaya;

import android.app.ActionBar;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.jaya.android.R;
import org.jaya.android.SearchableActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Espresso integration tests for {@link SearchableActivity} search-scoping behaviour.
 *
 * Strategy
 * --------
 * We launch {@link SearchableActivity} with controlled intent extras and verify
 * the observable UI state (action-bar subtitle, list visibility) that depends
 * on whether a folder scope was provided.  Verifying actual filtered result
 * counts would require a live Lucene index, so those assertions are left to the
 * unit-level {@code SearchScopeTest} which exercises {@code filterResultsByPath}
 * directly.
 *
 * Scenarios covered
 * -----------------
 * UC-SA1  No EXTRA_FOLDER_PATH   → action bar subtitle is null (global search)
 * UC-SA2  With EXTRA_FOLDER_PATH → action bar subtitle shows "In: <folder name>"
 * UC-SA3  mFolderPath preserved  → after onNewIntent with ACTION_SEARCH (no path extra),
 *                                  mFolderPath is unchanged on the same instance
 * UC-SA4  Empty query not fired  → launching without ACTION_SEARCH does not trigger
 *                                  a search; list view is still shown
 */
@RunWith(AndroidJUnit4.class)
public class SearchableActivityTest {

    @Rule
    public ActivityTestRule<SearchableActivity> mActivityRule =
            new ActivityTestRule<>(SearchableActivity.class,
                    false,   // initialTouchMode
                    false);  // do NOT auto-launch — we build the intent first

    // ─── UC-SA1: no folder scope → no subtitle ───────────────────────────────

    /**
     * When {@code SearchableActivity} is launched without {@code EXTRA_FOLDER_PATH}
     * (the global-search path), the action bar must have no subtitle set.
     */
    @Test
    public void globalSearch_noSubtitleShown() {
        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);

        ActionBar actionBar = mActivityRule.getActivity().getActionBar();
        assertNull("Global search must not set an action bar subtitle",
                actionBar.getSubtitle());
    }

    // ─── UC-SA2: folder scope → subtitle shown ───────────────────────────────

    /**
     * When launched with {@code EXTRA_FOLDER_PATH} and {@code EXTRA_FOLDER_DISPLAY_NAME},
     * the action bar must show the subtitle "In: &lt;display name&gt;".
     */
    @Test
    public void folderSearch_subtitleShowsFolderName() {
        Intent intent = new Intent();
        intent.putExtra(SearchableActivity.EXTRA_FOLDER_PATH, "/mAdhva/");
        intent.putExtra(SearchableActivity.EXTRA_FOLDER_DISPLAY_NAME, "mAdhva");
        mActivityRule.launchActivity(intent);

        SearchableActivity activity = mActivityRule.getActivity();
        CharSequence subtitle = activity.getActionBar().getSubtitle();
        String expectedSubtitle = activity.getString(R.string.search_in_folder_subtitle, "mAdhva");

        assertEquals("Action bar subtitle must indicate the scoped folder",
                expectedSubtitle, subtitle);
    }

    // ─── UC-SA3: mFolderPath preserved across onNewIntent ────────────────────

    /**
     * After a folder-scoped launch, a subsequent {@code ACTION_SEARCH} intent
     * (fired by the SearchManager when the user submits a query) must NOT clear
     * {@code mFolderPath} — the same folder scope must still be active.
     *
     * <p>This is the regression case for the {@code singleTop} fix: without
     * {@code singleTop}, a fresh instance was created for each search query and
     * {@code mFolderPath} was lost.
     */
    @Test
    public void folderSearch_mFolderPathPreservedAfterSearchIntent() throws Exception {
        Intent launchIntent = new Intent();
        launchIntent.putExtra(SearchableActivity.EXTRA_FOLDER_PATH, "/purANa/");
        launchIntent.putExtra(SearchableActivity.EXTRA_FOLDER_DISPLAY_NAME, "purANa");
        mActivityRule.launchActivity(launchIntent);

        SearchableActivity activity = mActivityRule.getActivity();
        assertEquals("mFolderPath must be set after folder-scoped launch",
                "/purANa/", activity.getFolderPath());

        // Simulate the SearchManager delivering an ACTION_SEARCH intent (no EXTRA_FOLDER_PATH)
        final Intent searchIntent = new Intent(Intent.ACTION_SEARCH);
        searchIntent.putExtra(android.app.SearchManager.QUERY, "brahma");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivityRule.getActivity().onNewIntent(searchIntent);
            }
        });
        Thread.sleep(200);

        assertEquals("mFolderPath must be preserved after ACTION_SEARCH intent without path extra",
                "/purANa/", activity.getFolderPath());
    }

    // ─── UC-SA4: no ACTION_SEARCH on launch → list view rendered ─────────────

    /**
     * Launching {@code SearchableActivity} without {@code ACTION_SEARCH} (as
     * the TOC does for folder-scoped search — the user will type a query
     * themselves) must not crash and must render the list view.
     */
    @Test
    public void folderSearch_launchWithoutQuery_listViewShown() {
        Intent intent = new Intent();
        intent.putExtra(SearchableActivity.EXTRA_FOLDER_PATH, "/AgamAH/");
        intent.putExtra(SearchableActivity.EXTRA_FOLDER_DISPLAY_NAME, "AgamAH");
        mActivityRule.launchActivity(intent);

        onView(withId(android.R.id.list))
                .check(matches(isDisplayed()));
    }
}
