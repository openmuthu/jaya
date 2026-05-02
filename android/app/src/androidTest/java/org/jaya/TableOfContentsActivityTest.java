package org.jaya;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jaya.android.JayaApp;
import org.jaya.android.R;
import org.jaya.android.TableOfContentsActivity;
import org.jaya.search.JayaIndexMetadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

/**
 * Espresso integration tests for {@link TableOfContentsActivity}.
 *
 * Strategy
 * --------
 * Each test writes a controlled {@code .jaya-index-md.txt} file to the app's
 * real search-index directory before launching the activity, then restores the
 * previous file in {@link #tearDown()}.  This keeps the tests hermetic while
 * exercising the full stack (metadata I/O → tree build → adapter → ListView).
 *
 * Scenarios covered
 * -----------------
 * UC-A1  Empty catalog     → list shows zero items
 * UC-A2  Populated catalog → folder nodes are rendered in the list
 * UC-A3  Expand folder     → tapping a collapsed folder increases list size
 * UC-A4  Collapse folder   → tapping an expanded folder restores original size
 * UC-A5  Two folders       → both folder nodes visible; expanding one shows its
 *                            children without affecting the other folder node
 * UC-A6  Back navigation   → expansion state is preserved when onResume() fires
 *                            after returning from a child activity
 *
 * Note: UC-A-NAV (leaf tap → navigate to MainActivity) requires espresso-intents
 * and a pre-built Lucene index; it is described in the test plan but not
 * implemented here to avoid a hard dependency on index content.
 */
@RunWith(AndroidJUnit4.class)
public class TableOfContentsActivityTest {

    /**
     * Test data — two folders, three documents total.
     *   folder_alpha  → doc_one.txt, doc_two.txt
     *   folder_beta   → doc_three.txt
     *
     * After sortTree: folder_alpha (0), folder_beta (1)
     * Expanding folder_alpha (position 0) adds 2 children → total 4 items.
     */
    private static final String[] TEST_PATHS = {
            "folder_alpha/doc_one.txt",
            "folder_alpha/doc_two.txt",
            "folder_beta/doc_three.txt"
    };

    // Delay (ms) between taps and assertions to let the UI thread settle.
    // Espresso's IdlingResource mechanism handles most synchronisation, but
    // an explicit pause guards against residual animation on older devices.
    private static final long UI_SETTLE_MS = 200;

    @Rule
    public ActivityTestRule<TableOfContentsActivity> mActivityRule =
            new ActivityTestRule<>(TableOfContentsActivity.class,
                    false,  // initialTouchMode
                    false); // do NOT auto-launch — we set up data first

    private String mIndexDir;
    private File mMetadataFile;
    private String mBackupContent;   // original file content restored in tearDown

    @Before
    public void setUp() throws Exception {
        mIndexDir = JayaApp.getSearchIndexFolder();
        JayaIndexMetadata md = new JayaIndexMetadata(mIndexDir);
        mMetadataFile = md.getMetadataFile();

        // Back up existing metadata so tests don't corrupt real indexed data
        mBackupContent = readFileOrNull(mMetadataFile);

        // Create the index directory if it doesn't exist yet (fresh install)
        mMetadataFile.getParentFile().mkdirs();
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original metadata file
        if (mBackupContent != null) {
            writeFile(mMetadataFile, mBackupContent);
        } else {
            mMetadataFile.delete();
        }
    }

    // ─── UC-A1: empty catalog ────────────────────────────────────────────────

    /**
     * When no metadata file exists (or it is empty) the list must be empty.
     * The activity shows a toast but the list view should have zero items.
     */
    @Test
    public void emptyMetadata_listShowsNoItems() throws Exception {
        mMetadataFile.delete(); // ensure no metadata exists
        mActivityRule.launchActivity(null);

        onView(withId(R.id.toc_list_view))
                .check(matches(isDisplayed()))
                .check(matches(withAdapterCount(0)));
    }

    // ─── UC-A2: populated catalog renders folder nodes ────────────────────────

    /**
     * After pre-seeding the metadata with {@link #TEST_PATHS}, the activity
     * must display exactly two top-level folder nodes (all collapsed).
     */
    @Test
    public void populatedMetadata_showsTopLevelFolderNodes() throws Exception {
        seedMetadata(TEST_PATHS);
        mActivityRule.launchActivity(null);

        onView(withId(R.id.toc_list_view))
                .check(matches(isDisplayed()))
                .check(matches(withAdapterCount(2))); // folder_alpha, folder_beta
    }

