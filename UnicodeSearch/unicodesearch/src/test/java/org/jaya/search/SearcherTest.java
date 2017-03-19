package org.jaya.search;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;
import org.jaya.util.Constatants;

public class SearcherTest {

	public static void main(String[] args) {
		String searchString = "saatyaki";
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

}
