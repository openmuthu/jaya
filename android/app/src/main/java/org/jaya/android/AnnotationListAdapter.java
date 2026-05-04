package org.jaya.android;

import android.app.Activity;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.apache.lucene.document.Document;
import org.jaya.annotation.Annotation;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.search.ResultDocument;
import org.jaya.util.TimestampUtils;

import java.util.List;

class AnnotationListAdapter extends BaseAdapter implements JayaDocListView.IListAdapterWithScaleFactor {

    private float mScaleFactor = 1.0f;
    private List<Annotation> mAnnotationList;
    private Activity mActivity;

    public AnnotationListAdapter(Activity activity, List<Annotation> annotationList) {
        mAnnotationList = annotationList;
        mActivity = activity;
    }

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
            thisView.setTag(viewHolder);
        } else {
            thisView = convertView;
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.setItem(mAnnotationList.get(position));
        return thisView;
    }

    class ViewHolder {
        private Annotation mAnnotation;
        private ResultDocument mDocument;
        private View mItemView;
        private TextView mDocPathTextView;
        private TextView mDocContentsTextView;
        private TextView mUpdatedTimeTextView;
        private TextView mNameTextView;
        private TextView mGroupsTextView;

        public ViewHolder(Annotation annot, View itemView) {
            mDocPathTextView    = (TextView) itemView.findViewById(R.id.list_item_path);
            mDocContentsTextView = (TextView) itemView.findViewById(R.id.list_item_contents);
            mUpdatedTimeTextView = (TextView) itemView.findViewById(R.id.list_item_updated_time);
            mNameTextView        = (TextView) itemView.findViewById(R.id.list_item_name);
            mGroupsTextView      = (TextView) itemView.findViewById(R.id.list_item_groups);
            mDocContentsTextView.setLineSpacing(10, 1.0f);
            mAnnotation = annot;
            mDocument = JayaAppUtils.getDoc(mAnnotation);
            int bgColor = JayaAppUtils.getColorForDoc(mDocument.getId());
            mDocContentsTextView.setBackgroundColor(bgColor);
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
            contents = contents.substring(0, Math.min(100, contents.length())) + "...";
            mDocContentsTextView.setText(contents);
            mDocContentsTextView.setTextSize(getClampedFontSize(PreferencesManager.getFontSize()*mScaleFactor));
            int bgColor = JayaAppUtils.getColorForDoc(mDocument.getId());
            mDocContentsTextView.setBackgroundColor(bgColor);
            mDocPathTextView.setBackgroundColor(bgColor);
            mNameTextView.setBackgroundColor(bgColor);
            mUpdatedTimeTextView.setBackgroundColor(bgColor);
            mItemView.setBackgroundColor(bgColor);

            String name = mAnnotation.getName();
            if (name.matches("^[\\d].*")) {
                mNameTextView.setVisibility(View.GONE);
            } else {
                mNameTextView.setText(name);
                mNameTextView.setVisibility(View.VISIBLE);
            }
            mUpdatedTimeTextView.setText(TimestampUtils.getHumanReadableElapsedTimeFromNow(mAnnotation.getUpdatedDate()));

            String groupNames = JayaApp.getAnnotationManager().getGroupNamesString(mAnnotation);
            if (groupNames.isEmpty()) {
                mGroupsTextView.setVisibility(View.GONE);
            } else {
                mGroupsTextView.setText(groupNames);
                mGroupsTextView.setVisibility(View.VISIBLE);
            }
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
