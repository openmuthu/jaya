package org.jaya.scriptconverter;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link SCUtils}.
 */
public class SCUtilsTest {

    // --- isDevanagari ---

    @Test
    public void sc01_isDevanagari_devanagariChar() {
        // \u0905 is Devanagari 'a'
        assertTrue("sc01: Devanagari 'a' must be Devanagari", SCUtils.isDevanagari('\u0905'));
    }

    @Test
    public void sc02_isDevanagari_kannadaChar() {
        assertFalse("sc02: Kannada char must not be Devanagari", SCUtils.isDevanagari('\u0C85'));
    }

    @Test
    public void sc03_isDevanagari_ascii() {
        assertFalse("sc03: ASCII 'a' must not be Devanagari", SCUtils.isDevanagari('a'));
    }

    // --- isKannada ---

    @Test
    public void sc04_isKannada_kannadaChar() {
        // \u0C85 is Kannada 'a'
        assertTrue("sc04: Kannada 'a' must be Kannada", SCUtils.isKannada('\u0C85'));
    }

    @Test
    public void sc05_isKannada_devanagariChar() {
        assertFalse("sc05: Devanagari char must not be Kannada", SCUtils.isKannada('\u0905'));
    }

    // --- isTelugu ---

    @Test
    public void sc06_isTelugu_teluguChar() {
        // \u0C05 is Telugu 'a'
        assertTrue("sc06: Telugu 'a' must be Telugu", SCUtils.isTelugu('\u0C05'));
    }

    @Test
    public void sc07_isTelugu_ascii() {
        assertFalse("sc07: ASCII must not be Telugu", SCUtils.isTelugu('z'));
    }

    // --- guessScript(char) ---

    @Test
    public void sc08_guessScript_char_devanagari() {
        assertEquals("sc08: Devanagari char guesses DEVANAGARI", ScriptType.DEVANAGARI, SCUtils.guessScript('\u0905'));
    }

    @Test
    public void sc09_guessScript_char_kannada() {
        assertEquals("sc09: Kannada char guesses KANNADA", ScriptType.KANNADA, SCUtils.guessScript('\u0C85'));
    }

    @Test
    public void sc10_guessScript_char_telugu() {
        assertEquals("sc10: Telugu char guesses TELUGU", ScriptType.TELUGU, SCUtils.guessScript('\u0C05'));
    }

    @Test
    public void sc11_guessScript_char_ascii() {
        assertEquals("sc11: ASCII guesses ITRANS", ScriptType.ITRANS, SCUtils.guessScript('r'));
    }

    // --- guessScript(String) ---

    @Test
    public void sc12_guessScript_string_devanagari() {
        String devaStr = "\u0930\u093E\u092E"; // rAma in Devanagari
        assertEquals("sc12: Devanagari string", ScriptType.DEVANAGARI, SCUtils.guessScript(devaStr));
    }

    @Test
    public void sc13_guessScript_string_itrans() {
        assertEquals("sc13: ITRANS string", ScriptType.ITRANS, SCUtils.guessScript("rAma"));
    }

    @Test
    public void sc14_guessScript_string_kannada() {
        String kanStr = "\u0CB0\u0CBE\u0CAE"; // rAma in Kannada
        assertEquals("sc14: Kannada string", ScriptType.KANNADA, SCUtils.guessScript(kanStr));
    }

    @Test
    public void sc15_guessScript_string_whitespaceOnly_returnsITRANS() {
        assertEquals("sc15: whitespace only defaults to ITRANS", ScriptType.ITRANS, SCUtils.guessScript("   "));
    }

    // --- isDependentCharacter ---

    @Test
    public void sc16_isDependentCharacter_devanagariVirama() {
        // \u094D is Devanagari virama (halant) — a dependent character
        assertTrue("sc16: Devanagari virama is dependent", SCUtils.isDependentCharacter('\u094D'));
    }

    @Test
    public void sc17_isDependentCharacter_kannadaVirama() {
        // \u0CCD is Kannada virama
        assertTrue("sc17: Kannada virama is dependent", SCUtils.isDependentCharacter('\u0CCD'));
    }

    @Test
    public void sc18_isDependentCharacter_devanagariConsonant_notDependent() {
        // \u0915 is 'ka' — an independent consonant
        assertFalse("sc18: 'ka' is not a dependent character", SCUtils.isDependentCharacter('\u0915'));
    }

    @Test
    public void sc19_isDependentCharacter_itransChar_notDependent() {
        assertFalse("sc19: ASCII is not dependent", SCUtils.isDependentCharacter('k'));
    }

    // --- convertStringToScript ---

    @Test
    public void sc20_convertStringToScript_nullDest_returnsOriginal() {
        String input = "rAma";
        assertEquals("sc20: null dest returns input unchanged", input, SCUtils.convertStringToScript(input, null));
    }

