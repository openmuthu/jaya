package org.jaya.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jaya.annotation.Annotation;
import org.jaya.annotation.AnnotationManager;
import org.jaya.annotation.BookmarkGroup;
import org.jaya.search.ResultDocument;

import java.util.ArrayList;
import java.util.List;

public class AnnotationsActivity extends Activity {

    private static final int CHIP_PADDING_DP   = 12;
    private static final int CHIP_MARGIN_DP    = 4;
    private static final int CHIP_TEXT_SIZE_SP = 13;

    /** null means "All" (no group filter). */
    private String mSelectedGroupId = null;
    private List<Annotation> mAnnotationList = new ArrayList<>();
    private AnnotationListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotations);
        getActionBar().setIcon(android.R.color.transparent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-validate the selected group (it might have been deleted).
        if (mSelectedGroupId != null) {
            boolean stillExists = false;
            for (BookmarkGroup g : JayaApp.getAnnotationManager().getGroups()) {
                if (g.getId().equals(mSelectedGroupId)) { stillExists = true; break; }
            }
            if (!stillExists) mSelectedGroupId = null;
        }
        rebuildGroupChips();
        refreshList();
    }

    // -------------------------------------------------------------------------
    // Group filter chips
    // -------------------------------------------------------------------------

    private void rebuildGroupChips() {
        LinearLayout container = (LinearLayout) findViewById(R.id.group_chips_container);
        container.removeAllViews();

        // "All" chip
        container.addView(buildChip(getString(R.string.bookmarks_filter_all), null));

        // One chip per group
        for (BookmarkGroup g : JayaApp.getAnnotationManager().getGroups()) {
            container.addView(buildChip(g.getName(), g.getId()));
        }

        // "+" button to add a new group
        TextView addBtn = buildChip("+", /* groupId */ "");
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { addNewGroup(); }
        });
        addBtn.setOnLongClickListener(null);  // no long-press on "+"
        container.addView(addBtn);
    }

    private TextView buildChip(final String label, final String groupId) {
        TextView chip = new TextView(this);
        int paddingPx = dpToPx(CHIP_PADDING_DP);
        int marginPx  = dpToPx(CHIP_MARGIN_DP);
        chip.setPadding(paddingPx, paddingPx / 2, paddingPx, paddingPx / 2);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(marginPx, marginPx, marginPx, marginPx);
        chip.setLayoutParams(lp);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, CHIP_TEXT_SIZE_SP);
        chip.setGravity(Gravity.CENTER);
        chip.setText(label);

        // Highlight the selected chip
        boolean isSelected = (groupId == null && mSelectedGroupId == null)
                || (groupId != null && groupId.equals(mSelectedGroupId));
        chip.setBackgroundColor(isSelected ? 0xFF888800 : 0xFF444444);
        chip.setTextColor(Color.WHITE);

        if (groupId == null) {
            // "All" chip — tap to clear filter
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    mSelectedGroupId = null;
                    rebuildGroupChips();
                    refreshList();
                }
            });
        } else if (!groupId.isEmpty()) {
            // Named group chip — tap to filter, long-press for actions
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    mSelectedGroupId = groupId;
                    rebuildGroupChips();
                    refreshList();
                }
            });
            chip.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) {
                    showGroupContextMenu(groupId, label);
                    return true;
                }
            });
        }
        return chip;
    }

    private void addNewGroup() {
        String name = JayaApp.getAnnotationManager().generateGroupName();
        JayaApp.getAnnotationManager().addGroup(name);
        JayaApp.saveMRUAndAnnotationsIfDirty();
        rebuildGroupChips();
    }

    private void showGroupContextMenu(final String groupId, final String currentName) {
        String[] options = {
                getString(R.string.group_rename),
                getString(R.string.group_delete)
        };
        new AlertDialog.Builder(this)
                .setTitle(currentName)
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) showRenameGroupDialog(groupId, currentName);
                        else            confirmDeleteGroup(groupId, currentName);
                    }
                })
                .show();
    }

    private void showRenameGroupDialog(final String groupId, String currentName) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(getString(R.string.group_rename_hint));
        input.setText(currentName);
        input.selectAll();
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.group_rename))
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            JayaApp.getAnnotationManager().renameGroup(groupId, newName);
                            JayaApp.saveMRUAndAnnotationsIfDirty();
                            rebuildGroupChips();
                            refreshList();  // group names on items may have changed
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmDeleteGroup(final String groupId, String name) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.group_delete))
                .setMessage(name)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JayaApp.getAnnotationManager().removeGroup(groupId);
                        JayaApp.saveMRUAndAnnotationsIfDirty();
                        if (groupId.equals(mSelectedGroupId)) mSelectedGroupId = null;
                        rebuildGroupChips();
                        refreshList();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // -------------------------------------------------------------------------
    // Annotation list
    // -------------------------------------------------------------------------

    private void refreshList() {
        AnnotationManager am = JayaApp.getAnnotationManager();
        if (mSelectedGroupId != null) {
            mAnnotationList = am.getAnnotationsForGroup(mSelectedGroupId);
        } else {
            mAnnotationList = am.getAnnotations();
        }

        if (mAnnotationList == null || mAnnotationList.isEmpty()) {
            Toast.makeText(this, R.string.no_results_found, Toast.LENGTH_SHORT).show();
            // Keep showing an empty list (don't return early) so chips remain visible.
        }

        ListView listView = (ListView) findViewById(R.id.annotations_list_view);
        if (mAdapter == null) {
            mAdapter = new AnnotationListAdapter(this, mAnnotationList);
            listView.setAdapter(mAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mAnnotationList == null || i >= mAnnotationList.size()) return;
                    ResultDocument resDoc = JayaAppUtils.getDoc(mAnnotationList.get(i));
                    Intent intent = new Intent(AnnotationsActivity.this, MainActivity.class);
                    intent.setAction(JayaApp.INTENT_OPEN_DOCUMENT_ID);
                    intent.putExtra("documentId", resDoc.getId());
                    startActivity(intent);
                }
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mAnnotationList == null || i >= mAnnotationList.size()) return false;
                    showAnnotationContextMenu(mAnnotationList.get(i));
                    return true;
                }
            });
        } else {
            mAdapter.setAnnotationListAndUpdateView(mAnnotationList);
        }
    }

    private void showAnnotationContextMenu(final Annotation annotation) {
        String[] options = {
                getString(R.string.bookmark_rename),
                getString(R.string.bookmark_assign_groups),
                getString(R.string.bookmark_share),
                getString(R.string.bookmark_delete)
        };
        new AlertDialog.Builder(this)
                .setTitle(annotation.getName())
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if      (which == 0) showRenameAnnotationDialog(annotation);
                        else if (which == 1) showAssignGroupsDialog(annotation);
                        else if (which == 2) shareAnnotationLink(annotation);
                        else                 confirmDeleteAnnotation(annotation);
                    }
                })
                .show();
    }

    private void shareAnnotationLink(Annotation annotation) {
        String url = BookmarkDeepLink.buildUrl(
                annotation.getDocPath(), annotation.getDocLocalId(),
                annotation.getContentFingerprint(), annotation.getName());
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(share, annotation.getName()));
    }

    private void showRenameAnnotationDialog(final Annotation annotation) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(getString(R.string.bookmark_rename_hint));
        input.setText(annotation.getName());
        input.selectAll();
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.bookmark_rename))
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            JayaApp.getAnnotationManager().renameAnnotation(annotation.getKey(), newName);
                            JayaApp.saveMRUAndAnnotationsIfDirty();
                            refreshList();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showAssignGroupsDialog(final Annotation annotation) {
        List<BookmarkGroup> allGroups = JayaApp.getAnnotationManager().getGroups();
        if (allGroups.isEmpty()) {
            Toast.makeText(this, R.string.bookmark_no_groups, Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] names  = new String[allGroups.size()];
        final boolean[] checked = new boolean[allGroups.size()];
        for (int i = 0; i < allGroups.size(); i++) {
            BookmarkGroup g = allGroups.get(i);
            names[i]   = g.getName();
            checked[i] = annotation.isInGroup(g.getId());
        }

        final List<BookmarkGroup> groupsRef = allGroups;
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.bookmark_assign_groups))
                .setMultiChoiceItems(names, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checked[which] = isChecked;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<String> selectedIds = new ArrayList<>();
                        for (int i = 0; i < groupsRef.size(); i++) {
                            if (checked[i]) selectedIds.add(groupsRef.get(i).getId());
                        }
                        JayaApp.getAnnotationManager().setAnnotationGroups(
                                annotation.getKey(), selectedIds);
                        JayaApp.saveMRUAndAnnotationsIfDirty();
                        refreshList();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmDeleteAnnotation(final Annotation annotation) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.bookmark_delete))
                .setMessage(annotation.getName())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JayaApp.getAnnotationManager().removeAnnotation(annotation);
                        JayaApp.saveMRUAndAnnotationsIfDirty();
                        refreshList();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
