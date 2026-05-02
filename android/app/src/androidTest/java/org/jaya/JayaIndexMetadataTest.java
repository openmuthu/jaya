package org.jaya;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.jaya.search.JayaIndexMetadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented integration tests for {@link JayaIndexMetadata}.
 *
 * Each test gets a fresh, isolated temporary directory inside the app's
 * internal files dir so tests do not interfere with each other or with
 * real indexed data.
 *
 * Scenarios covered
 * -----------------
 * UC-M1  No metadata file present    → getIndexedFilePathSet returns empty set
 * UC-M2  append persists to disk     → fresh instance reads persisted paths
 * UC-M3  hasIndexedFile              → true for appended path, false otherwise
 * UC-M4  remove deletes selected path, keeps others, persists the result
 * UC-M5  getIndexedFilePathSet       → returns a defensive copy (mutations
 *                                       do not affect the internal state)
 */
@RunWith(AndroidJUnit4.class)
public class JayaIndexMetadataTest {

    private File mMetadataDir;

    @Before
    public void setUp() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        // Use internal files dir — always writable, no external-storage permission needed
        mMetadataDir = new File(ctx.getFilesDir(),
                "test_index_" + System.currentTimeMillis());
        assertTrue("Could not create temp test dir", mMetadataDir.mkdirs());
    }

    @After
    public void tearDown() {
        deleteRecursively(mMetadataDir);
    }

    // ─── UC-M1: no metadata file ──────────────────────────────────────────────

    @Test
    public void getIndexedFilePathSet_noMetadataFile_returnsEmptySet() {
        JayaIndexMetadata md = new JayaIndexMetadata(mMetadataDir.getAbsolutePath());

        Set<String> result = md.getIndexedFilePathSet();

        assertNotNull(result);
        assertTrue("Expected empty set when no metadata file exists", result.isEmpty());
    }

    // ─── UC-M2: append persists to disk ──────────────────────────────────────

    @Test
    public void append_persistsPathsToDisk_readableByFreshInstance() {
        JayaIndexMetadata md = new JayaIndexMetadata(mMetadataDir.getAbsolutePath());
        Set<String> toAdd = new HashSet<String>();
        toAdd.add("AgamAH/prakAshasaMhitA.txt");
        toAdd.add("purANa/brahmaPurANa.txt");
        md.append(toAdd);

        // Fresh instance must read from file, not from previous in-memory state
        JayaIndexMetadata mdFresh = new JayaIndexMetadata(mMetadataDir.getAbsolutePath());
        Set<String> result = mdFresh.getIndexedFilePathSet();

        assertEquals("Both appended paths must be persisted", 2, result.size());
        assertTrue(result.contains("AgamAH/prakAshasaMhitA.txt"));
        assertTrue(result.contains("purANa/brahmaPurANa.txt"));
    }

    // ─── UC-M3: hasIndexedFile ────────────────────────────────────────────────

    @Test
    public void hasIndexedFile_trueForAppendedPath_falseForMissingPath() {
        JayaIndexMetadata md = new JayaIndexMetadata(mMetadataDir.getAbsolutePath());
        Set<String> toAdd = new HashSet<String>();
        toAdd.add("rAmAyaNa/vAlmIkirAmAyaNa.txt");
        md.append(toAdd);

        assertTrue("Must return true for a path that was appended",
                md.hasIndexedFile("rAmAyaNa/vAlmIkirAmAyaNa.txt"));
        assertFalse("Must return false for a path that was never added",
                md.hasIndexedFile("rAmAyaNa/nonexistent.txt"));
    }

    // ─── UC-M4: remove ────────────────────────────────────────────────────────

    @Test
    public void remove_deletesSelectedPath_keepsOthers_persists() {
        JayaIndexMetadata md = new JayaIndexMetadata(mMetadataDir.getAbsolutePath());
        Set<String> toAdd = new HashSet<String>();
        toAdd.add("AgamAH/text1.txt");
        toAdd.add("AgamAH/text2.txt");
        toAdd.add("purANa/text3.txt");
        md.append(toAdd);

        Set<String> toRemove = new HashSet<String>();
        toRemove.add("AgamAH/text1.txt");
        md.remove(toRemove);

        // In-memory state
        assertFalse("Removed path must not be present in memory",
                md.hasIndexedFile("AgamAH/text1.txt"));
        assertTrue("Other paths must remain", md.hasIndexedFile("AgamAH/text2.txt"));
        assertTrue("Other paths must remain", md.hasIndexedFile("purANa/text3.txt"));

        // Persisted state (fresh read)
        JayaIndexMetadata mdFresh = new JayaIndexMetadata(mMetadataDir.getAbsolutePath());
        assertFalse("Removed path must not be persisted",
                mdFresh.hasIndexedFile("AgamAH/text1.txt"));
        assertEquals("Two paths must remain on disk", 2,
                mdFresh.getIndexedFilePathSet().size());
    }

    // ─── UC-M5: defensive copy ────────────────────────────────────────────────

    @Test
    public void getIndexedFilePathSet_returnsDefensiveCopy_mutationDoesNotAffectInternalState() {
        JayaIndexMetadata md = new JayaIndexMetadata(mMetadataDir.getAbsolutePath());
        Set<String> toAdd = new HashSet<String>();
        toAdd.add("A/x.txt");
        md.append(toAdd);

        Set<String> copy = md.getIndexedFilePathSet();
        copy.clear();   // mutate the returned set

        Set<String> copy2 = md.getIndexedFilePathSet();
        assertEquals("Internal set must be unaffected by mutation of returned copy",
                1, copy2.size());
        assertTrue(copy2.contains("A/x.txt"));
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private void deleteRecursively(File f) {
        if (f == null) return;
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        f.delete();
    }
}
