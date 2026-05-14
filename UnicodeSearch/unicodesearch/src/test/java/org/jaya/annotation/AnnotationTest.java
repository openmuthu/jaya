package org.jaya.annotation;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Annotation}: fingerprint logic and group management.
 */
public class AnnotationTest {

    // --- buildFingerprint ---

    @Test
    public void an01_buildFingerprint_null_returnsEmpty() {
        assertEquals("an01: null returns empty", "", Annotation.buildFingerprint(null));
    }

    @Test
    public void an02_buildFingerprint_empty_returnsEmpty() {
        assertEquals("an02: empty string stays empty", "", Annotation.buildFingerprint(""));
    }

    @Test
    public void an03_buildFingerprint_shortContent_unchanged() {
        String content = "shrI rAma";
        String fp = Annotation.buildFingerprint(content);
        assertEquals("an03: short content preserved", content, fp);
    }

    @Test
    public void an04_buildFingerprint_collapseWhitespace() {
        String fp = Annotation.buildFingerprint("shrI  rAma"); // two spaces
        assertEquals("an04: internal whitespace collapsed", "shrI rAma", fp);
    }

    @Test
    public void an05_buildFingerprint_leadingTrailingWhitespace_trimmed() {
        String fp = Annotation.buildFingerprint("  hello world  ");
        assertEquals("an05: trimmed", "hello world", fp);
    }

