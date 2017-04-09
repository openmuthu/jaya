package org.jaya.search;

import org.apache.lucene.document.Document;

public class ResultDocument {
	private int mDocId;
	private Document mDocument;
	
	ResultDocument(int docId, Document doc){
		mDocId = docId;
		mDocument = doc;
	}
	
	public int getId(){
		return mDocId;
	}
	
	public Document getDoc(){
		return mDocument;
	}
}
