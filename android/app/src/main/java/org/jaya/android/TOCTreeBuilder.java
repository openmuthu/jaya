package org.jaya.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Pure-Java utility that converts a flat set of slash-separated ITRANS paths
 * into a hierarchical {@link TableOfContentsActivity.TreeNode} tree, sorts it,
 * and flattens it to the currently visible nodes.
 *
 * No Android-framework imports — fully testable with plain JUnit on the host JVM.
 */
class TOCTreeBuilder {

    /**
     * Converts a raw ITRANS path segment into a display label.
     * Implemented in production code via {@code SCUtils.convertStringToScript};
     * replaced with an identity function in unit tests.
     */
    interface LabelConverter {
        String toDisplayLabel(String itransSegment);
    }

    /** Identity converter: returns the segment unchanged (useful in tests). */
    static final LabelConverter IDENTITY = new LabelConverter() {
        @Override
        public String toDisplayLabel(String s) { return s; }
    };

    /**
     * Builds a tree from a set of slash-separated ITRANS paths.
     *
     * <p>Intermediate path segments become folder nodes; the final segment
     * becomes a leaf node carrying the full original path as its
     * {@code itransPath}.  Files with the same parent segment are grouped
     * under a single folder node.
     *
     * @param pathSet   full paths, e.g. {"AgamAH/text.txt", "purANa/sub/file.txt"}
     * @param converter called for every segment to produce its display label
     * @return a virtual root node (depth=-1) whose children are the top-level nodes
     */
    static TableOfContentsActivity.TreeNode buildTree(
            Set<String> pathSet, LabelConverter converter) {

        TableOfContentsActivity.TreeNode root =
                new TableOfContentsActivity.TreeNode("", "", null, -1);

        for (String path : pathSet) {
            // Paths in the index metadata are stored with a leading slash
            // (e.g. "/AgamAH/file.txt"). Strip it before splitting so that
            // split("/") does not produce a phantom empty first segment that
            // would create a blank hidden folder at the top of the tree.
            String normalised = path.startsWith("/") ? path.substring(1) : path;
            String[] segments = normalised.split("/");
            TableOfContentsActivity.TreeNode current = root;
            for (int i = 0; i < segments.length; i++) {
                String seg = segments[i];
                boolean isLast = (i == segments.length - 1);
                if (isLast) {
                    String label = converter.toDisplayLabel(removeExtension(seg));
                    current.children.add(
                            new TableOfContentsActivity.TreeNode(
                                    seg, label, path, current.depth + 1));
                } else {
                    TableOfContentsActivity.TreeNode folder = findFolder(current, seg);
                    if (folder == null) {
                        String label = converter.toDisplayLabel(seg);
                        folder = new TableOfContentsActivity.TreeNode(
                                seg, label, null, current.depth + 1);
                        current.children.add(folder);
                    }
                    current = folder;
                }
            }
        }
        return root;
    }

    /**
     * Recursively sorts each tree level: folders before leaves, then
     * alphabetically by {@code displayLabel} within each group.
     */
    static void sortTree(TableOfContentsActivity.TreeNode node) {
        Collections.sort(node.children, new Comparator<TableOfContentsActivity.TreeNode>() {
            @Override
            public int compare(TableOfContentsActivity.TreeNode a,
                               TableOfContentsActivity.TreeNode b) {
                if (a.isLeaf() != b.isLeaf()) {
                    return a.isLeaf() ? 1 : -1;
                }
                return a.displayLabel.compareTo(b.displayLabel);
            }
        });
        for (TableOfContentsActivity.TreeNode child : node.children) {
            if (!child.isLeaf()) {
                sortTree(child);
            }
        }
    }

    /**
     * Returns a flat, ordered list of all nodes that are currently visible,
     * honoring each folder node's {@code expanded} state.  Collapsed folders
     * hide their entire subtree.
     */
    static List<TableOfContentsActivity.TreeNode> flattenVisible(
            TableOfContentsActivity.TreeNode root) {
        List<TableOfContentsActivity.TreeNode> result =
                new ArrayList<TableOfContentsActivity.TreeNode>();
        for (TableOfContentsActivity.TreeNode child : root.children) {
            addVisible(child, result);
        }
        return result;
    }

    private static void addVisible(TableOfContentsActivity.TreeNode node,
                                   List<TableOfContentsActivity.TreeNode> result) {
        result.add(node);
        if (!node.isLeaf() && node.expanded) {
            for (TableOfContentsActivity.TreeNode child : node.children) {
                addVisible(child, result);
            }
        }
    }

    private static TableOfContentsActivity.TreeNode findFolder(
            TableOfContentsActivity.TreeNode parent, String itransSegment) {
        for (TableOfContentsActivity.TreeNode child : parent.children) {
            if (!child.isLeaf() && itransSegment.equals(child.itransSegment)) {
                return child;
            }
        }
        return null;
    }

    /** Strips the last dot-suffix from a filename. Package-private for testing. */
    static String removeExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }
}
