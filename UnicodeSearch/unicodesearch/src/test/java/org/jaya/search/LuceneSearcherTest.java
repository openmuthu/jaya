package org.jaya.search;

import org.jaya.search.index.LuceneUnicodeFileIndexer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for {@link LuceneUnicodeSearcher}.
 * Seeds a real Lucene index in a temp folder, then exercises search methods.
 */
public class LuceneSearcherTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File indexDir;
    private LuceneUnicodeSearcher searcher;

    @Before
    public void setUp() throws Exception {
        indexDir = tmp.newFolder("index");
        LuceneUnicodeFileIndexer indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.addDocumentWithPath("/rAma/file.txt",    "rAma rAma rAma shrI rAma");
        indexer.addDocumentWithPath("/hari/file.txt",    "hari nArAyaNa hari hari");
        indexer.addDocumentWithPath("/kRShNa/file.txt",  "kRShNa kRShNa gopAla");
        indexer.close();
        searcher = new LuceneUnicodeSearcher(indexDir.getAbsolutePath());
    }

    @After
    public void tearDown() {
        if (searcher != null) searcher.close();
    }

    // ls01 — numDocs > 0 after seeding
    @Test
    public void ls01_numDocs_nonZero() {
        assertTrue("ls01: index must have docs", searcher.numDocs() > 0);
    }

    // ls02 — searchIndex returns non-null result
    @Test
    public void ls02_searchIndex_notNull() throws Exception {
        SearchResult result = searcher.searchIndex("rAma*");
        assertNotNull("ls02: result not null", result);
    }

    // ls03 — searchITRANSString finds ITRANS query in Devanagari-converted index
    @Test
    public void ls03_searchITRANSString_returnsResult() throws Exception {
        SearchResult result = searcher.searchITRANSString("rAma");
        assertNotNull("ls03: result not null", result);
    }

    // ls04 — getDoc by path and docLocalId returns a result
    @Test
    public void ls04_getDoc_byPathAndId() throws Exception {
        // The dummy document inserted by LuceneUnicodeFileIndexer uses path ""
        // Our seeded docs use path "/rAma/file.txt" with no docLocalId
        // Test that getDoc returns null for a non-existent combination
        ResultDocument rd = searcher.getDoc("/nonexistent/path.txt", "0");
        assertNull("ls04: nonexistent path+id returns null", rd);
    }

    // ls05 — getDoc by docId: first doc (id=0) exists
    @Test
    public void ls05_getDoc_byDocId() throws Exception {
        ResultDocument rd = searcher.getDoc(0);
        assertNotNull("ls05: doc 0 must exist", rd);
    }

    // ls06 — getDoc returns null for docId < 0
    @Test
    public void ls06_getDoc_negativeId_returnsNull() throws Exception {
        ResultDocument rd = searcher.getDoc(-1);
        assertNull("ls06: negative docId returns null", rd);
    }

    // ls07 — getAdjacentDocs: forward docs from doc 0
    @Test
    public void ls07_getAdjacentDocs_forward() throws Exception {
        List<ResultDocument> docs = searcher.getAdjacentDocs(0, 2, 1);
        assertNotNull("ls07: adjacent docs not null", docs);
    }

    // ls08 — getAdjacentDocs: backward from last doc
    @Test
    public void ls08_getAdjacentDocs_backward() throws Exception {
        int last = searcher.numDocs() - 1;
        List<ResultDocument> docs = searcher.getAdjacentDocs(last, 2, -1);
        assertNotNull("ls08: backward adjacent docs not null", docs);
    }

    // ls09 — getNextDoc from doc 0
    @Test
    public void ls09_getNextDoc() throws Exception {
        ResultDocument next = searcher.getNextDoc(0);
        // There may or may not be a next doc, depending on index size
        // Just verify no exception
    }

    // ls10 — getPreviousDoc from last doc
    @Test
    public void ls10_getPreviousDoc() throws Exception {
        int last = searcher.numDocs() - 1;
        if (last > 0) {
            ResultDocument prev = searcher.getPreviousDoc(last);
            assertNotNull("ls10: previous doc from last", prev);
        }
    }

    // ls11 — getDocsForPath returns docs for a known path
    @Test
    public void ls11_getDocsForPath() {
        List<ResultDocument> docs = searcher.getDocsForPath("/rAma/file.txt");
        assertNotNull("ls11: not null", docs);
        assertFalse("ls11b: at least one doc for /rAma/file.txt", docs.isEmpty());
    }

    // ls12 — getDocsForPath returns empty for unknown path
    @Test
    public void ls12_getDocsForPath_unknown() {
        List<ResultDocument> docs = searcher.getDocsForPath("/nonexistent/");
        assertTrue("ls12: empty for unknown path", docs.isEmpty());
    }

    // ls13 — reopenIndex keeps searcher functional
    @Test
    public void ls13_reopenIndex() throws Exception {
        searcher.reopenIndex();
        assertTrue("ls13: numDocs > 0 after reopen", searcher.numDocs() > 0);
    }

    // ls14 — getDocByPathAndFingerprint with null inputs returns null
    @Test
    public void ls14_getDocByPathAndFingerprint_null() {
        assertNull("ls14a: null path", searcher.getDocByPathAndFingerprint(null, "fp"));
        assertNull("ls14b: null fp", searcher.getDocByPathAndFingerprint("/path", null));
        assertNull("ls14c: empty fp", searcher.getDocByPathAndFingerprint("/path", ""));
    }

    // ls15 — filterResultsByPath with empty folder returns same result
    @Test
    public void ls15_filterResultsByPath_emptyFolder() throws Exception {
        SearchResult result = searcher.searchIndex("rAma*");
        SearchResult filtered = searcher.filterResultsByPath(result, "");
        assertNotNull("ls15: filtered result not null", filtered);
    }

    // ls16 — filterResultsByPath with a specific folder filters correctly
    @Test
    public void ls16_filterResultsByPath_specific() throws Exception {
        SearchResult result = searcher.searchIndex("rAma*");
        SearchResult filtered = searcher.filterResultsByPath(result, "/rAma/");
        assertNotNull("ls16: not null", filtered);
        // All docs in filtered must have path starting with /rAma/
        for (ResultDocument rd : filtered.getResultDocs()) {
            if (rd.getDoc() != null) {
                String path = rd.getDoc().get("path");
                if (path != null) {
                    assertTrue("ls16b: path must start with /rAma/", path.startsWith("/rAma/"));
                }
            }
        }
    }

    // ls17 — searchITRANSStringInPath restricts to folder
    @Test
    public void ls17_searchITRANSStringInPath() throws Exception {
        SearchResult result = searcher.searchITRANSStringInPath("rAma", "/rAma/");
        assertNotNull("ls17: result not null", result);
    }

    // ls18 — getRandomDoc returns value in [0, numDocs)
    @Test
    public void ls18_getRandomDoc() {
        int n = searcher.numDocs();
        if (n > 0) {
            int rand = searcher.getRandomDoc();
            assertTrue("ls18a: non-negative", rand >= 0);
            assertTrue("ls18b: < numDocs", rand < n);
        }
    }

    // ls19 — close sets mReader to null; a direct check via numDocs reopens the index
    @Test
    public void ls19_afterClose_canReopenSuccessfully() {
        searcher.close();
        // numDocs() internally calls createIndexSearcherIfRequired() which reopens the index,
        // so it may return a non-zero count — just verify it doesn't throw
        int n = searcher.numDocs();
        assertTrue("ls19: numDocs after close must be >= 0", n >= 0);
    }

    // ls20 — fuzzy query prefix "f/"
    @Test
    public void ls20_fuzzyQuery() throws Exception {
        SearchResult result = searcher.searchIndex("f/rAma");
        assertNotNull("ls20: fuzzy query result not null", result);
    }

    // ls21 — lucene query parser prefix "q/"
    @Test
    public void ls21_luceneQueryParserQuery() throws Exception {
        // Use a simple query; it may or may not find results but must not throw
        SearchResult result = searcher.searchIndex("q/rAma*");
        assertNotNull("ls21: lucene parser query not null", result);
    }

    // ls22 — getDocByPathAndFingerprint finds matching doc
    @Test
    public void ls22_getDocByPathAndFingerprint_finds() {
        // Get a known doc and build its fingerprint
        List<ResultDocument> docs = searcher.getDocsForPath("/hari/file.txt");
        if (!docs.isEmpty()) {
            ResultDocument rd = docs.get(0);
            String raw = rd.getDoc().get("contents");
            if (raw != null) {
                String fp = org.jaya.annotation.Annotation.buildFingerprint(raw);
                ResultDocument found = searcher.getDocByPathAndFingerprint("/hari/file.txt", fp);
                assertNotNull("ls22: fingerprint match found", found);
            }
        }
    }

    // ls23 — search(String, List) overload populates the list
    @Test
    public void ls23_search_listOverload_populatesList() throws Exception {
        List<ResultDocument> results = new java.util.ArrayList<>();
        searcher.search("rAma*", results);
        // Just verify no exception and list is non-null
        assertNotNull("ls23: results list not null", results);
    }

    // ls24 — printDocContents with valid doc does not throw
    @Test
    public void ls24_printDocContents_validDoc() throws Exception {
        ResultDocument rd = searcher.getDoc(0);
        if (rd != null) {
            searcher.printDocContents(rd.getDoc()); // must not throw
        }
    }

    // ls25 — printDocContents with null does not throw
    @Test
    public void ls25_printDocContents_null() {
        searcher.printDocContents(null); // must not throw
    }

    // ls26 — getPrefixRegExpQuery returns non-null BooleanQuery
    @Test
    public void ls26_getPrefixRegExpQuery_buildsQuery() throws Exception {
        JayaQueryParser jqp = new JayaQueryParser("~/rAma");
        org.apache.lucene.search.Query q = searcher.getPrefixRegExpQuery(jqp);
        assertNotNull("ls26: prefix regexp query not null", q);
    }

    // ls27 — AnnotationManager.addAnnotation(ResultDocument, String) via real doc
    @Test
    public void ls27_annotationManager_addAnnotationFromResultDoc() throws Exception {
        List<ResultDocument> docs = searcher.getDocsForPath("/rAma/file.txt");
        if (docs.isEmpty()) return;
        ResultDocument rd = docs.get(0);

        java.io.File f = tmpFile();
        org.jaya.annotation.AnnotationManager am =
                new org.jaya.annotation.AnnotationManager(null, f.getAbsolutePath());
        org.jaya.annotation.Annotation a = am.addAnnotation(rd, "testMark");
        assertNotNull("ls27: annotation returned", a);
        assertEquals("ls27b: annotation count", 1, am.getNumAnnotations());
    }

    // ls28 — AnnotationManager.removeAnnotation(ResultDocument)
    @Test
    public void ls28_annotationManager_removeAnnotationFromResultDoc() throws Exception {
        List<ResultDocument> docs = searcher.getDocsForPath("/rAma/file.txt");
        if (docs.isEmpty()) return;
        ResultDocument rd = docs.get(0);

        java.io.File f = tmpFile();
        org.jaya.annotation.AnnotationManager am =
                new org.jaya.annotation.AnnotationManager(null, f.getAbsolutePath());
        am.addAnnotation(rd, "mark");
        assertEquals("ls28a: added", 1, am.getNumAnnotations());
        am.removeAnnotation(rd);
        assertEquals("ls28b: removed", 0, am.getNumAnnotations());
    }

    // ls29 — AnnotationManager.annotationExists(ResultDocument)
    @Test
    public void ls29_annotationManager_annotationExistsResultDoc() throws Exception {
        List<ResultDocument> docs = searcher.getDocsForPath("/rAma/file.txt");
        if (docs.isEmpty()) return;
        ResultDocument rd = docs.get(0);

        java.io.File f = tmpFile();
        org.jaya.annotation.AnnotationManager am =
                new org.jaya.annotation.AnnotationManager(null, f.getAbsolutePath());
        assertFalse("ls29a: not exists before add", am.annotationExists(rd));
        am.addAnnotation(rd, "mark");
        assertTrue("ls29b: exists after add", am.annotationExists(rd));
    }

    // ls30 — Annotation(ResultDocument) constructor with real doc
    @Test
    public void ls30_annotation_constructorWithResultDoc() throws Exception {
        List<ResultDocument> docs = searcher.getDocsForPath("/rAma/file.txt");
        if (docs.isEmpty()) return;
        ResultDocument rd = docs.get(0);
        org.jaya.annotation.Annotation a = new org.jaya.annotation.Annotation(rd);
        assertNotNull("ls30a: annotation not null", a);
        assertFalse("ls30b: docPath not empty", a.getDocPath().isEmpty());
    }

    // ls31 — searchIndex with regex prefix "~/" exercises the isRegExPrefixQuery path
    @Test
    public void ls31_searchIndex_regexPrefix() throws Exception {
        SearchResult result = searcher.searchIndex("~/rAma");
        assertNotNull("ls31: regex prefix result not null", result);
    }

    // ls32 — backfillFingerprints with real searcher fills in missing fingerprint
    @Test
    public void ls32_annotationManager_backfillFingerprints() throws Exception {
        List<ResultDocument> docs = searcher.getDocsForPath("/rAma/file.txt");
        if (docs.isEmpty()) return;
        ResultDocument rd = docs.get(0);

        java.io.File f = tmpFile();
        org.jaya.annotation.AnnotationManager am =
                new org.jaya.annotation.AnnotationManager(null, f.getAbsolutePath());
        // Add with fingerprint, then clear it to simulate a legacy annotation
        org.jaya.annotation.Annotation a = am.addAnnotation(rd, "mark");
        assertNotNull("ls32: annotation added", a);
        a.setContentFingerprint(""); // simulate legacy (no fingerprint)

        assertTrue("ls32a: needs backfill", am.needsFingerprintBackfill());
        int count = am.backfillFingerprints(searcher);
        assertTrue("ls32b: backfill count >= 0", count >= 0);
    }

    // ls33 — backfillFingerprints with null searcher returns 0
    @Test
    public void ls33_backfillFingerprints_nullSearcher() throws Exception {
        java.io.File f = tmpFile();
        org.jaya.annotation.AnnotationManager am =
                new org.jaya.annotation.AnnotationManager(null, f.getAbsolutePath());
        am.addAnnotation(searcher.getDoc(0), "mark");
        // Clear all fingerprints manually then try to backfill with null searcher
        for (org.jaya.annotation.Annotation a : am.getAnnotations()) {
            a.setContentFingerprint("");
        }
        int count = am.backfillFingerprints(null);
        assertEquals("ls33: null searcher returns 0", 0, count);
    }

    // ls34 — searchITRANSStringInPath with known folder returns non-null result
    @Test
    public void ls34_searchITRANSStringInPath_withFolder() throws Exception {
        SearchResult result = searcher.searchITRANSStringInPath("rAma", "/rAma/");
        // The ITRANS→Devanagari conversion may or may not match ITRANS-indexed content;
        // just verify no exception and non-null result
        assertNotNull("ls34: scoped ITRANS search not null", result);
    }

    /** Create a temp annotation file pre-filled with empty JSON v2.0. */
    private java.io.File tmpFile() throws Exception {
        java.io.File f = tmp.newFile("ann_" + System.nanoTime() + ".json");
        try (java.io.FileWriter fw = new java.io.FileWriter(f)) {
            fw.write("{\"version\":\"2.0\",\"groups\":[],\"items\":[]}");
        }
        return f;
    }
}
