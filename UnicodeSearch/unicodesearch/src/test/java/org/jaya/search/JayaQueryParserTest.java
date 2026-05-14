package org.jaya.search;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link JayaQueryParser}.
 */
public class JayaQueryParserTest {

    @Before
    public void setUp() {
        // Ensure accurate substring search is off by default
        JayaQueryParser.setAccurateSubstringSearchEnabled(false);
    }

    @After
    public void tearDown() {
        JayaQueryParser.setAccurateSubstringSearchEnabled(false);
    }

    // qp01 — plain query: parsed query has wildcard suffix
    @Test
    public void qp01_plainQuery_hasWildcardSuffix() {
        JayaQueryParser p = new JayaQueryParser("hari");
        assertTrue("qp01: parsed query must contain wildcard", p.getParsedQuery().contains("*"));
        assertFalse("qp01b: plain query is not fuzzy", p.isFuzzyQuery());
        assertFalse("qp01c: plain query is not lucene parser query", p.isLuceneQueryParserQuery());
    }

    // qp02 — f/ prefix → fuzzy query
    @Test
    public void qp02_fuzzyPrefix() {
        JayaQueryParser p = new JayaQueryParser("f/hari");
        assertTrue("qp02: must be fuzzy query", p.isFuzzyQuery());
        assertTrue("qp02b: fuzzy parsed query has ~ suffix", p.getParsedQuery().contains("~"));
        assertEquals("qp02c: original search string stripped", "hari", p.getOriginalSearchString());
    }

    // qp03 — q/ prefix → lucene query parser query
    @Test
    public void qp03_luceneParserPrefix() {
        JayaQueryParser p = new JayaQueryParser("q/contents:rAma");
        assertTrue("qp03: must be lucene parser query", p.isLuceneQueryParserQuery());
        assertEquals("qp03b: parsed query is the raw string", "contents:rAma", p.getParsedQuery());
    }

    // qp04 — ~/ prefix → regex prefix query
    @Test
    public void qp04_regexPrefixSlash() {
        JayaQueryParser p = new JayaQueryParser("~/rAma");
        assertTrue("qp04: must be regex prefix query", p.isRegExPrefixQuery());
    }

    // qp05 — tags extracted from comma syntax
    @Test
    public void qp05_tagsExtractedFromComma() {
        JayaQueryParser p = new JayaQueryParser("hari ,amara,kOsha");
        List<String> tags = p.getSearchTags();
        assertEquals("qp05a: two tags expected", 2, tags.size());
        assertTrue("qp05b: must contain 'amara'", tags.contains("amara"));
        assertTrue("qp05c: must contain 'kOsha'", tags.contains("kOsha"));
    }

    // qp06 — tags extracted from # syntax
    @Test
    public void qp06_tagsFromHash() {
        JayaQueryParser p = new JayaQueryParser("#purANa");
        List<String> tags = p.getSearchTags();
        assertEquals("qp06: one tag extracted", 1, tags.size());
        assertEquals("qp06b: tag is 'purANa'", "purANa", tags.get(0));
    }

    // qp07 — words extracted correctly
    @Test
    public void qp07_wordsExtracted() {
        JayaQueryParser p = new JayaQueryParser("hari krish");
        List<String> words = p.getSearchWords();
        assertEquals("qp07: two words expected", 2, words.size());
        assertTrue("qp07b: 'hari' in words", words.contains("hari"));
        assertTrue("qp07c: 'krish' in words", words.contains("krish"));
    }

    // qp08 — words and tags together
    @Test
    public void qp08_wordsAndTags() {
        JayaQueryParser p = new JayaQueryParser("hari ,purANa");
        assertEquals("qp08a: one word", 1, p.getSearchWords().size());
        assertEquals("qp08b: one tag", 1, p.getSearchTags().size());
        // parsed query wraps words in parens followed by tag clause
        String pq = p.getParsedQuery();
        assertTrue("qp08c: parsed query must reference tags field", pq.contains("tags:"));
    }

    // qp09 — tag-only query appends docLocalId:0 filter
    @Test
    public void qp09_tagOnly_appendsDocLocalIdFilter() {
        JayaQueryParser p = new JayaQueryParser(",purANa");
        String pq = p.getParsedQuery();
        assertTrue("qp09: tag-only must filter by docLocalId:0", pq.contains("docLocalId:0"));
    }

    // qp10 — accurate substring search mode wraps as regex prefix query
    @Test
    public void qp10_accurateSubstringSearch_isRegExPrefix() {
        JayaQueryParser.setAccurateSubstringSearchEnabled(true);
        JayaQueryParser p = new JayaQueryParser("rAma");
        assertTrue("qp10: accurate search must be regex prefix query", p.isRegExPrefixQuery());
    }

    // qp11 — word already ending with wildcard is not doubled
    @Test
    public void qp11_wordAlreadyHasWildcard_notDoubled() {
        JayaQueryParser p = new JayaQueryParser("rAma*");
        String pq = p.getParsedQuery();
        assertFalse("qp11: rAma* must not become rAma**", pq.contains("rAma**"));
    }
}
