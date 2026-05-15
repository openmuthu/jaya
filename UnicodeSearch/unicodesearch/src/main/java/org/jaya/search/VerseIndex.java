package org.jaya.search;

import org.jaya.util.Constatants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Builds a verse-number → Lucene-docId map for a single text file by scanning
 * every stored {@code FIELD_CONTENTS} chunk.
 *
 * <p>Two pattern families are recognised:
 * <ul>
 *   <li><b>Danda-wrapped</b> (higher confidence): {@code ॥1.2॥}, {@code ॥10.3.2॥}
 *   <li><b>Bare ASCII</b>: {@code 1.2}, {@code 1-2}, {@code 10.3.2}, {@code 1-2.3}
 * </ul>
 *
 * <p>Separators {@code '.'} and {@code '-'} are treated as equivalent; keys are
 * normalised to {@code '.'} so {@code "1-2"} and {@code "1.2"} resolve to the same
 * entry.  The first occurrence of each verse key wins.
 */
public class VerseIndex {

    /**
     * Bare verse numbers not preceded or followed by another digit.
     * Matches: 1.1 / 1-2 / 10.3.2 / 1-2.3 / 1.2-3
     */
    static final Pattern BARE_PATTERN =
            Pattern.compile("(?<![0-9])(\\d+[.\\-]\\d+(?:[.\\-]\\d+)?)(?![0-9])");

    /**
     * Verse numbers wrapped in Devanagari double-danda (U+0965).
     * Matches: ॥1.1॥ / ॥10.3.2॥
     */
    static final Pattern DANDA_PATTERN =
            Pattern.compile("\u0965(\\d+[.\\-]\\d+(?:[.\\-]\\d+)?)\u0965");

    /** Strips everything that is not a Unicode letter, digit, or combining mark. */
    private static final Pattern STRIP_PATTERN = Pattern.compile("[^\\p{L}\\p{N}\\p{M}]+");

    /** Number of characters to keep in each verse preview. */
    static final int PREVIEW_LENGTH = 30;

    /** Normalised verseKey → first Lucene docId containing it (insertion order). */
    private final Map<String, Integer> mVerseToDocId = new LinkedHashMap<String, Integer>();
    /** Normalised verseKey → short content preview from the chunk where it first appeared. */
    private final Map<String, String> mVerseToPreview = new LinkedHashMap<String, String>();
    /** chapter key → ordered list of verse keys in that chapter. */
    private final Map<String, List<String>> mChapterToVerses =
            new LinkedHashMap<String, List<String>>();
    /** 2 for x.y files, 3 for x.y.z files. */
    private int mDepth = 2;

    private VerseIndex() {}

    // ── Factories ─────────────────────────────────────────────────────────────

    /**
     * Scans all chunks of {@code filePath} in the index and builds the verse map.
     * Intended to be called from a background thread.
     */
    public static VerseIndex build(String filePath, LuceneUnicodeSearcher searcher) {
        return buildFromDocs(searcher.getDocsForPath(filePath));
    }

