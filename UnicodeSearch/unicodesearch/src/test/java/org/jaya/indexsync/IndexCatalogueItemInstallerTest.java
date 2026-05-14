package org.jaya.indexsync;

import org.jaya.search.JayaIndexMetadata;
import org.jaya.search.index.LuceneUnicodeFileIndexer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link IndexCatalogueItemInstaller#unistallItem}.
 *
 * Key invariants verified:
 *  - The callback is ALWAYS called (no spinner-forever regression).
 *  - Only files under "/<itemName>/" are removed; unrelated paths survive.
 *  - The prefix includes the leading '/' that matches how paths are stored
 *    in the Lucene index (addFilesToIndex strips FILES_TO_INDEX_DIRECTORY
 *    without a trailing slash, leaving a leading separator).
 */
public class IndexCatalogueItemInstallerTest {

    private static final String ITEM_NAME = "dAsasAhitya";
    private static final int CALLBACK_TIMEOUT_SECONDS = 10;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File indexDir;
    private IndexCatalogue catalogue;
    private IndexCatalogue.Item item;

    @Before
    public void setUp() throws Exception {
        indexDir = tmp.newFolder("index");

        // Build a real Lucene index with documents under two prefixes:
        //   /<ITEM_NAME>/... — should be deleted on uninstall
        //   /other/...       — should survive
        LuceneUnicodeFileIndexer indexer = new LuceneUnicodeFileIndexer(indexDir.getAbsolutePath());
        indexer.addDocumentWithPath("/" + ITEM_NAME + "/saint1/kIrtane/1-file.txt", "content 1");
        indexer.addDocumentWithPath("/" + ITEM_NAME + "/saint1/kIrtane/2-file.txt", "content 2");
        indexer.addDocumentWithPath("/" + ITEM_NAME + "/saint2/ugAbhOga/1-file.txt", "content 3");
        indexer.addDocumentWithPath("/other/text.txt", "other content");
        indexer.close();

        // Minimal IndexCatalogue — we only need item.getName() and item.setIsInstalled()
        // to work. The catalogue JSON is absent so setIsInstalled silently no-ops,
        // which is fine: unistallItem's finally block always fires the callback.
        catalogue = new IndexCatalogue();
        item = catalogue.new Item(catalogue, ITEM_NAME);
    }

    // ui01 — callback is called exactly once regardless of index state
    @Test
    public void ui01_uninstall_callbackAlwaysCalled() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        int[] errorHolder = {-1};

        IndexCatalogueItemInstaller.getInstance().unistallItem(
                item, indexDir.getAbsolutePath(),
                new IndexCatalogueItemInstaller.OnUninstalledCallback() {
                    @Override
                    public void onUninstalled(int error, IndexCatalogue.Item item) {
                        errorHolder[0] = error;
                        latch.countDown();
                    }
                });

        assertTrue("ui01: callback must fire within " + CALLBACK_TIMEOUT_SECONDS + "s",
                latch.await(CALLBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("ui01: error code must be 0 on success", 0, errorHolder[0]);
    }

    // ui02 — all files under the item's prefix are removed from the index
    @Test
    public void ui02_uninstall_removesItemPaths() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        IndexCatalogueItemInstaller.getInstance().unistallItem(
                item, indexDir.getAbsolutePath(),
                new IndexCatalogueItemInstaller.OnUninstalledCallback() {
                    @Override
                    public void onUninstalled(int error, IndexCatalogue.Item item) {
                        latch.countDown();
                    }
                });

        latch.await(CALLBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        Set<String> remaining = new JayaIndexMetadata(indexDir.getAbsolutePath()).getIndexedFilePathSet();
        assertFalse("ui02a: saint1/kIrtane/1 must be removed",
                remaining.contains("/" + ITEM_NAME + "/saint1/kIrtane/1-file.txt"));
        assertFalse("ui02b: saint1/kIrtane/2 must be removed",
                remaining.contains("/" + ITEM_NAME + "/saint1/kIrtane/2-file.txt"));
        assertFalse("ui02c: saint2/ugAbhOga/1 must be removed",
                remaining.contains("/" + ITEM_NAME + "/saint2/ugAbhOga/1-file.txt"));
    }

    // ui03 — files outside the item's prefix are not touched
    @Test
    public void ui03_uninstall_retainsUnrelatedPaths() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        IndexCatalogueItemInstaller.getInstance().unistallItem(
                item, indexDir.getAbsolutePath(),
                new IndexCatalogueItemInstaller.OnUninstalledCallback() {
                    @Override
                    public void onUninstalled(int error, IndexCatalogue.Item item) {
                        latch.countDown();
                    }
                });

        latch.await(CALLBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        Set<String> remaining = new JayaIndexMetadata(indexDir.getAbsolutePath()).getIndexedFilePathSet();
        assertTrue("ui03: /other/text.txt must survive uninstall of " + ITEM_NAME,
                remaining.contains("/other/text.txt"));
    }

    // ui04 — callback fires even when the index directory does not exist
    @Test
    public void ui04_uninstall_callbackFiredWhenIndexMissing() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        int[] errorHolder = {-1};

        IndexCatalogueItemInstaller.getInstance().unistallItem(
                item, "/nonexistent/path/that/does/not/exist",
                new IndexCatalogueItemInstaller.OnUninstalledCallback() {
                    @Override
                    public void onUninstalled(int error, IndexCatalogue.Item item) {
                        errorHolder[0] = error;
                        latch.countDown();
                    }
                });

        assertTrue("ui04: callback must fire even on bad index path",
                latch.await(CALLBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("ui04: error must be non-zero for bad index path", 1, errorHolder[0]);
    }
}
