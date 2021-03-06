package org.jaya.android;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import org.jaya.search.ResultDocument;
import org.jaya.search.index.LuceneUnicodeFileIndexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    public static final int NUM_ITEMS_TO_LOAD_MORE = 4;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mDrawerListTitles;
    private ActionBarDrawerToggle mDrawerToggle;

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
        else {
            int docId = PreferencesManager.getLastViewedDocId();
            showDocumentId(PreferencesManager.getLastViewedDocId());
        }
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

        setupNavigationDrawerItems();

        mAssetsManager = new AssetsManager(getApplicationContext());
        PermissionRequestor.requestPermissionIfRequired(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionRequestor.WRITE_EXTERNAL_STORAGE_PERMISSSION_REQUEST_ID);

        Intent intent = getIntent();
        if( intent != null )
            handleIntent(intent);

        //showDocumentId(3000);
    }

    @Override
    protected void onPause() {
//        final ListView listView = (ListView) findViewById(R.id.doc_list_view);
//        if( listView != null && mDocumentList != null ){
//            ResultDocument doc =  mDocumentList.get( listView.getFirstVisiblePosition() );
//            PreferencesManager.setLastViewedDocId(doc.getId());
//        }
        super.onPause();
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
        final JayaDocListView listView = (JayaDocListView) findViewById(R.id.doc_list_view);

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

                    if( listView.isScalingInProgress() )
                        return;

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
                        else if (listView.isOverscrollDown() && listView.getFirstVisiblePosition() <= 0) {
                            int oldFirstPos = listView.getFirstVisiblePosition();
                            Log.d(JayaApp.APP_NAME, "onScrollStateChanged(): fetching previous docs ");
                            ResultDocument lastDoc = mDocumentList.get(0);
                            List<ResultDocument> prevDocs = JayaApp.getSearcher().getAdjacentDocs(lastDoc.getId(), NUM_ITEMS_TO_LOAD_MORE, -1);
                            int index = listView.getFirstVisiblePosition() + prevDocs.size();
                            int top = listView.getTop();
                            mDocumentList.addAll(0, prevDocs);
                            DocumentListAdapter listAdapter = (DocumentListAdapter)view.getAdapter();
                            listAdapter.setDocumentListAndUpdateView(mDocumentList);
                            //listView.setSelectionFromTop(index, top);
                            listView.setSelection(index);
                        }
                    }
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_random_doc).setVisible(true);
        return super.onPrepareOptionsMenu(menu);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //int id = item.getItemId();
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        else if( item.getItemId() == R.id.action_random_doc ){
            showDocumentId(JayaApp.getSearcher().getRandomDoc());
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupNavigationDrawerItems(){
        mDrawerListTitles = getResources().getStringArray(R.array.drawer_options_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerListTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
//        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        if( mDrawerToggle != null && mDrawerLayout != null )
            mDrawerLayout.removeDrawerListener(mDrawerToggle);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

//        if (savedInstanceState == null) {
//            selectItem(0);
//        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mDrawerLayout.closeDrawer(mDrawerList);
            if( position == 0 ){
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        }
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
