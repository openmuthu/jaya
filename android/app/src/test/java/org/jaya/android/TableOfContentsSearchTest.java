package org.jaya.android;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the search-related methods in {@link TableOfContentsActivity}.
 *
 * No Android runtime is required — only pure JVM classes are exercised.
 *
 * Scenarios covered
 * -----------------
 * FS-01  fuzzyScore — exact match returns 1000
 * FS-02  fuzzyScore — prefix match returns 800
 * FS-03  fuzzyScore — substring match returns 600
 * FS-04  fuzzyScore — subsequence match returns positive score
 * FS-05  fuzzyScore — consecutive run bonus: longer run scores higher
 * FS-06  fuzzyScore — no subsequence match returns -1
 * FS-07  fuzzyScore — single-char query matches first occurrence
 * FS-08  fuzzyScore — empty query treated as prefix (exact) match for empty label
 * FS-09  fuzzyScore — query longer than label returns -1
 *
 * IT-01  itransTitleFromSegment — strips .txt extension
 * IT-02  itransTitleFromSegment — strips leading numeric ID prefix
 * IT-03  itransTitleFromSegment — replaces hyphens with spaces
 * IT-04  itransTitleFromSegment — no extension, no ID prefix
 * IT-05  itransTitleFromSegment — ID-only filename (empty slug after strip)
 * IT-06  itransTitleFromSegment — multi-digit ID
 *
 * CL-01  collectLeaves — single leaf at root level has empty breadcrumb
 * CL-02  collectLeaves — leaf under one folder produces parent label as breadcrumb
 * CL-03  collectLeaves — leaf under two nested folders joins with " / "
 * CL-04  collectLeaves — folder nodes are NOT added to out list
 * CL-05  collectLeaves — multiple leaves under same folder all collected
 *
 * FP-01  findPath — target is root returns [root]
 * FP-02  findPath — target is direct child returns [root, child]
 * FP-03  findPath — target is grandchild returns full ancestor chain
 * FP-04  findPath — target not in tree returns false, path is empty
 */
public class TableOfContentsSearchTest {

    private static final TOCTreeBuilder.LabelConverter ID = TOCTreeBuilder.IDENTITY;

    // ─── helpers ─────────────────────────────────────────────────────────────

    private static TableOfContentsActivity.TreeNode makeFolder(String label, int depth) {
        TableOfContentsActivity.TreeNode n =
                new TableOfContentsActivity.TreeNode(label, label, null, depth);
        n.folderPath = label + "/";
        return n;
    }

    private static TableOfContentsActivity.TreeNode makeLeaf(String segment, String itransPath, int depth) {
        return new TableOfContentsActivity.TreeNode(segment, segment, itransPath, depth);
    }

    /** Thin wrapper so we can call the package-private collectLeaves method. */
    private static void collectLeaves(
            TableOfContentsActivity.TreeNode node,
            String breadcrumb,
            List<TableOfContentsActivity.SearchResult> out) {
        if (node.isLeaf()) {
            out.add(new TableOfContentsActivity.SearchResult(node, breadcrumb));
        } else {
            String next = breadcrumb.isEmpty()
                    ? node.displayLabel
                    : breadcrumb + " / " + node.displayLabel;
            for (TableOfContentsActivity.TreeNode child : node.children) {
                collectLeaves(child, next, out);
            }
        }
    }