    // ─── UC-A3: tap folder → expands ─────────────────────────────────────────

    /**
     * Tapping the first folder node (folder_alpha, which has 2 children)
     * must increase the visible list count from 2 to 4.
     */
    @Test
    public void tapCollapsedFolder_expandsAndShowsChildren() throws Exception {
        seedMetadata(TEST_PATHS);
        mActivityRule.launchActivity(null);

        onView(withId(R.id.toc_list_view)).check(matches(withAdapterCount(2)));

        // Tap the first folder node (folder_alpha after alphabetical sort)
        onData(anything())
                .inAdapterView(withId(R.id.toc_list_view))
                .atPosition(0)
                .perform(click());

        Thread.sleep(UI_SETTLE_MS);

        // folder_alpha (expanded) + doc_one + doc_two + folder_beta = 4
        onView(withId(R.id.toc_list_view)).check(matches(withAdapterCount(4)));
    }

    // ─── UC-A4: tap expanded folder → collapses ──────────────────────────────

    /**
     * Tapping an already-expanded folder must collapse it and restore the
     * original count.
     */
    @Test
    public void tapExpandedFolder_collapsesAndHidesChildren() throws Exception {
        seedMetadata(TEST_PATHS);
        mActivityRule.launchActivity(null);

        // Expand folder_alpha
        onData(anything())
                .inAdapterView(withId(R.id.toc_list_view))
                .atPosition(0)
                .perform(click());
        Thread.sleep(UI_SETTLE_MS);
        onView(withId(R.id.toc_list_view)).check(matches(withAdapterCount(4)));

        // Collapse folder_alpha (still at position 0)
        onData(anything())
                .inAdapterView(withId(R.id.toc_list_view))
                .atPosition(0)
                .perform(click());
        Thread.sleep(UI_SETTLE_MS);

        onView(withId(R.id.toc_list_view)).check(matches(withAdapterCount(2)));
    }

    // ─── UC-A5: expanding one folder does not affect sibling folder ───────────

    /**
     * After expanding folder_alpha, folder_beta (now at position 3) must
     * still be a collapsed folder node — i.e. it must not have its own children
     * visible yet.
     */
    @Test
    public void expandOneFolderLeavesOtherFolderCollapsed() throws Exception {
        seedMetadata(TEST_PATHS);
        mActivityRule.launchActivity(null);

        // Expand folder_alpha
        onData(anything())
                .inAdapterView(withId(R.id.toc_list_view))
                .atPosition(0)
                .perform(click());
        Thread.sleep(UI_SETTLE_MS);

        // Total = 4: [folder_alpha, doc_one, doc_two, folder_beta]
        onView(withId(R.id.toc_list_view)).check(matches(withAdapterCount(4)));

        // Now expand folder_beta at position 3 (1 child → total 5)
        onData(anything())
                .inAdapterView(withId(R.id.toc_list_view))
                .atPosition(3)
                .perform(click());
        Thread.sleep(UI_SETTLE_MS);

        onView(withId(R.id.toc_list_view)).check(matches(withAdapterCount(5)));
    }

    // ─── UC-A6: back navigation preserves expansion state ─────────────────────

