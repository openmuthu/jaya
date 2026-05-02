package org.jaya.android;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TOCTreeBuilder}.
 *
 * Uses the identity {@link TOCTreeBuilder.LabelConverter} so every assertion
 * is on raw ITRANS segment names — no Android runtime required.
 *
 * Scenarios covered
 * -----------------
 * UC-T1  Empty path set             → root has no children
 * UC-T2  Single-level (no folder)   → leaf at depth 0
 * UC-T3  Two-level path             → folder (depth 0) + leaf (depth 1)
 * UC-T4  Multiple files same folder → grouped under one folder node
 * UC-T5  Different folders          → separate top-level folder nodes
 * UC-T6  Three-level nesting        → three nested nodes
 * UC-T7  File without extension     → display label equals segment
 * UC-T8  itransPath preserved       → full original path stored on leaf
 * UC-T9  LabelConverter called      → invoked for every segment
 * UC-T10 sortTree folders-first     → folders appear before leaves
 * UC-T11 sortTree alphabetical folders → folders sorted A→Z
 * UC-T12 sortTree alphabetical leaves  → leaves within a folder sorted A→Z
 * UC-T13 sortTree recursive         → nested folders also sorted
 * UC-T14 flattenVisible all collapsed → only top-level nodes returned
 * UC-T15 flattenVisible expand one  → folder + its children appear
 * UC-T16 flattenVisible collapse    → children disappear after collapse
 * UC-T17 flattenVisible nested expand → grandchildren appear when both levels open
 * UC-T18 isLeaf / depth / removeExtension helpers
 */
public class TOCTreeBuilderTest {

    private static final TOCTreeBuilder.LabelConverter ID = TOCTreeBuilder.IDENTITY;

    // ─── helpers ─────────────────────────────────────────────────────────────

    private static Set<String> paths(String... items) {
        return new HashSet<String>(Arrays.asList(items));
    }

    private static TableOfContentsActivity.TreeNode folder(String label, int depth) {
        return new TableOfContentsActivity.TreeNode(label, label, null, depth);
    }

    private static TableOfContentsActivity.TreeNode leaf(String path, int depth) {
        return new TableOfContentsActivity.TreeNode("seg", "seg", path, depth);
    }

    // ─── UC-T0: leading slash in paths (real device metadata format) ──────────

