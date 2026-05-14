package org.jaya.util;

import org.json.simple.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link StringUtils}.
 */
public class StringUtilsTest {

    // su01 — join with comma delimiter
    @Test
    public void su01_join_multipleElements() {
        String result = StringUtils.join(", ", new String[]{"a", "b", "c"});
        assertEquals("su01: joined string must be 'a, b, c'", "a, b, c", result);
    }

    // su02 — join with single element: no delimiter appended
    @Test
    public void su02_join_singleElement() {
        String result = StringUtils.join("-", new String[]{"only"});
        assertEquals("su02: single element must have no delimiter", "only", result);
    }

    // su03 — join with empty array returns empty string
    @Test
    public void su03_join_emptyArray() {
        String result = StringUtils.join(",", new String[]{});
        assertEquals("su03: empty array must produce empty string", "", result);
    }

    // su04 — join preserves empty tokens
    @Test
    public void su04_join_emptyTokensPreserved() {
        String result = StringUtils.join("|", new String[]{"a", "", "b"});
        assertEquals("su04: empty token must be preserved", "a||b", result);
    }

    // su05 — prettyJSONString returns a non-empty string for a simple JSON object
    @Test
    public void su05_prettyJSONString_simpleObject() {
        JSONObject obj = new JSONObject();
        obj.put("key", "value");
        String result = StringUtils.prettyJSONString(obj);
        assertNotNull("su05a: result must not be null", result);
        assertFalse("su05b: result must not be empty", result.isEmpty());
        assertTrue("su05c: result must contain the key", result.contains("key"));
    }

    // su06 — prettyJSONString on empty object returns braces
    @Test
    public void su06_prettyJSONString_emptyObject() {
        JSONObject obj = new JSONObject();
        String result = StringUtils.prettyJSONString(obj);
        assertNotNull("su06a: result must not be null", result);
        assertTrue("su06b: empty object must contain '{'", result.contains("{"));
    }
}
