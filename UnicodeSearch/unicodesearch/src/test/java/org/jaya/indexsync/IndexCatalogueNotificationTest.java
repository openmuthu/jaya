package org.jaya.indexsync;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for the ConcurrentModificationException fix in
 * {@link IndexCatalogue#notifyCatalogueUpdate} and
 * {@link IndexCatalogue#notifyCatalogueDetailsUpdate}.
 *
 * Root bug: those methods iterated {@code mEventListeners} using a for-each loop.
 * Listeners that call {@code removeEventListener(this)} inside their callback
 * modified the list mid-iteration, throwing ConcurrentModificationException.
 * The exception was caught silently, but any listeners after the one that removed
 * itself were never notified — leaving the Remove spinner up forever.
 *
 * Fix: iterate over a snapshot ({@code new ArrayList<>(mEventListeners)}).
 *
 * These tests use the package-private helpers added for testing:
 *   - {@code new IndexCatalogue()} — creates an isolated instance
 *   - {@code IndexCatalogue.resetStateForTest()} — clears shared static state
 *   - {@code notifyCatalogueUpdate(int)} / {@code notifyCatalogueDetailsUpdate(int)}
 *   - {@code removeEventListener(EventListener)}
 */
public class IndexCatalogueNotificationTest {

    private IndexCatalogue cat;

    @Before
    public void setUp() {
        IndexCatalogue.resetStateForTest();
        cat = new IndexCatalogue();
    }

    @After
    public void tearDown() {
        IndexCatalogue.resetStateForTest();
    }

    // -----------------------------------------------------------------------
    // notifyCatalogueUpdate
    // -----------------------------------------------------------------------

    // cn01 — a listener that removes itself must not throw and must fire
    @Test
    public void cn01_catalogueUpdate_selfRemovingListener_noException() {
        boolean[] fired = {false};
        Exception[] caught = {null};

        IndexCatalogue.EventListener self = new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) {
                fired[0] = true;
                cat.removeEventListener(this);
            }
            @Override public void onCatalogueDetailsUpdated(int error) {}
        };

        cat.addEventListener(self);
        try {
            cat.notifyCatalogueUpdate(0);
        } catch (Exception e) {
            caught[0] = e;
        }

        assertNull("cn01a: no exception must escape notifyCatalogueUpdate", caught[0]);
        assertEquals("cn01b: self-removing listener must have fired", true, fired[0]);
    }

    // cn02 — listeners added AFTER the self-removing one must still be notified
    @Test
    public void cn02_catalogueUpdate_otherListenersNotifiedAfterSelfRemoval() {
        int[] count = {0};

        IndexCatalogue.EventListener selfRemoving = new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) {
                count[0]++;
                cat.removeEventListener(this);
            }
            @Override public void onCatalogueDetailsUpdated(int error) {}
        };

        IndexCatalogue.EventListener normal = new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) { count[0]++; }
            @Override public void onCatalogueDetailsUpdated(int error) {}
        };

        cat.addEventListener(selfRemoving);
        cat.addEventListener(normal);
        cat.notifyCatalogueUpdate(0);

        assertEquals("cn02: both listeners must fire (total count = 2)", 2, count[0]);
    }

    // cn03 — five self-removing listeners all fire
    @Test
    public void cn03_catalogueUpdate_manyListenersEachRemovingThemselves_allFire() {
        int[] count = {0};

        for (int i = 0; i < 5; i++) {
            cat.addEventListener(new IndexCatalogue.EventListener() {
                @Override public void onCatalogueUpdated(int error) {
                    count[0]++;
                    cat.removeEventListener(this);
                }
                @Override public void onCatalogueDetailsUpdated(int error) {}
            });
        }

        cat.notifyCatalogueUpdate(0);

        assertEquals("cn03: all 5 self-removing listeners must fire", 5, count[0]);
    }

    // cn04 — error code is forwarded correctly to all listeners
    @Test
    public void cn04_catalogueUpdate_errorCodePropagatedToAllListeners() {
        int[] received1 = {-1};
        int[] received2 = {-1};

        cat.addEventListener(new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) { received1[0] = error; }
            @Override public void onCatalogueDetailsUpdated(int error) {}
        });
        cat.addEventListener(new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) { received2[0] = error; }
            @Override public void onCatalogueDetailsUpdated(int error) {}
        });

        cat.notifyCatalogueUpdate(42);

        assertEquals("cn04a: listener 1 must receive error 42", 42, received1[0]);
        assertEquals("cn04b: listener 2 must receive error 42", 42, received2[0]);
    }

    // -----------------------------------------------------------------------
    // notifyCatalogueDetailsUpdate — same fix, same scenarios
    // -----------------------------------------------------------------------

    // cn05 — self-removing listener on details notification must not throw and must fire
    @Test
    public void cn05_detailsUpdate_selfRemovingListener_noException() {
        boolean[] fired = {false};
        Exception[] caught = {null};

        IndexCatalogue.EventListener self = new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) {}
            @Override public void onCatalogueDetailsUpdated(int error) {
                fired[0] = true;
                cat.removeEventListener(this);
            }
        };

        cat.addEventListener(self);
        try {
            cat.notifyCatalogueDetailsUpdate(0);
        } catch (Exception e) {
            caught[0] = e;
        }

        assertNull("cn05a: no exception must escape notifyCatalogueDetailsUpdate", caught[0]);
        assertEquals("cn05b: self-removing listener must have fired", true, fired[0]);
    }

    // cn06 — second listener still notified after first removes itself during details update
    @Test
    public void cn06_detailsUpdate_otherListenersNotifiedAfterSelfRemoval() {
        int[] count = {0};

        IndexCatalogue.EventListener selfRemoving = new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) {}
            @Override public void onCatalogueDetailsUpdated(int error) {
                count[0]++;
                cat.removeEventListener(this);
            }
        };

        IndexCatalogue.EventListener normal = new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) {}
            @Override public void onCatalogueDetailsUpdated(int error) { count[0]++; }
        };

        cat.addEventListener(selfRemoving);
        cat.addEventListener(normal);
        cat.notifyCatalogueDetailsUpdate(0);

        assertEquals("cn06: both listeners must fire (total count = 2)", 2, count[0]);
    }

    // cn07 — error code is forwarded correctly to all detail-update listeners
    @Test
    public void cn07_detailsUpdate_errorCodePropagatedToAllListeners() {
        int[] received1 = {-1};
        int[] received2 = {-1};

        cat.addEventListener(new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) {}
            @Override public void onCatalogueDetailsUpdated(int error) { received1[0] = error; }
        });
        cat.addEventListener(new IndexCatalogue.EventListener() {
            @Override public void onCatalogueUpdated(int error) {}
            @Override public void onCatalogueDetailsUpdated(int error) { received2[0] = error; }
        });

        cat.notifyCatalogueDetailsUpdate(7);

        assertEquals("cn07a: listener 1 must receive error 7", 7, received1[0]);
        assertEquals("cn07b: listener 2 must receive error 7", 7, received2[0]);
    }
}
