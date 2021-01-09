package org.jaya.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jaya.indexsync.IndexCatalogue;
import org.jaya.indexsync.IndexCatalogueItemDownloader;
import org.jaya.indexsync.IndexCatalogueItemInstaller;
import org.jaya.scriptconverter.SCUtils;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.search.JayaIndexMetadata;
import org.jaya.search.SearchResult;
import org.jaya.util.FileDownloader;
import org.jaya.util.StringUtils;
import org.jaya.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created by murthy on 07/05/17.
 */

public class IndexCatalogListAdapter extends BaseAdapter {

    private IndexCatalogue mIndexCatalogue = IndexCatalogue.getInstance();
    private IndexCatalogueItemDownloader mIndexCatalogueItemDownloader = IndexCatalogueItemDownloader.getInstance();
    private List<IndexCatalogue.Item> mItemList = new ArrayList<>();
    private Context mContext;
    ProgressDialog progressDialog = null;

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
        Set<String> itemNames = mIndexCatalogue.getItemNames();
        if( itemNames == null )
            return;
        for(String itemName: itemNames ){
            mItemList.add(mIndexCatalogue.getItemByName(itemName));
        }

        Collections.sort(mItemList, new Comparator<IndexCatalogue.Item>() {
            @Override
            public int compare(IndexCatalogue.Item it1, IndexCatalogue.Item it2) {
                ScriptType scriptType = PreferencesManager.getPreferredOutputScriptType();
                return it1.getNameInPreferredScript(scriptType).compareTo(it2.getNameInPreferredScript(scriptType));
            }
        });
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
        private Button mBtnRemove;
        private Button mBtnInfo;
        private Button mBtnDownloadOrUpdate;
        private Button mBtnOpen;
        private ProgressBar mProgressBar;

        private void showProgressBar(){
            if( progressDialog == null )
                progressDialog = new ProgressDialog(mBtnInfo.getContext());
            if( progressDialog != null ) {
                progressDialog.setMessage(mBtnInfo.getResources().getString(R.string.please_wait));
                progressDialog.show();
            }
        }

        private void dismissProgressBar(){
            if(progressDialog != null && progressDialog.isShowing() )
                progressDialog.dismiss();
            progressDialog = null;
        }

        private String convertCatalogItemDetailsToPreferredScript(String itemDetails){
            String[] lines = itemDetails.split(JayaIndexMetadata.MD_REC_DELEMITER);
            String[] convertedLines = new String[lines.length];
            int i = 0;
            for(String line:lines){
                convertedLines[i] = SCUtils.convertStringToScript(Utils.removeExtension(line), PreferencesManager.getPreferredOutputScriptType());
                i++;
            }
            Arrays.sort(convertedLines);
            return StringUtils.join("\n", convertedLines);
        }

        public ViewHolder(IndexCatalogue.Item item, View itemView) {

            mCatalogItemNameView = (TextView) itemView.findViewById(R.id.index_catalogue_item_name);
            mCatalogItemSizeView = (TextView) itemView.findViewById(R.id.index_catalogue_item_size);
            mBtnInfo = (Button)itemView.findViewById(R.id.btnIndexCatalogueItemInfo);
            mBtnDownloadOrUpdate = (Button)itemView.findViewById(R.id.btnIndexCatalogueDownloadOrUpdate);
            mBtnOpen = (Button)itemView.findViewById(R.id.btnIndexCatalogueItemOpen);
            mBtnRemove = (Button)itemView.findViewById(R.id.btnIndexCatalogueItemRemove);
            mProgressBar = (ProgressBar)itemView.findViewById(R.id.progressBarIndexCatalogueItem);
            mBytesDownloadedView = (TextView) itemView.findViewById(R.id.index_catalogue_item_bytes_downloaded);

            int bgColor = JayaAppUtils.getRandomColor();
            itemView.setBackgroundColor(bgColor);
            mCatalogItem = item;
            mBtnInfo.setOnClickListener(new View.OnClickListener() {
                //ProgressDialog progressDialog = null;
                @Override
                public void onClick(View v) {
                    showProgressBar();
                    mCatalogItem.getIncludedFiles(new IndexCatalogue.ItemDetailsCallback(){
                        @Override
                        public void onDataArrived(final String data, final int error){
                            JayaApp.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgressBar();
                                    //if( error == 0 ) {
                                        AlertDialog alertDialog = new AlertDialog.Builder(mBtnInfo.getContext()).create();
                                        alertDialog.setTitle(mBtnInfo.getResources().getString(R.string.included_files));
                                        String convertedData = convertCatalogItemDetailsToPreferredScript(data);
                                        alertDialog.setMessage(convertedData);
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

            mBtnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    showProgressBar();
                    mBtnDownloadOrUpdate.setEnabled(false);
                    IndexCatalogueItemInstaller installer = IndexCatalogueItemInstaller.getInstance();
                    installer.unistallItem(mCatalogItem, IndexCatalogue.getInstance().getAppIndexFolderPath(),
                            new IndexCatalogueItemInstaller.OnUninstalledCallback() {
                                @Override
                                public void onUninstalled(int error, IndexCatalogue.Item item) {
                                    JayaApp.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dismissProgressBar();
                                            mBtnDownloadOrUpdate.setEnabled(true);
                                            refresh();
                                            view.invalidate();
                                        }
                                    });
                                }
                            });
                }
            });

            mBtnOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String name = mCatalogItem.getName();
                        String[] parts = mCatalogItem.getName().split("_");
                        for(int i=parts.length-1;i>=0;i--){
                            try {
                                Integer.parseInt(parts[i]);
                            }catch (NumberFormatException ex){
                                name = parts[i];
                                break;
                            }
                        }
//                        Intent intent = new Intent(mContext, SearchableActivity.class);
//                        intent.setAction(Intent.ACTION_SEARCH);
//                        intent.putExtra(SearchManager.QUERY, ","+name);
//                        mContext.startActivity(intent);

                        SearchResult sr =  JayaApp.getSearcher().searchITRANSString(","+name);
                        if( sr.getResultDocs().size() > 0 ){
                            int docId = sr.getResultDocs().get(0).getId();
                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.setAction(JayaApp.INTENT_OPEN_DOCUMENT_ID);
                            intent.putExtra("documentId", docId);
                            mContext.startActivity(intent);
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
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

        private void updateRemoveButtonState(){
            if( mCatalogItem == null )
                return;
            if( mCatalogItem.getIsInstalled() ){
                mBtnRemove.setEnabled(true);
                mBtnOpen.setEnabled(true);
            }
            else{
                mBtnRemove.setEnabled(false);
                mBtnOpen.setEnabled(false);
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
            updateRemoveButtonState();
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