    /**
     * Package-private factory used by unit tests — accepts pre-built documents
     * so no real Lucene index is needed.
     */
    static VerseIndex buildFromDocs(List<ResultDocument> docs) {
        VerseIndex idx = new VerseIndex();
        for (ResultDocument rd : docs) {
            if (rd.getDoc() == null) continue;
            String contents = rd.getDoc().get(Constatants.FIELD_CONTENTS);
            if (contents != null) idx.extractVerses(contents, rd.getId());
        }
        idx.buildChapterMap();
        return idx;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** {@code true} if at least one verse number was found in this file. */
    public boolean hasVerses() {
        return !mVerseToDocId.isEmpty();
    }

    /** Ordered list of unique chapter keys (first component of the verse key). */
    public List<String> getChapters() {
        return new ArrayList<String>(mChapterToVerses.keySet());
    }

    /**
     * Ordered list of verse keys belonging to {@code chapter}, in the order
     * they were first encountered in the file.
     */
    public List<String> getVersesForChapter(String chapter) {
        List<String> v = mChapterToVerses.get(chapter);
        return v != null ? new ArrayList<String>(v) : new ArrayList<String>();
    }

    /**
     * Returns the Lucene docId for the first chunk that contains {@code verseKey},
     * or {@code -1} if the key is unknown.  Accepts {@code '.'} or {@code '-'}
     * as the separator.
     */
    public int getDocId(String verseKey) {
        Integer id = mVerseToDocId.get(normalise(verseKey));
        return id != null ? id : -1;
    }

    /** 2 for x.y files, 3 for x.y.z files. */
    public int getDepth() {
        return mDepth;
    }

    /**
     * Returns a short content preview for {@code verseKey}: the first
     * {@link #PREVIEW_LENGTH} characters of the chunk that introduced it,
     * with all whitespace and non-letter/digit characters stripped.
     * Returns an empty string if the key is unknown.
     */
    public String getPreview(String verseKey) {
        String preview = mVerseToPreview.get(normalise(verseKey));
        return preview != null ? preview : "";
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void extractVerses(String contents, int docId) {
        // Danda-wrapped: higher confidence, process first
        Matcher m = DANDA_PATTERN.matcher(contents);
        while (m.find()) {
            String key = normalise(m.group(1));
            if (!mVerseToDocId.containsKey(key)) {
                mVerseToDocId.put(key, docId);
                mVerseToPreview.put(key, previewAround(contents, m));
            }
        }
        // Bare ASCII: only record if not already captured via danda
        m = BARE_PATTERN.matcher(contents);
        while (m.find()) {
            String key = normalise(m.group(1));
            if (!mVerseToDocId.containsKey(key)) {
                mVerseToDocId.put(key, docId);
                mVerseToPreview.put(key, previewAround(contents, m));
            }
        }
    }

    /**
     * Strips all non-letter/non-digit/non-mark characters, skips any leading
     * digit run (e.g. chunk sequence numbers like "17615"), then returns the
     * first {@link #PREVIEW_LENGTH} characters of what remains.
     */
    static String makePreview(String contents) {
        String stripped = STRIP_PATTERN.matcher(contents).replaceAll("");
        // Skip leading ASCII digits — these are indexing artefacts (page/shloka
        // numbers) that appear before the actual text in the stored chunks.
        int start = 0;
        while (start < stripped.length()
                && stripped.charAt(start) >= '0' && stripped.charAt(start) <= '9') {
            start++;
        }
        if (start > 0) stripped = stripped.substring(start);
        return stripped.length() > PREVIEW_LENGTH
                ? stripped.substring(0, PREVIEW_LENGTH)
                : stripped;
    }

    /**
     * Chooses the best preview for a verse marker match.
     * Prefers text after the marker (next-verse content); falls back to text
     * before the marker when the marker sits at the end of a verse
     * (e.g. Bhāgavata Purāṇa style: {@code verse text ॥3.1.1॥}).
     */
    private static String previewAround(String contents, Matcher m) {
        // Preview from text after the verse-number marker so each verse
        // in the same Lucene document gets its own distinct preview.
        String after = makePreview(contents.substring(m.end()));
        if (!after.isEmpty()) return after;
        // Marker is at end of verse — use the verse body that precedes it.
        return makePreview(contents.substring(0, m.start()));
    }

    /** Replaces {@code '-'} with {@code '.'} for canonical key form. */
    static String normalise(String raw) {
        return raw.replace('-', '.');
    }

    private void buildChapterMap() {
        for (String key : mVerseToDocId.keySet()) {
            String[] parts = key.split("\\.");
            int depth = Math.min(parts.length, 3);
            if (depth > mDepth) mDepth = depth;
            String chapter = parts[0];
            if (!mChapterToVerses.containsKey(chapter)) {
                mChapterToVerses.put(chapter, new ArrayList<String>());
            }
            mChapterToVerses.get(chapter).add(key);
        }
    }
}
