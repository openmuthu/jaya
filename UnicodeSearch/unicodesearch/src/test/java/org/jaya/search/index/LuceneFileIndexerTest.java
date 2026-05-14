package org.jaya.search.index;

import org.jaya.search.JayaIndexMetadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link LuceneUnicodeFileIndexer#deleteIndexEntriesWithFilePathPrefix}.
 *
 * Paths in the real index always start with '/' because addFilesToIndex strips
 * FILES_TO_INDEX_DIRECTORY (no trailing slash) from the absolute file path.
 * Tests here use the same leading-slash convention.
 */
public class LuceneFileIndexerTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File indexDir;
    private LuceneUnicodeFileIndexer indexer;

    @Before
    public void setUp() throws Exception {
        indexDir = tmp.newFolder("index");
        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        // Populate with two items, each with two files
        indexer.addDocumentWithPath("/item1/fileA.txt", "content of item1 fileA");
        indexer.addDocumentWithPath("/item1/sub/fileB.txt", "content of item1 fileB");
        indexer.addDocumentWithPath("/item2/fileC.txt", "content of item2 fileC");
        indexer.addDocumentWithPath("/item2/fileD.txt", "content of item2 fileD");
        indexer.close();
    }

    @After
    public void tearDown() throws Exception {
        // TemporaryFolder handles cleanup; indexer is closed in each test
    }

    private Set<String> metadata() {
        return new JayaIndexMetadata(indexDir.getAbsolutePath()).getIndexedFilePathSet();
    }

    // pu01 — prefix delete removes exactly the matching paths from metadata
    @Test
    public void pu01_prefixDelete_removesMatchingPathsFromMetadata() throws Exception {
        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.deleteIndexEntriesWithFilePathPrefix("/item1/");
        indexer.close();

        Set<String> remaining = metadata();
        assertFalse("pu01a: /item1/fileA.txt must be removed",
                remaining.contains("/item1/fileA.txt"));
        assertFalse("pu01b: /item1/sub/fileB.txt must be removed",
                remaining.contains("/item1/sub/fileB.txt"));
    }

    // pu02 — paths outside the prefix are untouched
    @Test
    public void pu02_prefixDelete_retainsNonMatchingPaths() throws Exception {
        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.deleteIndexEntriesWithFilePathPrefix("/item1/");
        indexer.close();

        Set<String> remaining = metadata();
        assertTrue("pu02a: /item2/fileC.txt must survive",
                remaining.contains("/item2/fileC.txt"));
        assertTrue("pu02b: /item2/fileD.txt must survive",
                remaining.contains("/item2/fileD.txt"));
    }

    // pu03 — deleting a prefix that matches nothing is a safe no-op
    @Test
    public void pu03_prefixDelete_noopWhenPrefixAbsent() throws Exception {
        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.deleteIndexEntriesWithFilePathPrefix("/nonexistent/");
        indexer.close();

        Set<String> remaining = metadata();
        assertEquals("pu03: all four paths must still be present", 4, remaining.size());
    }

    // pu04 — prefix WITHOUT leading slash must not match paths that have one
    @Test
    public void pu04_prefixWithoutLeadingSlash_doesNotMatchIndexedPaths() throws Exception {
        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        // "item1/" without the leading '/' — should match nothing
        indexer.deleteIndexEntriesWithFilePathPrefix("item1/");
        indexer.close();

        Set<String> remaining = metadata();
        assertTrue("pu04a: /item1/fileA.txt must be untouched without leading slash",
                remaining.contains("/item1/fileA.txt"));
        assertTrue("pu04b: /item1/sub/fileB.txt must be untouched without leading slash",
                remaining.contains("/item1/sub/fileB.txt"));
    }

    // pu05 — deleting all items leaves only the dummy document path (empty string)
    @Test
    public void pu05_prefixDelete_multipleCallsLeavesOnlyUnmatchedPaths() throws Exception {
        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.deleteIndexEntriesWithFilePathPrefix("/item1/");
        indexer.deleteIndexEntriesWithFilePathPrefix("/item2/");
        indexer.close();

        Set<String> remaining = metadata();
        assertFalse("pu05a: /item1/fileA.txt must be gone", remaining.contains("/item1/fileA.txt"));
        assertFalse("pu05b: /item1/sub/fileB.txt must be gone", remaining.contains("/item1/sub/fileB.txt"));
        assertFalse("pu05c: /item2/fileC.txt must be gone", remaining.contains("/item2/fileC.txt"));
        assertFalse("pu05d: /item2/fileD.txt must be gone", remaining.contains("/item2/fileD.txt"));
    }

    // pu06 — item name that is a string-prefix of another item name must NOT cross the boundary
    // e.g. deleting "/item/" must not touch "/item-extra/" or "/itemX/"
    @Test
    public void pu06_prefixDelete_doesNotCrossIntoSimilarlyNamedItem() throws Exception {
        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.addDocumentWithPath("/item/file.txt", "short item content");
        indexer.addDocumentWithPath("/itemExtra/file.txt", "longer item content");
        indexer.addDocumentWithPath("/item-variant/file.txt", "hyphenated variant");
        indexer.close();

        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.deleteIndexEntriesWithFilePathPrefix("/item/");
        indexer.close();

        Set<String> remaining = metadata();
        assertFalse("pu06a: /item/file.txt must be deleted",
                remaining.contains("/item/file.txt"));
        assertTrue("pu06b: /itemExtra/file.txt must NOT be deleted (different item)",
                remaining.contains("/itemExtra/file.txt"));
        assertTrue("pu06c: /item-variant/file.txt must NOT be deleted",
                remaining.contains("/item-variant/file.txt"));
    }

    // pu07 — realistic dAsasAhitya vs similar name scenario
    @Test
    public void pu07_prefixDelete_realWorldItemNameDoesNotAffectSiblings() throws Exception {
        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.addDocumentWithPath("/dAsasAhitya/saint1/kIrtane/1.txt", "content");
        indexer.addDocumentWithPath("/dAsasAhitya/saint2/ugAbhOga/1.txt", "content");
        indexer.addDocumentWithPath("/dAsasAhityaExtra/file.txt", "extra item");
        indexer.addDocumentWithPath("/dAsa/file.txt", "shorter name");
        indexer.close();

        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.deleteIndexEntriesWithFilePathPrefix("/dAsasAhitya/");
        indexer.close();

        Set<String> remaining = metadata();
        assertFalse("pu07a: saint1 file must be removed",
                remaining.contains("/dAsasAhitya/saint1/kIrtane/1.txt"));
        assertFalse("pu07b: saint2 file must be removed",
                remaining.contains("/dAsasAhitya/saint2/ugAbhOga/1.txt"));
        assertTrue("pu07c: /dAsasAhityaExtra/file.txt must survive",
                remaining.contains("/dAsasAhityaExtra/file.txt"));
        assertTrue("pu07d: /dAsa/file.txt must survive",
                remaining.contains("/dAsa/file.txt"));
    }

    // pu08 — null prefix is a safe no-op
    @Test
    public void pu08_prefixDelete_nullPrefix_isNoOp() throws Exception {
        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.deleteIndexEntriesWithFilePathPrefix(null);
        indexer.close();

        assertEquals("pu08: null prefix must leave all 4 paths intact", 4, metadata().size());
    }

    // pu09 — empty string prefix is a safe no-op (would otherwise delete everything)
    @Test
    public void pu09_prefixDelete_emptyPrefix_isNoOp() throws Exception {
        indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.deleteIndexEntriesWithFilePathPrefix("");
        indexer.close();

        assertEquals("pu09: empty prefix must leave all 4 paths intact", 4, metadata().size());
    }
}
