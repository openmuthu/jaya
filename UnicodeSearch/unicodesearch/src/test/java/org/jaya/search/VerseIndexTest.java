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
import static org.junit.Assert.assertNull;
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

    /** UC-P6: getPreview returns preview from text AFTER the verse marker. */
    @Test
    public void getPreview_returnsPreviewForKnownKey() {
        // "1.2" appears mid-document; preview should come from text after the marker
        VerseIndex idx = buildFrom(docWithContents(10,
                "नारायणाय नमः 1.2 इति"));
        String preview = idx.getPreview("1.2");
        assertFalse("Preview must be non-empty for a known key", preview.isEmpty());
        // Preview is built from " इति" (the text after "1.2"), not from the start
        assertTrue("Preview must contain text that follows the verse marker",
                preview.startsWith("इति"));
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
                docWithContents(1, "alpha 2.3 firstchunk"),
                docWithContents(2, "beta 2.3 secondchunk"));
        // Preview is from text after "2.3" in the first chunk → "firstchunk"
        String preview = idx.getPreview("2.3");
        assertTrue("Preview must be from first chunk", preview.startsWith("firstchunk"));
    }

    /**
     * UC-P11: the core bug fix — multiple verses in the same Lucene document
     * must each get a distinct preview derived from text after their own marker.
     */
    @Test
    public void getPreview_multipleVersesInSameDoc_distinctPreviews() {
        // Single document containing two danda-wrapped verses
        VerseIndex idx = buildFrom(docWithContents(1,
                "preamble \u09651.1\u0965 firstverse \u09651.2\u0965 secondverse"));
        String preview1 = idx.getPreview("1.1");
        String preview2 = idx.getPreview("1.2");
        assertFalse("1.1 preview must not be empty", preview1.isEmpty());
        assertFalse("1.2 preview must not be empty", preview2.isEmpty());
        assertFalse("Verses in same doc must have distinct previews",
                preview1.equals(preview2));
        assertTrue("1.1 preview from text after ॥1.1॥", preview1.startsWith("firstverse"));
        assertTrue("1.2 preview from text after ॥1.2॥", preview2.startsWith("secondverse"));
    }

    /** UC-P12: bare verse markers in the same document also get distinct previews. */
    @Test
    public void getPreview_multipleBarVersesInSameDoc_distinctPreviews() {
        VerseIndex idx = buildFrom(docWithContents(5,
                "2.1 alphacontent 2.2 betacontent"));
        String p1 = idx.getPreview("2.1");
        String p2 = idx.getPreview("2.2");
        assertFalse("2.1 and 2.2 must have distinct previews", p1.equals(p2));
        assertTrue("2.1 preview from text after '2.1'", p1.startsWith("alphacontent"));
        assertTrue("2.2 preview from text after '2.2'", p2.startsWith("betacontent"));
    }

    /**
     * UC-P13: end-of-verse style (Bhāgavata Purāṇa) — verse marker at the END
     * of a verse (e.g. {@code verse text ॥3.1.1॥}) must still produce a non-empty
     * preview using the text BEFORE the marker as a fallback.
     */
    @Test
    public void getPreview_endOfVerseMarker_fallsBackToTextBefore() {
        // Danda marker at end of verse — nothing follows it
        VerseIndex idx = buildFrom(docWithContents(10,
                "श्रीशुक उवाच \u09653.1.1\u0965"));
        String preview = idx.getPreview("3.1.1");
        assertFalse("End-of-verse marker must still produce non-empty preview", preview.isEmpty());
    }

    /**
     * UC-P14: multiple end-of-verse markers in the same document each fall back
     * to the text before their own marker — so previews are distinct.
     */
    @Test
    public void getPreview_multipleEndOfVerseMarkers_distinctPreviews() {
        VerseIndex idx = buildFrom(docWithContents(20,
                "verseone \u09653.1.1\u0965 versetwo \u09653.1.2\u0965"));
        String p1 = idx.getPreview("3.1.1");
        String p2 = idx.getPreview("3.1.2");
        assertFalse("3.1.1 and 3.1.2 must have distinct previews", p1.equals(p2));
        // ॥3.1.1॥ has "versetwo ॥3.1.2॥" after it — non-empty, so uses after text
        // ॥3.1.2॥ has nothing after it — falls back to text before
        assertFalse("3.1.2 preview must be non-empty", p2.isEmpty());
    }

    // ── normaliseDigits ───────────────────────────────────────────────────────

    /** UC-D1: Devanagari digits are converted to ASCII. */
    @Test
    public void normaliseDigits_devanagari() {
        // ०१२३४५६७८९ → 0123456789
        assertEquals("0123456789", VerseIndex.normaliseDigits("\u0966\u0967\u0968\u0969\u096A\u096B\u096C\u096D\u096E\u096F"));
    }

    /** UC-D2: Kannada digits are converted to ASCII. */
    @Test
    public void normaliseDigits_kannada() {
        // ೦೧೨ → 012
        assertEquals("012", VerseIndex.normaliseDigits("\uCE66\uCE67\uCE68"));
    }

    /** UC-D3: ASCII characters pass through unchanged. */
    @Test
    public void normaliseDigits_asciiUnchanged() {
        assertEquals("abc123", VerseIndex.normaliseDigits("abc123"));
    }

    /** UC-D4: null input returns null. */
    @Test
    public void normaliseDigits_nullInput() {
        assertNull(VerseIndex.normaliseDigits(null));
    }

    // ── Single-danda stotra-style verses (॥N॥ → key "1.N") ───────────────────

    /** UC-S1: ॥1॥ style verse is detected and stored as "1.1". */
    @Test
    public void singleDanda_detectedAsChapterOneVerse() {
        VerseIndex idx = buildFrom(docWithContents(10,
                "verse text \u09651\u0965 more text"));
        assertTrue("Single-danda stotra verse must be detected", idx.hasVerses());
        assertEquals("Key '1.1' must map to the doc", 10, idx.getDocId("1.1"));
    }

    /** UC-S2: multiple ॥N॥ verses appear under chapter "1". */
    @Test
    public void singleDanda_multipleVersesUnderChapterOne() {
        VerseIndex idx = buildFrom(
                docWithContents(1, "verse1 \u09651\u0965"),
                docWithContents(2, "verse2 \u09652\u0965"),
                docWithContents(3, "verse3 \u09653\u0965"));
        List<String> chapters = idx.getChapters();
        assertEquals("All stotra verses must be under one chapter", 1, chapters.size());
        assertEquals("1", chapters.get(0));
        List<String> verses = idx.getVersesForChapter("1");
        assertEquals(3, verses.size());
        assertTrue(verses.contains("1.1"));
        assertTrue(verses.contains("1.2"));
        assertTrue(verses.contains("1.3"));
    }

    /** UC-S3: ॥N॥ with optional spaces is detected. */
    @Test
    public void singleDanda_withSpaces_detected() {
        VerseIndex idx = buildFrom(docWithContents(5, "text \u0965 6 \u0965 after"));
        assertEquals("Spaced single-danda must detect '1.6'", 5, idx.getDocId("1.6"));
    }

    /** UC-S4: Kannada digit ॥ ೧ ॥ is detected via digit normalisation. */
    @Test
    public void singleDanda_kannadaDigit_detected() {
        // ॥ ೧ ॥ after digit normalisation becomes ॥ 1 ॥
        VerseIndex idx = buildFrom(docWithContents(7, "stotra \u0965 \uCE67 \u0965 end"));
        assertEquals("Kannada ೧ must normalise and match as '1.1'", 7, idx.getDocId("1.1"));
    }

    /** UC-S5: Devanagari danda verse ॥३.१.१॥ is detected after digit normalisation. */
    @Test
    public void dandaPattern_devanagariDigits_detected() {
        // ॥३.१.१॥ → after normalisation → ॥3.1.1॥
        VerseIndex idx = buildFrom(docWithContents(8,
                "text \u0965\u0969.\u0967.\u0967\u0965 after"));
        assertEquals("Devanagari ३.१.१ must normalise to key '3.1.1'", 8, idx.getDocId("3.1.1"));
    }
}
