package org.jaya.android;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.jaya.search.VerseIndex;

/**
 * Bottom-sheet dialog that displays a collapsible chapter/verse tree for a
 * single text file and notifies the caller when the user selects a verse.
 *
 * <p>The sheet slides up from the bottom of the screen using a custom window
 * animation and occupies the full screen width.
 */
class VerseNavSheet extends Dialog {

    interface OnVerseSelectedListener {
        void onVerseSelected(int docId);
    }

    VerseNavSheet(Context context,
                  final VerseIndex index,
                  final String title,
                  final OnVerseSelectedListener listener) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.verse_nav_sheet);

        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        window.getAttributes().windowAnimations = R.style.VerseNavSheetAnimation;

        TextView titleView = (TextView) findViewById(R.id.verse_nav_title);
        titleView.setText(title);

        final ListView listView = (ListView) findViewById(R.id.verse_nav_list);
        final VerseTreeAdapter adapter = new VerseTreeAdapter(context, index);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VerseTreeAdapter.Item item = (VerseTreeAdapter.Item) adapter.getItem(position);
                if (item.type == VerseTreeAdapter.TYPE_CHAPTER) {
                    adapter.toggleChapter(item.chapterKey);
                } else {
                    dismiss();
                    if (listener != null) listener.onVerseSelected(item.docId);
                }
            }
        });
    }
}
