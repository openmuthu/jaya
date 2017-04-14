package org.jaya.search;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;
import org.jaya.util.Constatants;

public class SearcherTest {
	
	public static void testAdjacentDocuments(){
		LuceneUnicodeSearcher searcher = new LuceneUnicodeSearcher(Constatants.INDEX_DIRECTORY);
		try {
			int start = 3000;
			for(int i=0;i<20;i++){
				ResultDocument doc = searcher.getNextDoc(start+i);
				searcher.printDocContents(doc.getDoc());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public static void testSearch(){
		//String searchString = "naraayaNa~ && guNai~ && sarva";
		//String searchString = "samantabhadrO* bhagavaa* && ({{{tags:}}}amara || {{{tags:}}}bhaarata)/q";
		String searchString = "raavaNa sahasra #raamaayaNa";
		ScriptConverter it2dev = ScriptConverterFactory.getScriptConverter(ScriptConverter.ITRANS_SCRIPT,
				ScriptConverter.DEVANAGARI_SCRIPT);
		String searchStringDev = it2dev.convert(searchString);
		LuceneUnicodeSearcher searcher = new LuceneUnicodeSearcher(Constatants.INDEX_DIRECTORY);
		try {
			searcher.searchIndex(searchStringDev);
			//searcher.searchIndex(searchStringDev+"~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public static void main(String[] args) {
		//testAdjacentDocuments();
		testSearch();
		return;
		


	}

}
