package org.jaya.search;

import org.apache.lucene.document.Document;
import org.jaya.scriptconverter.SCUtils;
import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.util.Constatants;
import org.jaya.util.Utils;

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
	
	public String getPathSansExtensionForScriptType(ScriptType dstType){
		if( mDocument == null )
			return "";
		String str = Utils.removeExtension(mDocument.get(Constatants.FIELD_PATH));
		ScriptType srcType = ScriptType.ITRANS;
		if( srcType == dstType )
			return str;
		ScriptConverter converter = ScriptConverterFactory.getScriptConverter(srcType, dstType);
		if( converter == null ){
			System.out.println("No script converter available for specified types. srcType: " + srcType.name() + " dstType: " + dstType.name());
			return str;
		}
		return converter.convert(str);		
	}

	public String getDocContentsForScriptType(ScriptType dstType){
		if( mDocument == null )
			return "";
		String str = mDocument.get(Constatants.FIELD_CONTENTS);
		ScriptType srcType = SCUtils.guessScript(str);
		if( srcType == dstType )
			return str;
		ScriptConverter converter = ScriptConverterFactory.getScriptConverter(srcType, dstType);
		if( converter == null ){
			System.out.println("No script converter available for specified types. srcType: " + srcType.name() + " dstType: " + dstType.name());
			return str;
		}
		return converter.convert(str);
	}
}
