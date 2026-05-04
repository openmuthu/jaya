package org.jaya.annotation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive tests for bookmark grouping functionality.
 *
 * All tests use TemporaryFolder so they leave no permanent files on disk.
 * AnnotationManager.addAnnotationDirect() is used instead of ResultDocument
 * to avoid a Lucene index dependency.
 */
public class BookmarkGroupTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private AnnotationManager mgr;
    private File dataFile;

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private AnnotationManager newManager() throws Exception {
        return new AnnotationManager(null, dataFile.getAbsolutePath());
    }

    private Annotation addBookmark(String docPath, String docLocalId, String name) {
        return mgr.addAnnotationDirect(docPath, docLocalId, name);
    }

    /** Write an empty-but-valid v1.0 JSON file so AnnotationManager can parse it. */
    private void writeEmptyFile() throws Exception {
        try (FileWriter fw = new FileWriter(dataFile)) {
            fw.write("{\"version\":\"1.0\",\"items\":[]}");
        }
    }

    @Before
    public void setUp() throws Exception {
        dataFile = tmp.newFile("bookmarks.json");
        writeEmptyFile();
        mgr = newManager();
    }

    // ===========================================================================
    // BookmarkGroup model tests
    // ===========================================================================

    @Test
    public void bg01_createNewGroupHasUniqueId() {
        BookmarkGroup g1 = BookmarkGroup.createNew("Favorites");
        BookmarkGroup g2 = BookmarkGroup.createNew("Reading List");
        assertNotNull(g1.getId());
        assertNotNull(g2.getId());
        assertNotEquals(g1.getId(), g2.getId());
    }

    @Test
    public void bg02_groupNameIsStoredAndRetrievable() {
        BookmarkGroup g = BookmarkGroup.createNew("Favorites");
        assertEquals("Favorites", g.getName());
    }

    @Test
    public void bg03_groupNameCanBeChanged() {
        BookmarkGroup g = BookmarkGroup.createNew("Favorites");
        g.setName("New Name");
        assertEquals("New Name", g.getName());
    }

    @Test
    public void bg04_groupCreatedDateIsSet() {
        BookmarkGroup g = BookmarkGroup.createNew("Test");
        assertNotNull(g.getCreatedDate());
    }

    // ===========================================================================
    // Annotation group membership tests
    // ===========================================================================

    @Test
    public void am01_newAnnotationHasNoGroups() {
        Annotation a = addBookmark("/path/doc.txt", "id1", "my bookmark");
        assertTrue(a.getGroupIds().isEmpty());
    }

    @Test
    public void am02_addGroupIdAddsToAnnotation() {
        Annotation a = addBookmark("/path/doc.txt", "id1", "my bookmark");
        a.addGroupId("gid-1");
        assertTrue(a.isInGroup("gid-1"));
    }

    @Test
    public void am03_addDuplicateGroupIdIsNoop() {
        Annotation a = addBookmark("/path/doc.txt", "id1", "my bookmark");
        a.addGroupId("gid-1");
        a.addGroupId("gid-1");
        assertEquals(1, a.getGroupIds().size());
    }

    @Test
    public void am04_removeGroupIdRemovesFromAnnotation() {
        Annotation a = addBookmark("/path/doc.txt", "id1", "my bookmark");
        a.addGroupId("gid-1");
        a.removeGroupId("gid-1");
        assertFalse(a.isInGroup("gid-1"));
    }

    @Test
    public void am05_setGroupIdsReplacesExisting() {
        Annotation a = addBookmark("/path/doc.txt", "id1", "my bookmark");
        a.addGroupId("gid-old");
        a.setGroupIds(java.util.Arrays.asList("gid-new1", "gid-new2"));
        assertFalse(a.isInGroup("gid-old"));
        assertTrue(a.isInGroup("gid-new1"));
        assertTrue(a.isInGroup("gid-new2"));
    }

    @Test
    public void am06_getGroupIdsReturnsCopy() {
        Annotation a = addBookmark("/path/doc.txt", "id1", "my bookmark");
        a.addGroupId("gid-1");
        List<String> ids = a.getGroupIds();
        ids.clear();                          // mutate the returned list
        assertTrue(a.isInGroup("gid-1"));     // original unchanged
    }

    // ===========================================================================
    // AnnotationManager group CRUD
    // ===========================================================================

    @Test
    public void mg01_addGroupStoresGroup() {
        BookmarkGroup g = mgr.addGroup("Favorites");
        List<BookmarkGroup> groups = mgr.getGroups();
        assertEquals(1, groups.size());
        assertEquals("Favorites", groups.get(0).getName());
        assertEquals(g.getId(), groups.get(0).getId());
    }

    @Test
    public void mg02_multipleGroupsRetainedInOrder() {
        mgr.addGroup("Alpha");
        mgr.addGroup("Beta");
        mgr.addGroup("Gamma");
        List<BookmarkGroup> groups = mgr.getGroups();
        assertEquals(3, groups.size());
        assertEquals("Alpha", groups.get(0).getName());
        assertEquals("Beta",  groups.get(1).getName());
        assertEquals("Gamma", groups.get(2).getName());
    }

    @Test
    public void mg03_renameGroupChangesName() {
        BookmarkGroup g = mgr.addGroup("Old");
        boolean ok = mgr.renameGroup(g.getId(), "New");
        assertTrue(ok);
        assertEquals("New", mgr.getGroups().get(0).getName());
    }

    @Test
    public void mg04_renameNonExistentGroupReturnsFalse() {
        assertFalse(mgr.renameGroup("no-such-id", "X"));
    }

    @Test
    public void mg05_removeGroupDeletesItFromList() {
        BookmarkGroup g = mgr.addGroup("Temp");
        mgr.removeGroup(g.getId());
        assertTrue(mgr.getGroups().isEmpty());
    }

    @Test
    public void mg06_removeGroupAlsoRemovesGroupIdFromAnnotations() {
        BookmarkGroup g = mgr.addGroup("Fav");
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        mgr.assignAnnotationToGroup(a.getKey(), g.getId());
        assertTrue(a.isInGroup(g.getId()));
        mgr.removeGroup(g.getId());
        assertFalse(a.isInGroup(g.getId()));
    }

    @Test
    public void mg07_removeNonExistentGroupReturnsFalse() {
        assertFalse(mgr.removeGroup("ghost-id"));
    }

    // ===========================================================================
    // AnnotationManager assignment methods
    // ===========================================================================

    @Test
    public void as01_assignAnnotationToGroupWorks() {
        BookmarkGroup g = mgr.addGroup("Fav");
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        boolean ok = mgr.assignAnnotationToGroup(a.getKey(), g.getId());
        assertTrue(ok);
        assertTrue(a.isInGroup(g.getId()));
    }

    @Test
    public void as02_assignToUnknownGroupReturnsFalse() {
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        assertFalse(mgr.assignAnnotationToGroup(a.getKey(), "no-group"));
    }

    @Test
    public void as03_assignUnknownAnnotationReturnsFalse() {
        BookmarkGroup g = mgr.addGroup("Fav");
        assertFalse(mgr.assignAnnotationToGroup("no-annotation-key", g.getId()));
    }

    @Test
    public void as04_unassignAnnotationFromGroupWorks() {
        BookmarkGroup g = mgr.addGroup("Fav");
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        mgr.assignAnnotationToGroup(a.getKey(), g.getId());
        mgr.unassignAnnotationFromGroup(a.getKey(), g.getId());
        assertFalse(a.isInGroup(g.getId()));
    }

    @Test
    public void as05_setAnnotationGroupsFiltersUnknownIds() {
        BookmarkGroup g = mgr.addGroup("Known");
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        mgr.setAnnotationGroups(a.getKey(),
                java.util.Arrays.asList(g.getId(), "unknown-id"));
        List<String> ids = a.getGroupIds();
        assertEquals(1, ids.size());
        assertTrue(ids.contains(g.getId()));
    }

    @Test
    public void as06_setAnnotationGroupsReplacesPreviousGroups() {
        BookmarkGroup g1 = mgr.addGroup("G1");
        BookmarkGroup g2 = mgr.addGroup("G2");
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        mgr.assignAnnotationToGroup(a.getKey(), g1.getId());
        mgr.setAnnotationGroups(a.getKey(),
                java.util.Arrays.asList(g2.getId()));
        assertFalse(a.isInGroup(g1.getId()));
        assertTrue(a.isInGroup(g2.getId()));
    }

    @Test
    public void as07_annotationCanBelongToMultipleGroups() {
        BookmarkGroup g1 = mgr.addGroup("Vedas");
        BookmarkGroup g2 = mgr.addGroup("Favorites");
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        mgr.assignAnnotationToGroup(a.getKey(), g1.getId());
        mgr.assignAnnotationToGroup(a.getKey(), g2.getId());
        assertTrue(a.isInGroup(g1.getId()));
        assertTrue(a.isInGroup(g2.getId()));
        assertEquals(2, a.getGroupIds().size());
    }

    // ===========================================================================
    // getAnnotationsForGroup
    // ===========================================================================

    @Test
    public void gf01_getAnnotationsForGroupReturnsOnlyGroupMembers() {
        BookmarkGroup g = mgr.addGroup("G");
        Annotation a1 = addBookmark("/p/d1.txt", "id1", "b1");
        Annotation a2 = addBookmark("/p/d2.txt", "id2", "b2");
        addBookmark("/p/d3.txt", "id3", "b3");          // not in group
        mgr.assignAnnotationToGroup(a1.getKey(), g.getId());
        mgr.assignAnnotationToGroup(a2.getKey(), g.getId());
        List<Annotation> members = mgr.getAnnotationsForGroup(g.getId());
        assertEquals(2, members.size());
        assertTrue(members.contains(a1) || members.stream().anyMatch(x -> x.getKey().equals(a1.getKey())));
        assertTrue(members.stream().anyMatch(x -> x.getKey().equals(a2.getKey())));
    }

    @Test
    public void gf02_getAnnotationsForNonExistentGroupReturnsEmpty() {
        addBookmark("/p/d.txt", "id1", "bk");
        List<Annotation> members = mgr.getAnnotationsForGroup("ghost-id");
        assertTrue(members.isEmpty());
    }

    // ===========================================================================
    // renameAnnotation
    // ===========================================================================

    @Test
    public void ra01_renameAnnotationChangesName() {
        Annotation a = addBookmark("/p/d.txt", "id1", "old-name");
        boolean ok = mgr.renameAnnotation(a.getKey(), "new-name");
        assertTrue(ok);
        assertEquals("new-name", a.getName());
    }

    @Test
    public void ra02_renameNonExistentAnnotationReturnsFalse() {
        assertFalse(mgr.renameAnnotation("no-such-key", "whatever"));
    }

    // ===========================================================================
    // generateGroupName (auto-naming)
    // ===========================================================================

    @Test
    public void gn01_firstGroupIsGroup1() {
        assertEquals("Group 1", mgr.generateGroupName());
    }

    @Test
    public void gn02_afterAddingGroup1NextIsGroup2() {
        mgr.addGroup("Group 1");
        assertEquals("Group 2", mgr.generateGroupName());
    }

    @Test
    public void gn03_skipsExistingNames() {
        mgr.addGroup("Group 1");
        mgr.addGroup("My Stuff");   // custom name occupies slot 2 count but name differs
        // size is 2 so next candidate is "Group 3", but "Group 2" is still free
        // generateGroupName tries n = size+1 = 3, "Group 3" is free → returns "Group 3"
        // Actually generateGroupName starts at size+1=3; "Group 3" is not taken → "Group 3"
        String name = mgr.generateGroupName();
        assertFalse(name.isEmpty());
        assertNotEquals("Group 1", name);
    }

    @Test
    public void gn04_deletedGroupNameBecomesAvailableAgain() {
        BookmarkGroup g1 = mgr.addGroup("Group 1");
        mgr.removeGroup(g1.getId());
        // Now size = 0, generateGroupName → "Group 1"
        assertEquals("Group 1", mgr.generateGroupName());
    }

    // ===========================================================================
    // getGroupNamesString
    // ===========================================================================

    @Test
    public void gs01_noGroupsReturnsEmptyString() {
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        assertEquals("", mgr.getGroupNamesString(a));
    }

    @Test
    public void gs02_oneGroupReturnsGroupName() {
        BookmarkGroup g = mgr.addGroup("Favorites");
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        mgr.assignAnnotationToGroup(a.getKey(), g.getId());
        assertEquals("Favorites", mgr.getGroupNamesString(a));
    }

    @Test
    public void gs03_multipleGroupsReturnsCommaSeparated() {
        BookmarkGroup g1 = mgr.addGroup("Alpha");
        BookmarkGroup g2 = mgr.addGroup("Beta");
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        mgr.assignAnnotationToGroup(a.getKey(), g1.getId());
        mgr.assignAnnotationToGroup(a.getKey(), g2.getId());
        String result = mgr.getGroupNamesString(a);
        assertTrue(result.contains("Alpha"));
        assertTrue(result.contains("Beta"));
        assertTrue(result.contains(","));
    }

    @Test
    public void gs04_nullAnnotationReturnsEmptyString() {
        assertEquals("", mgr.getGroupNamesString(null));
    }

    // ===========================================================================
    // Persistence: save and reload (round-trip)
    // ===========================================================================

    @Test
    public void rt01_groupsPersistedAcrossReload() throws Exception {
        mgr.addGroup("Favorites");
        mgr.addGroup("To Read");
        mgr.saveIfDirty();

        AnnotationManager mgr2 = newManager();
        List<BookmarkGroup> groups = mgr2.getGroups();
        assertEquals(2, groups.size());
        assertEquals("Favorites", groups.get(0).getName());
        assertEquals("To Read",   groups.get(1).getName());
    }

    @Test
    public void rt02_annotationGroupAssignmentsPersistedAcrossReload() throws Exception {
        BookmarkGroup g = mgr.addGroup("Favorites");
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        mgr.assignAnnotationToGroup(a.getKey(), g.getId());
        mgr.saveIfDirty();

        AnnotationManager mgr2 = newManager();
        List<Annotation> anns = mgr2.getAnnotations();
        assertEquals(1, anns.size());
        assertTrue(anns.get(0).isInGroup(g.getId()));
    }

    @Test
    public void rt03_notesPersistedAcrossReload() throws Exception {
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        a.setNotes("important verse");
        mgr.saveIfDirty();

        AnnotationManager mgr2 = newManager();
        assertEquals("important verse", mgr2.getAnnotations().get(0).getNotes());
    }

    @Test
    public void rt04_annotationNameRenamePersistedAcrossReload() throws Exception {
        addBookmark("/p/d.txt", "id1", "original");
        mgr.renameAnnotation("/p/d.txtid1", "updated");
        mgr.saveIfDirty();

        AnnotationManager mgr2 = newManager();
        assertEquals("updated", mgr2.getAnnotations().get(0).getName());
    }

    @Test
    public void rt05_multipleGroupMembershipsPersistedAcrossReload() throws Exception {
        BookmarkGroup g1 = mgr.addGroup("G1");
        BookmarkGroup g2 = mgr.addGroup("G2");
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        mgr.assignAnnotationToGroup(a.getKey(), g1.getId());
        mgr.assignAnnotationToGroup(a.getKey(), g2.getId());
        mgr.saveIfDirty();

        AnnotationManager mgr2 = newManager();
        Annotation reloaded = mgr2.getAnnotations().get(0);
        assertTrue(reloaded.isInGroup(g1.getId()));
        assertTrue(reloaded.isInGroup(g2.getId()));
        assertEquals(2, reloaded.getGroupIds().size());
    }

    @Test
    public void rt06_v1JsonUpgradedToV2OnNextSave() throws Exception {
        // Write a v1.0 file manually with one annotation
        try (FileWriter fw = new FileWriter(dataFile)) {
            fw.write("{\"version\":\"1.0\",\"items\":[" +
                    "{\"docPath\":\"/p/d.txt\",\"docLocalId\":\"id1\"," +
                    "\"updated\":\"2024-01-01T00:00:00.000Z\",\"name\":\"legacy\"}" +
                    "]}");
        }
        AnnotationManager legacyMgr = newManager();
        assertEquals(1, legacyMgr.getAnnotations().size());

        // Add a group and save — should now be v2.0
        legacyMgr.addGroup("New Group");
        legacyMgr.saveIfDirty();

        // Reload and verify
        AnnotationManager mgr2 = newManager();
        assertEquals(1, mgr2.getAnnotations().size());
        assertEquals(1, mgr2.getGroups().size());
        assertEquals("New Group", mgr2.getGroups().get(0).getName());
    }

    // ===========================================================================
    // Edge-cases
    // ===========================================================================

    @Test
    public void ec01_removeGroupThenCheckAnnotationGroupIds() {
        BookmarkGroup g1 = mgr.addGroup("G1");
        BookmarkGroup g2 = mgr.addGroup("G2");
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        mgr.assignAnnotationToGroup(a.getKey(), g1.getId());
        mgr.assignAnnotationToGroup(a.getKey(), g2.getId());
        mgr.removeGroup(g1.getId());
        assertEquals(1, a.getGroupIds().size());
        assertTrue(a.isInGroup(g2.getId()));
    }

    @Test
    public void ec02_setAnnotationGroupsWithEmptyListClearsAll() {
        BookmarkGroup g = mgr.addGroup("G");
        Annotation a = addBookmark("/p/d.txt", "id1", "bk");
        mgr.assignAnnotationToGroup(a.getKey(), g.getId());
        mgr.setAnnotationGroups(a.getKey(), new java.util.ArrayList<String>());
        assertTrue(a.getGroupIds().isEmpty());
    }

    @Test
    public void ec03_getAnnotationsForGroupIsNewestFirst() throws Exception {
        BookmarkGroup g = mgr.addGroup("G");
        Annotation a1 = addBookmark("/p/d1.txt", "id1", "b1");
        // Small sleep to ensure different timestamps
        Thread.sleep(10);
        Annotation a2 = addBookmark("/p/d2.txt", "id2", "b2");
        mgr.assignAnnotationToGroup(a1.getKey(), g.getId());
        mgr.assignAnnotationToGroup(a2.getKey(), g.getId());
        List<Annotation> members = mgr.getAnnotationsForGroup(g.getId());
        // a2 is newer so it should come first
        assertEquals(a2.getKey(), members.get(0).getKey());
    }
}
