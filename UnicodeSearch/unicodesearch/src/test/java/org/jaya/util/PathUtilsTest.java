package org.jaya.util;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link PathUtils}.
 */
public class PathUtilsTest {

    // pp01 — single component appended with separator
    @Test
    public void pp01_singleComponent() {
        String result = PathUtils.get("/base", "file.txt");
        assertEquals("pp01: single component", "/base" + File.separator + "file.txt", result);
    }

    // pp02 — multiple components chained
    @Test
    public void pp02_multipleComponents() {
        String result = PathUtils.get("/root", "a", "b", "c.txt");
        String expected = "/root" + File.separator + "a" + File.separator + "b" + File.separator + "c.txt";
        assertEquals("pp02: three components must be chained", expected, result);
    }

    // pp03 — no components returns prefix unchanged
    @Test
    public void pp03_noComponents() {
        String result = PathUtils.get("/base");
        assertEquals("pp03: no components must return prefix unchanged", "/base", result);
    }

    // pp04 — prefix that already ends with separator does not double-up
    @Test
    public void pp04_prefixWithTrailingSeparator() {
        String prefix = "/base" + File.separator;
        String result = PathUtils.get(prefix, "file.txt");
        assertFalse("pp04: no double separator expected", result.contains(File.separator + File.separator));
        assertTrue("pp04b: path must end with file.txt", result.endsWith("file.txt"));
    }
}
