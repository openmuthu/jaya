# Bookmark Resilience: Content Fingerprint Design

## The Problem

Each grantha file is split into numbered chunks during indexing. A bookmark
stores `docPath + docLocalId` where `docLocalId` is a counter (0, 1, 2, ...)
assigned to chunks within that file. If the file is updated — say a new verse
is inserted in the middle — every chunk after the insertion point gets
renumbered. A bookmark pointing to chunk `5` might now actually be at chunk
`6`, and the lookup `(path, 5)` either finds the wrong chunk or returns
nothing, silently breaking the bookmark.

## Solution: Content Fingerprint

### Generation (at bookmark time)

When a bookmark is added, the `ResultDocument` is in memory with its full text
content. We extract the first 120 characters of that content, normalize
whitespace (trim + collapse multiple spaces/newlines into one space), and store
it alongside the `docLocalId`:

```
raw content:  "shrI rAma\n  jaya rAma\njaya jaya rAma\n..."
fingerprint:  "shrI rAma jaya rAma jaya jaya rAma..."   (≤120 chars, normalized)
```

The fingerprint is stored in the JSON file as a `"fingerprint"` field on each
bookmark entry (JSON format v2.0).

### Lookup (at display time)

**Normal case** — index unchanged:

```
getDoc(docPath, docLocalId)  →  finds the chunk immediately  ✓
```

**After index rebuild** — `docLocalId` has shifted:

```
getDoc(docPath, "5")  →  returns null
  ↓ fallback
getDocByPathAndFingerprint(docPath, fingerprint)
  → loads all chunks for this path via getDocsForPath()
  → normalises each chunk's content the same way
  → returns the chunk whose normalised prefix matches
  ✓ found at docLocalId "6"
  ↓ auto-heal
healAnnotationLocalId(oldKey, "6")
  → updates stored docLocalId from "5" to "6"
  → marks dirty → saved to disk
  next lookup uses fast path again  ✓
```

### Migration of Existing Bookmarks

Old bookmarks (created before this feature) have no fingerprint stored. On
every app launch, `JayaApp.backfillBookmarkFingerprintsAsync()` runs on a
background thread:

```
for each annotation with empty fingerprint:
    getDoc(docPath, docLocalId)   // current index still valid
    extract fingerprint from the found chunk
    store it on the annotation
save to disk
```

This runs silently on the first launch after upgrading. As long as the user
launches the app at least once before a grantha update occurs, all their
existing bookmarks are automatically protected with no user action required.

**Cost on subsequent launches:** `backfillBookmarkFingerprintsAsync()` first
calls `needsFingerprintBackfill()` — a pure in-memory loop over all annotations
checking one string field each. Once all fingerprints are populated (after the
first post-upgrade launch), this returns false immediately and no worker thread
is ever spawned again. The ongoing overhead is microseconds, no I/O.

## Relevant Files

| File | Role |
|------|------|
| `UnicodeSearch/.../annotation/Annotation.java` | `mContentFingerprint` field; `buildFingerprint()`; `setDocLocalId()` |
| `UnicodeSearch/.../annotation/AnnotationManager.java` | Captures fingerprint in `addAnnotation()`; persists in JSON v2.0; `backfillFingerprints()`; `healAnnotationLocalId()` |
| `UnicodeSearch/.../search/LuceneUnicodeSearcher.java` | `getDocByPathAndFingerprint()` — scans all chunks for a path and matches by fingerprint |
| `android/.../JayaAppUtils.java` | `getDoc()` — tries fast path, falls back to fingerprint, auto-heals on recovery |
| `android/.../JayaApp.java` | `backfillBookmarkFingerprintsAsync()` — background worker called at app launch |
| `android/.../MainActivity.java` | Calls `backfillBookmarkFingerprintsAsync()` in `onCreate()` |
| `UnicodeSearch/.../annotation/BookmarkGroupTest.java` | Tests `fp01`–`fp15` cover all paths |

## Performance of the Fallback Scan

The fallback calls `getDocsForPath()` which retrieves all chunks for the file,
then compares fingerprints linearly. This might look like an O(n) scan, but in
practice it is not a concern:

- **The fallback only runs when `(docPath, docLocalId)` returns null** — i.e.,
  after an index rebuild where the localId shifted. This is a rare event, not
  something that happens on every display call.
- **`getDocsForPath()` is a single indexed Lucene `TermQuery` on the `path`
  field.** Lucene returns only that file's chunks; it does not scan the whole
  index. The `mReader.maxDoc()` passed to `search()` is merely an upper bound
  on results, not a scan limit.
- **Chunk counts per file are small.** With 128–512 chars per chunk, even a
  500,000-char grantha produces at most ~1,000–4,000 chunks. Loading them and
  comparing 120-char strings is sub-millisecond.
- **Auto-heal eliminates repeat fallbacks.** Once recovered, the corrected
  `docLocalId` is saved to disk, so subsequent lookups use the fast path again.

## Alternative: Store Fingerprint as a Lucene Field

A faster alternative is to store the fingerprint as a **`StringField`**
(non-analyzed, indexed verbatim) directly in each Lucene document at index
time. Lookup becomes a single `TermQuery` — O(log N) via the inverted index,
with no per-file scan.

```java
// LuceneUnicodeFileIndexer — when writing each chunk:
String fp = Annotation.buildFingerprint(lines);
document.add(new StringField(Constatants.FIELD_FINGERPRINT, fp, Field.Store.YES));

// LuceneUnicodeSearcher — fallback lookup:
TermQuery fpQuery = new TermQuery(new Term(Constatants.FIELD_FINGERPRINT, fingerprint));
```

**Why `StringField` and not `TextField`?**  `TextField` runs the content
through a full-text analyzer which may lowercase or strip characters. ITRANS
uses uppercase letters for long vowels (`A`, `I`, `U`), so lowercasing would
corrupt the fingerprint and break matching. `StringField` indexes the value
verbatim with no analysis, making it safe for ITRANS fingerprints.

**Trade-off:** this requires re-indexing all granthas to populate the new
field. Old indexes won't have it, so the current scan-based fallback must
remain for backward compatibility. New downloads would automatically get the
fast lookup.

| Approach | Lookup speed | Requires re-index | Works on old indexes |
|---|---|---|---|
| Current (scan chunks for path) | Fast enough (~1 ms) | No | Yes |
| `StringField` in Lucene | O(log N) | Yes (new downloads only) | No — needs fallback |

Given the rarity of the fallback and the small chunk counts involved, the
current approach is sufficient. The `StringField` optimization is noted here
for future consideration if chunk counts grow significantly.

## Limitations

- Does not survive the grantha text itself being **edited** (different wording,
  not just insertions elsewhere). For classical Sanskrit texts this is
  essentially never the case.
- A bookmark created on a device that never launches the app before a re-index
  will not have a fingerprint and cannot be recovered. This is the same failure
  mode as before this feature — it is not made worse.