    @Test
    public void sc21_convertStringToScript_sameScript_returnsOriginal() {
        String input = "rAma";
        assertEquals("sc21: ITRANS -> ITRANS returns original", input, SCUtils.convertStringToScript(input, ScriptType.ITRANS));
    }

    @Test
    public void sc22_convertStringToScript_itransToDevanagari() {
        String input = "rAma";
        String result = SCUtils.convertStringToScript(input, ScriptType.DEVANAGARI);
        // Must contain Devanagari characters
        boolean hasDevanagari = false;
        for (char c : result.toCharArray()) {
            if (SCUtils.isDevanagari(c)) { hasDevanagari = true; break; }
        }
        assertTrue("sc22: converted string must contain Devanagari chars", hasDevanagari);
    }

    // --- isDevanagariDependentCharacter ---

    @Test
    public void sc23_isDevanagariDependentChar_virama() {
        // \u094D is Devanagari virama — in range 0x093A-0x094F
        assertTrue("sc23: Devanagari virama is dependent",
                SCUtils.isDevanagariDependentCharacter('\u094D'));
    }

    @Test
    public void sc24_isDevanagariDependentChar_anusvara() {
        // \u0902 is Devanagari anusvara — in range 0x0900-0x0903
        assertTrue("sc24: Devanagari anusvara is dependent",
                SCUtils.isDevanagariDependentCharacter('\u0902'));
    }

    @Test
    public void sc25_isDevanagariDependentChar_vowelSign() {
        // \u0962 is Devanagari vowel sign — in range 0x0962-0x0963
        assertTrue("sc25: Devanagari vowel sign is dependent",
                SCUtils.isDevanagariDependentCharacter('\u0962'));
    }

    @Test
    public void sc26_isDevanagariDependentChar_consonant_notDependent() {
        // \u0915 is 'ka' consonant — not dependent
        assertFalse("sc26: 'ka' is not Devanagari dependent",
                SCUtils.isDevanagariDependentCharacter('\u0915'));
    }

    // --- isKannadaDependentCharacter ---

    @Test
    public void sc27_isKannadaDependentChar_anusvara() {
        // \u0C82 is Kannada anusvara — in range 0x0C82-0x0C83
        assertTrue("sc27: Kannada anusvara is dependent",
                SCUtils.isKannadaDependentCharacter('\u0C82'));
    }

    @Test
    public void sc28_isKannadaDependentChar_virama() {
        // \u0CCD is Kannada virama — in range 0x0CBC-0x0CDD
        assertTrue("sc28: Kannada virama is dependent",
                SCUtils.isKannadaDependentCharacter('\u0CCD'));
    }

    @Test
    public void sc29_isKannadaDependentChar_consonant_notDependent() {
        assertFalse("sc29: Kannada 'ka' is not dependent",
                SCUtils.isKannadaDependentCharacter('\u0C95'));
    }

    // --- isKannadaHalanth ---

    @Test
    public void sc30_isKannadaHalanth_virama() {
        assertTrue("sc30: Kannada virama is halanth",
                SCUtils.isKannadaHalanth('\u0CCD'));
    }

    @Test
    public void sc31_isKannadaHalanth_otherChar_false() {
        assertFalse("sc31: other char is not halanth",
                SCUtils.isKannadaHalanth('\u0C95'));
    }

    // --- isTeluguDependentCharacter ---

    @Test
    public void sc32_isTeluguDependentChar_anusvara() {
        // \u0C02 is Telugu anusvara — in range 0x0C02-0x0C03
        assertTrue("sc32: Telugu anusvara is dependent",
                SCUtils.isTeluguDependentCharacter('\u0C02'));
    }

    @Test
    public void sc33_isTeluguDependentChar_virama() {
        // \u0C4D is Telugu virama — in range 0x0C3A-0x0C4F
        assertTrue("sc33: Telugu virama is dependent",
                SCUtils.isTeluguDependentCharacter('\u0C4D'));
    }

    @Test
    public void sc34_isTeluguDependentChar_consonant_notDependent() {
        assertFalse("sc34: Telugu 'ka' is not dependent",
                SCUtils.isTeluguDependentCharacter('\u0C15'));
    }

    // --- isDependentCharacter with Telugu/Kannada ---

    @Test
    public void sc35_isDependentCharacter_teluguVirama() {
        assertTrue("sc35: Telugu virama is dependent",
                SCUtils.isDependentCharacter('\u0C4D'));
    }

    @Test
    public void sc36_isDependentCharacter_ascii_notDependent() {
        assertFalse("sc36: ASCII is not dependent",
                SCUtils.isDependentCharacter('a'));
    }
}
