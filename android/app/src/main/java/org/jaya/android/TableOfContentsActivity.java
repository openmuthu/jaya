package org.jaya.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.jaya.annotation.Annotation;
import org.jaya.scriptconverter.SCUtils;
import org.jaya.search.JayaIndexMetadata;
import org.jaya.search.ResultDocument;
import org.jaya.util.Utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;

public class TableOfContentsActivity extends Activity {

    class TOCItem{
        private String mITRANSPath; // Path value in ITRANS
        private String mPath; // Path value in the current script type
        TOCItem(String itransPath, String path){
            mITRANSPath = itransPath;
            mPath = path;
        }

        String getITRANSPath(){
            return mITRANSPath;
        }

        String getPath(){
            return mPath;
        }
    }

    TOCItem[] mTOCItems = new TOCItem[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_of_contents);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListAdapter();
    }

    private void setListAdapter() {
        JayaIndexMetadata mt = new JayaIndexMetadata(JayaApp.getSearchIndexFolder());
        Set<String> pathSet = mt.getIndexedFilePathSet();
        mTOCItems = new TOCItem[pathSet.size()];
        int index = 0;
        for(String path:pathSet){
            String pathInCurScriptType = Utils.removeExtension(SCUtils.convertStringToScript(path, PreferencesManager.getPreferredOutputScriptType()));
            mTOCItems[index] = new TOCItem(path, pathInCurScriptType);
            index++;
        }
        if (mTOCItems == null || mTOCItems.length == 0) {
            Toast.makeText(this, R.string.no_results_found, Toast.LENGTH_SHORT).show();
            return;
        }
        Arrays.sort(mTOCItems, new Comparator<TOCItem>() {
            @Override
            public int compare(TOCItem tocItem, TOCItem t1) {
                return tocItem.getPath().compareTo(t1.getPath());
            }
        });

        ListView listView = (ListView)findViewById(R.id.toc_list_view);

        listView.setAdapter(new TableOfContentsListAdapter(this, mTOCItems));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if( mTOCItems == null || mTOCItems.length == 0)
                    return;
                ResultDocument resDoc = JayaAppUtils.getDoc(new Annotation(mTOCItems[i].getITRANSPath(), "0", "", new Date()));
                Intent intent = new Intent(TableOfContentsActivity.this, MainActivity.class);
                intent.setAction(JayaApp.INTENT_OPEN_DOCUMENT_ID);
                intent.putExtra("documentId", resDoc.getId());
                startActivity(intent);
            }
        });
    }
}
