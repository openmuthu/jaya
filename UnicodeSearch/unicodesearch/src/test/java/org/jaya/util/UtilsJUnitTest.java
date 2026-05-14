package org.jaya.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * JUnit tests for {@link Utils} (pure-Java methods only).
 */
public class UtilsJUnitTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    // --- removeExtension ---

    @Test
    public void ut01_removeExtension_normal() {
        assertEquals("ut01: dot extension removed", "file", Utils.removeExtension("file.txt"));
    }

    @Test
    public void ut02_removeExtension_null() {
        assertNull("ut02: null input must return null", Utils.removeExtension(null));
    }

    @Test
    public void ut03_removeExtension_noExtension() {
        assertEquals("ut03: no dot in name", "nodot", Utils.removeExtension("nodot"));
    }

    @Test
    public void ut04_removeExtension_dotAtStart() {
        // dotIndex <= 0 means hidden file stays as-is
        assertEquals("ut04: leading dot only", ".hidden", Utils.removeExtension(".hidden"));
    }

    // --- getFileExtension ---

    @Test
    public void ut05_getFileExtension_simple() {
        assertEquals("ut05: simple extension", "txt", Utils.getFileExtension("file.txt"));
    }

    @Test
    public void ut06_getFileExtension_null() {
        assertNull("ut06: null input returns null", Utils.getFileExtension(null));
    }

    @Test
    public void ut07_getFileExtension_noExtension() {
        assertEquals("ut07: no extension returns empty", "", Utils.getFileExtension("fileNoExt"));
    }

    @Test
    public void ut08_getFileExtension_withPath() {
        assertEquals("ut08: extension after path", "java", Utils.getFileExtension("/a/b/Foo.java"));
    }

    // --- getBaseName ---

    @Test
    public void ut09_getBaseName_simple() {
        assertEquals("ut09: basename without path", "file", Utils.getBaseName("/some/path/file.txt"));
    }

    @Test
    public void ut10_getBaseName_null() {
        assertNull("ut10: null returns null", Utils.getBaseName(null));
    }

    @Test
    public void ut11_getBaseName_noExtension() {
        assertEquals("ut11: no extension stays whole", "noext", Utils.getBaseName("/path/noext"));
    }

    // --- getHumanReadableSize ---

    @Test
    public void ut12_humanReadableSize_bytes() {
        assertEquals("ut12: < 1024 is bytes", "512 B", Utils.getHumanReadableSize(512));
    }

    @Test
    public void ut13_humanReadableSize_kilobytes() {
        String result = Utils.getHumanReadableSize(1024);
        assertTrue("ut13: 1024 bytes is KB", result.contains("K"));
    }

    @Test
    public void ut14_humanReadableSize_megabytes() {
        String result = Utils.getHumanReadableSize(1024L * 1024L);
        assertTrue("ut14: 1 MB", result.contains("M"));
    }

    // --- containsViramaChars ---

    @Test
    public void ut15_containsViramaChars_withDoubleStroke() {
        // \u0965 is the double-danda (Devanagari purna virama)
        assertTrue("ut15: \\u0965 must count as virama", Utils.containsViramaChars("\u0965"));
    }

    @Test
    public void ut16_containsViramaChars_doubleDanda() {
        // Two \u0964 in a row
        assertTrue("ut16: double \\u0964 counts", Utils.containsViramaChars("\u0964\u0964"));
    }

    @Test
    public void ut17_containsViramaChars_doubleSlash() {
        assertTrue("ut17: '// ' triggers virama", Utils.containsViramaChars("// text"));
    }

    @Test
    public void ut18_containsViramaChars_doublePipe() {
        assertTrue("ut18: '|| ' triggers virama", Utils.containsViramaChars("|| text"));
    }

    @Test
    public void ut19_containsViramaChars_empty() {
        assertFalse("ut19: empty string returns false", Utils.containsViramaChars(""));
    }

    @Test
    public void ut20_containsViramaChars_null() {
        assertFalse("ut20: null returns false", Utils.containsViramaChars(null));
    }

    @Test
    public void ut21_containsViramaChars_noVirama() {
        assertFalse("ut21: normal text has no virama", Utils.containsViramaChars("hello world"));
    }

    // --- closeSilently ---

    @Test
    public void ut22_closeSilently_null_noThrow() {
        // Must not throw
        Utils.closeSilently(null);
    }

    @Test
    public void ut23_closeSilently_closesResource() throws IOException {
        final boolean[] closed = {false};
        Closeable c = new Closeable() {
            @Override
            public void close() {
                closed[0] = true;
            }
        };
        Utils.closeSilently(c);
        assertTrue("ut23: resource must have been closed", closed[0]);
    }

    // --- deleteDir ---

    @Test
    public void ut24_deleteDir_removesNestedContents() throws IOException {
        File dir = tmp.newFolder("outer");
        File inner = new File(dir, "inner");
        inner.mkdir();
        new File(inner, "file.txt").createNewFile();

        Utils.deleteDir(dir);

        assertFalse("ut24: directory must be deleted", dir.exists());
    }

    // --- guessFileEncoding ---

    @Test
    public void ut25_guessFileEncoding_utf8File() throws IOException {
        File f = tmp.newFile("utf8.txt");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write("hello".getBytes("UTF-8"));
        }
        String enc = Utils.guessFileEncoding(f.getAbsolutePath());
        assertEquals("ut25: no BOM means UTF-8", "UTF-8", enc);
    }

    @Test
    public void ut26_guessFileEncoding_utf16BEFile() throws IOException {
        File f = tmp.newFile("utf16be.txt");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(new byte[]{(byte)0xFE, (byte)0xFF, 0x00, 0x41}); // UTF-16 BE BOM
        }
        String enc = Utils.guessFileEncoding(f.getAbsolutePath());
        assertEquals("ut26: UTF-16 BE BOM detected", "UTF-16", enc);
    }

    @Test
    public void ut27_guessFileEncoding_utf16LEFile() throws IOException {
        File f = tmp.newFile("utf16le.txt");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(new byte[]{(byte)0xFF, (byte)0xFE, 0x41, 0x00}); // UTF-16 LE BOM
        }
        String enc = Utils.guessFileEncoding(f.getAbsolutePath());
        assertEquals("ut27: UTF-16 LE BOM detected", "UTF-16", enc);
    }

    // --- getFileName ---

    @Test
    public void ut28_getFileName_simple() {
        assertEquals("ut28: filename with path", "file.txt", Utils.getFileName("/some/path/file.txt"));
    }

    @Test
    public void ut29_getFileName_null() {
        assertNull("ut29: null path returns null", Utils.getFileName(null));
    }

    @Test
    public void ut30_getFileName_noSlash() {
        assertEquals("ut30: no slash returns whole string", "file.txt", Utils.getFileName("file.txt"));
    }

    // --- getTagsBasedOnFilePath ---

    @Test
    public void ut31_getTagsBasedOnFilePath_notEmpty() {
        String tags = Utils.getTagsBasedOnFilePath("/rAma/rAmAyaNa.txt");
        assertNotNull("ut31: not null", tags);
        assertFalse("ut31b: not empty", tags.isEmpty());
    }

    // --- getListOfFiles ---

    @Test
    public void ut32_getListOfFiles_recursive() throws IOException {
        File dir = tmp.newFolder("root");
        File sub = new File(dir, "sub");
        sub.mkdir();
        new File(dir, "a.txt").createNewFile();
        new File(sub, "b.txt").createNewFile();

        java.util.List<File> files = Utils.getListOfFiles(dir);
        assertEquals("ut32: two files found recursively", 2, files.size());
    }

    @Test
    public void ut33_getListOfFiles_emptyDir() throws IOException {
        File dir = tmp.newFolder("empty");
        java.util.List<File> files = Utils.getListOfFiles(dir);
        assertTrue("ut33: empty dir returns empty list", files.isEmpty());
    }

    // --- getFirstLevelDirs ---

    @Test
    public void ut34_getFirstLevelDirs() throws IOException {
        File dir = tmp.newFolder("toplevel");
        new File(dir, "subA").mkdir();
        new File(dir, "subB").mkdir();
        new File(dir, "file.txt").createNewFile();

        java.util.List<File> dirs = Utils.getFirstLevelDirs(dir);
        assertEquals("ut34: two first-level dirs", 2, dirs.size());
    }
}
