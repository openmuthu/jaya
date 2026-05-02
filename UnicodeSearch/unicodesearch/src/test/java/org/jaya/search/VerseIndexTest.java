package org.jaya.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.junit.Test;
import org.jaya.util.Constatants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link VerseIndex}.
 *
 * No Lucene index on disk is required — documents are built in-memory using
 * {@link VerseIndex#buildFromDocs(List)}.
 *
 * Scenarios covered
 * -----------------
 * UC-V1  No verse numbers         → hasVerses() false
 * UC-V2  Bare dot separator       → 1.2 detected
 * UC-V3  Bare hyphen separator    → 1-2 normalised to key "1.2"
 * UC-V4  Three-part key           → 1.2.3 detected, depth == 3
 * UC-V5  Mixed separators         → 1.2, 1-2, 1.2.3 all detected
 * UC-V6  Danda-wrapped            → ॥1.2॥ detected
 * UC-V7  Danda beats bare         → danda occurrence wins over later bare match
 * UC-V8  First occurrence wins    → same key in two chunks maps to first chunk
 * UC-V9  Chapter grouping         → getChapters() and getVersesForChapter() correct
 * UC-V10 getDocId() unknown key   → -1 returned
 * UC-V11 Normalise in getDocId()  → "1-2" and "1.2" resolve to same entry
 * UC-V12 Depth detection          → 2-part → depth 2, 3-part → depth 3
 * UC-V13 No false positive        → "12" (no separator) not matched
 * UC-V14 Surrounding digits guard → "12.3" not prefix-matched by "2.3"
 * UC-V15 Empty doc list           → hasVerses() false
 */
public class VerseIndexTest {

    // ── helpers ──────────────────────────────────────────────────────────────

    /** Build a ResultDocument whose FIELD_CONTENTS equals {@code contents}. */
    private ResultDocument docWithContents(int docId, String contents) {
        Document luceneDoc = new Document();
        luceneDoc.add(new StringField(Constatants.FIELD_PATH, "/test/file.txt", Field.Store.YES));
        luceneDoc.add(new StringField(Constatants.FIELD_DOC_LOCAL_ID,
                String.valueOf(docId), Field.Store.YES));
        luceneDoc.add(new TextField(Constatants.FIELD_CONTENTS, contents, Field.Store.YES));
        return new ResultDocument(docId, luceneDoc);
    }

    private VerseIndex buildFrom(ResultDocument... docs) {
        return VerseIndex.buildFromDocs(Arrays.asList(docs));
    }

    // ── UC-V1 ────────────────────────────────────────────────────────────────

    @Test
    public void noVerseNumbers_hasVersesFalse() {
        VerseIndex idx = buildFrom(docWithContents(0, "श्रीमद्भगवद्गीता"));
        assertFalse("No verse numbers → hasVerses must be false", idx.hasVerses());
    }

    // ── UC-V2 ────────────────────────────────────────────────────────────────

    @Test
    public void bareDotSeparator_detected() {
        VerseIndex idx = buildFrom(docWithContents(10, "some text 1.2 more text"));
        assertTrue(idx.hasVerses());
        assertEquals(10, idx.getDocId("1.2"));
    }

    // ── UC-V3 ────────────────────────────────────────────────────────────────

    @Test
    public void bareHyphenSeparator_normalisedToDot() {
        VerseIndex idx = buildFrom(docWithContents(20, "verse 1-2 here"));
        assertTrue(idx.hasVerses());
        assertEquals("1-2 must normalise to key '1.2'", 20, idx.getDocId("1.2"));
        assertEquals("Original hyphen form must also resolve", 20, idx.getDocId("1-2"));
    }

    // ── UC-V4 ────────────────────────────────────────────────────────────────

    @Test
    public void threePartKey_detectedAndDepthThree() {
        VerseIndex idx = buildFrom(docWithContents(30, "text 1.2.3 end"));
        assertTrue(idx.hasVerses());
        assertEquals(30, idx.getDocId("1.2.3"));
        assertEquals("Three-part verse → depth must be 3", 3, idx.getDepth());
    }

    // ── UC-V5 ────────────────────────────────────────────────────────────────

    @Test
    public void mixedSeparators_allDetected() {
        VerseIndex idx = buildFrom(
                docWithContents(1, "1.2 first"),
                docWithContents(2, "1-3 second"),
                docWithContents(3, "2.1.4 third"));
        assertEquals(1, idx.getDocId("1.2"));
        assertEquals(2, idx.getDocId("1.3"));   // 1-3 → key 1.3
        assertEquals(3, idx.getDocId("2.1.4"));
    }

    // ── UC-V6 ────────────────────────────────────────────────────────────────

    @Test
    public void dandaWrapped_detected() {
        // U+0965 = ॥
        VerseIndex idx = buildFrom(docWithContents(40, "text \u09651.2\u0965 more"));
        assertTrue(idx.hasVerses());
        assertEquals(40, idx.getDocId("1.2"));
    }

    // ── UC-V7 ────────────────────────────────────────────────────────────────

    @Test
    public void danda_winsOverLaterBareMatch() {
        // Chunk 5 has danda-wrapped 1.2; chunk 6 has bare 1.2 — chunk 5 must win
        VerseIndex idx = buildFrom(
                docWithContents(5, "line \u09651.2\u0965"),
                docWithContents(6, "also 1.2 here"));
        assertEquals("Danda match in chunk 5 must win", 5, idx.getDocId("1.2"));
    }

    // ── UC-V8 ────────────────────────────────────────────────────────────────

    @Test
    public void firstOccurrenceWins_sameKeyInTwoChunks() {
        VerseIndex idx = buildFrom(
                docWithContents(100, "text 3.4 here"),
                docWithContents(200, "again 3.4 here"));
        assertEquals("First chunk must own the verse key", 100, idx.getDocId("3.4"));
    }

    // ── UC-V9 ────────────────────────────────────────────────────────────────

    @Test
    public void chapterGrouping_correctChaptersAndVerses() {
        VerseIndex idx = buildFrom(
                docWithContents(1, "1.1 text"),
                docWithContents(2, "1.2 text"),
                docWithContents(3, "2.1 text"));

        List<String> chapters = idx.getChapters();
        assertEquals(2, chapters.size());
        assertEquals("1", chapters.get(0));
        assertEquals("2", chapters.get(1));

        List<String> ch1Verses = idx.getVersesForChapter("1");
        assertEquals(2, ch1Verses.size());
        assertTrue(ch1Verses.contains("1.1"));
        assertTrue(ch1Verses.contains("1.2"));

        List<String> ch2Verses = idx.getVersesForChapter("2");
        assertEquals(1, ch2Verses.size());
        assertEquals("2.1", ch2Verses.get(0));
    }

    // ── UC-V10 ───────────────────────────────────────────────────────────────

    @Test
    public void getDocId_unknownKey_returnsMinusOne() {
        VerseIndex idx = buildFrom(docWithContents(0, "1.2 text"));
        assertEquals(-1, idx.getDocId("9.9"));
    }

    // ── UC-V11 ───────────────────────────────────────────────────────────────

    @Test
    public void getDocId_hyphenAndDotResolveSameEntry() {
        VerseIndex idx = buildFrom(docWithContents(77, "text 4.5 end"));
        assertEquals(77, idx.getDocId("4.5"));
        assertEquals("Hyphen form must resolve same entry", 77, idx.getDocId("4-5"));
    }

    // ── UC-V12 ───────────────────────────────────────────────────────────────

    @Test
    public void depthDetection_twoPart_depthTwo() {
        VerseIndex idx = buildFrom(docWithContents(0, "1.2 text"));
        assertEquals(2, idx.getDepth());
    }

    @Test
    public void depthDetection_threePart_depthThree() {
        VerseIndex idx = buildFrom(
                docWithContents(0, "1.2 text"),
                docWithContents(1, "1.2.3 text"));
        assertEquals(3, idx.getDepth());
    }

    // ── UC-V13 ───────────────────────────────────────────────────────────────

    @Test
    public void plainNumber_noFalsePositive() {
        // "12" alone (no separator) must not be treated as a verse number
        VerseIndex idx = buildFrom(docWithContents(0, "chapter 12 is long"));
        assertFalse("Plain integer without separator must not match", idx.hasVerses());
    }

    // ── UC-V14 ───────────────────────────────────────────────────────────────

    @Test
    public void surroundingDigitGuard_noFalsePositive() {
        // "12.3" must not produce a match for "2.3" (surrounded by leading digit)
        VerseIndex idx = buildFrom(docWithContents(0, "value 12.3 end"));
        assertEquals("2.3 must not match inside 12.3", -1, idx.getDocId("2.3"));
        // 12.3 itself should match
        assertEquals(0, idx.getDocId("12.3"));
    }

    // ── UC-V15 ───────────────────────────────────────────────────────────────

    @Test
    public void emptyDocList_hasVersesFalse() {
        VerseIndex idx = VerseIndex.buildFromDocs(new ArrayList<ResultDocument>());
        assertFalse(idx.hasVerses());
        assertTrue(idx.getChapters().isEmpty());
    }

    // ── normalise helper ──────────────────────────────────────────────────────

    @Test
    public void normalise_replacesHyphenWithDot() {
        assertEquals("1.2.3", VerseIndex.normalise("1-2.3"));
        assertEquals("1.2.3", VerseIndex.normalise("1.2-3"));
        assertEquals("1.2.3", VerseIndex.normalise("1-2-3"));
        assertEquals("1.2",   VerseIndex.normalise("1.2"));
    }

    // ── makePreview ───────────────────────────────────────────────────────────

    /** UC-P1: strips spaces and punctuation, keeps letters and digits (after leading digit skip). */
    @Test
    public void makePreview_stripsWhitespaceAndSpecialChars() {
        // "Hello, World! 123" → strip specials → "HelloWorld123" → no leading digits before letters
        // leading digits check: 'H' is not a digit so no skip
        String result = VerseIndex.makePreview("  Hello, World! 123  ");
        assertEquals("HelloWorld123", result);
    }

    /** UC-P1b: leading ASCII digit run (chunk artefact) is skipped before the text. */
    @Test
    public void makePreview_skipsLeadingDigits() {
        // "17615 प्रिय" → strip spaces → "17615प्रिय" → skip "17615" → "प्रिय"
        String result = VerseIndex.makePreview("17615 प्रिय");
        assertEquals("प्रिय", result);
    }

    /** UC-P1c: if content is all digits (no letters), result is empty. */
    @Test
    public void makePreview_allDigits_returnsEmpty() {
        assertEquals("", VerseIndex.makePreview("12345"));
    }

    /** UC-P2: truncates to PREVIEW_LENGTH characters after stripping and digit-skip. */
    @Test
    public void makePreview_truncatesToPreviewLength() {
        // 40 letters → should truncate to 30
        String input = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKL";
        String result = VerseIndex.makePreview(input);
        assertEquals(VerseIndex.PREVIEW_LENGTH, result.length());
        assertEquals("abcdefghijklmnopqrstuvwxyzABCD", result);
    }

    /** UC-P3: short content shorter than PREVIEW_LENGTH is returned in full (after digit skip). */
    @Test
    public void makePreview_shortContent_returnedFull() {
        // no leading digits, so returned as-is
        String result = VerseIndex.makePreview("abc123");
        assertEquals("abc123", result);
    }

    /** UC-P4: Devanagari letters are kept (they are Unicode letters). */
    @Test
    public void makePreview_devanagariLettersKept() {
        String result = VerseIndex.makePreview("नारायण");
        assertEquals("नारायण", result);
    }

    /** UC-P5: empty string produces empty preview. */
    @Test
    public void makePreview_emptyInput_emptyOutput() {
        assertEquals("", VerseIndex.makePreview(""));
    }

    // ── getPreview ────────────────────────────────────────────────────────────

    /** UC-P6: getPreview returns preview stored for that verse key. */
    @Test
    public void getPreview_returnsPreviewForKnownKey() {
        // Content has lots of letters so the preview is non-empty
        VerseIndex idx = buildFrom(docWithContents(10,
                "नारायणाय नमः 1.2 इति"));
        String preview = idx.getPreview("1.2");
        assertFalse("Preview must be non-empty for a known key", preview.isEmpty());
        // Content stripped of specials; check it starts with the Devanagari text
        assertTrue("Preview must contain content chars",
                preview.startsWith("नारायणाय"));
    }

    /** UC-P7: getPreview returns empty string for an unknown key. */
    @Test
    public void getPreview_unknownKey_returnsEmpty() {
        VerseIndex idx = buildFrom(docWithContents(10, "text 1.2 here"));
        assertEquals("", idx.getPreview("9.9"));
    }

    /** UC-P8: getPreview normalises hyphen separator in the query key. */
    @Test
    public void getPreview_hyphenKeyNormalised() {
        VerseIndex idx = buildFrom(docWithContents(20, "some content 3.4 end"));
        String byDot    = idx.getPreview("3.4");
        String byHyphen = idx.getPreview("3-4");
        assertEquals("Hyphen and dot forms must return same preview", byDot, byHyphen);
        assertFalse(byDot.isEmpty());
    }

    /** UC-P9: danda-wrapped verse stores correct preview. */
    @Test
    public void getPreview_dandaVerse_storesPreview() {
        VerseIndex idx = buildFrom(docWithContents(5,
                "सोऽयम् \u09651.1\u0965 पदम्"));
        String preview = idx.getPreview("1.1");
        assertFalse("Danda verse must also have a preview", preview.isEmpty());
    }

    /** UC-P10: first-occurrence-wins applies to preview too. */
    @Test
    public void getPreview_firstOccurrenceWinsForPreview() {
        VerseIndex idx = buildFrom(
                docWithContents(1, "alpha 2.3 text"),
                docWithContents(2, "beta 2.3 text"));
        // Preview must come from the first chunk (chunk 1 → starts with "alpha")
        String preview = idx.getPreview("2.3");
        assertTrue("Preview must be from first chunk", preview.startsWith("alpha"));
    }
}
