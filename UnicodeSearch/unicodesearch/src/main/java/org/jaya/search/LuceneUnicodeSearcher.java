package org.jaya.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;
import org.jaya.util.Constatants;

public class LuceneUnicodeSearcher {

	IndexReader mReader;
	IndexSearcher mIndexSearcher;
	QueryParser mQueryParser;

	public static void main(String[] args) throws Exception {
		LuceneUnicodeSearcher searcher = new LuceneUnicodeSearcher(Constatants.INDEX_DIRECTORY);
		searcher.searchIndex("रायणं");
	}

	public LuceneUnicodeSearcher(String indexDirectoryPath){
		try {
			mReader = DirectoryReader.open(FSDirectory.open(new File(indexDirectoryPath)));
			mIndexSearcher = new IndexSearcher(mReader);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
			mQueryParser = new QueryParser(Version.LUCENE_47, Constatants.FIELD_CONTENTS, analyzer);
		}
		catch (IOException ex){
			ex.printStackTrace();
		}
	}

	public void close(){
		try {
			if(mReader != null) {
				mReader.close();
				mReader = null;
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
		}
	}

	public List<Document> searchITRANSString(String searchString) throws IOException, ParseException {
		ScriptConverter it2dev = ScriptConverterFactory.getScriptConverter(ScriptConverter.ITRANS_SCRIPT,
				ScriptConverter.DEVANAGARI_SCRIPT);
		String searchStringDev = it2dev.convert(searchString);
		return searchIndex(searchStringDev);
	}

	public void search(String searchString, List<Document> retVal) throws IOException, ParseException {
		Query query = mQueryParser.parse(searchString);
		if(query == null)
			return;
		//SpanTermQuery query = new SpanTermQuery(new Term(Constatants.FIELD_CONTENTS, searchString + "~"));
		TopDocs result = mIndexSearcher.search(query, Constatants.MAX_RESULTS);
		System.out.println("Number of hits: " + result.totalHits);
		for (ScoreDoc topdoc : result.scoreDocs) {
			Document doc = mIndexSearcher.doc(topdoc.doc);
			retVal.add(doc);
			String fp = doc.get(Constatants.FIELD_PATH);
			String contents = doc.get(Constatants.FIELD_CONTENTS);
			System.out.println("String :" + searchString + " matched in : " + fp);
			System.out.println("String :" + searchString + " matched is :\n" + contents);
		}
	}

	public List<Document> searchIndex(String searchString) throws IOException, ParseException {
		System.out.println("Searching for '" + searchString + "'");
//		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(Constatants.INDEX_DIRECTORY)));
//		IndexSearcher indexSearcher = new IndexSearcher(reader);
//
//		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
//		QueryParser queryParser = new QueryParser(Version.LUCENE_47, Constatants.FIELD_CONTENTS, analyzer);

		ArrayList<Document> retVal = new ArrayList<>();

		Query query = mQueryParser.parse(searchString);
		if(query == null)
			return retVal;
		//SpanTermQuery query = new SpanTermQuery(new Term(Constatants.FIELD_CONTENTS, searchString + "~"));
		TopDocs result = mIndexSearcher.search(query, Constatants.MAX_RESULTS);
		System.out.println("Number of hits: " + result.totalHits);
		for (ScoreDoc topdoc : result.scoreDocs) {
			Document doc = mIndexSearcher.doc(topdoc.doc);
			retVal.add(doc);
			String fp = doc.get(Constatants.FIELD_PATH);
			String contents = doc.get(Constatants.FIELD_CONTENTS);
			System.out.println("String :" + searchString + " matched in : " + fp);
			System.out.println("String :" + searchString + " matched is :\n" + contents);
		}

		if( retVal.size() < 10 ){
			search(searchString+"~", retVal);
		}

		return retVal;

		//reader.close();

		/*
		 * Iterator<Hit> it = hits.iterator(); while (it.hasNext()) { Hit hit =
		 * it.next(); Document document = hit.getDocument(); String path =
		 * document.get(FIELD_PATH); System.out.println("Hit: " + path); }
		 */

	}

}
