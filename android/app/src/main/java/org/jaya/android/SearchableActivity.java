package org.jaya.android;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.lucene.document.Document;
import org.jaya.scriptconverter.SCUtils;
import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.search.LuceneUnicodeSearcher;
import org.jaya.search.ResultDocument;
import org.jaya.search.SearchResult;
import org.jaya.util.Constatants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class SearchableActivity extends ListActivity {

    ProgressDialog mProgressDialog = null;
    SearchResult mSearchResult;
    LuceneUnicodeSearcher mSearcher = null;
    String mCurrentQuery;
    ScriptConverter mITransToDevnagari = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS,
            ScriptType.DEVANAGARI);

    private static ScheduledThreadPoolExecutor gThreadPool = new ScheduledThreadPoolExecutor(2);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_list);
        getActionBar().setIcon(android.R.color.transparent);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(final String query) {
        try {
            Log.d(JayaApp.APP_NAME, "doMySearch");
            if( mProgressDialog == null )
                mProgressDialog = new ProgressDialog(this);
            if( mProgressDialog != null ) {
                mProgressDialog.setMessage(getResources().getString(R.string.please_wait));
                mProgressDialog.show();
            }

            FutureTask<Integer> ft = new FutureTask<Integer>(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {

                    try {
                        mSearcher = JayaApp.getSearcher();
                        String itransQuery = SCUtils.convertStringToScript(query, ScriptType.ITRANS);
                        mSearchResult = mSearcher.searchITRANSString(itransQuery);

                        mCurrentQuery = mITransToDevnagari.convert(itransQuery);
                    }catch (Exception ex){
                        Log.e(JayaApp.APP_NAME, "Exception: "+ Log.getStackTraceString(ex));
                    }

                    JayaApp.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if( mProgressDialog != null ) {
                                mProgressDialog.dismiss();
                                mProgressDialog = null;
                            }
                            setListAdapter();
                        }
                    });

                    return null;
                }
            });

            gThreadPool.submit(ft);

            //ft.get();

        }
        catch (Exception ex){
            ex.printStackTrace();
        }

//        setListAdapter(new SimpleCursorAdapter(this, R.layout.container_list_item_view, cursor,
//                new String[] {DatabaseConstants.COL_LANG_NAME }, new int[]{R.id.list_item}));
    }

    private void setListAdapter() {
        if (mSearchResult == null) {
            Toast.makeText(this, R.string.no_results_found, Toast.LENGTH_SHORT).show();
            return;
        }

        ListView listView = getListView();

        if(mSearchResult.getResultDocs().size() == 0){
            Toast.makeText(this, R.string.no_results_found, Toast.LENGTH_SHORT).show();
        }

        listView.setAdapter(new DocumentListAdapter(this, mSearchResult));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if( mSearchResult == null )
                    return;
                List<ResultDocument> resultDocs = mSearchResult.getResultDocs();
                ResultDocument resDoc = resultDocs.get(i);
                Intent intent = new Intent(SearchableActivity.this, MainActivity.class);
                //intent.setClassName("org.jaya.android", "org.jaya.android.MainActivity");
                intent.setAction(JayaApp.INTENT_OPEN_DOCUMENT_ID);
                intent.putExtra("documentId", resDoc.getId());
                startActivity(intent);
                //Toast.makeText(getApplicationContext(),resDoc.get(Constatants.FIELD_PATH),Toast.LENGTH_LONG).show();//show the selected image in toast according to position
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
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        CommonMenuItemsHandler.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CommonMenuItemsHandler.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        CommonMenuItemsHandler.onMenuOpened(featureId, menu);
        return super.onMenuOpened(featureId, menu);
    }
}