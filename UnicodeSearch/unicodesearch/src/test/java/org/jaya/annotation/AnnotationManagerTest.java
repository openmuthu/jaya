package org.jaya.annotation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link AnnotationManager}.
 * Tests use a temp-folder JSON file; no network or real Lucene index needed.
 */
public class AnnotationManagerTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File annotationsFile;

    /** Write a bookmarks JSON file with given content and return an AnnotationManager over it. */
    private AnnotationManager manager(String json) throws Exception {
        annotationsFile = tmp.newFile("bookmarks.json");
        try (FileWriter fw = new FileWriter(annotationsFile)) {
            fw.write(json);
        }
        return new AnnotationManager(null, annotationsFile.getAbsolutePath());
    }

    private AnnotationManager emptyManager() throws Exception {
        return manager("{\"version\":\"2.0\",\"groups\":[],\"items\":[]}");
    }

    // am01 — empty file loads without errors
    @Test
    public void am01_emptyFile_loadsCleanly() throws Exception {
        AnnotationManager am = emptyManager();
        assertEquals("am01: no annotations", 0, am.getNumAnnotations());
    }

    // am02 — v1 items loaded correctly
    @Test
    public void am02_v1Items_loaded() throws Exception {
        String json = "{\"version\":\"1.0\",\"items\":["
                + "{\"docPath\":\"/path/file.txt\",\"docLocalId\":\"0\","
                + "\"updated\":\"2020-01-01T00:00:00.000Z\",\"name\":\"rAma\",\"notes\":\"\"}"
                + "]}";
        AnnotationManager am = manager(json);
        assertEquals("am02: one v1 annotation loaded", 1, am.getNumAnnotations());
        List<Annotation> list = am.getAnnotations();
        assertEquals("am02b: annotation name", "rAma", list.get(0).getName());
    }

    // am03 — v2 items with groups loaded
    @Test
    public void am03_v2Items_withGroups_loaded() throws Exception {
        String json = "{\"version\":\"2.0\","
                + "\"groups\":[{\"id\":\"g1\",\"name\":\"Favourites\",\"createdDate\":\"2021-01-01T00:00:00.000Z\"}],"
                + "\"items\":["
                + "{\"docPath\":\"/p/f.txt\",\"docLocalId\":\"2\","
                + "\"updated\":\"2021-06-01T00:00:00.000Z\",\"name\":\"shrI\",\"notes\":\"\","
                + "\"fingerprint\":\"fp123\",\"groups\":[\"g1\"]}"
                + "]}";
        AnnotationManager am = manager(json);
        assertEquals("am03a: one annotation", 1, am.getNumAnnotations());
        Annotation a = am.getAnnotations().get(0);
        assertEquals("am03b: fingerprint loaded", "fp123", a.getContentFingerprint());
        assertTrue("am03c: annotation is in g1", a.isInGroup("g1"));
        List<BookmarkGroup> groups = am.getGroups();
        assertEquals("am03d: one group", 1, groups.size());
        assertEquals("am03e: group name", "Favourites", groups.get(0).getName());
    }

    // am04 — addAnnotationDirect adds annotation
    @Test
    public void am04_addAnnotationDirect() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/f.txt", "5", "myMark");
        assertEquals("am04: annotation added", 1, am.getNumAnnotations());
    }

    // am05 — addAnnotationDirect on existing key updates name
    @Test
    public void am05_addAnnotationDirect_updatesExisting() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/f.txt", "5", "old");
        am.addAnnotationDirect("/p/f.txt", "5", "new");
        assertEquals("am05a: still one annotation", 1, am.getNumAnnotations());
        assertEquals("am05b: name updated", "new", am.getAnnotations().get(0).getName());
    }

    // am06 — removeAnnotation by Annotation object
    @Test
    public void am06_removeAnnotation() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/f.txt", "3", "test");
        Annotation a = am.getAnnotations().get(0);
        am.removeAnnotation(a);
        assertEquals("am06: annotation removed", 0, am.getNumAnnotations());
    }

    // am07 — removeAnnotation of nonexistent annotation is a no-op
    @Test
    public void am07_removeAnnotation_nonexistent_noOp() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/f.txt", "3", "test");
        Annotation fake = new Annotation("/other", "99", "fake", new java.util.Date());
        am.removeAnnotation(fake); // should not throw
        assertEquals("am07: original untouched", 1, am.getNumAnnotations());
    }

    // am08 — annotationExists
    @Test
    public void am08_annotationExists() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/f.txt", "3", "mark");
        Annotation a = am.getAnnotations().get(0);
        assertTrue("am08a: exists by annotation", am.annotationExists(a));
    }

    // am09 — annotationExists returns false for null
    @Test
    public void am09_annotationExists_null() throws Exception {
        AnnotationManager am = emptyManager();
        assertFalse("am09: null returns false", am.annotationExists((Annotation) null));
    }

    // am10 — getAnnotations returns newest-first order
    @Test
    public void am10_getAnnotations_newestFirst() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/f.txt", "1", "first");
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        am.addAnnotationDirect("/p/f.txt", "2", "second");
        List<Annotation> list = am.getAnnotations();
        assertEquals("am10a: two annotations", 2, list.size());
        assertEquals("am10b: newest is second", "second", list.get(0).getName());
    }

    // am11 — renameAnnotation
    @Test
    public void am11_renameAnnotation() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/f.txt", "0", "old");
        String key = am.getAnnotations().get(0).getKey();
        assertTrue("am11a: rename returns true", am.renameAnnotation(key, "new"));
        assertEquals("am11b: name updated", "new", am.getAnnotations().get(0).getName());
    }

    // am12 — renameAnnotation returns false for missing key
    @Test
    public void am12_renameAnnotation_missing() throws Exception {
        AnnotationManager am = emptyManager();
        assertFalse("am12: missing key returns false", am.renameAnnotation("no-such-key", "x"));
    }

    // am13 — addGroup and getGroups
    @Test
    public void am13_addGroup() throws Exception {
        AnnotationManager am = emptyManager();
        BookmarkGroup g = am.addGroup("Devotional");
        assertEquals("am13a: one group", 1, am.getGroups().size());
        assertEquals("am13b: group name", "Devotional", g.getName());
        assertNotNull("am13c: group id not null", g.getId());
    }

    // am14 — renameGroup
    @Test
    public void am14_renameGroup() throws Exception {
        AnnotationManager am = emptyManager();
        BookmarkGroup g = am.addGroup("OldName");
        assertTrue("am14a: rename returns true", am.renameGroup(g.getId(), "NewName"));
        assertEquals("am14b: name updated", "NewName", am.getGroups().get(0).getName());
    }

    // am15 — removeGroup removes group and detaches from annotations
    @Test
    public void am15_removeGroup_detachesAnnotations() throws Exception {
        AnnotationManager am = emptyManager();
        BookmarkGroup g = am.addGroup("G1");
        am.addAnnotationDirect("/p/f.txt", "0", "mark");
        String key = am.getAnnotations().get(0).getKey();
        am.assignAnnotationToGroup(key, g.getId());
        assertTrue("am15a: in group before removal", am.getAnnotations().get(0).isInGroup(g.getId()));
        am.removeGroup(g.getId());
        assertEquals("am15b: no groups", 0, am.getGroups().size());
        assertFalse("am15c: annotation detached", am.getAnnotations().get(0).isInGroup(g.getId()));
    }

    // am16 — removeGroup of nonexistent id returns false
    @Test
    public void am16_removeGroup_missing() throws Exception {
        AnnotationManager am = emptyManager();
        assertFalse("am16: missing group returns false", am.removeGroup("no-such"));
    }

    // am17 — assignAnnotationToGroup
    @Test
    public void am17_assignAnnotationToGroup() throws Exception {
        AnnotationManager am = emptyManager();
        BookmarkGroup g = am.addGroup("G1");
        am.addAnnotationDirect("/p/f.txt", "0", "mark");
        String key = am.getAnnotations().get(0).getKey();
        assertTrue("am17a: assign returns true", am.assignAnnotationToGroup(key, g.getId()));
        assertTrue("am17b: annotation in group", am.getAnnotations().get(0).isInGroup(g.getId()));
    }

    // am18 — assignAnnotationToGroup: missing group returns false
    @Test
    public void am18_assignAnnotationToGroup_missingGroup() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/f.txt", "0", "mark");
        String key = am.getAnnotations().get(0).getKey();
        assertFalse("am18: missing group returns false", am.assignAnnotationToGroup(key, "no-group"));
    }

    // am19 — unassignAnnotationFromGroup
    @Test
    public void am19_unassignAnnotationFromGroup() throws Exception {
        AnnotationManager am = emptyManager();
        BookmarkGroup g = am.addGroup("G1");
        am.addAnnotationDirect("/p/f.txt", "0", "mark");
        String key = am.getAnnotations().get(0).getKey();
        am.assignAnnotationToGroup(key, g.getId());
        assertTrue("am19a: in group", am.getAnnotations().get(0).isInGroup(g.getId()));
        am.unassignAnnotationFromGroup(key, g.getId());
        assertFalse("am19b: removed from group", am.getAnnotations().get(0).isInGroup(g.getId()));
    }

    // am20 — getAnnotationsForGroup
    @Test
    public void am20_getAnnotationsForGroup() throws Exception {
        AnnotationManager am = emptyManager();
        BookmarkGroup g = am.addGroup("G1");
        am.addAnnotationDirect("/p/f1.txt", "0", "mark1");
        am.addAnnotationDirect("/p/f2.txt", "0", "mark2");
        String key1 = "/p/f1.txt0";
        am.assignAnnotationToGroup(key1, g.getId());
        List<Annotation> inGroup = am.getAnnotationsForGroup(g.getId());
        assertEquals("am20: one annotation in group", 1, inGroup.size());
        assertEquals("am20b: correct annotation", "mark1", inGroup.get(0).getName());
    }

    // am21 — setAnnotationGroups replaces membership
    @Test
    public void am21_setAnnotationGroups() throws Exception {
        AnnotationManager am = emptyManager();
        BookmarkGroup g1 = am.addGroup("G1");
        BookmarkGroup g2 = am.addGroup("G2");
        am.addAnnotationDirect("/p/f.txt", "0", "mark");
        String key = am.getAnnotations().get(0).getKey();
        am.assignAnnotationToGroup(key, g1.getId());
        am.setAnnotationGroups(key, Arrays.asList(g2.getId()));
        assertFalse("am21a: no longer in g1", am.getAnnotations().get(0).isInGroup(g1.getId()));
        assertTrue("am21b: now in g2", am.getAnnotations().get(0).isInGroup(g2.getId()));
    }

    // am22 — setAnnotationGroups ignores unknown group ids
    @Test
    public void am22_setAnnotationGroups_unknownIdIgnored() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/f.txt", "0", "mark");
        String key = am.getAnnotations().get(0).getKey();
        am.setAnnotationGroups(key, Arrays.asList("unknown-group"));
        assertEquals("am22: no groups assigned", 0, am.getAnnotations().get(0).getGroupIds().size());
    }

    // am23 — getGroupNamesString
    @Test
    public void am23_getGroupNamesString() throws Exception {
        AnnotationManager am = emptyManager();
        BookmarkGroup g = am.addGroup("Devotional");
        am.addAnnotationDirect("/p/f.txt", "0", "mark");
        String key = am.getAnnotations().get(0).getKey();
        am.assignAnnotationToGroup(key, g.getId());
        String names = am.getGroupNamesString(am.getAnnotations().get(0));
        assertEquals("am23: group name string", "Devotional", names);
    }

    // am24 — getGroupNamesString with null returns empty
    @Test
    public void am24_getGroupNamesString_null() throws Exception {
        AnnotationManager am = emptyManager();
        assertEquals("am24: null returns empty", "", am.getGroupNamesString(null));
    }

    // am25 — generateGroupName returns "Group 1" initially
    @Test
    public void am25_generateGroupName_firstGroup() throws Exception {
        AnnotationManager am = emptyManager();
        assertEquals("am25: first group name", "Group 1", am.generateGroupName());
    }

    // am26 — generateGroupName increments when "Group N" already used
    @Test
    public void am26_generateGroupName_increments() throws Exception {
        AnnotationManager am = emptyManager();
        am.addGroup("Group 1");
        assertEquals("am26: second group name", "Group 2", am.generateGroupName());
    }

    // am27 — healAnnotationLocalId re-keys the annotation
    @Test
    public void am27_healAnnotationLocalId() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/f.txt", "3", "mark");
        String oldKey = "/p/f.txt3";
        am.healAnnotationLocalId(oldKey, "99");
        String newKey = "/p/f.txt99";
        assertTrue("am27a: new key present", am.annotationExists(new Annotation("/p/f.txt", "99", "mark", new java.util.Date())));
        // old key is gone — annotationExists via docPath+docLocalId
        assertFalse("am27b: old key gone", am.annotationExists(new Annotation("/p/f.txt", "3", "mark", new java.util.Date())));
    }

    // am28 — needsFingerprintBackfill
    @Test
    public void am28_needsFingerprintBackfill_true() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/f.txt", "0", "mark"); // no fingerprint
        assertTrue("am28: needs backfill", am.needsFingerprintBackfill());
    }

    // am29 — needsFingerprintBackfill false when all have fingerprints
    @Test
    public void am29_needsFingerprintBackfill_false() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/f.txt", "0", "mark");
        am.getAnnotations().get(0).setContentFingerprint("fp-set");
        assertFalse("am29: no backfill needed", am.needsFingerprintBackfill());
    }

    // am30 — saveIfDirty writes JSON, reload preserves data
    @Test
    public void am30_saveIfDirty_persistsData() throws Exception {
        AnnotationManager am = emptyManager();
        am.addAnnotationDirect("/p/file.txt", "7", "saved");
        am.markDirty();
        am.saveIfDirty();
        // Reload from the same file
        AnnotationManager am2 = new AnnotationManager(null, annotationsFile.getAbsolutePath());
        assertEquals("am30a: annotation count preserved", 1, am2.getNumAnnotations());
        assertEquals("am30b: annotation name preserved", "saved", am2.getAnnotations().get(0).getName());
    }

    // am31 — maxItems constructor parameter is accepted without error
    @Test
    public void am31_maxItemsConstructor_noError() throws Exception {
        annotationsFile = tmp.newFile("maxitems.json");
        try (FileWriter fw = new FileWriter(annotationsFile)) {
            fw.write("{\"version\":\"2.0\",\"groups\":[],\"items\":[]}");
        }
        AnnotationManager am = new AnnotationManager(null, annotationsFile.getAbsolutePath(), 2);
        assertNotNull("am31: constructor with maxItems succeeds", am);
        assertEquals("am31b: starts empty", 0, am.getNumAnnotations());
    }
}
