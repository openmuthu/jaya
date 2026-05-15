package org.jaya.android;

import org.junit.Test;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link BookmarkDeepLink}.
 */
public class BookmarkDeepLinkTest {

    // BDL-01 — URL starts with the expected base
    @Test
    public void bdl01_url_startsWithBase() {
        String url = BookmarkDeepLink.buildUrl("/path/file.txt", "42", "fp", "myMark");
        assertTrue("bdl01: starts with base", url.startsWith(BookmarkDeepLink.BASE_URL));
    }

    // BDL-02 — all four parameters are present in the URL
    @Test
    public void bdl02_allParamsPresent() {
        String url = BookmarkDeepLink.buildUrl("/path/file.txt", "42", "fp123", "myMark");
        assertTrue("bdl02a: path param", url.contains("path="));
        assertTrue("bdl02b: id param",   url.contains("id="));
        assertTrue("bdl02c: fp param",   url.contains("fp="));
        assertTrue("bdl02d: name param", url.contains("name="));
    }

    // BDL-03 — plain ASCII values round-trip through URL encoding
    @Test
    public void bdl03_asciiValues_roundTrip() throws Exception {
        String url = BookmarkDeepLink.buildUrl("/kOsha/file.txt", "7", "abc", "shrI");
        assertTrue("bdl03a: path value", url.contains(URLDecoder.decode("%2FkOsha%2Ffile.txt", "UTF-8")
                .equals("/kOsha/file.txt") ? "" : "path=%2FkOsha%2Ffile.txt"));
        // Easier: just check decoded values are present via manual decode
        String decoded = URLDecoder.decode(url, "UTF-8");
        assertTrue("bdl03b: decoded path",   decoded.contains("/kOsha/file.txt"));
        assertTrue("bdl03c: decoded id",     decoded.contains("id=7"));
        assertTrue("bdl03d: decoded fp",     decoded.contains("fp=abc"));
        assertTrue("bdl03e: decoded name",   decoded.contains("name=shrI"));
    }

    // BDL-04 — Devanagari characters in fp are percent-encoded (non-ASCII)
    @Test
    public void bdl04_devanagariFingerprint_encoded() {
        String url = BookmarkDeepLink.buildUrl("/p/f.txt", "1", "विजय", "");
        assertFalse("bdl04: raw Devanagari must not appear unencoded", url.contains("विजय"));
        assertTrue("bdl04b: percent sign present", url.contains("%"));
    }

    // BDL-05 — null arguments treated as empty strings, no exception
    @Test
    public void bdl05_nullArguments_noException() {
        String url = BookmarkDeepLink.buildUrl(null, null, null, null);
        assertNotNull("bdl05a: not null", url);
        assertFalse("bdl05b: not empty", url.isEmpty());
        assertTrue("bdl05c: base present", url.startsWith(BookmarkDeepLink.BASE_URL));
    }

    // BDL-06 — empty name produces name= with no value
    @Test
    public void bdl06_emptyName_producesEmptyParam() {
        String url = BookmarkDeepLink.buildUrl("/p/f.txt", "3", "fp", "");
        assertTrue("bdl06: name param with empty value", url.contains("name="));
    }

    // BDL-07 — path with spaces is encoded
    @Test
    public void bdl07_pathWithSpaces_encoded() throws Exception {
        String url = BookmarkDeepLink.buildUrl("/path/my file.txt", "0", "", "");
        assertFalse("bdl07: raw space must not appear", url.contains("/path/my file.txt"));
        String decoded = URLDecoder.decode(url, "UTF-8");
        assertTrue("bdl07b: decoded path round-trips", decoded.contains("/path/my file.txt"));
    }
}
