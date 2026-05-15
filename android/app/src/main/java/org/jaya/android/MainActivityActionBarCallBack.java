package org.jaya.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.jaya.annotation.Annotation;
import org.jaya.annotation.AnnotationManager;
import org.jaya.annotation.BookmarkGroup;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.search.ResultDocument;
import org.jaya.util.TimestampUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class MainActivityActionBarCallBack implements ActionMode.Callback {

    private ResultDocument mResultDocument;
    private WeakReference<Activity> mActivity;

    public MainActivityActionBarCallBack(ResultDocument rd, Activity activity) {
        mResultDocument = rd;
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        try {
            ResultDocument doc = (ResultDocument) mode.getTag();
            switch (item.getItemId()) {
                case R.id.action_bookmark: {
                    String addBookmarkString = JayaApp.getAppContext().getResources()
                            .getString(R.string.action_bookmark_add);
                    String deleteBookmarkString = JayaApp.getAppContext().getResources()
                            .getString(R.string.action_bookmark_delete);
                    if (item.getTitle().equals(addBookmarkString)) {
                        item.setTitle(deleteBookmarkString);
                        Annotation annotation = JayaApp.getAnnotationManager()
                                .addAnnotation(doc, TimestampUtils.nowAsString());
                        if (annotation != null) {
                            showGroupPickerDialog(annotation, false);
                        }
                    } else {
                        item.setTitle(addBookmarkString);
                        JayaApp.getAnnotationManager().removeAnnotation(
                                new Annotation(doc, TimestampUtils.nowAsString(), new Date()));
                        JayaApp.saveMRUAndAnnotationsIfDirty();
                    }
                    break;
                }
                case R.id.action_bookmark_share: {
                    Annotation existing = JayaApp.getAnnotationManager().getAnnotation(doc);
                    String sharePath = doc.getDoc() != null
                            ? doc.getDoc().get(org.jaya.util.Constatants.FIELD_PATH) : "";
                    String shareLocalId = doc.getDoc() != null
                            ? doc.getDoc().get(org.jaya.util.Constatants.FIELD_DOC_LOCAL_ID) : "";
                    String shareFp = existing != null
                            ? existing.getContentFingerprint()
                            : Annotation.buildFingerprint(doc.getDoc() != null
                                    ? doc.getDoc().get(org.jaya.util.Constatants.FIELD_CONTENTS) : "");
                    String shareName = existing != null ? existing.getName() : "";
                    String url = BookmarkDeepLink.buildUrl(sharePath, shareLocalId, shareFp, shareName);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, url);
                    Activity a = mActivity.get();
                    if (a != null) a.startActivity(Intent.createChooser(share, null));
                    break;
                }
                case R.id.action_copy: {
                    ClipboardManager clipboard = (ClipboardManager) JayaApp.getAppContext()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    StringBuffer buf = new StringBuffer();
                    ScriptType dstType = PreferencesManager.getPreferredOutputScriptType();
                    int numDocsToCopy = PreferencesManager.getNumberOfAdjacentDocsToCopy();
                    List<ResultDocument> adjacentDocs = JayaApp.getSearcher()
                            .getAdjacentDocs(doc.getId(), numDocsToCopy, 1);
                    buf.append(doc.getDocContentsForScriptType(dstType));
                    for (ResultDocument document : adjacentDocs) {
                        buf.append(document.getDocContentsForScriptType(dstType));
                    }
                    buf.append(System.getProperty("line.separator")).append("@@@[");
                    buf.append(doc.getPathSansExtensionForScriptType(dstType));
                    buf.append("]");
                    ClipData clip = ClipData.newPlainText("", buf.toString());
                    clipboard.setPrimaryClip(clip);
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.main_activity_list_item_context_menu, menu);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        String addBookmarkString = JayaApp.getAppContext().getResources()
                .getString(R.string.action_bookmark_add);
        String deleteBookmarkString = JayaApp.getAppContext().getResources()
                .getString(R.string.action_bookmark_delete);
        MenuItem bookmarkMenu = menu.findItem(R.id.action_bookmark);
        if (JayaApp.getAnnotationManager().annotationExists(mResultDocument))
            bookmarkMenu.setTitle(deleteBookmarkString);
        else
            bookmarkMenu.setTitle(addBookmarkString);
        mode.setTitle("");
        return false;
    }

    // -------------------------------------------------------------------------
    // Group picker shown immediately after a bookmark is added
    // -------------------------------------------------------------------------

    /**
     * Show a group-assignment dialog for the given annotation.
     *
     * @param annotation  the newly-added (or existing) bookmark
     * @param initialSave if true, save&dismiss has already been called by caller
     */
    private void showGroupPickerDialog(final Annotation annotation, boolean initialSave) {
        Activity activity = mActivity.get();
        if (activity == null || activity.isFinishing()) return;

        buildAndShowGroupPicker(activity, annotation);
    }

    private void buildAndShowGroupPicker(final Activity activity, final Annotation annotation) {
        final AnnotationManager am = JayaApp.getAnnotationManager();
        final List<BookmarkGroup> groups = am.getGroups();

        final String[] names    = new String[groups.size()];
        final boolean[] checked = new boolean[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            names[i]   = groups.get(i).getName();
            checked[i] = annotation.isInGroup(groups.get(i).getId());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.bookmark_assign_groups);

        if (groups.isEmpty()) {
            // No groups yet — show hint text so the dialog isn't confusing
            builder.setMessage(R.string.bookmark_no_groups_hint);
        } else {
            builder.setMultiChoiceItems(names, checked,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            checked[which] = isChecked;
                        }
                    });
        }

        // OK — persist the selection
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<String> selectedIds = new ArrayList<>();
                for (int i = 0; i < groups.size(); i++) {
                    if (checked[i]) selectedIds.add(groups.get(i).getId());
                }
                am.setAnnotationGroups(annotation.getKey(), selectedIds);
                JayaApp.saveMRUAndAnnotationsIfDirty();
            }
        });

        // Skip — bookmark already saved, just close without group assignment
        builder.setNegativeButton(R.string.bookmark_groups_skip, null);

        // "New Group" — create an auto-named group then reopen the dialog
        builder.setNeutralButton(R.string.group_new, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Save current selection before dismissing
                List<String> selectedIds = new ArrayList<>();
                for (int i = 0; i < groups.size(); i++) {
                    if (checked[i]) selectedIds.add(groups.get(i).getId());
                }
                am.setAnnotationGroups(annotation.getKey(), selectedIds);

                // Show a rename dialog so the user can name the new group
                showNewGroupDialog(activity, annotation);
            }
        });

        builder.show();
    }

    private void showNewGroupDialog(final Activity activity, final Annotation annotation) {
        final AnnotationManager am = JayaApp.getAnnotationManager();
        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.group_rename_hint);
        final String defaultName = am.generateGroupName();
        input.setText(defaultName);
        input.selectAll();

        new AlertDialog.Builder(activity)
                .setTitle(R.string.group_new)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString().trim();
                        if (name.isEmpty()) name = defaultName;
                        BookmarkGroup newGroup = am.addGroup(name);
                        // Pre-assign the new group, then reopen picker
                        annotation.addGroupId(newGroup.getId());
                        buildAndShowGroupPicker(activity, annotation);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User cancelled group creation; reopen the picker without the new group
                        buildAndShowGroupPicker(activity, annotation);
                    }
                })
                .show();
    }
}
