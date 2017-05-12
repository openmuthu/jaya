package org.jaya.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jaya.indexsync.IndexCatalogue;
import org.jaya.indexsync.IndexCatalogueItemDownloader;
import org.jaya.scriptconverter.SCUtils;
import org.jaya.util.FileDownloader;
import org.jaya.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

/**
 * Created by murthy on 07/05/17.
 */

public class IndexCatalogListAdapter extends BaseAdapter {

    private IndexCatalogue mIndexCatalogue = IndexCatalogue.getInstance();
    private IndexCatalogueItemDownloader mIndexCatalogueItemDownloader = IndexCatalogueItemDownloader.getInstance();
    private List<IndexCatalogue.Item> mItemList = new ArrayList<>();
    private Context mContext;

    public IndexCatalogListAdapter(Context context){
        mContext = context;
        setIndexCatalogue(mIndexCatalogue);
    }

    public void setIndexCatalogueAndUpdateView(IndexCatalogue indexCatalogue){
        setIndexCatalogue(indexCatalogue);
        notifyDataSetChanged();
    }

    private void setIndexCatalogue(IndexCatalogue indexCatalogue){
        mIndexCatalogue = indexCatalogue;
        mItemList.clear();
        for(String itemName: mIndexCatalogue.getItemNames() ){
            mItemList.add(mIndexCatalogue.getItemByName(itemName));
        }
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View thisView;
        IndexCatalogListAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            thisView = View.inflate(mContext, R.layout.index_catalogue_list_item, null);
            viewHolder = new IndexCatalogListAdapter.ViewHolder((IndexCatalogue.Item) getItem(position), thisView);

            thisView.setTag(viewHolder);
        } else {
            thisView = convertView;
            viewHolder = (IndexCatalogListAdapter.ViewHolder) convertView.getTag();
        }

