package org.jaya.android;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.widget.SearchView;

import org.jaya.android.R;
import org.jaya.search.index.LuceneUnicodeFileIndexer;

import java.io.IOException;
import android.Manifest;

public class MainActivity extends Activity {

    AssetsManager mAssetsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAssetsManager = new AssetsManager(getApplicationContext());
        PermissionRequestor.requestPermissionIfRequired(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionRequestor.WRITE_EXTERNAL_STORAGE_PERMISSSION_REQUEST_ID);
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
                    indexFiles(false);
                }
                break;
        }
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
        try {
            if( !JayaApp.isIndexingRequired() && !bForceReIndexing)
                return;
            LuceneUnicodeFileIndexer indexer = new LuceneUnicodeFileIndexer();
            indexer.createIndex(JayaApp.getSearchIndexFolder(), JayaApp.getDocumentsFolder());
        }
        catch(IOException ex){
            Log.d("Indexer", "IOException in indexFiles");
        }
    }
}
