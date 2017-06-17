package org.jaya.android;

import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.apache.lucene.document.Document;
import org.jaya.annotation.Annotation;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.search.ResultDocument;
import org.jaya.search.SearchResult;
import org.jaya.util.Constatants;
import org.jaya.util.TimestampUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created by murthy on 08/04/17.
 */

class TableOfContentsListAdapter extends BaseAdapter implements JayaDocListView.IListAdapterWithScaleFactor {

    //private ListViewRowClickListener mRowClickListener;
    private float mScaleFactor = 1.0f;
    private TableOfContentsActivity.TOCItem[] mTOCItems;
    //private SearchResult mSearchResult = null;
    private Activity mActivity;

    public TableOfContentsListAdapter(Activity activity, TableOfContentsActivity.TOCItem[] tocItems) {
        mTOCItems = tocItems;
        mActivity = activity;
    }

    public void setAnnotationListAndUpdateView(TableOfContentsActivity.TOCItem[] tocItems){
        mTOCItems = tocItems;
        notifyDataSetChanged();
    }

    @Override
    public void onScaleBegin(ScaleGestureDetector detector) {
        mScaleFactor = 1.0f;
    }

    @Override
    public void setScaleFactor(float scaleFactor){
        mScaleFactor = scaleFactor;
    }

    @Override
    public void onScaleEnd(float scaleFactor){
        mScaleFactor = 1.0f;
        float currentSize = PreferencesManager.getFontSize();
        PreferencesManager.setFontSize( getClampedFontSize(currentSize * scaleFactor) );
    }

    float getClampedFontSize(float fontSize){
        return Math.max(PreferencesManager.MIN_FONT_SIZE, Math.min(fontSize, PreferencesManager.MAX_FONT_SIZE));
    }

    @Override
    public int getCount() {
        return mTOCItems.length;
    }

    @Override
    public Object getItem(int position) {
        try {
            return mTOCItems[position];
        }catch (ArrayIndexOutOfBoundsException ex){
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View thisView;
        ViewHolder viewHolder;

        if (convertView == null) {
            thisView = View.inflate(mActivity, R.layout.toc_list_item, null);
            viewHolder = new ViewHolder(mTOCItems[position], thisView);

            //thisView.setOnLongClickListener(mRowClickListener);
            //thisView.setOnClickListener(mRowClickListener);
            thisView.setTag(viewHolder);
        } else {
            thisView = convertView;
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.setItem(mTOCItems[position]);
        return thisView;
    }

    /**
     * ViewHolder pattern
     * 1. Don't inflate a view when convertView passed in
     * 2. Hold subviews to avoid "findViewById" lookup cost
     */
    class ViewHolder {
        private TableOfContentsActivity.TOCItem mTOCItem;
        private View mItemView;
        private TextView mDocPathTextView;

        public ViewHolder(TableOfContentsActivity.TOCItem tocItem, View itemView) {

            mDocPathTextView = (TextView) itemView.findViewById(R.id.list_item_path);
            mTOCItem = tocItem;
            int bgColor = JayaAppUtils.getRandomColor();
            mDocPathTextView.setBackgroundColor(bgColor);
            itemView.setBackgroundColor(bgColor);
            mItemView = itemView;
            refresh();
        }

        private void refresh() {
            mDocPathTextView.setText(mTOCItem.getPath());
            int bgColor = JayaAppUtils.getRandomColor();
            mDocPathTextView.setTextSize(getClampedFontSize(PreferencesManager.getFontSize()*mScaleFactor));
            mDocPathTextView.setBackgroundColor(bgColor);
            mItemView.setBackgroundColor(bgColor);
        }

        public void setItem(TableOfContentsActivity.TOCItem item) {
            mTOCItem = item;
            refresh();
        }

        public TableOfContentsActivity.TOCItem getItem() {
            return mTOCItem;
        }
    }
}