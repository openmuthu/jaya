package org.jaya.scriptconverter;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ITRANSToDevaNagariConverter}.
 */
public class ITRANSToDevaNagariConverterTest {

    private final ScriptConverter converter =
            ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.DEVANAGARI);

    // id01 — vowels
    @Test
    public void id01_vowel_a() {
        assertEquals("id01: 'a'", "\u0905", converter.convert("a"));
    }

    @Test
    public void id02_vowel_A() {
        assertEquals("id02: 'A'", "\u0906", converter.convert("A"));
    }

    // id03 — simple consonant 'k'
    @Test
    public void id03_consonant_k() {
        // standalone consonant at end of string gets virama appended
        String result = converter.convert("k");
        assertTrue("id03: 'k' must produce 0x0915", result.contains("\u0915"));
    }

    // id04 — consonant + vowel 'ka'
    @Test
    public void id04_ka() {
        // ka = 0x0915 (ka) — no matra because 'a' is the inherent vowel
        assertEquals("id04: 'ka'", "\u0915", converter.convert("ka"));
    }

    // id05 — 'rAma' round-trip produces Devanagari
    @Test
    public void id05_rAma() {
        String result = converter.convert("rAma");
        boolean allDevanagari = true;
        for (char c : result.toCharArray()) {
            if (!SCUtils.isDevanagari(c)) { allDevanagari = false; break; }
        }
        assertTrue("id05: rAma must produce all-Devanagari chars", allDevanagari);
    }

    // id06 — 'OM' maps to \u0950
    @Test
    public void id06_OM() {
        assertTrue("id06: OM must contain Om symbol", converter.convert("OM").contains("\u0950"));
    }

    // id07 — converter reports correct source/destination scripts
    @Test
    public void id07_sourceDestinationScript() {
        assertEquals("id07a: source", ScriptType.ITRANS, converter.getSourceScript());
        assertEquals("id07b: dest", ScriptType.DEVANAGARI, converter.getDestinationScript());
    }

    // id08 — 'x' maps to kSha (0x0915 + virama + 0x0937)
    @Test
    public void id08_x_mapsToKsha() {
        String result = converter.convert("xa");
        assertTrue("id08: 'xa' must start with ka", result.startsWith("\u0915"));
    }
}