        viewHolder.setItem(mItemList.get(position));
        return thisView;
    }

    /**
     * ViewHolder pattern
     * 1. Don't inflate a view when convertView passed in
     * 2. Hold subviews to avoid "findViewById" lookup cost
     */
    class ViewHolder {
        private IndexCatalogue.Item mCatalogItem;
        //private ViewGroup mCell;
        private TextView mCatalogItemNameView;
        private TextView mCatalogItemSizeView;
        private TextView mBytesDownloadedView;
        private Button mBtnInfo;
        private Button mBtnDownloadOrUpdate;
        private ProgressBar mProgressBar;

        public ViewHolder(IndexCatalogue.Item item, View itemView) {

            mCatalogItemNameView = (TextView) itemView.findViewById(R.id.index_catalogue_item_name);
            mCatalogItemSizeView = (TextView) itemView.findViewById(R.id.index_catalogue_item_size);
            mBtnInfo = (Button)itemView.findViewById(R.id.btnIndexCatalogueItemInfo);
            mBtnDownloadOrUpdate = (Button)itemView.findViewById(R.id.btnIndexCatalogueDownloadOrUpdate);
            mProgressBar = (ProgressBar)itemView.findViewById(R.id.progressBarIndexCatalogueItem);
            mBytesDownloadedView = (TextView) itemView.findViewById(R.id.index_catalogue_item_bytes_downloaded);

            int bgColor = JayaAppUtils.getRandomColor();
            itemView.setBackgroundColor(bgColor);
            mCatalogItem = item;
            mBtnInfo.setOnClickListener(new View.OnClickListener() {
                ProgressDialog progressDialog = null;
                @Override
                public void onClick(View v) {
                    if( progressDialog == null )
                        progressDialog = new ProgressDialog(mBtnInfo.getContext());
                    mCatalogItem.getIncludedFiles(new IndexCatalogue.ItemDetailsCallback(){
                        @Override
                        public void onDataArrived(final String data, final int error){
                            JayaApp.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if( progressDialog.isShowing() )
                                        progressDialog.dismiss();
                                    progressDialog = null;
                                    //if( error == 0 ) {
                                        AlertDialog alertDialog = new AlertDialog.Builder(mBtnInfo.getContext()).create();
                                        alertDialog.setTitle(mBtnInfo.getResources().getString(R.string.included_files));
                                        alertDialog.setMessage(data);
                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        alertDialog.show();
                                        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
                                        textView.setTextSize(12);
                                    //}
                                }
                            });
                        }
                    });
                    if( progressDialog != null ) {
                        progressDialog.setMessage(mBtnInfo.getResources().getString(R.string.please_wait));
                        progressDialog.show();
                    }
                }
            });

            mBtnDownloadOrUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final TextView tv = (TextView)v;
                    if( tv.getText().equals(tv.getResources().getString(R.string.cancel)) ){
                        mIndexCatalogueItemDownloader.cancelItemDownload(mCatalogItem.getName());
                        refresh();
                        tv.invalidate();
                    }
                    else if(tv.getText().equals(tv.getResources().getString(R.string.download))
                            || tv.getText().equals(tv.getResources().getString(R.string.update)) ){
                        if( !JayaAppUtils.isNetworkAvailable() ){
                            AlertDialog alertDialog = new AlertDialog.Builder(tv.getContext()).create();
                            alertDialog.setTitle(tv.getResources().getString(R.string.alert));
                            alertDialog.setMessage(tv.getResources().getString(R.string.no_internet_message));
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                            return;
                        }
                        mIndexCatalogueItemDownloader.addItemToDownloadQueue(mCatalogItem.getName(), new FileDownloader.ProgressCallback() {
                            @Override
                            public void onContentLength(long contentLength) {

                            }

                            @Override
                            public void onBytesDownloaded(long totalBytesDownloaded) {
                                JayaApp.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refresh();
                                        tv.invalidate();
                                    }
                                });
                            }

                            @Override
                            public void onComplete(final int error) {
                                JayaApp.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refresh();
                                        tv.invalidate();
                                        if( error == 0 )
                                            JayaApp.getSearcher().reopenIndex();
                                    }
                                });
                            }
                        });
                        refresh();
                        tv.invalidate();
                    }
                }
            });
            refresh();
        }

        private void updateProgressBarState(){
            if( mIndexCatalogueItemDownloader.isItemDownloading(mCatalogItem.getName()) ){
                mProgressBar.setIndeterminate(true);
                mProgressBar.setVisibility(View.VISIBLE);
                mBytesDownloadedView.setVisibility(View.VISIBLE);
                String text = Utils.getHumanReadableSize(mIndexCatalogueItemDownloader.getDownloadedBytesForItem(mCatalogItem.getName()));
                mBytesDownloadedView.setText(text);
            }
            else{
                mProgressBar.setVisibility(View.INVISIBLE);
                mBytesDownloadedView.setVisibility(View.INVISIBLE);
            }
        }

        private void updateDownloadButtonState(){
            TextView tv = mBtnDownloadOrUpdate;
            tv.setEnabled(true);
            if( mIndexCatalogueItemDownloader.isItemDownloading(mCatalogItem.getName()) ){
                tv.setText(tv.getResources().getString(R.string.cancel));
            }
            else if( mCatalogItem.getIsInstalled()){
                if( mCatalogItem.getIsUpdateAvailable() )
                    tv.setText(tv.getResources().getString(R.string.update));
                else {
                    tv.setText(tv.getResources().getString(R.string.uptodate));
                    tv.setEnabled(false);
                }
            }
            else{
                tv.setText(tv.getResources().getString(R.string.download));
            }
        }

        private void refresh() {
            String nameInPreferedScript = SCUtils.convertStringToScript(mCatalogItem.getName(), PreferencesManager.getPreferredOutputScriptType());
            mCatalogItemNameView.setText(nameInPreferedScript);
            mBtnInfo.setText(mBtnInfo.getResources().getString(R.string.learn_more));
            mCatalogItemSizeView.setText(Utils.getHumanReadableSize(mCatalogItem.getSize()));
            updateDownloadButtonState();
            updateProgressBarState();
        }

        public void setItem(IndexCatalogue.Item item) {
            mCatalogItem = item;
            refresh();
        }

        public IndexCatalogue.Item getItem() {
            return mCatalogItem;
        }
    }
}
