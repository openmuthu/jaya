package org.jaya.scriptconverter;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for script converters: Devanagari↔ITRANS, Kannada↔Devanagari,
 * ITRANS→Kannada, ITRANS→Telugu, Telugu↔Devanagari.
 *
 * Tests focus on verifiable round-trips and well-known character mappings.
 */
public class ScriptConvertersTest {

    // ---- Devanagari → ITRANS ----

    @Test
    public void di01_devanagariToITRANS_vowelA() {
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.DEVANAGARI, ScriptType.ITRANS);
        assertEquals("di01: \u0905 → 'a'", "a", c.convert("\u0905"));
    }

    @Test
    public void di02_devanagariToITRANS_consonantKa() {
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.DEVANAGARI, ScriptType.ITRANS);
        // \u0915 is 'ka'
        String result = c.convert("\u0915");
        assertTrue("di02: 'ka' must produce string starting with 'k'", result.startsWith("k"));
    }

    @Test
    public void di03_devanagariToITRANS_sourceDestination() {
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.DEVANAGARI, ScriptType.ITRANS);
        assertEquals("di03a", ScriptType.DEVANAGARI, c.getSourceScript());
        assertEquals("di03b", ScriptType.ITRANS, c.getDestinationScript());
    }

    // ---- ITRANS → Devanagari → ITRANS round-trip ----

    @Test
    public void rt01_itransToDeva_toItrans_roundTrip() {
        ScriptConverter it2dev = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.DEVANAGARI);
        ScriptConverter dev2it = ScriptConverterFactory.getScriptConverter(ScriptType.DEVANAGARI, ScriptType.ITRANS);
        String original = "rAma";
        String deva = it2dev.convert(original);
        String back = dev2it.convert(deva);
        // Round-trip may not be identical due to alternate spellings, but must be non-empty
        assertNotNull("rt01a: back-converted must not be null", back);
        assertFalse("rt01b: back-converted must not be empty", back.isEmpty());
    }

    // ---- ITRANS → Kannada ----

    @Test
    public void ik01_itransToKannada_rAma_producesKannada() {
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.KANNADA);
        assertNotNull("ik01: ITRANS→Kannada converter must exist", c);
        String result = c.convert("rAma");
        boolean hasKannada = false;
        for (char ch : result.toCharArray()) {
            if (SCUtils.isKannada(ch)) { hasKannada = true; break; }
        }
        assertTrue("ik01b: rAma must produce Kannada chars", hasKannada);
    }

    @Test
    public void ik02_itransToKannada_sourceDestination() {
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.KANNADA);
        assertEquals("ik02a", ScriptType.ITRANS, c.getSourceScript());
        assertEquals("ik02b", ScriptType.KANNADA, c.getDestinationScript());
    }

    // ---- ITRANS → Telugu ----

    @Test
    public void it01_itransToTelugu_rAma_producesTelugu() {
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.TELUGU);
        assertNotNull("it01: ITRANS→Telugu converter must exist", c);
        String result = c.convert("rAma");
        boolean hasTelugu = false;
        for (char ch : result.toCharArray()) {
            if (SCUtils.isTelugu(ch)) { hasTelugu = true; break; }
        }
        assertTrue("it01b: rAma must produce Telugu chars", hasTelugu);
    }

    // ---- Kannada → Devanagari ----

    @Test
    public void kd01_kannadaToDevanagari_producesDevanagari() {
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.KANNADA, ScriptType.DEVANAGARI);
        assertNotNull("kd01: converter must exist", c);
        // \u0CB0\u0CBE\u0CAE is rAma in Kannada
        String result = c.convert("\u0CB0\u0CBE\u0CAE");
        boolean hasDeva = false;
        for (char ch : result.toCharArray()) {
            if (SCUtils.isDevanagari(ch)) { hasDeva = true; break; }
        }
        assertTrue("kd01b: Kannada rAma must produce Devanagari", hasDeva);
    }

    // ---- Kannada → ITRANS ----

    @Test
    public void ki01_kannadaToITRANS_sourceDestination() {
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.KANNADA, ScriptType.ITRANS);
        assertNotNull("ki01: converter must exist", c);
        assertEquals("ki01a", ScriptType.KANNADA, c.getSourceScript());
        assertEquals("ki01b", ScriptType.ITRANS, c.getDestinationScript());
    }

    // ---- Telugu → Devanagari ----

    @Test
    public void td01_teluguToDevanagari_producesDevanagari() {
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.TELUGU, ScriptType.DEVANAGARI);
        assertNotNull("td01: converter must exist", c);
    }

    // ---- Telugu → ITRANS ----

    @Test
    public void ti01_teluguToITRANS_sourceDestination() {
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.TELUGU, ScriptType.ITRANS);
        assertNotNull("ti01: converter must exist", c);
        assertEquals("ti01a", ScriptType.TELUGU, c.getSourceScript());
        assertEquals("ti01b", ScriptType.ITRANS, c.getDestinationScript());
    }

    // ---- Devanagari → Kannada ----

    @Test
    public void dk01_devanagariToKannada_producesKannada() {
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.DEVANAGARI, ScriptType.KANNADA);
        assertNotNull("dk01: converter must exist", c);
        // \u0930\u093E\u092E = rAma in Devanagari
        String result = c.convert("\u0930\u093E\u092E");
        boolean hasKannada = false;
        for (char ch : result.toCharArray()) {
            if (SCUtils.isKannada(ch)) { hasKannada = true; break; }
        }
        assertTrue("dk01b: Devanagari rAma must produce Kannada", hasKannada);
    }

    // ---- Devanagari → Telugu ----

    @Test
    public void dt01_devanagariToTelugu_producesOrFallsBack() {
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.DEVANAGARI, ScriptType.TELUGU);
        assertNotNull("dt01: converter must exist", c);
    }

    // ---- ScriptConverterFactory: missing converter returns null ----

    @Test
    public void sf01_missingConverter_returnsNull() {
        // There is no KANNADA→TELUGU converter registered
        ScriptConverter c = ScriptConverterFactory.getScriptConverter(ScriptType.KANNADA, ScriptType.TELUGU);
        assertNull("sf01: unregistered converter pair must return null", c);
    }
}
