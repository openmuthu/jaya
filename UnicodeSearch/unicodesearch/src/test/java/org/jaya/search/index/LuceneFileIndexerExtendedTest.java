package org.jaya.search.index;

import org.jaya.search.JayaIndexMetadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Extended tests for {@link LuceneUnicodeFileIndexer}: covers addFilesToIndex,
 * mergeIndexes, hasIndexableExtension, and deleteIndexEntriesWithFilePath.
 */
public class LuceneFileIndexerExtendedTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File indexDir;
    private File sourceDir;
    private LuceneUnicodeFileIndexer indexer;

    @Before
    public void setUp() throws Exception {
        indexDir = tmp.newFolder("idx");
        sourceDir = tmp.newFolder("src");
        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
    }

    @After
    public void tearDown() {
        if (indexer != null) {
            indexer.close();
        }
    }

    private File createTextFile(File dir, String name, String content) throws Exception {
        File f = new File(dir, name);
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8")) {
            w.write(content);
        }
        return f;
    }

    // ex01 — hasIndexableExtension: .txt is indexable
    @Test
    public void ex01_hasIndexableExtension_txt() {
        assertTrue("ex01: .txt is indexable", LuceneUnicodeFileIndexer.hasIndexableExtension("file.txt"));
    }

    // ex02 — hasIndexableExtension: .kw is not indexable
    @Test
    public void ex02_hasIndexableExtension_kw() {
        assertFalse("ex02: .kw is not indexable", LuceneUnicodeFileIndexer.hasIndexableExtension("tags.kw"));
    }

    // ex03 — hasIndexableExtension: no extension is not indexable
    @Test
    public void ex03_hasIndexableExtension_noExtension() {
        assertFalse("ex03: no extension is not indexable", LuceneUnicodeFileIndexer.hasIndexableExtension("fileNoExt"));
    }

    // ex04 — addFilesToIndex indexes .txt files from directory
    @Test
    public void ex04_addFilesToIndex_indexesTxtFiles() throws Exception {
        createTextFile(sourceDir, "rAma.txt",
                "rAma hari rAma\nrAma rAma hari hari\nrAma rAma rAma\nrAma");
        indexer.addFilesToIndex(sourceDir.getAbsolutePath());
        indexer.close();
        indexer = null;
        // Check metadata was written
        JayaIndexMetadata md = new JayaIndexMetadata(indexDir.getAbsolutePath());
        Set<String> paths = md.getIndexedFilePathSet();
        assertFalse("ex04: at least one path indexed", paths.isEmpty());
        // The path must contain "rAma"
        boolean found = false;
        for (String p : paths) {
            if (p.contains("rAma")) { found = true; break; }
        }
        assertTrue("ex04b: rAma.txt path in metadata", found);
    }

    // ex05 — addFilesToIndex skips non-.txt files
    @Test
    public void ex05_addFilesToIndex_skipsNonTxtFiles() throws Exception {
        createTextFile(sourceDir, "data.kw", "keyword data");
        indexer.addFilesToIndex(sourceDir.getAbsolutePath());
        indexer.close();
        indexer = null;
        JayaIndexMetadata md = new JayaIndexMetadata(indexDir.getAbsolutePath());
        Set<String> paths = md.getIndexedFilePathSet();
        boolean foundKw = false;
        for (String p : paths) {
            if (p.contains("data.kw")) { foundKw = true; break; }
        }
        assertFalse("ex05: .kw file must not be in metadata", foundKw);
    }

    // ex06 — deleteIndexEntriesWithFilePath removes exact path
    @Test
    public void ex06_deleteIndexEntriesWithFilePath() throws Exception {
        indexer.addDocumentWithPath("/item/file.txt", "some content");
        indexer.close();

        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.deleteIndexEntriesWithFilePath("/item/file.txt");
        indexer.close();
        indexer = null;

        JayaIndexMetadata md = new JayaIndexMetadata(indexDir.getAbsolutePath());
        assertFalse("ex06: deleted path must be gone",
                md.hasIndexedFile("/item/file.txt"));
    }

    // ex07 — mergeIndexes combines two indexes
    @Test
    public void ex07_mergeIndexes() throws Exception {
        // Create a second index
        File idx2 = tmp.newFolder("idx2");
        LuceneUnicodeFileIndexer idx2Indexer = new LuceneUnicodeFileIndexer(idx2.getAbsolutePath());
        idx2Indexer.addDocumentWithPath("/merged/file.txt", "merged content");
        idx2Indexer.close();

        // Add something to the main indexer too
        indexer.addDocumentWithPath("/main/file.txt", "main content");

        // Merge idx2 into main
        java.util.List<String> paths = new java.util.ArrayList<>();
        paths.add(idx2.getAbsolutePath());
        indexer.mergeIndexes(paths);
        indexer.close();
        indexer = null;

        JayaIndexMetadata md = new JayaIndexMetadata(indexDir.getAbsolutePath());
        Set<String> allPaths = md.getIndexedFilePathSet();
        assertTrue("ex07a: main path present", allPaths.contains("/main/file.txt"));
        assertTrue("ex07b: merged path present", allPaths.contains("/merged/file.txt"));
    }

    // ex08 — addFilesToIndex handles UTF-16 BOM file
    @Test
    public void ex08_addFilesToIndex_utf16File() throws Exception {
        // Create a file with UTF-16 LE BOM
        File f = new File(sourceDir, "utf16.txt");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            byte[] bom = new byte[]{(byte)0xFF, (byte)0xFE}; // UTF-16 LE BOM
            fos.write(bom);
            // Write "rAma" in UTF-16 LE
            String content = "rAma hari\n";
            fos.write(content.getBytes("UTF-16LE"));
        }
        // Should not throw
        indexer.addFilesToIndex(sourceDir.getAbsolutePath());
        indexer.close();
        indexer = null;
    }

    // ex09 — deleteIndexEntriesWithFilePathPrefix removes matching paths
    @Test
    public void ex09_deleteIndexEntriesWithFilePathPrefix() throws Exception {
        indexer.addDocumentWithPath("/prefix/doc1.txt", "content one");
        indexer.addDocumentWithPath("/prefix/doc2.txt", "content two");
        indexer.addDocumentWithPath("/other/doc3.txt",  "content three");
        indexer.close();

        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.deleteIndexEntriesWithFilePathPrefix("/prefix/");
        indexer.close();
        indexer = null;

        JayaIndexMetadata md = new JayaIndexMetadata(indexDir.getAbsolutePath());
        assertFalse("ex09a: prefix/doc1 removed", md.hasIndexedFile("/prefix/doc1.txt"));
        assertFalse("ex09b: prefix/doc2 removed", md.hasIndexedFile("/prefix/doc2.txt"));
        assertTrue("ex09c: other/doc3 still present", md.hasIndexedFile("/other/doc3.txt"));
    }

    // ex10 — deleteIndexEntriesWithFilePathPrefix with null/empty is a no-op
    @Test
    public void ex10_deletePrefix_nullOrEmpty_noOp() throws Exception {
        indexer.addDocumentWithPath("/k/file.txt", "kRShNa");
        indexer.deleteIndexEntriesWithFilePathPrefix(null);
        indexer.deleteIndexEntriesWithFilePathPrefix("");
        indexer.close();
        indexer = null;
        JayaIndexMetadata md = new JayaIndexMetadata(indexDir.getAbsolutePath());
        assertTrue("ex10: path still present after no-op deletes", md.hasIndexedFile("/k/file.txt"));
    }

    // ex11 — getListOfFilesToIndex lists files recursively
    @Test
    public void ex11_getListOfFilesToIndex_recursive() throws Exception {
        File sub = new File(sourceDir, "sub");
        sub.mkdir();
        createTextFile(sourceDir, "top.txt", "top");
        createTextFile(sub, "nested.txt", "nested");

        java.util.List<File> files = indexer.getListOfFilesToIndex(sourceDir);
        assertEquals("ex11: two files found", 2, files.size());
    }

    // ex12 — addADummyDocumentIfIndexIsEmpty is a no-op when index already has segments
    @Test
    public void ex12_addADummyDocument_noopIfIndexExists() throws Exception {
        // Constructor already called addADummyDocumentIfIndexIsEmpty once
        // Calling it again should be a no-op (segments.gen exists)
        int before = countDocs(indexDir);
        indexer.addADummyDocumentIfIndexIsEmpty();
        // Commit and check
        indexer.close();
        indexer = null;
        int after = countDocs(indexDir);
        assertEquals("ex12: doc count unchanged after no-op call", before, after);
    }

    private int countDocs(File dir) throws Exception {
        org.apache.lucene.index.DirectoryReader reader =
                org.apache.lucene.index.DirectoryReader.open(
                        org.apache.lucene.store.FSDirectory.open(dir));
        int n = reader.maxDoc();
        reader.close();
        return n;
    }
}
