package org.jaya.android;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.search.LuceneUnicodeSearcher;
import org.jaya.search.ResultDocument;
import org.jaya.search.SearchResult;
import org.jaya.util.Constatants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchableActivity extends ListActivity {

    SearchResult mSearchResult;
    LuceneUnicodeSearcher mSearcher = null;
    String mCurrentQuery;
    ScriptConverter mITransToDevnagari = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS,
            ScriptType.DEVANAGARI);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_list);
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

    private void doMySearch(String query) {
        try {
            Log.d(JayaApp.APP_NAME, "doMySearch");
            mSearcher = JayaApp.getSearcher();
            mSearchResult = mSearcher.searchITRANSString(query);

            mCurrentQuery = mITransToDevnagari.convert(query);

            setListAdapter();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        catch (ParseException ex){
            ex.printStackTrace();
        }

//        setListAdapter(new SimpleCursorAdapter(this, R.layout.container_list_item_view, cursor,
//                new String[] {DatabaseConstants.COL_LANG_NAME }, new int[]{R.id.list_item}));
    }

    private void setListAdapter() {
        if (mSearchResult == null)
            return;

        ListView listView = getListView();

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

    private void setListAdapter1(){
        if( mSearchResult == null )
            return;

        ListView listView = getListView();

        List<ResultDocument> resultDocs = mSearchResult.getResultDocs();

        ArrayList<HashMap<String,String>> arrayList=new ArrayList<>();
        for (int i=0;i<resultDocs.size();i++)
        {
            HashMap<String,String> hashMap=new HashMap<>();//create a hashmap to store the data in key value pair
            Document resDoc = resultDocs.get(i).getDoc();
            String path = resDoc.get(Constatants.FIELD_PATH);
            path = path.substring(path.lastIndexOf("/"), path.length());
            hashMap.put("path", path);
            hashMap.put("contents",resDoc.get(Constatants.FIELD_CONTENTS));
            arrayList.add(hashMap);//add the hashmap into arrayList
        }

        if(resultDocs.size() == 0)
        {
            HashMap<String,String> hashMap=new HashMap<>();//create a hashmap to store the data in key value pair
            hashMap.put("path","No results found.");
            hashMap.put("contents","");
            arrayList.add(hashMap);//add the hashmap into arrayList
        }

        String[] from={"path","contents"};//string array
        int[] to={R.id.list_item_path,R.id.list_item_contents};//int array of views id's
        //SimpleAdapter simpleAdapter = new SimpleAdapter(this,arrayList,R.layout.container_list_item_view,from,to);//Create object and set the parameters for simpleAdapter
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,arrayList,R.layout.container_list_item_view,from,to) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position, convertView, parent);
                TextView pathTextView = (TextView)view.findViewById(R.id.list_item_path);
                TextView contentsTextView = (TextView)view.findViewById(R.id.list_item_contents);
                String actualText = contentsTextView.getText().toString();
                Spanned spannedText = Html.fromHtml(mSearchResult.getSpannedStringBasedOnCurrentQuery(actualText.trim(), PreferencesManager.getPreferredOutputScriptType()));
                contentsTextView.setAllCaps(false);
                contentsTextView.setText(spannedText);
                contentsTextView.setTextSize(PreferencesManager.getFontSize());
                int bgColor = JayaAppUtils.getColorForDoc(mSearchResult.getResultDocs().get(position).getId());
                contentsTextView.setBackgroundColor(bgColor);
                pathTextView.setBackgroundColor(bgColor);
                return view;
            }
        };
        setListAdapter(simpleAdapter);//sets the adapter for listView

        //perform listView item click event
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

        setListAdapter(simpleAdapter);
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
        return true;
    }
}