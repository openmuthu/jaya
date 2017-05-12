package org.jaya.bookmark;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.ref.WeakReference;
import java.util.Set;

import org.jaya.search.LuceneUnicodeSearcher;
import org.jaya.search.ResultDocument;
import org.jaya.util.Constatants;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class BookmarkManager {

	private WeakReference<LuceneUnicodeSearcher> mSearcher = new WeakReference<LuceneUnicodeSearcher>(null);
	private String mBookmarksFilePath;
	private JSONObject mBookmarksJSONObject;
	
	public BookmarkManager(LuceneUnicodeSearcher searcher, String bookmarksFilePath){
		mSearcher = new WeakReference<LuceneUnicodeSearcher>(searcher);
		mBookmarksFilePath = bookmarksFilePath;
		JSONParser parser = new JSONParser();
		try{
			mBookmarksJSONObject = (JSONObject) parser.parse(new FileReader(new File(bookmarksFilePath)));
		}
		catch(Exception ex){
			ex.printStackTrace();
			mBookmarksJSONObject = new JSONObject();
		}
	}
	
	private void writeBookmarksJSON(){
		try(FileWriter fw = new FileWriter(new File(mBookmarksFilePath))){
			fw.write(mBookmarksJSONObject.toJSONString());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public Bookmark addBookmark(ResultDocument resDoc, String name){
		if( resDoc == null || name == null || name.isEmpty() )
			return null;
		for(int i=0;;i++){
			if( mBookmarksJSONObject.containsKey(name) )
				name += "_" + i;
			else
				break;
		}
		JSONObject bookmarkJSONObject = new JSONObject();
		bookmarkJSONObject.put("docPath", resDoc.getDoc().get(Constatants.FIELD_PATH));
		bookmarkJSONObject.put("docLocalId", resDoc.getDoc().get(Constatants.FIELD_DOC_LOCAL_ID));
		mBookmarksJSONObject.put(name, bookmarkJSONObject);
		writeBookmarksJSON();
		return new Bookmark(resDoc, name);
	}
	
	public void removeBookmark(Bookmark bookmark){
		if( bookmark == null || !mBookmarksJSONObject.containsKey(bookmark.getName()) )
			return;
		mBookmarksJSONObject.remove(bookmark.getName());
		writeBookmarksJSON();
	}
	
	public int getNumBookmarks(){
		return mBookmarksJSONObject.size();
	}
	
	public Set<String> getBookmarkNames(){
		try{
			Set<String> obj = (Set<String>)mBookmarksJSONObject.keySet();
			return obj;
		}catch(Exception ex){
			ex.printStackTrace();
		}	
		return null;		
	}
	
	public Bookmark getBookmarkByName(String name){
		if( !mBookmarksJSONObject.containsKey(name) )
			return null;
		JSONObject obj = (JSONObject) mBookmarksJSONObject.get(name);
		return new Bookmark((String)obj.get("docPath"), (String)obj.get("docLocalId"), name);
	}
}
