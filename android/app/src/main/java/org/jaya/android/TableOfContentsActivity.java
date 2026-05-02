package org.jaya.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import org.jaya.annotation.Annotation;
import org.jaya.scriptconverter.SCUtils;
import org.jaya.search.JayaIndexMetadata;
import org.jaya.search.ResultDocument;
import org.jaya.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TableOfContentsActivity extends Activity {

    static class TreeNode {
        final String itransSegment;  // raw ITRANS path segment (used for folder dedup)
        final String displayLabel;   // label in the current display script
        final String itransPath;     // null for folders; full ITRANS path for leaf documents
        final int depth;
        boolean expanded;
        final List<TreeNode> children = new ArrayList<TreeNode>();

        TreeNode(String itransSegment, String displayLabel, String itransPath, int depth) {
            this.itransSegment = itransSegment;
            this.displayLabel = displayLabel;
            this.itransPath = itransPath;
            this.depth = depth;
            this.expanded = false;
        }

        boolean isLeaf() {
            return itransPath != null;
        }
    }

    private TreeNode mRoot;
    private final List<TreeNode> mVisibleNodes = new ArrayList<TreeNode>();
    private TableOfContentsListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_of_contents);
        getActionBar().setIcon(android.R.color.transparent);
        setupListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        buildAndShowTree();
    }

    private void setupListView() {
        ListView listView = (ListView) findViewById(R.id.toc_list_view);
        mAdapter = new TableOfContentsListAdapter(this, mVisibleNodes,
                new TableOfContentsListAdapter.OnNodeClickListener() {
                    @Override
                    public void onNodeClick(TreeNode node) {
                        handleNodeClick(node);
                    }
                });
        listView.setAdapter(mAdapter);
    }

    private void buildAndShowTree() {
        JayaIndexMetadata mt = new JayaIndexMetadata(JayaApp.getSearchIndexFolder());
        Set<String> pathSet = mt.getIndexedFilePathSet();
        if (pathSet == null || pathSet.isEmpty()) {
            Toast.makeText(this, R.string.no_results_found, Toast.LENGTH_SHORT).show();
            return;
        }
        TOCTreeBuilder.LabelConverter converter = new TOCTreeBuilder.LabelConverter() {
            @Override
            public String toDisplayLabel(String itransSegment) {
                return SCUtils.convertStringToScript(
                        itransSegment, PreferencesManager.getPreferredOutputScriptType());
            }
        };
        mRoot = TOCTreeBuilder.buildTree(pathSet, converter);
        TOCTreeBuilder.sortTree(mRoot);
        refreshVisibleNodes();
    }

    private void refreshVisibleNodes() {
        mVisibleNodes.clear();
        if (mRoot != null) {
            mVisibleNodes.addAll(TOCTreeBuilder.flattenVisible(mRoot));
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void handleNodeClick(TreeNode node) {
        if (node.isLeaf()) {
            ResultDocument resDoc = JayaAppUtils.getDoc(
                    new Annotation(node.itransPath, "0", "", new Date()));
            Intent intent = new Intent(TableOfContentsActivity.this, MainActivity.class);
            intent.setAction(JayaApp.INTENT_OPEN_DOCUMENT_ID);
            intent.putExtra("documentId", resDoc.getId());
            startActivity(intent);
        } else {
            node.expanded = !node.expanded;
            refreshVisibleNodes();
        }
    }
}
