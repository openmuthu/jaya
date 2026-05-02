package org.jaya.android;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.jaya.scriptconverter.SCUtils;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.search.VerseIndex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Flat-list adapter backing the verse navigation bottom sheet.
 *
 * <p>The list shows chapter rows (type {@link #TYPE_CHAPTER}) and, when a chapter
 * is expanded, verse rows (type {@link #TYPE_VERSE}) indented below it.  Tapping
 * a chapter row toggles it open/closed; tapping a verse row triggers navigation.
 */
class VerseTreeAdapter extends BaseAdapter {

    static final int TYPE_CHAPTER = 0;
    static final int TYPE_VERSE   = 1;

    /** A single row in the flat list. */
    static class Item {
        final int    type;
        final String label;
        /** Chapter key (e.g. "1") — set for TYPE_CHAPTER rows. */
        final String chapterKey;
        /** Lucene docId — set for TYPE_VERSE rows; -1 for chapters. */
        final int    docId;

        Item(int type, String label, String chapterKey, int docId) {
            this.type       = type;
            this.label      = label;
            this.chapterKey = chapterKey;
            this.docId      = docId;
        }

        static Item chapter(String key) {
            return new Item(TYPE_CHAPTER, key, key, -1);
        }

        static Item verse(String verseKey, int docId) {
            return new Item(TYPE_VERSE, verseKey, null, docId);
        }
    }

    private final Context    mContext;
    private final VerseIndex mIndex;
    private final List<Item> mItems          = new ArrayList<Item>();
    private final Set<String> mExpandedChapters = new HashSet<String>();

    VerseTreeAdapter(Context context, VerseIndex index) {
        mContext = context;
        mIndex   = index;
        rebuild();
    }

    /** Toggle the expanded/collapsed state of {@code chapterKey} and refresh. */
    void toggleChapter(String chapterKey) {
        if (mExpandedChapters.contains(chapterKey)) {
            mExpandedChapters.remove(chapterKey);
        } else {
            mExpandedChapters.add(chapterKey);
        }
        rebuild();
    }

    // ── BaseAdapter ───────────────────────────────────────────────────────────

    @Override public int getCount()                    { return mItems.size(); }
    @Override public Object getItem(int position)      { return mItems.get(position); }
    @Override public long getItemId(int position)      { return position; }
    @Override public int getViewTypeCount()            { return 2; }
    @Override public int getItemViewType(int position) { return mItems.get(position).type; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = mItems.get(position);
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.verse_nav_item, null);
        }
        TextView arrowView = (TextView) convertView.findViewById(R.id.verse_item_arrow);
        TextView labelView = (TextView) convertView.findViewById(R.id.verse_item_label);

        float fontSp = PreferencesManager.getFontSize();

        if (item.type == TYPE_CHAPTER) {
            boolean expanded = mExpandedChapters.contains(item.chapterKey);
            arrowView.setText(expanded ? "\u25BC" : "\u25B6"); // ▼ or ▶
            String chPreview = chapterPreview(item.chapterKey);
            String chDisplay = chPreview.isEmpty()
                    ? item.label : item.label + "  " + chPreview;
            labelView.setText(chDisplay);
            labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSp);
            labelView.setTextColor(0xFF000000);
            convertView.setPadding(dpToPx(4), dpToPx(6), dpToPx(8), dpToPx(6));
        } else {
            arrowView.setText("");
            String preview = scriptConvert(mIndex.getPreview(item.label));
            String display = preview.isEmpty() ? item.label : item.label + "  " + preview;
            labelView.setText(display);
            labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSp * 0.9f);
            labelView.setTextColor(0xFF444444);
            convertView.setPadding(dpToPx(32), dpToPx(4), dpToPx(8), dpToPx(4));
        }
        return convertView;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void rebuild() {
        mItems.clear();
        for (String chapter : mIndex.getChapters()) {
            mItems.add(Item.chapter(chapter));
            if (mExpandedChapters.contains(chapter)) {
                for (String verse : mIndex.getVersesForChapter(chapter)) {
                    mItems.add(Item.verse(verse, mIndex.getDocId(verse)));
                }
            }
        }
        notifyDataSetChanged();
    }

    /** Returns the script-converted preview for the first verse in {@code chapterKey}. */
    private String chapterPreview(String chapterKey) {
        List<String> verses = mIndex.getVersesForChapter(chapterKey);
        if (verses.isEmpty()) return "";
        return scriptConvert(mIndex.getPreview(verses.get(0)));
    }

    /** Converts an ITRANS preview string to the user's preferred output script. */
    private String scriptConvert(String itrans) {
        return scriptConvert(itrans, PreferencesManager.getPreferredOutputScriptType());
    }

    /** Package-private; accepts an explicit target script so the conversion can be unit-tested. */
    static String scriptConvert(String itrans, ScriptType targetScript) {
        if (itrans.isEmpty()) return itrans;
        return SCUtils.convertStringToScript(itrans, targetScript);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                mContext.getResources().getDisplayMetrics());
    }
}
