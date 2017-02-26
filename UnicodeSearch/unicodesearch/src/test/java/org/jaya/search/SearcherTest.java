package org.jaya.search;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;

public class SearcherTest {

	public static void main(String[] args) {
		String searchString = "rAyaNaM";
		ScriptConverter it2dev = ScriptConverterFactory.getScriptConverter(ScriptConverter.ITRANS_SCRIPT,
				ScriptConverter.DEVANAGARI_SCRIPT);
		String searchStringDev = it2dev.convert(searchString);
		LuceneUnicodeSearcher searcher = new LuceneUnicodeSearcher();
		try {
			searcher.searchIndex(searchStringDev);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
