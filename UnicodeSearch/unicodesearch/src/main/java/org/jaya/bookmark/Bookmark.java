package org.jaya.bookmark;

import org.jaya.search.ResultDocument;
import org.jaya.util.Constatants;

public class Bookmark {
	
	private String mDocPath = "";
	private String mDocLocalId = "";
	private String mName = "";

	public Bookmark(ResultDocument resDoc, String name){
		if( resDoc == null || resDoc.getDoc() == null )
			return;
		mDocLocalId = resDoc.getDoc().get(Constatants.FIELD_DOC_LOCAL_ID);
		mDocPath = resDoc.getDoc().get(Constatants.FIELD_PATH);
		mName = name;
	}
	
	public Bookmark(String docPath, String docLocalId, String name){
		mDocPath = docPath;
		mDocLocalId = docLocalId;
		mName = name;
	}
	
	public String getDocPath(){
		return mDocPath;
	}
	
	public String getDocLocalId(){
		return mDocLocalId;
	}
	
	public String getName(){
		return mName;
	}
}
