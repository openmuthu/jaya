package org.jaya.android;

import org.jaya.scriptconverter.ScriptType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the script-conversion helper in {@link VerseTreeAdapter}.
 *
 * {@link VerseTreeAdapter#scriptConvert(String, ScriptType)} is the only
 * testable seam that doesn't require an Android runtime: it is pure-Java
 * and accepts the target {@link ScriptType} as an explicit parameter, so
 * there is no dependency on {@code PreferencesManager} or SharedPreferences.
 *
 * Scenarios covered
 * -----------------
 * SC-1  Empty string        → returned unchanged (no NPE / conversion attempted)
 * SC-2  ITRANS passthrough  → ITRANS target returns the input as-is
 * SC-3  ITRANS → Devanagari → well-known syllable converts correctly
 * SC-4  ITRANS → Kannada    → same syllable converts to Kannada Unicode
 * SC-5  ITRANS → Telugu     → same syllable converts to Telugu Unicode
 * SC-6  ASCII digits        → passed through unchanged by all scripts
 */
public class VerseTreeAdapterTest {

    // ── SC-1 ─────────────────────────────────────────────────────────────────

    @Test
    public void scriptConvert_emptyString_returnsEmpty() {
        assertEquals("", VerseTreeAdapter.scriptConvert("", ScriptType.DEVANAGARI));
        assertEquals("", VerseTreeAdapter.scriptConvert("", ScriptType.KANNADA));
        assertEquals("", VerseTreeAdapter.scriptConvert("", ScriptType.ITRANS));
    }

    // ── SC-2 ─────────────────────────────────────────────────────────────────

    @Test
    public void scriptConvert_itransTarget_returnsSameString() {
        String itrans = "nArAyaNa";
        assertEquals(itrans, VerseTreeAdapter.scriptConvert(itrans, ScriptType.ITRANS));
    }

    // ── SC-3 ─────────────────────────────────────────────────────────────────

    @Test
    public void scriptConvert_devanagari_convertsCorrectly() {
        // "na" in ITRANS → न in Devanagari
        String result = VerseTreeAdapter.scriptConvert("na", ScriptType.DEVANAGARI);
        assertEquals("न", result);
    }

    // ── SC-4 ─────────────────────────────────────────────────────────────────

    @Test
    public void scriptConvert_kannada_convertsCorrectly() {
        // "na" in ITRANS → ನ in Kannada (U+0CA8)
        String result = VerseTreeAdapter.scriptConvert("na", ScriptType.KANNADA);
        assertEquals("ನ", result);
    }

    // ── SC-5 ─────────────────────────────────────────────────────────────────

    @Test
    public void scriptConvert_telugu_convertsCorrectly() {
        // "na" in ITRANS → న in Telugu (U+0C28)
        String result = VerseTreeAdapter.scriptConvert("na", ScriptType.TELUGU);
        assertEquals("న", result);
    }

    // ── SC-6 ─────────────────────────────────────────────────────────────────

    @Test
    public void scriptConvert_asciiDigits_passedThrough() {
        // Digits are not script-converted; they should survive unchanged
        String result = VerseTreeAdapter.scriptConvert("42", ScriptType.DEVANAGARI);
        assertEquals("42", result);
    }
}
