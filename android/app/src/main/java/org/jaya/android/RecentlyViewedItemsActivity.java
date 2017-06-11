package org.jaya.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.jaya.annotation.Annotation;
import org.jaya.search.ResultDocument;

import java.util.List;

public class RecentlyViewedItemsActivity extends Activity {

    List<Annotation> mMRUItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recently_viewed_items);
        setListAdapter();
    }

    private void setListAdapter() {
        mMRUItemList = JayaApp.getMRUDocsManager().getAnnotations();
        if (mMRUItemList == null || mMRUItemList.size() == 0) {
            Toast.makeText(this, R.string.no_results_found, Toast.LENGTH_SHORT).show();
            return;
        }

        ListView listView = (ListView)findViewById(R.id.mru_list_view);

        listView.setAdapter(new AnnotationListAdapter(this, mMRUItemList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if( mMRUItemList == null )
                    return;
                ResultDocument resDoc = JayaAppUtils.getDoc(mMRUItemList.get(i));
                Intent intent = new Intent(RecentlyViewedItemsActivity.this, MainActivity.class);
                intent.setAction(JayaApp.INTENT_OPEN_DOCUMENT_ID);
                intent.putExtra("documentId", resDoc.getId());
                startActivity(intent);
            }
        });
    }
}