    /**
     * Regression test for the bug where {@code onResume()} called
     * {@code buildAndShowTree()} unconditionally, resetting all
     * {@link TableOfContentsActivity.TreeNode#expanded} flags every time the
     * user returned from a child activity.
     *
     * <p>The fix: {@code onResume()} only builds the tree when {@code mRoot} is
     * {@code null}.  On subsequent resumes it calls {@code refreshVisibleNodes()}
     * so the list re-renders without discarding expansion state.
     *
     * <p>Simulation: {@link Instrumentation#callActivityOnPause} +
     * {@link Instrumentation#callActivityOnResume} mimics the lifecycle sequence
     * that occurs when the user taps a leaf, lands in {@code MainActivity}, and
     * presses back — without requiring a real second activity to be launched.
     */
    @Test
    public void backNavigation_preservesExpansionState() throws Exception {
        seedMetadata(TEST_PATHS);
        mActivityRule.launchActivity(null);

        // Expand folder_alpha (position 0 after sort)
        onData(anything())
                .inAdapterView(withId(R.id.toc_list_view))
                .atPosition(0)
                .perform(click());
        Thread.sleep(UI_SETTLE_MS);

        // Confirm expanded: folder_alpha + doc_one + doc_two + folder_beta = 4
        onView(withId(R.id.toc_list_view)).check(matches(withAdapterCount(4)));

        // Simulate the activity lifecycle triggered by back-navigation:
        //   TOC paused  (user tapped a leaf and MainActivity launched)
        //   TOC resumed (user pressed back in MainActivity)
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.callActivityOnPause(mActivityRule.getActivity());
        instrumentation.callActivityOnResume(mActivityRule.getActivity());
        Thread.sleep(UI_SETTLE_MS);

        // Expansion state must be intact — still 4 items, not reset to 2
        onView(withId(R.id.toc_list_view))
                .check(matches(withAdapterCount(4)));
    }

    // ─── UC-A7: long-press folder node shows context menu ────────────────────

    /**
     * Long-pressing a folder node must display an AlertDialog containing the
     * "Search in this folder" option.  This verifies the long-click listener
     * is wired up and the dialog renders the correct menu item.
     *
     * <p>The test does NOT tap the dialog item (that would launch
     * {@code SearchableActivity} which requires a live Lucene index).
     * Pressing the back button dismisses the dialog.
     */
    @Test
    public void longPressFolder_showsSearchInThisFolderDialog() throws Exception {
        seedMetadata(TEST_PATHS);
        mActivityRule.launchActivity(null);

        // folder_alpha is at position 0 after alphabetical sort
        onData(anything())
                .inAdapterView(withId(R.id.toc_list_view))
                .atPosition(0)
                .perform(longClick());

        Thread.sleep(UI_SETTLE_MS);

        // The dialog must show the "Search in this folder" option
        onView(withText(R.string.search_in_this_folder))
                .check(matches(isDisplayed()));

        // Dismiss the dialog
        android.support.test.espresso.Espresso.pressBack();
    }

    /**
     * Long-pressing a leaf node must NOT show the context menu dialog
     * (only folders can be searched within).
     *
     * <p>Expand folder_alpha so its leaf children become visible at positions
     * 1 and 2, then long-press one of the leaves and verify the dialog does
     * not appear.
     */
    @Test
    public void longPressLeaf_doesNotShowContextMenu() throws Exception {
        seedMetadata(TEST_PATHS);
        mActivityRule.launchActivity(null);

        // Expand folder_alpha to expose its leaf children
        onData(anything())
                .inAdapterView(withId(R.id.toc_list_view))
                .atPosition(0)
                .perform(click());
        Thread.sleep(UI_SETTLE_MS);

        // Long-press a leaf (position 1 = first child of folder_alpha)
        onData(anything())
                .inAdapterView(withId(R.id.toc_list_view))
                .atPosition(1)
                .perform(longClick());

        Thread.sleep(UI_SETTLE_MS);

        // The "Search in this folder" dialog must NOT be on screen
        onView(withText(R.string.search_in_this_folder))
                .check(doesNotExist());
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private void seedMetadata(String[] paths) {
        JayaIndexMetadata md = new JayaIndexMetadata(mIndexDir);
        Set<String> pathSet = new HashSet<String>();
        for (String p : paths) pathSet.add(p);
        md.append(pathSet);
    }

    private static String readFileOrNull(File file) {
        if (!file.exists()) return null;
        try {
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\r\n");
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static void writeFile(File file, String content) {
        try {
            java.io.FileWriter fw = new java.io.FileWriter(file, false);
            fw.write(content);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Custom Espresso {@link Matcher} that asserts the adapter behind a
     * {@link ListView} has exactly {@code expectedCount} items.
     */
    private static Matcher<View> withAdapterCount(final int expectedCount) {
        return new BoundedMatcher<View, ListView>(ListView.class) {
            @Override
            protected boolean matchesSafely(ListView listView) {
                Adapter adapter = listView.getAdapter();
                return adapter != null && adapter.getCount() == expectedCount;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("ListView whose adapter has " + expectedCount + " items");
            }
        };
    }
}