    /**
     * Paths stored in .jaya-index-md.txt start with '/' (e.g. "/AgamAH/file.txt").
     * A leading slash must NOT create a phantom blank folder at the top of the tree.
     */
    @Test
    public void buildTree_leadingSlashPaths_noPhantomTopLevelFolder() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("/AgamAH/prakAshasaMhitA.txt",
                      "/AgamAH/hayashIrShapaMcarAtram.txt",
                      "/purANa/brahmaPurANa.txt"), ID);

        // Must have exactly 2 real folders, not a blank phantom folder
        assertEquals("Should have 2 top-level folders", 2, root.children.size());
        for (TableOfContentsActivity.TreeNode child : root.children) {
            assertFalse("No child label should be empty", child.displayLabel.isEmpty());
            assertFalse("Top-level nodes must be folders", child.isLeaf());
        }
    }

    @Test
    public void buildTree_leadingSlashPaths_itransPathPreservedWithSlash() {
        // The original path (with slash) must be kept on the leaf so document
        // lookup in the Lucene index continues to work correctly.
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("/AgamAH/prakAshasaMhitA.txt"), ID);

        TableOfContentsActivity.TreeNode leaf =
                root.children.get(0).children.get(0);
        assertEquals("/AgamAH/prakAshasaMhitA.txt", leaf.itransPath);
    }

    // ─── UC-T1: empty path set ────────────────────────────────────────────────

    @Test
    public void buildTree_emptyPathSet_rootHasNoChildren() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(paths(), ID);
        assertNotNull(root);
        assertTrue("Root should have no children for empty input", root.children.isEmpty());
    }

    // ─── UC-T2: single-level (no folder) ─────────────────────────────────────

    @Test
    public void buildTree_singleLevelPath_createsLeafAtDepthZero() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(paths("text.txt"), ID);

        assertEquals(1, root.children.size());
        TableOfContentsActivity.TreeNode leaf = root.children.get(0);
        assertTrue("Single segment should be a leaf", leaf.isLeaf());
        assertEquals("Leaf depth should be 0", 0, leaf.depth);
        assertEquals("Extension must be stripped from display label", "text", leaf.displayLabel);
        assertEquals("itransPath must be the full original path", "text.txt", leaf.itransPath);
    }

    // ─── UC-T3: two-level path ────────────────────────────────────────────────

    @Test
    public void buildTree_twoLevelPath_createsFolderWithLeafChild() {
        TableOfContentsActivity.TreeNode root =
                TOCTreeBuilder.buildTree(paths("AgamAH/text.txt"), ID);

        assertEquals(1, root.children.size());

        TableOfContentsActivity.TreeNode folderNode = root.children.get(0);
        assertFalse("Intermediate segment must be a folder", folderNode.isLeaf());
        assertEquals(0, folderNode.depth);
        assertEquals("AgamAH", folderNode.displayLabel);
        assertEquals("AgamAH", folderNode.itransSegment);

        assertEquals(1, folderNode.children.size());
        TableOfContentsActivity.TreeNode leafNode = folderNode.children.get(0);
        assertTrue("Final segment must be a leaf", leafNode.isLeaf());
        assertEquals(1, leafNode.depth);
        assertEquals("text", leafNode.displayLabel);
        assertEquals("AgamAH/text.txt", leafNode.itransPath);
    }

    // ─── UC-T4: multiple files in same folder grouped ─────────────────────────

    @Test
    public void buildTree_multipleFilesInSameFolder_groupedUnderOneFolderNode() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("AgamAH/a.txt", "AgamAH/b.txt", "AgamAH/c.txt"), ID);

        assertEquals("All files share one folder node", 1, root.children.size());
        TableOfContentsActivity.TreeNode folderNode = root.children.get(0);
        assertFalse(folderNode.isLeaf());
        assertEquals(3, folderNode.children.size());
        for (TableOfContentsActivity.TreeNode child : folderNode.children) {
            assertTrue("Each child must be a leaf", child.isLeaf());
        }
    }

    // ─── UC-T5: different folders create separate top-level nodes ─────────────

    @Test
    public void buildTree_differentFolders_createsSeparateFolderNodes() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("A/x.txt", "B/y.txt", "C/z.txt"), ID);

        assertEquals(3, root.children.size());
        for (TableOfContentsActivity.TreeNode child : root.children) {
            assertFalse("Every top-level child must be a folder", child.isLeaf());
            assertEquals("Each folder must have exactly one leaf", 1, child.children.size());
        }
    }

    // ─── UC-T6: three-level nesting ───────────────────────────────────────────

    @Test
    public void buildTree_threeLevelPath_createsThreeLevelsOfNesting() {
        TableOfContentsActivity.TreeNode root =
                TOCTreeBuilder.buildTree(paths("a/b/leaf.txt"), ID);

        TableOfContentsActivity.TreeNode level0 = root.children.get(0);
        assertFalse(level0.isLeaf());
        assertEquals(0, level0.depth);
        assertEquals("a", level0.displayLabel);

        TableOfContentsActivity.TreeNode level1 = level0.children.get(0);
        assertFalse(level1.isLeaf());
        assertEquals(1, level1.depth);
        assertEquals("b", level1.displayLabel);

        TableOfContentsActivity.TreeNode level2 = level1.children.get(0);
        assertTrue(level2.isLeaf());
        assertEquals(2, level2.depth);
        assertEquals("leaf", level2.displayLabel);
        assertEquals("a/b/leaf.txt", level2.itransPath);
    }

    // ─── UC-T7: file without extension ────────────────────────────────────────

    @Test
    public void buildTree_fileWithNoExtension_labelEqualsSegmentName() {
        TableOfContentsActivity.TreeNode root =
                TOCTreeBuilder.buildTree(paths("noext"), ID);
        assertEquals("noext", root.children.get(0).displayLabel);
    }

    // ─── UC-T8: itransPath preserved exactly ──────────────────────────────────

    @Test
    public void buildTree_itransPath_isPreservedExactlyOnLeaf() {
        String path = "purANa/brahma-purANa.txt";
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(paths(path), ID);
        TableOfContentsActivity.TreeNode leaf = root.children.get(0).children.get(0);
        assertEquals(path, leaf.itransPath);
    }

    // ─── UC-T9: LabelConverter is called for every segment ───────────────────

    @Test
    public void buildTree_labelConverter_isCalledForFolderAndFileSegments() {
        final StringBuilder calls = new StringBuilder();
        TOCTreeBuilder.LabelConverter recorder = new TOCTreeBuilder.LabelConverter() {
            @Override
            public String toDisplayLabel(String s) {
                calls.append('[').append(s).append(']');
                return s;
            }
        };
        TOCTreeBuilder.buildTree(paths("myFolder/myFile.txt"), recorder);

        String log = calls.toString();
        assertTrue("Folder segment must be converted: " + log, log.contains("[myFolder]"));
        assertTrue("Stripped file segment must be converted: " + log, log.contains("[myFile]"));
    }

    // ─── UC-T10: sortTree — folders before leaves ─────────────────────────────

    @Test
    public void sortTree_foldersAlwaysBeforeLeaves() {
        // z_leaf sorts after folders alphabetically but must appear last
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("z_leaf.txt", "A/child.txt", "B/child.txt"), ID);
        TOCTreeBuilder.sortTree(root);

        assertFalse("Position 0 must be a folder", root.children.get(0).isLeaf());
        assertFalse("Position 1 must be a folder", root.children.get(1).isLeaf());
        assertTrue("Position 2 must be the leaf", root.children.get(2).isLeaf());
    }

    // ─── UC-T11: sortTree — alphabetical within folder nodes ─────────────────

    @Test
    public void sortTree_folderNodesSortedAlphabetically() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("C/x.txt", "A/x.txt", "B/x.txt"), ID);
        TOCTreeBuilder.sortTree(root);

        assertEquals("A", root.children.get(0).displayLabel);
        assertEquals("B", root.children.get(1).displayLabel);
        assertEquals("C", root.children.get(2).displayLabel);
    }

    // ─── UC-T12: sortTree — alphabetical within leaf nodes ───────────────────

    @Test
    public void sortTree_leafNodesWithinFolderSortedAlphabetically() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("folder/c.txt", "folder/a.txt", "folder/b.txt"), ID);
        TOCTreeBuilder.sortTree(root);

        List<TableOfContentsActivity.TreeNode> kids = root.children.get(0).children;
        assertEquals("a", kids.get(0).displayLabel);
        assertEquals("b", kids.get(1).displayLabel);
        assertEquals("c", kids.get(2).displayLabel);
    }

    // ─── UC-T13: sortTree — recursive on nested folders ──────────────────────

    @Test
    public void sortTree_recursivelyAppliesToNestedFolders() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("parent/z_sub/x.txt", "parent/a_sub/x.txt"), ID);
        TOCTreeBuilder.sortTree(root);

        List<TableOfContentsActivity.TreeNode> subFolders = root.children.get(0).children;
        assertEquals("a_sub", subFolders.get(0).displayLabel);
        assertEquals("z_sub", subFolders.get(1).displayLabel);
    }

    // ─── UC-T14a: sortTree — case-insensitive folder order ───────────────────
    //
    // Regression test for String.compareTo (case-sensitive) being used instead
    // of compareToIgnoreCase.  In ASCII, 'A'(65) < 'a'(97), so case-sensitive
    // sort placed every uppercase-initial segment above every lowercase-initial
    // one.  For example "AgamAH" sorted before "advaitam" even though 'd' < 'g'.
    //
    // Real category names from the app index:
    //   "AgamAH"   — uppercase A  (long 'aa' in ITRANS)
    //   "advaitam" — lowercase a  (short 'a' in ITRANS)
    //   "mAdhva"   — lowercase m
    //
    // Case-insensitive expected order: advaitam < AgamAH < mAdhva
    //   because ignore-case: "advaitam"[1]='d' < "agamah"[1]='g' < "madhva"[0]='m'

    @Test
    public void sortTree_mixedCaseFolders_sortsCaseInsensitively() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("AgamAH/x.txt", "advaitam/x.txt", "mAdhva/x.txt"), ID);
        TOCTreeBuilder.sortTree(root);

        assertEquals("advaitam should come first (ignore-case: ad < ag)",
                "advaitam", root.children.get(0).displayLabel);
        assertEquals("AgamAH should come second (ignore-case: ag < ma)",
                "AgamAH",   root.children.get(1).displayLabel);
        assertEquals("mAdhva should come last",
                "mAdhva",   root.children.get(2).displayLabel);
    }

    // ─── UC-T14b: sortTree — case-insensitive leaf order within a folder ──────

    @Test
    public void sortTree_mixedCaseLeaves_sortsCaseInsensitively() {
        // "prakAshasaMhitA" starts with lowercase 'p'
        // "Hayagriva"       starts with uppercase 'H'
        // "zrutaprakAshikA" starts with lowercase 'z'
        // Case-sensitive wrong order: H < p < z  (uppercase first)
        // Case-insensitive correct order: H < p < z  — same here because H < p < z
        // Use a clearer example: "Brahma", "agnipurANa", "Vishnu"
        // Case-sensitive: B(66) < V(86) < a(97) → Brahma, Vishnu, agnipurANa
        // Case-insensitive: a < b < v           → agnipurANa, Brahma, Vishnu
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("purANa/Brahma.txt", "purANa/agnipurANa.txt", "purANa/Vishnu.txt"), ID);
        TOCTreeBuilder.sortTree(root);

        List<TableOfContentsActivity.TreeNode> leaves = root.children.get(0).children;
        assertEquals("agnipurANa should be first (ignore-case: a < b < v)",
                "agnipurANa", leaves.get(0).displayLabel);
        assertEquals("Brahma", leaves.get(1).displayLabel);
        assertEquals("Vishnu", leaves.get(2).displayLabel);
    }

    // ─── UC-T14: flattenVisible — all collapsed ───────────────────────────────

    @Test
    public void flattenVisible_allCollapsed_showsOnlyTopLevelNodes() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("A/x.txt", "A/y.txt", "B/z.txt"), ID);

        List<TableOfContentsActivity.TreeNode> visible = TOCTreeBuilder.flattenVisible(root);

        assertEquals("Only top-level folders visible when all collapsed", 2, visible.size());
        for (TableOfContentsActivity.TreeNode n : visible) {
            assertFalse("Top-level nodes should be folders", n.isLeaf());
        }
    }

    // ─── UC-T15: flattenVisible — expand one folder ───────────────────────────

    @Test
    public void flattenVisible_expandOneFolder_showsFolderAndItsChildren() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("A/x.txt", "A/y.txt", "B/z.txt"), ID);
        TOCTreeBuilder.sortTree(root);

        // Expand folder "A" (position 0 after sort)
        root.children.get(0).expanded = true;

        List<TableOfContentsActivity.TreeNode> visible = TOCTreeBuilder.flattenVisible(root);
        // A (expanded) + x + y + B (collapsed) = 4
        assertEquals(4, visible.size());
        assertFalse("Position 0: expanded folder A", visible.get(0).isLeaf());
        assertTrue("Position 1: child leaf", visible.get(1).isLeaf());
        assertTrue("Position 2: child leaf", visible.get(2).isLeaf());
        assertFalse("Position 3: collapsed folder B", visible.get(3).isLeaf());
    }

    // ─── UC-T16: flattenVisible — collapse hides children ────────────────────

    @Test
    public void flattenVisible_afterCollapse_childrenDisappearFromList() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("A/x.txt", "A/y.txt"), ID);
        TableOfContentsActivity.TreeNode folderA = root.children.get(0);

        folderA.expanded = true;
        assertEquals("Expanded: folder + 2 children", 3, TOCTreeBuilder.flattenVisible(root).size());

        folderA.expanded = false;
        assertEquals("Collapsed: only the folder", 1, TOCTreeBuilder.flattenVisible(root).size());
    }

    // ─── UC-T17: flattenVisible — nested expand ───────────────────────────────

    @Test
    public void flattenVisible_nestedExpand_showsGrandchildren() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("A/B/leaf.txt"), ID);
        TableOfContentsActivity.TreeNode nodeA = root.children.get(0);
        TableOfContentsActivity.TreeNode nodeB = nodeA.children.get(0);

        // Only A expanded → A + B (B's child hidden)
        nodeA.expanded = true;
        assertEquals(2, TOCTreeBuilder.flattenVisible(root).size());

        // Both A and B expanded → A + B + leaf
        nodeB.expanded = true;
        assertEquals(3, TOCTreeBuilder.flattenVisible(root).size());

        // Collapse A → hides B and leaf even though B.expanded=true
        nodeA.expanded = false;
        assertEquals(1, TOCTreeBuilder.flattenVisible(root).size());
    }

    // ─── UC-T18: helpers ──────────────────────────────────────────────────────

    @Test
    public void treeNode_folderNode_isNotLeaf() {
        TableOfContentsActivity.TreeNode f = folder("seg", 0);
        assertFalse(f.isLeaf());
    }

    @Test
    public void treeNode_leafNode_isLeaf() {
        TableOfContentsActivity.TreeNode l = leaf("folder/file.txt", 1);
        assertTrue(l.isLeaf());
    }

    @Test
    public void removeExtension_removesLastDotSuffix() {
        assertEquals("file", TOCTreeBuilder.removeExtension("file.txt"));
        assertEquals("archive.tar", TOCTreeBuilder.removeExtension("archive.tar.gz"));
        assertEquals("noext", TOCTreeBuilder.removeExtension("noext"));
        assertEquals("", TOCTreeBuilder.removeExtension(".hidden"));
    }

    // ─── UC-T19: folderPath computation ──────────────────────────────────────
    //
    // folderPath is the slash-terminated Lucene path prefix for a folder node,
    // used to scope "search within a category".  Root gets "/", top-level folders
    // get "/<seg>/", deeper folders extend the parent's folderPath.
    // Leaf nodes have folderPath == null.

    @Test
    public void buildTree_topLevelFolder_hasFolderPathSlashSegSlash() {
        TableOfContentsActivity.TreeNode root =
                TOCTreeBuilder.buildTree(paths("AgamAH/file.txt"), ID);

        TableOfContentsActivity.TreeNode folder = root.children.get(0);
        assertFalse("Must be a folder", folder.isLeaf());
        assertEquals("/AgamAH/", folder.folderPath);
    }

    @Test
    public void buildTree_nestedFolder_hasFolderPathWithFullAncestors() {
        TableOfContentsActivity.TreeNode root =
                TOCTreeBuilder.buildTree(paths("mAdhva/sarvamUla/file.txt"), ID);

        TableOfContentsActivity.TreeNode topFolder = root.children.get(0);
        TableOfContentsActivity.TreeNode nestedFolder = topFolder.children.get(0);
        assertFalse(nestedFolder.isLeaf());
        assertEquals("/mAdhva/sarvamUla/", nestedFolder.folderPath);
    }

    @Test
    public void buildTree_leafNode_hasFolderPathNull() {
        TableOfContentsActivity.TreeNode root =
                TOCTreeBuilder.buildTree(paths("AgamAH/file.txt"), ID);

        TableOfContentsActivity.TreeNode leafNode =
                root.children.get(0).children.get(0);
        assertTrue("Must be a leaf", leafNode.isLeaf());
        assertNull("Leaf folderPath must be null", leafNode.folderPath);
    }

    @Test
    public void buildTree_leadingSlashPaths_folderPathStartsWithSlash() {
        // Paths stored on device have a leading slash: "/AgamAH/file.txt"
        TableOfContentsActivity.TreeNode root =
                TOCTreeBuilder.buildTree(paths("/AgamAH/file.txt"), ID);

        TableOfContentsActivity.TreeNode folder = root.children.get(0);
        assertEquals("Folder path must start with '/'", "/AgamAH/", folder.folderPath);
    }

    @Test
    public void buildTree_multipleFoldersShareNoFolderPath() {
        TableOfContentsActivity.TreeNode root = TOCTreeBuilder.buildTree(
                paths("A/x.txt", "B/y.txt"), ID);
        TOCTreeBuilder.sortTree(root);

        assertEquals("/A/", root.children.get(0).folderPath);
        assertEquals("/B/", root.children.get(1).folderPath);
    }
}
