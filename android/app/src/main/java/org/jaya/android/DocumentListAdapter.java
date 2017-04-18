package org.jaya.android;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.apache.lucene.document.Document;
import org.jaya.search.ResultDocument;
import org.jaya.util.Constatants;

import java.util.List;

/**
 * Created by murthy on 08/04/17.
 */

class DocumentListAdapter extends BaseAdapter {

    //private ListViewRowClickListener mRowClickListener;
    private List<ResultDocument> mDocumentList;
    private Activity mActivity;

    public DocumentListAdapter(Activity activity, List<ResultDocument> documentList) {
        mDocumentList = documentList;
        mActivity = activity;
        //mRowClickListener = new ListViewRowClickListener();
    }

    public void setDocumentListAndUpdateView(List<ResultDocument> documentList){
        mDocumentList = documentList;
        notifyDataSetChanged();
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
            mDocContentesTextView.setTextSize(24);

            mDocument = item;
            refresh();
        }

        private void refresh() {
            Document doc = mDocument.getDoc();
            if (doc == null)
                return;
            mDocPathTextView.setText(doc.get(Constatants.FIELD_PATH));
            String contents = mDocument.getDocContentsForScriptType(PreferencesManager.getPreferredOutputScriptType());
            mDocContentesTextView.setText(contents);
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