    @Test
    public void an06_buildFingerprint_longContent_truncatedAt120() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append('a');
        String fp = Annotation.buildFingerprint(sb.toString());
        assertEquals("an06: truncated at FINGERPRINT_LENGTH", Annotation.FINGERPRINT_LENGTH, fp.length());
    }

    @Test
    public void an07_buildFingerprint_exactLength_notTruncated() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Annotation.FINGERPRINT_LENGTH; i++) sb.append('x');
        String fp = Annotation.buildFingerprint(sb.toString());
        assertEquals("an07: exact length preserved", Annotation.FINGERPRINT_LENGTH, fp.length());
    }

    // --- setContentFingerprint / getContentFingerprint ---

    @Test
    public void an08_setGetFingerprint() {
        Annotation a = new Annotation("/path", "0", "name", new Date());
        a.setContentFingerprint("test-fp");
        assertEquals("an08: fingerprint stored and retrieved", "test-fp", a.getContentFingerprint());
    }

    @Test
    public void an09_setFingerprint_null_storesEmpty() {
        Annotation a = new Annotation("/path", "0", "name", new Date());
        a.setContentFingerprint(null);
        assertEquals("an09: null fp stored as empty", "", a.getContentFingerprint());
    }

    // --- group management ---

    @Test
    public void an10_addGroupId_single() {
        Annotation a = new Annotation("/path", "0", "name", new Date());
        a.addGroupId("g1");
        assertTrue("an10: group g1 added", a.isInGroup("g1"));
    }

    @Test
    public void an11_addGroupId_noDuplicates() {
        Annotation a = new Annotation("/path", "0", "name", new Date());
        a.addGroupId("g1");
        a.addGroupId("g1");
        assertEquals("an11: duplicate not added", 1, a.getGroupIds().size());
    }

    @Test
    public void an12_addGroupId_null_ignored() {
        Annotation a = new Annotation("/path", "0", "name", new Date());
        a.addGroupId(null);
        assertEquals("an12: null not added", 0, a.getGroupIds().size());
    }

    @Test
    public void an13_removeGroupId() {
        Annotation a = new Annotation("/path", "0", "name", new Date());
        a.addGroupId("g1");
        a.addGroupId("g2");
        a.removeGroupId("g1");
        assertFalse("an13a: g1 removed", a.isInGroup("g1"));
        assertTrue("an13b: g2 still present", a.isInGroup("g2"));
    }

    @Test
    public void an14_setGroupIds_replacesPrevious() {
        Annotation a = new Annotation("/path", "0", "name", new Date());
        a.addGroupId("old");
        a.setGroupIds(Arrays.asList("g1", "g2"));
        assertFalse("an14a: old group gone", a.isInGroup("old"));
        assertTrue("an14b: g1 present", a.isInGroup("g1"));
        assertTrue("an14c: g2 present", a.isInGroup("g2"));
    }

    @Test
    public void an15_getGroupIds_returnsCopy() {
        Annotation a = new Annotation("/path", "0", "name", new Date());
        a.addGroupId("g1");
        List<String> ids = a.getGroupIds();
        ids.add("mutant");
        assertFalse("an15: mutating returned list must not affect annotation", a.isInGroup("mutant"));
    }

    @Test
    public void an16_isInGroup_false_forAbsentGroup() {
        Annotation a = new Annotation("/path", "0", "name", new Date());
        assertFalse("an16: absent group must return false", a.isInGroup("missing"));
    }

    // --- basic field accessors ---

    @Test
    public void an17_directConstructor_fieldsSet() {
        Date d = new Date(1_000_000L);
        Annotation a = new Annotation("/doc/path", "42", "myName", d);
        assertEquals("an17a: docPath", "/doc/path", a.getDocPath());
        assertEquals("an17b: docLocalId", "42", a.getDocLocalId());
        assertEquals("an17c: name", "myName", a.getName());
        assertEquals("an17d: updatedDate", d, a.getUpdatedDate());
    }

    @Test
    public void an18_setName() {
        Annotation a = new Annotation("/path", "0", "original", new Date());
        a.setName("updated");
        assertEquals("an18: name updated", "updated", a.getName());
    }

    @Test
    public void an19_setNotes() {
        Annotation a = new Annotation("/path", "0", "name", new Date());
        a.setNotes("my notes");
        assertEquals("an19: notes stored", "my notes", a.getNotes());
    }

    @Test
    public void an20_getKey_isDocPathPlusLocalId() {
        Annotation a = new Annotation("/doc/path", "7", "name", new Date());
        assertEquals("an20: key is docPath+localId", "/doc/path7", a.getKey());
    }

    // --- constructors with null ResultDocument ---

    @Test
    public void an21_constructorResultDoc_null_producesEmpty() {
        // When ResultDocument is null, init() returns early — fields stay empty
        Annotation a = new Annotation((org.jaya.search.ResultDocument) null);
        assertEquals("an21a: path empty", "", a.getDocPath());
        assertEquals("an21b: localId empty", "", a.getDocLocalId());
    }

    @Test
    public void an22_constructorResultDocNameDate_null_producesEmpty() {
        Annotation a = new Annotation((org.jaya.search.ResultDocument) null, "shrI", new Date());
        assertEquals("an22: path empty when null resDoc", "", a.getDocPath());
    }

    // --- setDocLocalId / setUpdatedDate ---

    @Test
    public void an23_setDocLocalId() {
        Annotation a = new Annotation("/p/f", "1", "n", new Date());
        a.setDocLocalId("99");
        assertEquals("an23: localId updated", "99", a.getDocLocalId());
        assertEquals("an23b: key uses new id", "/p/f99", a.getKey());
    }

    @Test
    public void an24_setUpdatedDate() {
        Date d1 = new Date(1000);
        Date d2 = new Date(2000);
        Annotation a = new Annotation("/p/f", "0", "n", d1);
        a.setUpdatedDate(d2);
        assertEquals("an24: date updated", d2, a.getUpdatedDate());
    }

    // --- equals ---

    @Test
    public void an25_equals_sameRefDocPlusId() {
        // equals uses == comparison (reference equality) for the string fields —
        // same literal constants satisfy ==
        Annotation a = new Annotation("/p/f", "0", "x", new Date());
        assertTrue("an25: annotation equals itself", a.equals(a));
    }

    @Test
    public void an26_equals_nonAnnotationReturnsFalse() {
        Annotation a = new Annotation("/p/f", "0", "x", new Date());
        assertFalse("an26: not equal to string", a.equals("string"));
    }

    @Test
    public void an27_equals_null_returnsFalse() {
        Annotation a = new Annotation("/p/f", "0", "x", new Date());
        assertFalse("an27: not equal to null", a.equals(null));
    }
}