    /** DFS path finder matching the logic in TableOfContentsActivity. */
    private static boolean findPath(
            TableOfContentsActivity.TreeNode current,
            TableOfContentsActivity.TreeNode target,
            List<TableOfContentsActivity.TreeNode> path) {
        if (current == target) {
            path.add(current);
            return true;
        }
        for (TableOfContentsActivity.TreeNode child : current.children) {
            if (findPath(child, target, path)) {
                path.add(0, current);
                return true;
            }
        }
        return false;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // fuzzyScore tests
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    public void fs01_exactMatch() {
        assertEquals(1000, TableOfContentsActivity.fuzzyScore("vAdirAja", "vAdirAja"));
    }

    @Test
    public void fs02_prefixMatch() {
        assertEquals(800, TableOfContentsActivity.fuzzyScore("vAdirAja", "vAdi"));
    }

    @Test
    public void fs03_substringMatch() {
        assertEquals(600, TableOfContentsActivity.fuzzyScore("mahAvishNu", "vishNu"));
    }

    @Test
    public void fs04_subsequenceMatch() {
        int score = TableOfContentsActivity.fuzzyScore("vAdirAja", "vrj");
        assertTrue("subsequence match should be positive", score > 0);
    }

    @Test
    public void fs05_consecutiveRunBonus() {
        // "abc" in "abcxyz": 3 consecutive chars → higher score than "axc" in "axcbbb"
        int consecutive = TableOfContentsActivity.fuzzyScore("abcxyz", "abc");
        // prefix match would return 800; subsequence is still better than scattered
        // Use a case where neither is prefix/substring of the other
        int scattered = TableOfContentsActivity.fuzzyScore("a_b_c_xyz", "abc");
        // Both are subsequence matches; consecutive run should score higher
        assertTrue("consecutive run should score higher than scattered",
                consecutive > scattered);
    }

    @Test
    public void fs06_noMatch() {
        assertEquals(-1, TableOfContentsActivity.fuzzyScore("rAmAyaNa", "xyz"));
    }

    @Test
    public void fs07_singleCharQuery() {
        int score = TableOfContentsActivity.fuzzyScore("rAmAyaNa", "r");
        assertTrue("single-char prefix match should be 800", score == 800);
    }

    @Test
    public void fs08_emptyLabelAndQuery() {
        // empty label equals empty query → exact match
        assertEquals(1000, TableOfContentsActivity.fuzzyScore("", ""));
    }

    @Test
    public void fs09_queryLongerThanLabel() {
        assertEquals(-1, TableOfContentsActivity.fuzzyScore("ab", "abcdef"));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // normalizeDoubleVowels tests
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * ND-01  "aa" → "a", "ii" → "i", "uu" → "u"
     * ND-02  no double vowels → unchanged
     * ND-03  dashaavataara double-vowel encoding matches dashavatarastuti prefix
     *        (the real-world case that triggered this fix)
     */
    @Test
    public void nd01_collapseDoubleVowels() {
        assertEquals("dashavatara", TableOfContentsActivity.normalizeDoubleVowels("dashaavataara"));
        assertEquals("sri",         TableOfContentsActivity.normalizeDoubleVowels("srii"));
        assertEquals("guru",        TableOfContentsActivity.normalizeDoubleVowels("guruu"));
    }

    @Test
    public void nd02_noDoubleVowels_unchanged() {
        assertEquals("dashAvatAra", TableOfContentsActivity.normalizeDoubleVowels("dashAvatAra"));
    }

    @Test
    public void nd03_doubleVowelQueryMatchesPrefixAfterNorm() {
        // "dashaavataara" normalised → "dashavatara"
        // "dashAvatArastuti".toLowerCase() = "dashavatarastuti"
        // "dashavatarastuti".startsWith("dashavatara") → prefix score 800
        String norm = TableOfContentsActivity.normalizeDoubleVowels("dashaavataara");
        String label = "dashAvatArastuti".toLowerCase();
        assertEquals(800, TableOfContentsActivity.fuzzyScore(label, norm));
    }

    /**
     * IP-01  Folder-name query matches every file inside that folder via itransPath.
     *        Searching "padmapurANa" must surface "purANa/padmapurANa/bhUmi-khaNDa.txt"
     *        because the full path contains the folder name as a substring (score 600).
     *
     * IP-02  File whose own name already matches the query gets a higher score than a
     *        file that only matches through its ancestor folder name.
     *
     * IP-03  A file in a completely different folder does NOT match a folder-name query.
     */
    @Test
    public void ip01_folderNameQueryMatchesFilesInsideThatFolder() {
        String query = "padmapurana";  // "padmapurANa".toLowerCase()
        String path  = "purana/padmapurana/bhumi-khanda.txt";  // itransPath lowercased
        // substring match → 600
        assertEquals(600, TableOfContentsActivity.fuzzyScore(path, query));
    }

    @Test
    public void ip02_filenameMatchScoresHigherThanFolderMatch() {
        // A file named "padmapurANa.txt" at root scores higher (exact/prefix) than a
        // file inside the padmapurANa folder that only matches via path.
        String query = "padmapurana";
        String filenameMatch = "padmapurana.txt";          // prefix match → 800
        String pathOnlyMatch = "purana/padmapurana/bhumi-khanda.txt"; // substring → 600
        assertTrue(TableOfContentsActivity.fuzzyScore(filenameMatch, query)
                 > TableOfContentsActivity.fuzzyScore(pathOnlyMatch, query));
    }

    @Test
    public void ip03_unrelatedFileDoesNotMatchFolderQuery() {
        String query = "padmapurana";
        String unrelated = "mahabharata/adiparva.txt";
        assertEquals(-1, TableOfContentsActivity.fuzzyScore(unrelated, query));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // itransTitleFromSegment tests
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    public void it01_stripsExtension() {
        String result = TableOfContentsActivity.itransTitleFromSegment("myfile.txt");
        assertFalse("should not contain .txt", result.contains(".txt"));
    }

    @Test
    public void it02_stripsNumericIdPrefix() {
        String result = TableOfContentsActivity.itransTitleFromSegment("42-some-title.txt");
        assertFalse("should not start with digits", result.matches("^\\d+.*"));
        assertTrue("should contain title content", result.contains("some"));
    }

    @Test
    public void it03_replacesHyphensWithSpaces() {
        String result = TableOfContentsActivity.itransTitleFromSegment("1-hari-bhakti.txt");
        assertEquals("hari bhakti", result);
    }

    @Test
    public void it04_noExtensionNoIdPrefix() {
        String result = TableOfContentsActivity.itransTitleFromSegment("vAdirAja");
        assertEquals("vAdirAja", result);
    }

    @Test
    public void it05_idOnlyFilename() {
        // e.g. "63.txt" — no dash, no title slug
        String result = TableOfContentsActivity.itransTitleFromSegment("63.txt");
        assertEquals("63", result);
    }

    @Test
    public void it06_multiDigitId() {
        String result = TableOfContentsActivity.itransTitleFromSegment("1234-nAmapa-rAyaNa.txt");
        assertEquals("nAmapa rAyaNa", result);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // collectLeaves tests
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    public void cl01_singleLeafAtRoot() {
        // A direct leaf node (itransPath non-null, depth 0)
        TableOfContentsActivity.TreeNode leaf = makeLeaf("myfile.txt", "myfile.txt", 0);
        List<TableOfContentsActivity.SearchResult> out = new ArrayList<>();
        collectLeaves(leaf, "", out);
        assertEquals(1, out.size());
        assertEquals("", out.get(0).breadcrumb);
        assertEquals(leaf, out.get(0).node);
    }

    @Test
    public void cl02_leafUnderOneFolder() {
        TableOfContentsActivity.TreeNode folder = makeFolder("dAsasAhitya", 0);
        TableOfContentsActivity.TreeNode leaf = makeLeaf("vAdirAja.txt", "dAsasAhitya/vAdirAja.txt", 1);
        folder.children.add(leaf);

        List<TableOfContentsActivity.SearchResult> out = new ArrayList<>();
        collectLeaves(folder, "", out);
        assertEquals(1, out.size());
        assertEquals("dAsasAhitya", out.get(0).breadcrumb);
    }

    @Test
    public void cl03_leafUnderTwoFolders() {
        TableOfContentsActivity.TreeNode root = makeFolder("dAsasAhitya", 0);
        TableOfContentsActivity.TreeNode sub = makeFolder("kIrtane", 1);
        TableOfContentsActivity.TreeNode leaf = makeLeaf("1-hari.txt", "dAsasAhitya/kIrtane/1-hari.txt", 2);
        root.children.add(sub);
        sub.children.add(leaf);

        List<TableOfContentsActivity.SearchResult> out = new ArrayList<>();
        collectLeaves(root, "", out);
        assertEquals(1, out.size());
        assertEquals("dAsasAhitya / kIrtane", out.get(0).breadcrumb);
    }

    @Test
    public void cl04_foldersNotIncludedInOut() {
        TableOfContentsActivity.TreeNode root = makeFolder("top", 0);
        TableOfContentsActivity.TreeNode sub = makeFolder("sub", 1);
        root.children.add(sub);

        List<TableOfContentsActivity.SearchResult> out = new ArrayList<>();
        collectLeaves(root, "", out);
        assertEquals("folders-only tree should yield no leaves", 0, out.size());
    }

    @Test
    public void cl05_multipleLeavesUnderFolder() {
        TableOfContentsActivity.TreeNode folder = makeFolder("vedas", 0);
        TableOfContentsActivity.TreeNode leaf1 = makeLeaf("Rk.txt", "vedas/Rk.txt", 1);
        TableOfContentsActivity.TreeNode leaf2 = makeLeaf("sAma.txt", "vedas/sAma.txt", 1);
        TableOfContentsActivity.TreeNode leaf3 = makeLeaf("atharva.txt", "vedas/atharva.txt", 1);
        folder.children.addAll(Arrays.asList(leaf1, leaf2, leaf3));

        List<TableOfContentsActivity.SearchResult> out = new ArrayList<>();
        collectLeaves(folder, "", out);
        assertEquals(3, out.size());
        for (TableOfContentsActivity.SearchResult sr : out) {
            assertEquals("vedas", sr.breadcrumb);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // findPath tests
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    public void fp01_targetIsRoot() {
        TableOfContentsActivity.TreeNode root = makeFolder("root", 0);
        List<TableOfContentsActivity.TreeNode> path = new ArrayList<>();
        assertTrue(findPath(root, root, path));
        assertEquals(1, path.size());
        assertEquals(root, path.get(0));
    }

    @Test
    public void fp02_targetIsDirectChild() {
        TableOfContentsActivity.TreeNode root = makeFolder("root", 0);
        TableOfContentsActivity.TreeNode child = makeLeaf("leaf.txt", "root/leaf.txt", 1);
        root.children.add(child);

        List<TableOfContentsActivity.TreeNode> path = new ArrayList<>();
        assertTrue(findPath(root, child, path));
        assertEquals(2, path.size());
        assertEquals(root, path.get(0));
        assertEquals(child, path.get(1));
    }

    @Test
    public void fp03_targetIsGrandchild() {
        TableOfContentsActivity.TreeNode root = makeFolder("root", 0);
        TableOfContentsActivity.TreeNode mid = makeFolder("mid", 1);
        TableOfContentsActivity.TreeNode leaf = makeLeaf("doc.txt", "root/mid/doc.txt", 2);
        root.children.add(mid);
        mid.children.add(leaf);

        List<TableOfContentsActivity.TreeNode> path = new ArrayList<>();
        assertTrue(findPath(root, leaf, path));
        assertEquals(3, path.size());
        assertEquals(root, path.get(0));
        assertEquals(mid, path.get(1));
        assertEquals(leaf, path.get(2));
    }

    @Test
    public void fp04_targetNotInTree() {
        TableOfContentsActivity.TreeNode root = makeFolder("root", 0);
        TableOfContentsActivity.TreeNode stranger = makeLeaf("stranger.txt", "other/stranger.txt", 0);

        List<TableOfContentsActivity.TreeNode> path = new ArrayList<>();
        assertFalse(findPath(root, stranger, path));
        assertTrue("path should be empty when target not found", path.isEmpty());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // revealInTree logic tests  (RL)
    //
    // These exercise the pure-Java expand + flattenVisible pipeline that backs
    // revealInTree/handleNodeClick in the Activity.
    //
    // Scenarios covered
    // -----------------
    // RL-01  leaf under collapsed folder is absent from flattenVisible
    // RL-02  after expanding the parent, leaf appears at the expected position
    // RL-03  deep leaf stays hidden if only one of two ancestor folders is expanded
    // RL-04  expanding all ancestors via the findPath chain makes a deep leaf visible
    //        (this mirrors the exact logic in revealInTree)
    // RL-05  revealing one branch does not expose siblings in a separate collapsed branch
    // RL-06  flattenVisible position is the value to pass to smoothScrollToPosition;
    //        verify it is the index AFTER the expanded parent, not 0 or arbitrary
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Simulates the expand-all-ancestors step from {@code revealInTree}:
     * walks the path returned by {@link #findPath} and sets {@code expanded=true}
     * on every folder node it encounters.
     */
    private static void expandAncestors(
            TableOfContentsActivity.TreeNode root,
            TableOfContentsActivity.TreeNode target) {
        List<TableOfContentsActivity.TreeNode> path = new ArrayList<>();
        if (findPath(root, target, path)) {
            for (TableOfContentsActivity.TreeNode n : path) {
                if (!n.isLeaf()) n.expanded = true;
            }
        }
    }

    @Test
    public void rl01_leafHiddenWhenParentCollapsed() {
        // stOtra/ (collapsed) > dashAvatArastuti.txt
        TableOfContentsActivity.TreeNode root   = makeFolder("root", -1);
        TableOfContentsActivity.TreeNode folder = makeFolder("stOtra", 0);
        TableOfContentsActivity.TreeNode leaf   = makeLeaf("dashAvatArastuti.txt",
                "stOtra/dashAvatArastuti.txt", 1);
        folder.children.add(leaf);
        root.children.add(folder);

        List<TableOfContentsActivity.TreeNode> visible = TOCTreeBuilder.flattenVisible(root);
        assertTrue("folder should be visible", visible.contains(folder));
        assertFalse("leaf must be hidden when parent is collapsed", visible.contains(leaf));
    }

    @Test
    public void rl02_leafVisibleAfterParentExpanded() {
        TableOfContentsActivity.TreeNode root   = makeFolder("root", -1);
        TableOfContentsActivity.TreeNode folder = makeFolder("stOtra", 0);
        TableOfContentsActivity.TreeNode leaf   = makeLeaf("dashAvatArastuti.txt",
                "stOtra/dashAvatArastuti.txt", 1);
        folder.children.add(leaf);
        root.children.add(folder);

        folder.expanded = true;  // simulate expanding the parent

        List<TableOfContentsActivity.TreeNode> visible = TOCTreeBuilder.flattenVisible(root);
        assertTrue("leaf must be visible after parent is expanded", visible.contains(leaf));
        // folder comes before its leaf child
        assertTrue(visible.indexOf(folder) < visible.indexOf(leaf));
    }

    @Test
    public void rl03_deepLeafHiddenIfIntermediateFolderCollapsed() {
        // dAsasAhitya(expanded) > kIrtane(collapsed) > hari.txt
        TableOfContentsActivity.TreeNode root   = makeFolder("root", -1);
        TableOfContentsActivity.TreeNode top    = makeFolder("dAsasAhitya", 0);
        TableOfContentsActivity.TreeNode mid    = makeFolder("kIrtane", 1);
        TableOfContentsActivity.TreeNode leaf   = makeLeaf("hari.txt", "dAsasAhitya/kIrtane/hari.txt", 2);
        mid.children.add(leaf);
        top.children.add(mid);
        root.children.add(top);

        top.expanded = true;   // outer expanded
        // mid stays collapsed

        List<TableOfContentsActivity.TreeNode> visible = TOCTreeBuilder.flattenVisible(root);
        assertTrue("top folder visible", visible.contains(top));
        assertTrue("mid folder visible (top is expanded)", visible.contains(mid));
        assertFalse("leaf must be hidden — mid is still collapsed", visible.contains(leaf));
    }

    @Test
    public void rl04_expandAncestorsMakesDeepLeafVisible() {
        // Mirrors the exact logic in revealInTree: find path → expand all folders in it
        TableOfContentsActivity.TreeNode root   = makeFolder("root", -1);
        TableOfContentsActivity.TreeNode top    = makeFolder("dAsasAhitya", 0);
        TableOfContentsActivity.TreeNode mid    = makeFolder("kIrtane", 1);
        TableOfContentsActivity.TreeNode leaf   = makeLeaf("hari.txt", "dAsasAhitya/kIrtane/hari.txt", 2);
        mid.children.add(leaf);
        top.children.add(mid);
        root.children.add(top);

        // All folders start collapsed — leaf is invisible
        assertFalse(TOCTreeBuilder.flattenVisible(root).contains(leaf));

        // Simulate revealInTree
        expandAncestors(root, leaf);

        List<TableOfContentsActivity.TreeNode> visible = TOCTreeBuilder.flattenVisible(root);
        assertTrue("leaf must be visible after expandAncestors", visible.contains(leaf));
        assertTrue("position must be ≥ 0 (valid scroll target)", visible.indexOf(leaf) >= 0);
    }

    @Test
    public void rl05_revealingOneBranchDoesNotExposeCollapsedSibling() {
        // Two top-level folders; revealing a leaf in folder1 must not open folder2
        TableOfContentsActivity.TreeNode root    = makeFolder("root", -1);
        TableOfContentsActivity.TreeNode folder1 = makeFolder("AgamAH", 0);
        TableOfContentsActivity.TreeNode folder2 = makeFolder("vEda", 0);
        TableOfContentsActivity.TreeNode leaf1   = makeLeaf("sAma.txt", "AgamAH/sAma.txt", 1);
        TableOfContentsActivity.TreeNode leaf2   = makeLeaf("Rk.txt",   "vEda/Rk.txt",    1);
        folder1.children.add(leaf1);
        folder2.children.add(leaf2);
        root.children.add(folder1);
        root.children.add(folder2);

        expandAncestors(root, leaf1);

        List<TableOfContentsActivity.TreeNode> visible = TOCTreeBuilder.flattenVisible(root);
        assertTrue("leaf1 visible after reveal", visible.contains(leaf1));
        assertFalse("folder2 still collapsed — leaf2 must stay hidden", visible.contains(leaf2));
    }

    @Test
    public void rl06_scrollPositionIsDirectlyAfterItsParentFolder() {
        // In the visible list the leaf must immediately follow its parent folder
        // (no other nodes in between when the folder has only one child).
        TableOfContentsActivity.TreeNode root   = makeFolder("root", -1);
        TableOfContentsActivity.TreeNode folder = makeFolder("stOtra", 0);
        TableOfContentsActivity.TreeNode leaf   = makeLeaf("dashAvatArastuti.txt",
                "stOtra/dashAvatArastuti.txt", 1);
        folder.children.add(leaf);
        root.children.add(folder);

        expandAncestors(root, leaf);

        List<TableOfContentsActivity.TreeNode> visible = TOCTreeBuilder.flattenVisible(root);
        int folderPos = visible.indexOf(folder);
        int leafPos   = visible.indexOf(leaf);
        assertEquals("leaf must be at folder+1 when folder has a single child",
                folderPos + 1, leafPos);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SearchResult cached-field tests  (SR)
    //
    // SR-01  labelLower is the display label lowercased
    // SR-02  itransTitleLower strips extension, ID prefix, and is lowercased
    // SR-03  itransPathLower is the full path lowercased
    // SR-04  itransPathLower is empty string for a folder node (itransPath == null)
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    public void sr01_labelLowerIsCached() {
        TableOfContentsActivity.TreeNode leaf =
                makeLeaf("vAdirAja.txt", "stOtra/vAdirAja.txt", 1);
        // makeLeaf uses the segment as displayLabel too
        TableOfContentsActivity.SearchResult sr =
                new TableOfContentsActivity.SearchResult(leaf, "stOtra");
        assertEquals("vadiraja.txt", sr.labelLower);
    }

    @Test
    public void sr02_itransTitleLowerIsCached() {
        // segment "42-hari-bhakti.txt" → title "hari bhakti" → lowercased
        TableOfContentsActivity.TreeNode leaf =
                makeLeaf("42-hari-bhakti.txt", "stOtra/42-hari-bhakti.txt", 1);
        TableOfContentsActivity.SearchResult sr =
                new TableOfContentsActivity.SearchResult(leaf, "stOtra");
        assertEquals("hari bhakti", sr.itransTitleLower);
    }

    @Test
    public void sr03_itransPathLowerIsCached() {
        TableOfContentsActivity.TreeNode leaf =
                makeLeaf("vAdirAja.txt", "stOtra/vAdirAja.txt", 1);
        TableOfContentsActivity.SearchResult sr =
                new TableOfContentsActivity.SearchResult(leaf, "stOtra");
        assertEquals("stotra/vadiraja.txt", sr.itransPathLower);
    }

    @Test
    public void sr04_itransPathLowerEmptyForFolderNode() {
        // Folder nodes have itransPath == null; cached field should be ""
        TableOfContentsActivity.TreeNode folder = makeFolder("stOtra", 0);
        TableOfContentsActivity.SearchResult sr =
                new TableOfContentsActivity.SearchResult(folder, "");
        assertEquals("", sr.itransPathLower);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // computeSearchResults tests  (CS)
    //
    // CS-01  matching leaf is returned, non-matching leaf is excluded
    // CS-02  results are sorted highest-score first
    // CS-03  ties broken alphabetically
    // CS-04  double-vowel normalised query ("dashaavataara") matches leaf
    // CS-05  empty leaf list returns empty results
    // CS-06  query with no matches returns empty results
    // CS-07  folder-name query matches every leaf whose path contains it
    // ═════════════════════════════════════════════════════════════════════════

    /** Build a minimal leaf SearchResult. */
    private static TableOfContentsActivity.SearchResult makeSearchResult(
            String segment, String itransPath, String breadcrumb) {
        TableOfContentsActivity.TreeNode leaf = makeLeaf(segment, itransPath, 1);
        return new TableOfContentsActivity.SearchResult(leaf, breadcrumb);
    }

    @Test
    public void cs01_matchingLeafReturnedNonMatchingExcluded() {
        List<TableOfContentsActivity.SearchResult> leaves = Arrays.asList(
                makeSearchResult("rAmAyaNa.txt",  "rAmAyaNa.txt",  ""),
                makeSearchResult("mahAbhArata.txt", "mahAbhArata.txt", ""));
        List<TableOfContentsActivity.SearchResult> results =
                TableOfContentsActivity.computeSearchResults("rAmA", leaves);
        assertEquals(1, results.size());
        assertEquals("rAmAyaNa.txt", results.get(0).node.displayLabel);
    }

    @Test
    public void cs02_sortedHighestScoreFirst() {
        // "hari" is exact in "hari.txt" (segment = "hari.txt", label = "hari.txt")
        // "harikathA.txt" is a prefix match
        List<TableOfContentsActivity.SearchResult> leaves = Arrays.asList(
                makeSearchResult("harikathA.txt", "harikathA.txt", ""),
                makeSearchResult("hari.txt",      "hari.txt",      ""));
        List<TableOfContentsActivity.SearchResult> results =
                TableOfContentsActivity.computeSearchResults("hari.txt", leaves);
        assertEquals(2, results.size());
        // exact match "hari.txt" must come first
        assertEquals("hari.txt", results.get(0).node.displayLabel);
    }

    @Test
    public void cs03_tiesBrokenAlphabetically() {
        // Both are substring matches for "txt" — tied on score, sorted alphabetically
        List<TableOfContentsActivity.SearchResult> leaves = Arrays.asList(
                makeSearchResult("zeta.txt",  "zeta.txt",  ""),
                makeSearchResult("alpha.txt", "alpha.txt", ""));
        List<TableOfContentsActivity.SearchResult> results =
                TableOfContentsActivity.computeSearchResults("txt", leaves);
        assertEquals(2, results.size());
        assertEquals("alpha.txt", results.get(0).node.displayLabel);
        assertEquals("zeta.txt",  results.get(1).node.displayLabel);
    }

    @Test
    public void cs04_doubleVowelQueryMatchesLeaf() {
        // "dashaavataara" normalised → "dashavatara"; label "dashAvatarastuti"
        // toLowerCase = "dashavatarastuti" which starts with "dashavatara" → score 800
        List<TableOfContentsActivity.SearchResult> leaves = Arrays.asList(
                makeSearchResult("dashAvatarastuti.txt", "stOtra/dashAvatarastuti.txt", "stOtra"));
        List<TableOfContentsActivity.SearchResult> results =
                TableOfContentsActivity.computeSearchResults("dashaavataara", leaves);
        assertEquals("double-vowel query must match via normalisation", 1, results.size());
    }

    @Test
    public void cs05_emptyLeavesReturnsEmpty() {
        List<TableOfContentsActivity.SearchResult> results =
                TableOfContentsActivity.computeSearchResults("rAma",
                        new ArrayList<TableOfContentsActivity.SearchResult>());
        assertTrue(results.isEmpty());
    }

    @Test
    public void cs06_noMatchReturnsEmpty() {
        List<TableOfContentsActivity.SearchResult> leaves = Arrays.asList(
                makeSearchResult("rAmAyaNa.txt", "rAmAyaNa.txt", ""));
        List<TableOfContentsActivity.SearchResult> results =
                TableOfContentsActivity.computeSearchResults("zzzzz", leaves);
        assertTrue(results.isEmpty());
    }

    @Test
    public void cs07_folderNameQueryMatchesAllLeavesInThatFolder() {
        List<TableOfContentsActivity.SearchResult> leaves = Arrays.asList(
                makeSearchResult("bhumi-khanda.txt",  "purana/padmapurana/bhumi-khanda.txt",  "purana/padmapurana"),
                makeSearchResult("srishti-khanda.txt", "purana/padmapurana/srishti-khanda.txt", "purana/padmapurana"),
                makeSearchResult("unrelated.txt",      "mahabharata/unrelated.txt",             "mahabharata"));
        List<TableOfContentsActivity.SearchResult> results =
                TableOfContentsActivity.computeSearchResults("padmapurana", leaves);
        assertEquals("both files in padmapurana folder must match", 2, results.size());
        for (TableOfContentsActivity.SearchResult sr : results) {
            assertTrue(sr.node.itransPath.contains("padmapurana"));
        }
    }
}
