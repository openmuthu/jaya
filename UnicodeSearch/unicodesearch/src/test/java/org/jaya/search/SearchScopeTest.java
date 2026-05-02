package org.jaya.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.junit.Before;
import org.junit.Test;
import org.jaya.util.Constatants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Unit tests for {@link LuceneUnicodeSearcher#filterResultsByPath}.
 *
 * No Lucene index on disk required — we build {@link SearchResult} /
 * {@link ResultDocument} objects in-memory using the package-private
 * constructors (same package: {@code org.jaya.search}).
 *
 * Scenarios covered
 * -----------------
 * UC-S1  null folderPath          → all results returned unchanged
 * UC-S2  empty folderPath         → all results returned unchanged
 * UC-S3  matching prefix          → only documents under that folder
 * UC-S4  non-matching prefix      → empty result list
 * UC-S5  partial prefix rejected  → "/A/" does not match "/AB/file.txt"
 * UC-S6  nested subfolder         → "/A/" matches "/A/B/file.txt"
 * UC-S7  multi-folder, one scope  → only the selected folder's docs returned
 * UC-S8  leading-slash paths      → real device path format works correctly
 */
public class SearchScopeTest {

    private LuceneUnicodeSearcher mSearcher;

    @Before
    public void setUp() {
        // No real index needed — we call filterResultsByPath directly
        // and never open the index reader.
        mSearcher = new LuceneUnicodeSearcher("/dev/null") {
            @Override
            public void createIndexSearcherIfRequired() {
                // Skip index-open so no IOException / no real directory needed.
            }
        };
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    /** Build a single-document SearchResult whose FIELD_PATH equals {@code path}. */
    private SearchResult resultWithPaths(String... paths) {
        JayaQueryParser jqp = new JayaQueryParser("dummy");
        java.util.List<ResultDocument> docs = new java.util.ArrayList<>();
        for (String path : paths) {
            Document luceneDoc = new Document();
            luceneDoc.add(new StringField(Constatants.FIELD_PATH, path, Field.Store.YES));
            docs.add(new ResultDocument(0, luceneDoc));
        }
        return new SearchResult(jqp, docs);
    }

    // ─── UC-S1: null folderPath → unchanged ──────────────────────────────────

    @Test
    public void filter_nullFolderPath_returnsAllResults() {
        SearchResult input = resultWithPaths("/AgamAH/a.txt", "/purANa/b.txt");
        SearchResult output = mSearcher.filterResultsByPath(input, null);
        assertEquals("All results must pass through when folderPath is null",
                2, output.getResultDocs().size());
        assertSame("Same SearchResult object must be returned",
                input, output);
    }

    // ─── UC-S2: empty folderPath → unchanged ─────────────────────────────────

    @Test
    public void filter_emptyFolderPath_returnsAllResults() {
        SearchResult input = resultWithPaths("/AgamAH/a.txt", "/purANa/b.txt");
        SearchResult output = mSearcher.filterResultsByPath(input, "");
        assertEquals(2, output.getResultDocs().size());
        assertSame(input, output);
    }

    // ─── UC-S3: matching prefix ───────────────────────────────────────────────

    @Test
    public void filter_matchingFolderPath_returnsOnlyMatchingDocs() {
        SearchResult input = resultWithPaths(
                "/AgamAH/prakAshasaMhitA.txt",
                "/AgamAH/hayashIrSha.txt",
                "/purANa/brahmaPurANa.txt");

        SearchResult output = mSearcher.filterResultsByPath(input, "/AgamAH/");

        assertEquals("Only AgamAH documents should be returned", 2, output.getResultDocs().size());
        for (ResultDocument rd : output.getResultDocs()) {
            String path = rd.getDoc().get(Constatants.FIELD_PATH);
            assertEquals("Path must start with /AgamAH/",
                    true, path.startsWith("/AgamAH/"));
        }
    }

    // ─── UC-S4: non-matching prefix → empty ──────────────────────────────────

    @Test
    public void filter_noMatchingDocs_returnsEmptyList() {
        SearchResult input = resultWithPaths("/AgamAH/a.txt", "/purANa/b.txt");
        SearchResult output = mSearcher.filterResultsByPath(input, "/vEda/");
        assertEquals("No documents match /vEda/ prefix", 0, output.getResultDocs().size());
    }

    // ─── UC-S5: partial-prefix false-positive rejected ────────────────────────
    //
    // "/A/" must NOT match "/AB/file.txt" even though "/AB/..." starts with "/A".
    // The trailing slash in the folder path prevents this ambiguity.

    @Test
    public void filter_partialPrefixNotMatched_noFalsePositive() {
        SearchResult input = resultWithPaths("/AgamAH/a.txt");
        // "/Ag/" is a prefix of "/AgamAH/a.txt" but it is NOT a valid folder boundary.
        // Our folder paths always end with "/" after the full segment name, so "/Ag/"
        // would never be produced — this test guards against regressions.
        SearchResult output = mSearcher.filterResultsByPath(input, "/Ag/");
        assertEquals("Partial segment name must not match a longer segment", 0, output.getResultDocs().size());
    }

    @Test
    public void filter_folderNameIsSubstringOfSiblingFolder_noFalsePositive() {
        // "/mAdhva/" must NOT match documents in "/mAdhvavedAnta/"
        SearchResult input = resultWithPaths(
                "/mAdhva/sarvamUla.txt",
                "/mAdhvavedAnta/tAtparyacandrikA.txt");

        SearchResult output = mSearcher.filterResultsByPath(input, "/mAdhva/");

        assertEquals("Only /mAdhva/ docs should match, not /mAdhvavedAnta/",
                1, output.getResultDocs().size());
        assertEquals("/mAdhva/sarvamUla.txt",
                output.getResultDocs().get(0).getDoc().get(Constatants.FIELD_PATH));
    }

    // ─── UC-S6: nested subfolder is included ─────────────────────────────────

    @Test
    public void filter_topLevelFolderPath_includesNestedSubfolderDocs() {
        SearchResult input = resultWithPaths(
                "/mAdhva/sarvamUla/file.txt",
                "/mAdhva/pramANapaddhati.txt",
                "/purANa/brahmaPurANa.txt");

        SearchResult output = mSearcher.filterResultsByPath(input, "/mAdhva/");

        assertEquals("Both direct and nested mAdhva docs must be included",
                2, output.getResultDocs().size());
    }

    @Test
    public void filter_nestedFolderPath_excludesParentFolderDocs() {
        // Scoping to "/mAdhva/sarvamUla/" must NOT include "/mAdhva/pramANapaddhati.txt"
        SearchResult input = resultWithPaths(
                "/mAdhva/sarvamUla/file.txt",
                "/mAdhva/pramANapaddhati.txt");

        SearchResult output = mSearcher.filterResultsByPath(input, "/mAdhva/sarvamUla/");

        assertEquals("Only the nested subfolder's doc must be returned", 1, output.getResultDocs().size());
        assertEquals("/mAdhva/sarvamUla/file.txt",
                output.getResultDocs().get(0).getDoc().get(Constatants.FIELD_PATH));
    }

    // ─── UC-S7: multi-folder, only selected folder returned ──────────────────

    @Test
    public void filter_multipleTopLevelFolders_onlySelectedFolderReturned() {
        SearchResult input = resultWithPaths(
                "/AgamAH/a.txt",
                "/mAdhva/b.txt",
                "/purANa/c.txt",
                "/vEda/d.txt");

        SearchResult output = mSearcher.filterResultsByPath(input, "/purANa/");

        assertEquals(1, output.getResultDocs().size());
        assertEquals("/purANa/c.txt",
                output.getResultDocs().get(0).getDoc().get(Constatants.FIELD_PATH));
    }

    // ─── UC-S8: leading-slash paths (real device format) ─────────────────────

    @Test
    public void filter_leadingSlashPaths_matchesFolderPathWithLeadingSlash() {
        // Paths on device are stored in Lucene with a leading slash.
        // folderPath computed by TOCTreeBuilder also starts with "/".
        SearchResult input = resultWithPaths(
                "/AgamAH/prakAshasaMhitA.txt",
                "/purANa/nIlamata.txt");

        SearchResult scoped = mSearcher.filterResultsByPath(input, "/AgamAH/");
        assertEquals(1, scoped.getResultDocs().size());
        assertEquals("/AgamAH/prakAshasaMhitA.txt",
                scoped.getResultDocs().get(0).getDoc().get(Constatants.FIELD_PATH));

        SearchResult global = mSearcher.filterResultsByPath(input, null);
        assertEquals(2, global.getResultDocs().size());
    }

    // ─── UC-S9: empty input results ──────────────────────────────────────────

    @Test
    public void filter_emptyResultSet_returnsEmptyList() {
        SearchResult input = resultWithPaths(); // no documents
        SearchResult output = mSearcher.filterResultsByPath(input, "/AgamAH/");
        assertEquals(0, output.getResultDocs().size());
    }

}
