package org.jaya.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ZipUtils}.
 */
public class ZipUtilsTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    /** Creates a zip file containing one entry with the given name and content. */
    private File createZip(String entryName, byte[] content) throws Exception {
        File zipFile = tmp.newFile("test.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(content);
            zos.closeEntry();
        }
        return zipFile;
    }

    // zu01 — unzip extracts file with correct content
    @Test
    public void zu01_unzip_extractsFile() throws Exception {
        byte[] content = "shrI rAma".getBytes("UTF-8");
        File zipFile = createZip("output.txt", content);
        File destDir = tmp.newFolder("dest");

        ZipUtils.unzipFile(zipFile.getAbsolutePath(), destDir.getAbsolutePath());

        File extracted = new File(destDir, "output.txt");
        assertTrue("zu01a: extracted file must exist", extracted.exists());
        assertEquals("zu01b: content must match", content.length, (int) extracted.length());
    }

    // zu02 — unzip with nested entry creates parent directories
    @Test
    public void zu02_unzip_createsNestedDirectories() throws Exception {
        byte[] content = "nested content".getBytes("UTF-8");
        File zipFile = createZip("a/b/file.txt", content);
        File destDir = tmp.newFolder("nested");

        ZipUtils.unzipFile(zipFile.getAbsolutePath(), destDir.getAbsolutePath());

        File extracted = new File(destDir, "a" + File.separator + "b" + File.separator + "file.txt");
        assertTrue("zu02: nested file must exist", extracted.exists());
    }

    // zu03 — unzip clears dest directory first (previous files removed)
    @Test
    public void zu03_unzip_clearsPreviousDestContent() throws Exception {
        File destDir = tmp.newFolder("fresh");
        // plant a file that should NOT survive
        File oldFile = new File(destDir, "stale.txt");
        oldFile.createNewFile();
        assertTrue("precondition: stale file exists", oldFile.exists());

        File zipFile = createZip("new.txt", "new".getBytes("UTF-8"));
        ZipUtils.unzipFile(zipFile.getAbsolutePath(), destDir.getAbsolutePath());

        assertFalse("zu03: stale file must be cleared on unzip", oldFile.exists());
    }
}
