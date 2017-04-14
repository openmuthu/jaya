package org.jaya.android;

import android.Manifest;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import org.jaya.search.ResultDocument;
import org.jaya.search.index.LuceneUnicodeFileIndexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {

    public static final int NUM_ITEMS_TO_LOAD_MORE = 4;
    public static final int INITIAL_DOCUMENT_ID = 2500;

    private ProgressDialog mProgress;
    private int mProgressStatus = 0;

    private Handler mHandler = new Handler();

    AssetsManager mAssetsManager;

    List<ResultDocument> mDocumentList = new ArrayList<>();

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.getAction().equals(JayaApp.INTENT_OPEN_DOCUMENT_ID)) {
            int docId = intent.getIntExtra("documentId", 0);
            showDocumentId(docId);
        }
        else
            showDocumentId(INITIAL_DOCUMENT_ID);
    }

    public void showDocumentId(int docId){
        try {
            ResultDocument resDoc = JayaApp.getSearcher().getDoc(docId);
            if( resDoc == null )
                return;
            mDocumentList.clear();
            List<ResultDocument> nextDocs = JayaApp.getSearcher().getAdjacentDocs(docId, NUM_ITEMS_TO_LOAD_MORE, 1);
            //List<ResultDocument> prevDocs = JayaApp.getSearcher().getAdjacentDocs(docId, NUM_ITEMS_TO_LOAD_MORE, -1);
            //mDocumentList.addAll(prevDocs);
            mDocumentList.add(resDoc);
            mDocumentList.addAll(nextDocs);
            setListAdapter();

        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAssetsManager = new AssetsManager(getApplicationContext());
        PermissionRequestor.requestPermissionIfRequired(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionRequestor.WRITE_EXTERNAL_STORAGE_PERMISSSION_REQUEST_ID);

        Intent intent = getIntent();
        if( intent != null )
            handleIntent(intent);

        //showDocumentId(3000);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        switch(requestCode)
        {
            case PermissionRequestor.WRITE_EXTERNAL_STORAGE_PERMISSSION_REQUEST_ID:
                {
                    if( mAssetsManager == null )
                        return;
                    mAssetsManager.copyResourcesToCacheIfRequired(this);
                    JayaApp.getSearcher().createIndexSearcherIfRequired();
                    indexFiles(false);
                }
                break;
        }
    }

    private void setListAdapter(){
        final ListView listView = getListView();

        DocumentListAdapter docListAdapter = new DocumentListAdapter(this, mDocumentList);
        listView.setAdapter(docListAdapter);//sets the adapter for listView

        listView.setOnScrollListener(new AbsListView.OnScrollListener(){

            @Override
            public void onScroll(AbsListView view,
                                 int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
            }
            @Override
            public void onScrollStateChanged(AbsListView view,int scrollState) {
                try {
                    int threshold = 1;
                    int count = listView.getCount();

                    Log.d(JayaApp.APP_NAME, "onScrollStateChanged(): state " + scrollState);
                    Log.d(JayaApp.APP_NAME, "onScrollStateChanged(): scrollY: " + listView.getScrollY() + " top:" + listView.getTop());
                    if (scrollState == SCROLL_STATE_IDLE) {
                        Log.d(JayaApp.APP_NAME, "onScrollStateChanged(): lastvisible position: " + listView.getLastVisiblePosition());
                        if (listView.getLastVisiblePosition() >= count - threshold) {
                            Log.d(JayaApp.APP_NAME, "onScrollStateChanged(): fetching next docs ");
                            ResultDocument lastDoc = mDocumentList.get(mDocumentList.size() - 1);
                            List<ResultDocument> nextDocs = JayaApp.getSearcher().getAdjacentDocs(lastDoc.getId(), NUM_ITEMS_TO_LOAD_MORE, 1);
                            mDocumentList.addAll(nextDocs);
                            DocumentListAdapter listAdapter = (DocumentListAdapter)view.getAdapter();
                            listAdapter.setDocumentListAndUpdateView(mDocumentList);
                        }
                        else if (listView.getFirstVisiblePosition() <= 0) {
                            int oldFirstPos = listView.getFirstVisiblePosition();
                            Log.d(JayaApp.APP_NAME, "onScrollStateChanged(): fetching previous docs ");
                            ResultDocument lastDoc = mDocumentList.get(0);
                            List<ResultDocument> prevDocs = JayaApp.getSearcher().getAdjacentDocs(lastDoc.getId(), NUM_ITEMS_TO_LOAD_MORE, -1);
                            int index = listView.getFirstVisiblePosition() + prevDocs.size();
                            int top = listView.getTop();
                            mDocumentList.addAll(0, prevDocs);
                            DocumentListAdapter listAdapter = (DocumentListAdapter)view.getAdapter();
                            listAdapter.setDocumentListAndUpdateView(mDocumentList);
                            listView.setSelectionFromTop(index, top);
                        }
                    }
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        ComponentName cn = new ComponentName(this, SearchableActivity.class);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(cn));
        return true;
    }

    protected void indexFiles(boolean bForceReIndexing)
    {
        return;
//        if( !JayaApp.isIndexingRequired() && !bForceReIndexing)
//            return;
//        if( mProgress != null )
//        {
//            // Indexing is already in progress.. just return
//            return;
//        }
//        mProgress = new ProgressDialog(this);
//        mProgress.setIndeterminate(true);
//        mProgress.setMessage("Please wait while indexing is in progress... This may take a few minutes.");
//        mProgress.show();
//        Thread t =  new Thread(new Runnable() {
//            public void run() {
//                try {
//                    LuceneUnicodeFileIndexer indexer = new LuceneUnicodeFileIndexer();
//                    indexer.createIndex(JayaApp.getSearchIndexFolder(), JayaApp.getDocumentsFolder());
//                }
//                catch(IOException ex){
//                    Log.d("Indexer", "IOException in indexFiles");
//                }
//                finally {
//                    // Update the progress bar
//                    mHandler.post(new Runnable() {
//                        public void run() {
//                            mProgress.dismiss();
//                            mProgress = null;
//                            showDocumentId(INITIAL_DOCUMENT_ID);
//                        }
//                    });
//                }
//            }
//        });
//        t.start();
    }
}
