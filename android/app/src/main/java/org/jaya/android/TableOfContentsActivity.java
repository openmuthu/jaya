package org.jaya.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jaya.annotation.Annotation;
import org.jaya.scriptconverter.SCUtils;
import org.jaya.search.JayaIndexMetadata;
import org.jaya.search.ResultDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TableOfContentsActivity extends Activity {

    static class TreeNode {
        final String itransSegment;  // raw ITRANS path segment (used for folder dedup)
        final String displayLabel;   // label in the current display script
        final String itransPath;     // null for folders; full ITRANS path for leaf documents
        String folderPath;           // slash-terminated prefix for folders; null for leaves
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

    // ── Search result ─────────────────────────────────────────────────────────

    static class SearchResult {
        final TreeNode node;
        final String breadcrumb;  // e.g. "dAsasAhitya / vAdirAjaru"
        int score;

        // Pre-computed search targets — built once, reused on every keystroke.
        final String labelLower;
        final String itransTitleLower;
        final String itransPathLower;

        SearchResult(TreeNode node, String breadcrumb) {
            this.node = node;
            this.breadcrumb = breadcrumb;
            this.labelLower       = node.displayLabel.toLowerCase();
            this.itransTitleLower = itransTitleFromSegment(node.itransSegment).toLowerCase();
            this.itransPathLower  = node.itransPath != null
                    ? node.itransPath.toLowerCase() : "";
        }
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private TreeNode mRoot;
    private final List<TreeNode> mVisibleNodes = new ArrayList<TreeNode>();
    private TableOfContentsListAdapter mAdapter;

    // Flat list of all leaf nodes collected at tree-build time
    private final List<SearchResult> mAllLeaves = new ArrayList<SearchResult>();

    // Results currently shown in the overlay
    private final List<SearchResult> mSearchResults = new ArrayList<SearchResult>();
    private SearchResultAdapter mSearchAdapter;

    private ListView mSearchResultsView;
    private EditText mSearchEdit;
    private ImageButton mClearButton;

    // Debounce: wait for typing to pause before running the search
    private final Handler mSearchHandler = new Handler();
    private static final int SEARCH_DEBOUNCE_MS = 200;

    // ── Activity lifecycle ────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_of_contents);
        getActionBar().setIcon(android.R.color.transparent);
        setupListView();
        setupSearchBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRoot == null) {
            buildAndShowTree();
        } else {
            refreshVisibleNodes();
        }
    }

    @Override
    public void onBackPressed() {
        if (mSearchResultsView.getVisibility() == View.VISIBLE) {
            clearSearch();
            return;
        }
        super.onBackPressed();
    }

    // ── Tree ListView setup ───────────────────────────────────────────────────

    private void setupListView() {
        ListView listView = (ListView) findViewById(R.id.toc_list_view);
        mAdapter = new TableOfContentsListAdapter(this, mVisibleNodes,
                new TableOfContentsListAdapter.OnNodeClickListener() {
                    @Override
                    public void onNodeClick(TreeNode node) {
                        handleNodeClick(node);
                    }
                });
        mAdapter.setOnNodeLongClickListener(
                new TableOfContentsListAdapter.OnNodeLongClickListener() {
                    @Override
                    public void onNodeLongClick(TreeNode node) {
                        handleNodeLongClick(node);
                    }
                });
        mAdapter.setOnNodeSearchClickListener(
                new TableOfContentsListAdapter.OnNodeSearchClickListener() {
                    @Override
                    public void onNodeSearchClick(TreeNode node) {
                        launchScopedSearch(node);
                    }
                });
        listView.setAdapter(mAdapter);
    }

    // ── Search bar setup ──────────────────────────────────────────────────────

    private void setupSearchBar() {
        mSearchEdit = (EditText) findViewById(R.id.toc_search_edit);
        mClearButton = (ImageButton) findViewById(R.id.toc_search_clear);
        mSearchResultsView = (ListView) findViewById(R.id.toc_search_results);

        mSearchAdapter = new SearchResultAdapter();
        mSearchResultsView.setAdapter(mSearchAdapter);

        mSearchResultsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchResult result = mSearchResults.get(position);
                clearSearch();
                revealInTree(result.node);
            }
        });

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSearch();
            }
        });

        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                final String query = s.toString().trim();
                if (query.isEmpty()) {
                    mSearchHandler.removeCallbacksAndMessages(null);
                    mSearchResultsView.setVisibility(View.GONE);
                    mClearButton.setVisibility(View.GONE);
                } else {
                    mClearButton.setVisibility(View.VISIBLE);
                    // Debounce: cancel any pending search and schedule a new one.
                    mSearchHandler.removeCallbacksAndMessages(null);
                    mSearchHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scheduleSearch(query);
                        }
                    }, SEARCH_DEBOUNCE_MS);
                }
            }
        });
    }

    // ── Tree building ─────────────────────────────────────────────────────────

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

        // Collect all leaf nodes with their breadcrumb paths for fast search
        mAllLeaves.clear();
        collectLeaves(mRoot, "", mAllLeaves);

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

    // ── Leaf collection ───────────────────────────────────────────────────────

    /**
     * DFS over the tree, collecting every leaf node together with its
     * display-script breadcrumb path (ancestor folder labels joined by " / ").
     */
    private void collectLeaves(TreeNode node, String breadcrumb, List<SearchResult> out) {
        if (node.isLeaf()) {
            out.add(new SearchResult(node, breadcrumb));
        } else {
            String next = breadcrumb.isEmpty()
                    ? node.displayLabel
                    : breadcrumb + " / " + node.displayLabel;
            for (TreeNode child : node.children) {
                collectLeaves(child, next, out);
            }
        }
    }

    // ── Fuzzy search ──────────────────────────────────────────────────────────

    /**
     * Score how well {@code label} matches {@code query} (both lower-cased by caller).
     * Returns -1 if there is no subsequence match at all.
     *
     * Scoring tiers (higher = better):
     *   1000  exact match
     *    800  label starts with query
     *    600  label contains query as substring
     *    1+   subsequence match — boosted by run of consecutive matched chars
     */
    static int fuzzyScore(String label, String query) {
        if (label.equals(query))       return 1000;
        if (label.startsWith(query))   return 800;
        if (label.contains(query))     return 600;

        // Subsequence match
        int qi = 0;
        int score = 0;
        int run = 0;
        int prevMatchIdx = -2;

        for (int li = 0; li < label.length() && qi < query.length(); li++) {
            if (label.charAt(li) == query.charAt(qi)) {
                run = (li == prevMatchIdx + 1) ? run + 1 : 1;
                score += run * 3;   // consecutive matches score higher
                prevMatchIdx = li;
                qi++;
            }
        }
        return (qi == query.length()) ? score : -1;
    }

    /**
     * Normalises consecutive duplicate vowels so that users who type ITRANS
     * long-vowel "aa"/"ii"/"uu" style match titles stored with uppercase
     * long-vowel notation (A/I/U, which collapse to single chars on toLowerCase).
     * e.g. "dashaavataara" → "dashavatara", which is then a prefix of
     * "dashavatarastuti" (from "dashAvatArastuti").
     */
    static String normalizeDoubleVowels(String s) {
        return s.replace("aa", "a").replace("ii", "i").replace("uu", "u");
    }

    /**
     * Runs the search on a background thread so the UI thread stays responsive,
     * then posts the sorted results back to the main thread.
     */
    private void scheduleSearch(final String rawQuery) {
        final List<SearchResult> snapshot = new ArrayList<SearchResult>(mAllLeaves);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<SearchResult> results = computeSearchResults(rawQuery, snapshot);
                mSearchHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSearchResults.clear();
                        mSearchResults.addAll(results);
                        mSearchResultsView.setVisibility(View.VISIBLE);
                        mSearchAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    /**
     * Pure computation — no UI calls.  Safe to run on a background thread.
     * Uses pre-cached lowercase strings from each {@link SearchResult} so
     * no string allocations happen inside the hot loop.
     */
    static List<SearchResult> computeSearchResults(
            String rawQuery, List<SearchResult> leaves) {
        String query     = rawQuery.toLowerCase();
        String queryNorm = normalizeDoubleVowels(query);
        boolean normDiffers = !queryNorm.equals(query);

        List<SearchResult> out = new ArrayList<SearchResult>();
        for (SearchResult sr : leaves) {
            // Use pre-computed lowercase fields — no allocation in the hot loop.
            int s1 = fuzzyScore(sr.labelLower,       query);
            int s2 = fuzzyScore(sr.itransTitleLower, query);
            int s3 = fuzzyScore(sr.itransPathLower,  query);
            // Also try the double-vowel-normalised query (aa→a, ii→i, uu→u)
            // so "dashaavataara" matches "dashAvatArastuti" etc.
            int s4 = normDiffers ? fuzzyScore(sr.labelLower,       queryNorm) : -1;
            int s5 = normDiffers ? fuzzyScore(sr.itransTitleLower, queryNorm) : -1;
            int s6 = normDiffers ? fuzzyScore(sr.itransPathLower,  queryNorm) : -1;
            int best = Math.max(Math.max(s1, s2), Math.max(Math.max(s3, s4), Math.max(s5, s6)));

            if (best > 0) {
                sr.score = best;
                out.add(sr);
            }
        }

        // Sort: highest score first; ties broken alphabetically by display label
        Collections.sort(out, new Comparator<SearchResult>() {
            @Override
            public int compare(SearchResult a, SearchResult b) {
                if (b.score != a.score) return b.score - a.score;
                return a.node.displayLabel.compareToIgnoreCase(b.node.displayLabel);
            }
        });
        return out;
    }

    /**
     * Given a raw ITRANS segment like "202-sUsuvadyAtake-haribhakutige.txt",
     * return "sUsuvadyAtake haribhakutige" (drop leading ID, drop extension,
     * replace hyphens with spaces).
     */
    static String itransTitleFromSegment(String segment) {
        String s = segment.endsWith(".txt")
                ? segment.substring(0, segment.length() - 4)
                : segment;
        // Strip leading "<digits>-" prefix (composition ID)
        s = s.replaceFirst("^\\d+-", "");
        return s.replace('-', ' ');
    }

    // ── Search results adapter ────────────────────────────────────────────────

    private class SearchResultAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSearchResults.isEmpty() ? 1 : mSearchResults.size();
        }

        @Override
        public Object getItem(int position) {
            return mSearchResults.isEmpty() ? null : mSearchResults.get(position);
        }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public boolean isEnabled(int position) {
            return !mSearchResults.isEmpty();
        }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            if (mSearchResults.isEmpty()) {
                // Show "no results" placeholder
                if (convertView == null || convertView.getId() != android.R.id.empty) {
                    convertView = View.inflate(TableOfContentsActivity.this,
                            android.R.layout.simple_list_item_1, null);
                    convertView.setId(android.R.id.empty);
                }
                ((TextView) convertView).setText(R.string.toc_search_no_results);
                return convertView;
            }

            if (convertView == null || convertView.getId() == android.R.id.empty) {
                convertView = View.inflate(TableOfContentsActivity.this,
                        R.layout.toc_search_result_item, null);
            }
            SearchResult sr = mSearchResults.get(position);
            ((TextView) convertView.findViewById(R.id.search_result_label))
                    .setText(sr.node.displayLabel);
            ((TextView) convertView.findViewById(R.id.search_result_breadcrumb))
                    .setText(sr.breadcrumb);
            return convertView;
        }
    }

    // ── Reveal in tree ────────────────────────────────────────────────────────

    /**
     * Expand all ancestor folders of {@code target}, highlight it, refresh
     * the list, then scroll so it is visible.
     *
     * The scroll is posted to the view queue so it runs after the ListView
     * has processed the notifyDataSetChanged() triggered by refreshVisibleNodes(),
     * otherwise setSelection is a no-op on stale layout state.
     */
    private void revealInTree(TreeNode target) {
        List<TreeNode> path = new ArrayList<TreeNode>();
        if (!findPath(mRoot, target, path)) return;

        // Expand every folder along the path (path includes root at index 0)
        for (TreeNode node : path) {
            if (!node.isLeaf()) {
                node.expanded = true;
            }
        }

        // Highlight before refreshing so the adapter paints it on the first draw
        mAdapter.setHighlightedNode(target);
        refreshVisibleNodes();

        final int pos = mVisibleNodes.indexOf(target);
        if (pos >= 0) {
            final ListView listView = (ListView) findViewById(R.id.toc_list_view);
            // Post so the scroll runs after the ListView has re-laid out
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setSelection(pos);
                }
            });
        }
    }

    /**
     * DFS from {@code current} searching for {@code target}.
     * On success {@code path} contains the route from root to target (inclusive).
     */
    private boolean findPath(TreeNode current, TreeNode target, List<TreeNode> path) {
        if (current == target) {
            path.add(current);
            return true;
        }
        for (TreeNode child : current.children) {
            if (findPath(child, target, path)) {
                path.add(0, current);
                return true;
            }
        }
        return false;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void clearSearch() {
        mSearchEdit.setText("");
        mSearchResultsView.setVisibility(View.GONE);
        mClearButton.setVisibility(View.GONE);
        // Dismiss keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(mSearchEdit.getWindowToken(), 0);
    }

    // ── Node interaction ──────────────────────────────────────────────────────

    private void handleNodeClick(TreeNode node) {
        mAdapter.setHighlightedNode(node);
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

    private void launchScopedSearch(TreeNode node) {
        String scopePath;
        if (node.isLeaf()) {
            scopePath = node.itransPath;
        } else {
            if (node.folderPath == null) return;
            scopePath = node.folderPath;
        }
        Intent intent = new Intent(this, SearchableActivity.class);
        intent.putExtra(SearchableActivity.EXTRA_FOLDER_PATH, scopePath);
        intent.putExtra(SearchableActivity.EXTRA_FOLDER_DISPLAY_NAME, node.displayLabel);
        startActivity(intent);
    }

    private void handleNodeLongClick(final TreeNode node) {
        if (node.isLeaf() || node.folderPath == null) return;

        new AlertDialog.Builder(this)
                .setTitle(node.displayLabel)
                .setItems(new CharSequence[]{
                        getString(R.string.search_in_this_folder)
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(TableOfContentsActivity.this,
                                SearchableActivity.class);
                        intent.putExtra(SearchableActivity.EXTRA_FOLDER_PATH, node.folderPath);
                        intent.putExtra(SearchableActivity.EXTRA_FOLDER_DISPLAY_NAME,
                                node.displayLabel);
                        startActivity(intent);
                    }
                })
                .show();
    }
}
