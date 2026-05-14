package org.jaya.indexsync;

import org.jaya.search.JayaIndexMetadata;
import org.jaya.search.index.LuceneUnicodeFileIndexer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

/**
 * Tests for {@link IndexCatalogueItemInstaller#installToIndex}.
 */
public class IndexCatalogueItemInstallerInstallTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    /** Build a zip containing a real Lucene index. */
    private File buildIndexZip(String itemName, File indexDir) throws Exception {
        // Build a small index first
        LuceneUnicodeFileIndexer indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.addDocumentWithPath("/" + itemName + "/file.txt", "content for " + itemName);
        indexer.close();

        // Zip all index files (read-only — do NOT truncate them)
        File zipFile = tmp.newFile(itemName + ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File f : indexDir.listFiles()) {
                if (f.isFile()) {
                    ZipEntry entry = new ZipEntry(f.getName());
                    zos.putNextEntry(entry);
                    java.nio.file.Files.copy(f.toPath(), zos);
                    zos.closeEntry();
                }
            }
        }
        return zipFile;
    }

    // ii01 — installToIndex returns false for non-existent zip
    @Test
    public void ii01_installToIndex_missingZip_returnsFalse() throws Exception {
        File destIndex = tmp.newFolder("dest");
        // Seed the dest index with dummy doc so Lucene can open it
        LuceneUnicodeFileIndexer seed = new LuceneUnicodeFileIndexer(destIndex.getAbsolutePath());
        seed.close();

        boolean result = IndexCatalogueItemInstaller.getInstance()
                .installToIndex("/nonexistent/path.zip", destIndex.getAbsolutePath());
        assertFalse("ii01: non-existent zip returns false", result);
    }

    // ii02 — installToIndex with a real index zip returns true and adds paths
    @Test
    public void ii02_installToIndex_realZip_returnsTrue() throws Exception {
        File srcIndex = tmp.newFolder("src-index");
        File destIndex = tmp.newFolder("dest-index");

        // Seed destination so Lucene can APPEND to it
        LuceneUnicodeFileIndexer seed = new LuceneUnicodeFileIndexer(destIndex.getAbsolutePath());
        seed.close();

        // Build a zip of a small Lucene index
        File zipFile = buildIndexZip("myitem", srcIndex);

        boolean result = IndexCatalogueItemInstaller.getInstance()
                .installToIndex(zipFile.getAbsolutePath(), destIndex.getAbsolutePath());
        assertTrue("ii02: real zip returns true", result);
    }
}
