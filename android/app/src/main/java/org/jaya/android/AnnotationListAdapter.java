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

class AnnotationListAdapter extends BaseAdapter implements JayaDocListView.IListAdapterWithScaleFactor {

    //private ListViewRowClickListener mRowClickListener;
    private float mScaleFactor = 1.0f;
    private List<Annotation> mAnnotationList;
    //private SearchResult mSearchResult = null;
    private Activity mActivity;

    public AnnotationListAdapter(Activity activity, List<Annotation> annotationList) {
        mAnnotationList = annotationList;
        mActivity = activity;
        //mRowClickListener = new ListViewRowClickListener();
    }

//    public AnnotationListAdapter(Activity activity, SearchResult searchResult) {
//        mDocumentList = searchResult.getResultDocs();
//        mActivity = activity;
//        mSearchResult = searchResult;
//        //mRowClickListener = new ListViewRowClickListener();
//    }

    public void setAnnotationListAndUpdateView(List<Annotation> annotationList){
        mAnnotationList = annotationList;
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
        return mAnnotationList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAnnotationList.get(position);
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
            thisView = View.inflate(mActivity, R.layout.annotation_list_item_view, null);
            viewHolder = new ViewHolder((Annotation) getItem(position), thisView);

            //thisView.setOnLongClickListener(mRowClickListener);
            //thisView.setOnClickListener(mRowClickListener);
            thisView.setTag(viewHolder);
        } else {
            thisView = convertView;
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.setItem(mAnnotationList.get(position));
        return thisView;
    }

    /**
     * ViewHolder pattern
     * 1. Don't inflate a view when convertView passed in
     * 2. Hold subviews to avoid "findViewById" lookup cost
     */
    class ViewHolder {
        private Annotation mAnnotation;
        private ResultDocument mDocument;
        private View mItemView;
        //private ViewGroup mCell;
        private TextView mDocPathTextView;
        private TextView mDocContentesTextView;
        private TextView mUpdatedTimeTextView;
        private TextView mNameTextView;

        public ViewHolder(Annotation annot, View itemView) {

            mDocPathTextView = (TextView) itemView.findViewById(R.id.list_item_path);
            mDocContentesTextView = (TextView)itemView.findViewById(R.id.list_item_contents);
            mUpdatedTimeTextView = (TextView)itemView.findViewById(R.id.list_item_updated_time);
            mNameTextView = (TextView)itemView.findViewById(R.id.list_item_name);
            //mDocContentesTextView.setTextIsSelectable(true);
            mDocContentesTextView.setLineSpacing(10, 1.0f);
            mAnnotation = annot;
            mDocument = JayaAppUtils.getDoc(mAnnotation);
            int bgColor = JayaAppUtils.getColorForDoc(mDocument.getId());
            mDocContentesTextView.setBackgroundColor(bgColor);
            mDocPathTextView.setBackgroundColor(bgColor);
            itemView.setBackgroundColor(bgColor);
            mItemView = itemView;
            refresh();
        }

        private void refresh() {
            Document doc = mDocument.getDoc();
            if (doc == null)
                return;
            ScriptType preferredScriptType = PreferencesManager.getPreferredOutputScriptType();
            mDocPathTextView.setText(mDocument.getPathSansExtensionForScriptType(preferredScriptType));
            String contents = mDocument.getDocContentsForScriptType(preferredScriptType).trim();
            contents = contents.substring(0, 100) + "...";
            mDocContentesTextView.setText(contents);
            mDocContentesTextView.setTextSize(getClampedFontSize(PreferencesManager.getFontSize()*mScaleFactor));
            int bgColor = JayaAppUtils.getColorForDoc(mDocument.getId());
            mDocContentesTextView.setBackgroundColor(bgColor);
            mDocPathTextView.setBackgroundColor(bgColor);
            String name = mAnnotation.getName();
            if( name.matches("^[\\d].*") ){ //if the name starts with a digit, don't show it
                mNameTextView.setVisibility(View.GONE);
            }
            else{
                mNameTextView.setText(name);
                mNameTextView.setVisibility(View.VISIBLE);
            }
            mUpdatedTimeTextView.setText(TimestampUtils.getHumanReadableElapsedTimeFromNow(mAnnotation.getUpdatedDate()));
            mNameTextView.setBackgroundColor(bgColor);
            mUpdatedTimeTextView.setBackgroundColor(bgColor);
            mItemView.setBackgroundColor(bgColor);
        }

        public void setItem(Annotation item) {
            mAnnotation = item;
            mDocument = JayaAppUtils.getDoc(item);
            refresh();
        }

        public ResultDocument getItem() {
            return mDocument;
        }
    }
}