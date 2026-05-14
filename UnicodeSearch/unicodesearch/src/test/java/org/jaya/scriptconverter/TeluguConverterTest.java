package org.jaya.scriptconverter;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link TeluguToITRANSConverter} and {@link ITRANSToTeluguConverter},
 * covering more character paths to bring their branch coverage up.
 */
public class TeluguConverterTest {

    private final ScriptConverter tel2it =
            ScriptConverterFactory.getScriptConverter(ScriptType.TELUGU, ScriptType.ITRANS);
    private final ScriptConverter it2tel =
            ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.TELUGU);

    // -- Telugu vowels to ITRANS --

    @Test
    public void tv01_teluguA_toITRANS() {
        assertEquals("tv01: Telugu 'a'", "a", tel2it.convert("\u0C05"));
    }

    @Test
    public void tv02_teluguAA_toITRANS() {
        assertEquals("tv02: Telugu 'A'", "A", tel2it.convert("\u0C06"));
    }

    @Test
    public void tv03_teluguI_toITRANS() {
        assertEquals("tv03: Telugu 'i'", "i", tel2it.convert("\u0C07"));
    }

    @Test
    public void tv04_teluguU_toITRANS() {
        assertEquals("tv04: Telugu 'u'", "u", tel2it.convert("\u0C09"));
    }

    @Test
    public void tv05_teluguE_toITRANS() {
        assertEquals("tv05: Telugu short-e", "e", tel2it.convert("\u0C0E"));
    }

    @Test
    public void tv06_teluguO_toITRANS() {
        assertEquals("tv06: Telugu 'o'", "o", tel2it.convert("\u0C12"));
    }

    @Test
    public void tv07_teluguAU_toITRANS() {
        assertEquals("tv07: Telugu 'au'", "au", tel2it.convert("\u0C14"));
    }

    // -- Telugu consonants to ITRANS --

    @Test
    public void tc01_teluguKa_toITRANS() {
        // \u0C15 = Telugu 'ka'
        String r = tel2it.convert("\u0C15");
        assertTrue("tc01: must contain 'k'", r.contains("k"));
    }

    @Test
    public void tc02_teluguNa_toITRANS() {
        String r = tel2it.convert("\u0C28");
        assertTrue("tc02: must contain 'n'", r.contains("n"));
    }

    @Test
    public void tc03_teluguRa_toITRANS() {
        String r = tel2it.convert("\u0C30");
        assertTrue("tc03: must contain 'r'", r.contains("r"));
    }

    @Test
    public void tc04_teluguMa_toITRANS() {
        String r = tel2it.convert("\u0C2E");
        assertTrue("tc04: must contain 'm'", r.contains("m"));
    }

    // -- ITRANS to Telugu --

    @Test
    public void it01_rAma_toTelugu_hasTelugu() {
        String result = it2tel.convert("rAma");
        boolean hasTelugu = false;
        for (char c : result.toCharArray()) {
            if (SCUtils.isTelugu(c)) { hasTelugu = true; break; }
        }
        assertTrue("it01: rAma must produce Telugu chars", hasTelugu);
    }

    @Test
    public void it02_hari_toTelugu() {
        String result = it2tel.convert("hari");
        assertNotNull("it02: not null", result);
        assertFalse("it02b: not empty", result.isEmpty());
    }

    // -- Round-trip: ITRANS → Telugu → ITRANS --

    @Test
    public void rt01_itransToTelugu_backToITRANS() {
        String orig = "rAma";
        String tel = it2tel.convert(orig);
        String back = tel2it.convert(tel);
        assertFalse("rt01: back-converted must not be empty", back.isEmpty());
    }

    // -- Source/destination scripts --

    @Test
    public void sd01_teluguToITRANS_scripts() {
        assertEquals("sd01a: source TELUGU", ScriptType.TELUGU, tel2it.getSourceScript());
        assertEquals("sd01b: dest ITRANS", ScriptType.ITRANS, tel2it.getDestinationScript());
    }

    @Test
    public void sd02_ITRANSToTelugu_scripts() {
        assertEquals("sd02a: source ITRANS", ScriptType.ITRANS, it2tel.getSourceScript());
        assertEquals("sd02b: dest TELUGU", ScriptType.TELUGU, it2tel.getDestinationScript());
    }

    // -- Telugu matras --

    @Test
    public void tm01_teluguMatraA_toITRANS() {
        // \u0C3E = Telugu vowel sign AA (matra)
        String r = tel2it.convert("\u0C3E");
        assertEquals("tm01: matra A", "A", r);
    }

    @Test
    public void tm02_teluguMatraI_toITRANS() {
        assertEquals("tm02: matra i", "i", tel2it.convert("\u0C3F"));
    }

    @Test
    public void tm03_teluguMatraU_toITRANS() {
        assertEquals("tm03: matra u", "u", tel2it.convert("\u0C41"));
    }
}
