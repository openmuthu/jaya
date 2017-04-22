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
import org.jaya.scriptconverter.ScriptType;
import org.jaya.search.ResultDocument;
import org.jaya.search.SearchResult;
import org.jaya.util.Constatants;

import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * Created by murthy on 08/04/17.
 */

class DocumentListAdapter extends BaseAdapter implements JayaDocListView.IListAdapterWithScaleFactor {

    //private ListViewRowClickListener mRowClickListener;
    private float mScaleFactor = 1.0f;
    private List<ResultDocument> mDocumentList;
    private SearchResult mSearchResult = null;
    private Activity mActivity;

    public DocumentListAdapter(Activity activity, List<ResultDocument> documentList) {
        mDocumentList = documentList;
        mActivity = activity;
        //mRowClickListener = new ListViewRowClickListener();
    }

    public DocumentListAdapter(Activity activity, SearchResult searchResult) {
        mDocumentList = searchResult.getResultDocs();
        mActivity = activity;
        mSearchResult = searchResult;
        //mRowClickListener = new ListViewRowClickListener();
    }

    public void setDocumentListAndUpdateView(List<ResultDocument> documentList){
        mDocumentList = documentList;
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
        return mDocumentList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDocumentList.get(position);
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
            thisView = View.inflate(mActivity, R.layout.container_list_item_view, null);
            viewHolder = new ViewHolder((ResultDocument) getItem(position), thisView);

            //thisView.setOnLongClickListener(mRowClickListener);
            //thisView.setOnClickListener(mRowClickListener);
            thisView.setTag(viewHolder);
        } else {
            thisView = convertView;
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.setItem(mDocumentList.get(position));
        return thisView;
    }

    /**
     * ViewHolder pattern
     * 1. Don't inflate a view when convertView passed in
     * 2. Hold subviews to avoid "findViewById" lookup cost
     */
    class ViewHolder {
        private ResultDocument mDocument;
        //private ViewGroup mCell;
        private TextView mDocPathTextView;
        private TextView mDocContentesTextView;

        public ViewHolder(ResultDocument item, View itemView) {

            mDocPathTextView = (TextView) itemView.findViewById(R.id.list_item_path);
            mDocContentesTextView = (TextView)itemView.findViewById(R.id.list_item_contents);
            //mDocContentesTextView.setTextIsSelectable(true);
            mDocContentesTextView.setLineSpacing(10, 1.0f);
            int bgColor = JayaAppUtils.getColorForDoc(item.getId());
            mDocContentesTextView.setBackgroundColor(bgColor);
            mDocPathTextView.setBackgroundColor(bgColor);

            mDocument = item;
            refresh();
        }

        private void refresh() {
            Document doc = mDocument.getDoc();
            if (doc == null)
                return;
            mDocPathTextView.setText(doc.get(Constatants.FIELD_PATH));
            ScriptType preferredScriptType = PreferencesManager.getPreferredOutputScriptType();
            String contents = mDocument.getDocContentsForScriptType(preferredScriptType).trim();
            if( mSearchResult != null ) {
                contents = mSearchResult.getSpannedStringBasedOnCurrentQuery(contents, preferredScriptType);
                Spanned spannedText = Html.fromHtml(contents);
                mDocContentesTextView.setText(spannedText);
            }
            else
                mDocContentesTextView.setText(contents);
            mDocContentesTextView.setTextSize(getClampedFontSize(PreferencesManager.getFontSize()*mScaleFactor));
            int bgColor = JayaAppUtils.getColorForDoc(mDocument.getId());
            mDocContentesTextView.setBackgroundColor(bgColor);
            mDocPathTextView.setBackgroundColor(bgColor);
        }

        public void setItem(ResultDocument item) {
            mDocument = item;
            refresh();
        }

        public ResultDocument getItem() {
            return mDocument;
        }
    }
}