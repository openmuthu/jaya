package org.jaya.search;

import org.jaya.scriptconverter.ScriptType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link SearchResult}.
 */
public class SearchResultTest {

    private SearchResult makeResult(String query) {
        JayaQueryParser parser = new JayaQueryParser(query);
        return new SearchResult(parser, new ArrayList<ResultDocument>());
    }

    // --- getDependentCharSequenceLength ---

    @Test
    public void sr01_dependentCharSequenceLength_zero_forAscii() {
        SearchResult sr = makeResult("rAma");
        assertEquals("sr01: ASCII has no dependent chars", 0,
                sr.getDependentCharSequenceLength("hello", 1));
    }

    @Test
    public void sr02_dependentCharSequenceLength_devanagariVirama() {
        SearchResult sr = makeResult("rAma");
        // \u0930\u093E = r + vowel sign AA (dependent char)
        // getDependentCharSequenceLength starting at index 1 (\u093E)
        String str = "\u0930\u093E\u092E"; // rAma in Devanagari (3 chars: r, matra-A, m)
        // Index 1 is the matra \u093E which is a dependent char
        int len = sr.getDependentCharSequenceLength(str, 1);
        assertTrue("sr02: matra is a dependent char, length >= 1", len >= 1);
    }

    @Test
    public void sr03_dependentCharSequenceLength_atEnd() {
        SearchResult sr = makeResult("r");
        // At the end of the string, should return 0
        assertEquals("sr03: beyond end returns 0", 0,
                sr.getDependentCharSequenceLength("abc", 3));
    }

    // --- getClosestMatch ---

    @Test
    public void sr04_closestMatch_exactMatch() {
        SearchResult sr = makeResult("rAma");
        SearchResult.Highlight h = sr.getClosestMatch("rAma hari", "rAma");
        assertEquals("sr04a: start index 0", 0, h.startIndex);
        assertEquals("sr04b: length 4", 4, h.length);
    }

    @Test
    public void sr05_closestMatch_notFound_returnsNegative() {
        SearchResult sr = makeResult("xyz");
        SearchResult.Highlight h = sr.getClosestMatch("rAma hari", "xyz");
        assertEquals("sr05: not found returns -1", -1, h.startIndex);
    }

    @Test
    public void sr06_closestMatch_partialMatch() {
        SearchResult sr = makeResult("rAm");
        // "rAmaz" not found but "rAma" partially contains "rAm"
        SearchResult.Highlight h = sr.getClosestMatch("hari rAma", "rAmaz");
        assertTrue("sr06: partial match has positive startIndex", h.startIndex >= 0);
        assertTrue("sr06b: partial match length >= 1", h.length >= 1);
    }

    @Test
    public void sr07_closestMatch_emptyWord() {
        SearchResult sr = makeResult("r");
        SearchResult.Highlight h = sr.getClosestMatch("anything", "");
        assertEquals("sr07: empty word gives length 0", 0, h.length);
    }

    // --- getSpannedString ---

    @Test
    public void sr08_getSpannedString_noHighlights() {
        SearchResult sr = makeResult("r");
        List<SearchResult.Highlight> highlights = new ArrayList<>();
        String result = sr.getSpannedString("plain text", highlights);
        assertEquals("sr08: no highlights returns plain text", "plain text", result);
    }

    @Test
    public void sr09_getSpannedString_withHighlight() {
        SearchResult sr = makeResult("r");
        List<SearchResult.Highlight> highlights = new ArrayList<>();
        highlights.add(sr.new Highlight(5, 4)); // "hari" in "rAma hari"
        String result = sr.getSpannedString("rAma hari", highlights);
        assertTrue("sr09a: result contains bold/span tags", result.contains("<b>"));
        assertTrue("sr09b: result contains span style", result.contains("span"));
        assertTrue("sr09c: highlighted word present", result.contains("hari"));
    }

    // --- getSpannedStringBasedOnCurrentQuery ---

    @Test
    public void sr10_getSpannedStringBasedOnCurrentQuery_basicMatch() {
        SearchResult sr = makeResult("rAma");
        String result = sr.getSpannedStringBasedOnCurrentQuery("rAma hari", ScriptType.ITRANS);
        assertNotNull("sr10: result not null", result);
        // Should contain the span for "rAma"
        assertTrue("sr10b: rAma highlighted", result.contains("rAma"));
    }

    @Test
    public void sr11_getSpannedStringBasedOnCurrentQuery_noMatch() {
        SearchResult sr = makeResult("xyz");
        String result = sr.getSpannedStringBasedOnCurrentQuery("rAma hari", ScriptType.ITRANS);
        // No match; result should be unchanged text (no highlight tags, or all text)
        assertNotNull("sr11: result not null", result);
        // Since "xyz" is not in the string, no span should wrap any real content
        assertFalse("sr11b: no span when no match", result.contains("#FF00FF"));
    }

    @Test
    public void sr12_getQueryParser() {
        JayaQueryParser parser = new JayaQueryParser("hari");
        SearchResult sr = new SearchResult(parser, new ArrayList<ResultDocument>());
        assertSame("sr12: returns same parser", parser, sr.getQueryParser());
    }

    @Test
    public void sr13_getResultDocs() {
        SearchResult sr = makeResult("r");
        assertNotNull("sr13: getResultDocs not null", sr.getResultDocs());
    }
}
