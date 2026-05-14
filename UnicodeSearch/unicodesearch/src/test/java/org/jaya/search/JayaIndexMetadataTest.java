package org.jaya.search;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link JayaIndexMetadata}.
 */
public class JayaIndexMetadataTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    // jm01 — empty (no metadata file): getIndexedFilePathSet returns empty set
    @Test
    public void jm01_emptyDirectory_emptySet() throws Exception {
        File dir = tmp.newFolder("idx");
        JayaIndexMetadata md = new JayaIndexMetadata(dir.getAbsolutePath());
        assertTrue("jm01: empty set on new dir", md.getIndexedFilePathSet().isEmpty());
    }

    // jm02 — append adds paths and hasIndexedFile reflects them
    @Test
    public void jm02_append_addsPath() throws Exception {
        File dir = tmp.newFolder("idx2");
        JayaIndexMetadata md = new JayaIndexMetadata(dir.getAbsolutePath());
        Set<String> paths = new HashSet<>();
        paths.add("/dAsasAhitya/f.txt");
        md.append(paths);
        assertTrue("jm02a: hasIndexedFile true", md.hasIndexedFile("/dAsasAhitya/f.txt"));
        assertTrue("jm02b: getIndexedFilePathSet contains path",
                md.getIndexedFilePathSet().contains("/dAsasAhitya/f.txt"));
    }

    // jm03 — remove deletes paths and metadata file is updated
    @Test
    public void jm03_remove_deletesPath() throws Exception {
        File dir = tmp.newFolder("idx3");
        JayaIndexMetadata md = new JayaIndexMetadata(dir.getAbsolutePath());
        Set<String> paths = new HashSet<>();
        paths.add("/a/b.txt");
        paths.add("/c/d.txt");
        md.append(paths);
        Set<String> toRemove = new HashSet<>();
        toRemove.add("/a/b.txt");
        md.remove(toRemove);
        assertFalse("jm03a: removed path gone", md.hasIndexedFile("/a/b.txt"));
        assertTrue("jm03b: other path survives", md.hasIndexedFile("/c/d.txt"));
    }

    // jm04 — persistence: reload from disk restores paths
    @Test
    public void jm04_persistence_reload() throws Exception {
        File dir = tmp.newFolder("idx4");
        JayaIndexMetadata md = new JayaIndexMetadata(dir.getAbsolutePath());
        Set<String> paths = new HashSet<>();
        paths.add("/persist/file.txt");
        md.append(paths);
        // Reload from same directory
        JayaIndexMetadata md2 = new JayaIndexMetadata(dir.getAbsolutePath());
        assertTrue("jm04: reloaded metadata contains path",
                md2.hasIndexedFile("/persist/file.txt"));
    }

    // jm05 — getIndexedFilePathSet(String) static method
    @Test
    public void jm05_staticParseMethod() {
        Set<String> set = JayaIndexMetadata.getIndexedFilePathSet("/a/b.txt\r\n/c/d.txt");
        assertEquals("jm05a: two paths parsed", 2, set.size());
        assertTrue("jm05b: /a/b.txt present", set.contains("/a/b.txt"));
    }

    // jm06 — getIndexedFilePathSet(null) returns empty set
    @Test
    public void jm06_staticParseMethod_null() {
        Set<String> set = JayaIndexMetadata.getIndexedFilePathSet(null);
        assertTrue("jm06: null returns empty set", set.isEmpty());
    }

    // jm07 — remove with null paths is no-op
    @Test
    public void jm07_remove_null_noOp() throws Exception {
        File dir = tmp.newFolder("idx7");
        JayaIndexMetadata md = new JayaIndexMetadata(dir.getAbsolutePath());
        Set<String> paths = new HashSet<>();
        paths.add("/a/b.txt");
        md.append(paths);
        md.remove(null); // must not throw
        assertTrue("jm07: path still there after null remove", md.hasIndexedFile("/a/b.txt"));
    }

    // jm08 — getMetadataFile returns expected path
    @Test
    public void jm08_getMetadataFile() throws Exception {
        File dir = tmp.newFolder("idx8");
        JayaIndexMetadata md = new JayaIndexMetadata(dir.getAbsolutePath());
        File mf = md.getMetadataFile();
        assertTrue("jm08: metadata file name", mf.getName().endsWith(".txt"));
    }

    // jm09 — hasIndexedFile returns false for absent path
    @Test
    public void jm09_hasIndexedFile_absent() throws Exception {
        File dir = tmp.newFolder("idx9");
        JayaIndexMetadata md = new JayaIndexMetadata(dir.getAbsolutePath());
        assertFalse("jm09: absent path returns false", md.hasIndexedFile("/not/here.txt"));
    }
}
