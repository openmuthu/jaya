package org.jaya.indexsync;

import org.jaya.scriptconverter.ScriptType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;

import static org.junit.Assert.*;

/**
 * Tests for {@link IndexCatalogue}: initialization, item access, and merge.
 *
 * Uses a recent lastSyncDate so readCatalog() calls notifyCatalogueUpdate
 * instead of syncCatalogueFromRemote (which would attempt network access).
 */
public class IndexCatalogueTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    /** Catalogue JSON with one installed item and a recent sync date. */
    private static final String CATALOG_JSON =
        "{\"version\":\"1.0\"," +
        "\"lastModified\":\"2026-05-01T00:00:00.000Z\"," +
        "\"lastSyncDate\":\"2026-05-14T00:00:00.000Z\"," +
        "\"baseUrl\":\"http://example.com\"," +
        "\"items\":{" +
        "  \"rAmAyaNa\":{\"relPath\":\"rama.zip\"," +
        "    \"lastModified\":\"2026-05-01T00:00:00.000Z\"," +
        "    \"size\":1024," +
        "    \"installed\":\"true\"," +
        "    \"updateAvailable\":\"false\"}" +
        "}}";

    private static final String CATALOG_WITH_TWO_ITEMS =
        "{\"version\":\"1.0\"," +
        "\"lastModified\":\"2026-05-14T00:00:00.000Z\"," +
        "\"lastSyncDate\":\"2026-05-14T00:00:00.000Z\"," +
        "\"baseUrl\":\"http://example.com\"," +
        "\"items\":{" +
        "  \"rAmAyaNa\":{\"relPath\":\"rama.zip\"," +
        "    \"lastModified\":\"2026-05-01T00:00:00.000Z\"," +
        "    \"size\":1024,\"installed\":\"true\",\"updateAvailable\":\"false\"}," +
        "  \"mahAbhArata\":{\"relPath\":\"maha.zip\"," +
        "    \"lastModified\":\"2026-05-01T00:00:00.000Z\"," +
        "    \"size\":2048,\"installed\":\"false\",\"updateAvailable\":\"false\"}" +
        "}}";

    private File catalogFolder;

    @Before
    public void setUp() throws Exception {
        IndexCatalogue.resetStateForTest();
        catalogFolder = tmp.newFolder("catalog");
    }

    @After
    public void tearDown() {
        IndexCatalogue.resetStateForTest();
    }

    /** Write catalog JSON to the catalog folder and initialize a catalogue. */
    private IndexCatalogue buildCatalogue(String json) throws Exception {
        File f = new File(catalogFolder, IndexCatalogue.INDEX_CATALOG_FILE_NAME);
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(json);
        }
        IndexCatalogue cat = new IndexCatalogue();
        File indexFolder = tmp.newFolder("idx_" + System.nanoTime());
        // Use a non-existent remote URL so any attempted sync fails silently
        boolean result = cat.initialize(catalogFolder.getAbsolutePath(),
                "http://localhost:1", indexFolder.getAbsolutePath());
        assertTrue("initialize returns true on first call", result);
        return cat;
    }

    // ic01 — initialize returns true for first call
    @Test
    public void ic01_initialize_firstCall_returnsTrue() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        assertNotNull("ic01: catalogue not null", cat);
    }

    // ic02 — initialize returns false on second call (already initialized)
    @Test
    public void ic02_initialize_secondCall_returnsFalse() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        boolean second = cat.initialize(catalogFolder.getAbsolutePath(),
                "http://localhost:1", tmp.newFolder("idx2").getAbsolutePath());
        assertFalse("ic02: second initialize returns false", second);
    }

    // ic03 — getAppIndexFolderPath returns the configured folder
    @Test
    public void ic03_getAppIndexFolderPath() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        assertNotNull("ic03: index folder path not null", cat.getAppIndexFolderPath());
    }

    // ic04 — getNumItems returns 1 for single-item catalog
    @Test
    public void ic04_getNumItems_one() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        assertEquals("ic04: one item", 1, cat.getNumItems());
    }

    // ic05 — getItemNames returns the item name set
    @Test
    public void ic05_getItemNames_containsRama() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        assertNotNull("ic05a: item names not null", cat.getItemNames());
        assertTrue("ic05b: contains rAmAyaNa", cat.getItemNames().contains("rAmAyaNa"));
    }

    // ic06 — getItemByName returns non-null for known item
    @Test
    public void ic06_getItemByName_known() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.Item item = cat.getItemByName("rAmAyaNa");
        assertNotNull("ic06: item not null", item);
    }

    // ic07 — getItemByName returns null for unknown item
    @Test
    public void ic07_getItemByName_unknown() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.Item item = cat.getItemByName("nonExistent");
        assertNull("ic07: unknown item returns null", item);
    }

    // ic08 — Item.getName returns the item name
    @Test
    public void ic08_item_getName() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.Item item = cat.getItemByName("rAmAyaNa");
        assertEquals("ic08: item name", "rAmAyaNa", item.getName());
    }

    // ic09 — Item.getURL returns a non-empty URL
    @Test
    public void ic09_item_getURL() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.Item item = cat.getItemByName("rAmAyaNa");
        String url = item.getURL();
        assertNotNull("ic09a: url not null", url);
        assertFalse("ic09b: url not empty", url.isEmpty());
        assertTrue("ic09c: url contains relPath", url.contains("rama.zip"));
    }

    // ic10 — Item.getLastModified returns a Date
    @Test
    public void ic10_item_getLastModified() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.Item item = cat.getItemByName("rAmAyaNa");
        assertNotNull("ic10: lastModified not null", item.getLastModified());
    }

    // ic11 — Item.getSize returns expected size
    @Test
    public void ic11_item_getSize() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.Item item = cat.getItemByName("rAmAyaNa");
        assertEquals("ic11: size 1024", 1024L, item.getSize());
    }

    // ic12 — Item.getIsInstalled returns true for installed item
    @Test
    public void ic12_item_getIsInstalled_true() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.Item item = cat.getItemByName("rAmAyaNa");
        assertTrue("ic12: installed=true", item.getIsInstalled());
    }

    // ic13 — Item.getIsUpdateAvailable returns false
    @Test
    public void ic13_item_getIsUpdateAvailable_false() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.Item item = cat.getItemByName("rAmAyaNa");
        assertFalse("ic13: no update available", item.getIsUpdateAvailable());
    }

    // ic14 — Item.setIsInstalled toggles the installed flag
    @Test
    public void ic14_item_setIsInstalled_false() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.Item item = cat.getItemByName("rAmAyaNa");
        item.setIsInstalled(false);
        assertFalse("ic14: installed=false after set", item.getIsInstalled());
    }

    // ic15 — Item.setIsInstalled(true) clears updateAvailable
    @Test
    public void ic15_item_setIsInstalled_true_clearsUpdateAvailable() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.Item item = cat.getItemByName("rAmAyaNa");
        item.setIsInstalled(true);
        assertFalse("ic15: updateAvailable=false after install", item.getIsUpdateAvailable());
    }

    // ic16 — Item.getNameInPreferredScript(ITRANS) returns ITRANS name unchanged
    @Test
    public void ic16_item_getNameInPreferredScript_itrans() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.Item item = cat.getItemByName("rAmAyaNa");
        assertEquals("ic16: ITRANS name unchanged", "rAmAyaNa",
                item.getNameInPreferredScript(ScriptType.ITRANS));
    }

    // ic17 — Item.getNameInPreferredScript(DEVANAGARI) returns Devanagari string
    @Test
    public void ic17_item_getNameInPreferredScript_devanagari() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.Item item = cat.getItemByName("rAmAyaNa");
        String dev = item.getNameInPreferredScript(ScriptType.DEVANAGARI);
        assertNotNull("ic17a: devanagari not null", dev);
        // Second call should return cached value
        String dev2 = item.getNameInPreferredScript(ScriptType.DEVANAGARI);
        assertEquals("ic17b: cached value same", dev, dev2);
    }

    // ic18 — getLastSyncDate returns a non-null date
    @Test
    public void ic18_getLastSyncDate_notNull() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        assertNotNull("ic18: lastSyncDate not null", cat.getLastSyncDate());
    }

    // ic19 — getLastModifedDate returns a non-null date
    @Test
    public void ic19_getLastModifiedDate() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        assertNotNull("ic19: lastModified not null", cat.getLastModifedDate());
    }

    // ic20 — readCatalog(true) force=true re-reads from file
    @Test
    public void ic20_readCatalog_forceTrue() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        // force=true triggers syncCatalogueFromRemote which will fail silently (bad URL)
        // Just verify no exception and catalogue is still readable
        cat.readCatalog(true);
        assertTrue("ic20: getNumItems >= 0", cat.getNumItems() >= 0);
    }

    // ic21 — loadCatalogDetailsFromFileIfRequired when no details file exists is a no-op
    @Test
    public void ic21_loadCatalogDetails_missingFile_noOp() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        // No cat-details.txt in catalogFolder — should be a silent no-op
        cat.loadCatalogDetailsFromFileIfRequired(false);
        assertNotNull("ic21: catalogue still valid after noop", cat.getItemNames());
    }

    // ic22 — loadCatalogDetailsFromFileIfRequired loads from existing file
    @Test
    public void ic22_loadCatalogDetails_existingFile() throws Exception {
        String detailsJson =
            "{\"lastModified\":\"2026-05-01T00:00:00.000Z\"," +
            "\"items\":{\"rAmAyaNa\":{\"files\":\"/rAmAyaNa/\"}}}";
        File detailsFile = new File(catalogFolder,
                IndexCatalogue.INDEX_CATALOG_DETAILS_FILE_NAME);
        try (FileWriter fw = new FileWriter(detailsFile)) {
            fw.write(detailsJson);
        }
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        cat.loadCatalogDetailsFromFileIfRequired(false);
        // getCatalogDetailsLastModifedDate should now return a real date
        assertNotNull("ic22: details lastModified not null",
                cat.getCatalogDetailsLastModifedDate());
    }

    // ic23 — mergeWithCatalog adds new item from incoming catalog
    @Test
    public void ic23_mergeWithCatalog_addsNewItem() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        JSONParser parser = new JSONParser();
        JSONObject newCatalog = (JSONObject) parser.parse(CATALOG_WITH_TWO_ITEMS);
        cat.mergeWithCatalog(newCatalog);
        // After merge the catalogue should have 2 items
        assertEquals("ic23: two items after merge", 2, cat.getNumItems());
    }

    // ic24 — mergeWithCatalog version mismatch returns false without crash
    @Test
    public void ic24_mergeWithCatalog_versionMismatch() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        JSONParser parser = new JSONParser();
        JSONObject mismatch = (JSONObject) parser.parse(
            "{\"version\":\"9.9\",\"items\":{}}");
        boolean result = cat.mergeWithCatalog(mismatch);
        assertFalse("ic24: version mismatch returns false", result);
    }

    // ic25 — addEventListener / removeEventListener round-trip
    @Test
    public void ic25_eventListeners_addAndRemove() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        IndexCatalogue.EventListener l = new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) {}
            @Override public void onCatalogueDetailsUpdated(int error) {}
        };
        cat.addEventListener(l);
        cat.addEventListener(l); // duplicate add is a no-op
        cat.removeEventListener(l);
        // No assertion needed — just verify no exception
    }

    // ic26 — getNumItems returns 0 when catalogue has no items
    @Test
    public void ic26_getNumItems_emptyItems() throws Exception {
        String emptyJson =
            "{\"version\":\"1.0\"," +
            "\"lastModified\":\"2026-05-14T00:00:00.000Z\"," +
            "\"lastSyncDate\":\"2026-05-14T00:00:00.000Z\"," +
            "\"baseUrl\":\"http://example.com\"," +
            "\"items\":{}}";
        IndexCatalogue cat = buildCatalogue(emptyJson);
        assertEquals("ic26: zero items", 0, cat.getNumItems());
    }

    // ic27 — notifyCatalogueUpdate directly with registered listener
    @Test
    public void ic27_notifyCatalogueUpdate_callsListener() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        final int[] called = {0};
        IndexCatalogue.EventListener l = new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) { called[0]++; }
            @Override public void onCatalogueDetailsUpdated(int error) {}
        };
        cat.addEventListener(l);
        cat.notifyCatalogueUpdate(0);
        assertEquals("ic27: listener called once", 1, called[0]);
    }

    // ic28 — notifyCatalogueDetailsUpdate directly with registered listener
    @Test
    public void ic28_notifyCatalogueDetailsUpdate_callsListener() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        final int[] called = {0};
        IndexCatalogue.EventListener l = new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) {}
            @Override public void onCatalogueDetailsUpdated(int error) { called[0]++; }
        };
        cat.addEventListener(l);
        cat.notifyCatalogueDetailsUpdate(0);
        assertEquals("ic28: details listener called once", 1, called[0]);
    }

    // ic29 — getAllIncludedFiles with zero items calls callback with error
    @Test
    public void ic29_getAllIncludedFiles_emptyItems_callsError() throws Exception {
        String emptyJson =
            "{\"version\":\"1.0\"," +
            "\"lastModified\":\"2026-05-14T00:00:00.000Z\"," +
            "\"lastSyncDate\":\"2026-05-14T00:00:00.000Z\"," +
            "\"baseUrl\":\"http://example.com\"," +
            "\"items\":{}}";
        IndexCatalogue cat = buildCatalogue(emptyJson);
        final int[] errorCode = {-1};
        cat.getAllIncludedFiles(new IndexCatalogue.ItemDetailsCallback() {
            @Override
            public void onDataArrived(String data, int error) {
                errorCode[0] = error;
            }
        });
        assertEquals("ic29: empty items callback with error=1", 1, errorCode[0]);
    }

    // ic30 — mergeWithCatalog removes item not in new catalog (triggers bNeedsIndexOptimization)
    @Test
    public void ic30_mergeWithCatalog_removesObsoleteItem() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_WITH_TWO_ITEMS);
        // New catalog only has one item — second item should be removed
        JSONParser parser = new JSONParser();
        JSONObject newCatalog = (JSONObject) parser.parse(CATALOG_JSON);
        cat.mergeWithCatalog(newCatalog);
        // rAmAyaNa remains but mahAbhArata is removed
        assertEquals("ic30: one item after removing obsolete", 1, cat.getNumItems());
    }

    // ic31 — getCatalogDetailsLastModifedDate returns Date(0) when no details loaded
    @Test
    public void ic31_catalogDetailsLastModified_noDetails() throws Exception {
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        // No details file loaded
        assertNotNull("ic31: returns a date (not null)", cat.getCatalogDetailsLastModifedDate());
    }

    // ic32 — syncCatalogueAdditionalDetailsFromRemoteIfRequired when dates match
    @Test
    public void ic32_syncAdditionalDetails_dateMatchSkipsDownload() throws Exception {
        // Create details file with same lastModified as catalog
        String detailsJson =
            "{\"lastModified\":\"2026-05-01T00:00:00.000Z\"," +
            "\"items\":{\"rAmAyaNa\":{\"files\":\"/rAmAyaNa/\"}}}";
        File detailsFile = new File(catalogFolder,
                IndexCatalogue.INDEX_CATALOG_DETAILS_FILE_NAME);
        try (FileWriter fw = new FileWriter(detailsFile)) {
            fw.write(detailsJson);
        }
        IndexCatalogue cat = buildCatalogue(CATALOG_JSON);
        cat.loadCatalogDetailsFromFileIfRequired(false);
        final int[] called = {0};
        // When dates match, notifyCatalogueDetailsUpdate(0) is called immediately
        IndexCatalogue.EventListener l = new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) {}
            @Override public void onCatalogueDetailsUpdated(int error) { called[0]++; }
        };
        // syncCatalogueAdditionalDetailsFromRemoteIfRequired adds the listener and
        // calls notifyCatalogueDetailsUpdate immediately when dates match
        cat.syncCatalogueAdditionalDetailsFromRemoteIfRequired(l);
        assertEquals("ic32: callback called immediately when dates match", 1, called[0]);
    }
}
