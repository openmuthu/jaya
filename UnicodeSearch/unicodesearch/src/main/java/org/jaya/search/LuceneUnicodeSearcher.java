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
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
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
	String mIndexDirectoryPath;

	public static void main(String[] args) throws Exception {
		LuceneUnicodeSearcher searcher = new LuceneUnicodeSearcher(Constatants.INDEX_DIRECTORY);
		searcher.searchIndex("रायणं");
	}

	public LuceneUnicodeSearcher(String indexDirectoryPath){
		mIndexDirectoryPath = indexDirectoryPath;
		createIndexSearcherIfRequired();
	}

	public void createIndexSearcherIfRequired(){
		try {
			if (mIndexSearcher != null)
				return;
			mReader = DirectoryReader.open(FSDirectory.open(new File(mIndexDirectoryPath)));
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

	public SearchResult searchITRANSString(String searchString) throws IOException, ParseException {
		ScriptConverter it2dev = ScriptConverterFactory.getScriptConverter(ScriptConverter.ITRANS_SCRIPT,
				ScriptConverter.DEVANAGARI_SCRIPT);
		String searchStringDev = it2dev.convert(searchString);
		return searchIndex(searchStringDev);
	}

	public void search(String searchString, List<ResultDocument> retVal) throws IOException, ParseException {
		Query query = mQueryParser.parse(searchString);
		if(query == null)
			return;
		TopDocs result = mIndexSearcher.search(query, Constatants.MAX_RESULTS);
		System.out.println("Number of hits: " + result.totalHits);
		for (ScoreDoc topdoc : result.scoreDocs) {
			Document doc = mIndexSearcher.doc(topdoc.doc);
			retVal.add(new ResultDocument(topdoc.doc, doc));
			String fp = doc.get(Constatants.FIELD_PATH);
			String contents = doc.get(Constatants.FIELD_CONTENTS);
			System.out.println("String :" + searchString + " matched in : " + fp + ", Score: " + topdoc.score);
			System.out.println("String :" + searchString + " matched is :\n" + contents);
		}
	}
	
	public Query getQueryForSearchString(String searchString){
//		Query query = mQueryParser.parse(searchString);
//		if(query == null)
//			return retVal;
		//TermQuery query = new TermQuery(new Term(Constatants.FIELD_CONTENTS, searchString));
//		PhraseQuery query = new PhraseQuery();
//		query.add(new Term(Constatants.FIELD_CONTENTS, searchString));		
		RegexpQuery query = new RegexpQuery(new Term(Constatants.FIELD_CONTENTS, ".*"+searchString+".*"));
		return query;
	}
	
	public List<ResultDocument> getAdjacentDocs(int docId, int nDocs, int dir) throws IOException{
		ArrayList<ResultDocument> retVal = new ArrayList<>();
		createIndexSearcherIfRequired();
		if( mReader == null )
			return retVal;
		int i = 1;
		int maxDoc = mReader.maxDoc();		
		do{
			int adjDocId =  docId+(dir*i);
			if( adjDocId < 0 || adjDocId > maxDoc )
				break;
			Document doc = mIndexSearcher.doc(adjDocId);
			++i;			
			if( doc == null )
				continue;
			if(dir < 0 )
				retVal.add(0, new ResultDocument(adjDocId, doc));
			else
				retVal.add(new ResultDocument(adjDocId, doc));
		}while(i<=nDocs);
		return retVal;
	}
	
	public ResultDocument getDoc(int docId) throws IOException{
		createIndexSearcherIfRequired();
		if( mReader == null )
			return null;
		int maxDoc = mReader.maxDoc();		
		if( docId < 0 || docId > maxDoc )
			return null;
		return new ResultDocument(docId, mIndexSearcher.doc(docId));
	}	
	
	public ResultDocument getNextDoc(int docId) throws IOException{
		List<ResultDocument> retVal = getAdjacentDocs(docId, 1, 1);
		if( retVal.size() == 1)
			return retVal.get(0);
		return null;
	}
	
	public ResultDocument getPreviousDoc(int docId) throws IOException{
		List<ResultDocument> retVal = getAdjacentDocs(docId, 1, -1);
		if( retVal.size() == 1)
			return retVal.get(0);
		return null;
	}	
	
	public void printDocContents(Document doc){
		if( doc == null )
			return;
		String fp = doc.get(Constatants.FIELD_PATH);
		String contents = doc.get(Constatants.FIELD_CONTENTS);
		System.out.println("Document path : " + fp);
		System.out.println("Document contents :\n" + contents);		
	}

	public SearchResult searchIndex(String searchString) throws IOException, ParseException {
		System.out.println("Searching for '" + searchString + "'");
//		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(Constatants.INDEX_DIRECTORY)));
//		IndexSearcher indexSearcher = new IndexSearcher(reader);
//
//		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
//		QueryParser queryParser = new QueryParser(Version.LUCENE_47, Constatants.FIELD_CONTENTS, analyzer);

		SearchResult retVal = null;
		ArrayList<ResultDocument> resultList = new ArrayList<>();

		createIndexSearcherIfRequired();

		JayaQueryParser jqp = new JayaQueryParser(searchString);
		retVal = new SearchResult(jqp, resultList);
		if( mIndexSearcher == null )
			return  retVal;		
		String parsedQueryString = jqp.getParsedQuery();

		if( jqp.isRegExPrefixQuery() ){			
			searchString = parsedQueryString;
			Query query = getQueryForSearchString(searchString);
			TopDocs result = mIndexSearcher.search(query, Constatants.MAX_RESULTS);
			System.out.println("Number of hits: " + result.totalHits);
			for (ScoreDoc topdoc : result.scoreDocs) {
				Document doc = mIndexSearcher.doc(topdoc.doc);
				resultList.add(new ResultDocument(topdoc.doc, doc));
				String fp = doc.get(Constatants.FIELD_PATH);
				String contents = doc.get(Constatants.FIELD_CONTENTS);
				System.out.println("String :" + searchString + " matched in : " + fp + ", Score: " + topdoc.score);
				System.out.println("String :" + searchString + " matched is :\n" + contents);
			}
		}
		else if( jqp.isLuceneQueryParserQuery() ){
			searchString = parsedQueryString;
			search(searchString, resultList);
		}
		else if( jqp.isFuzzyQuery() ){
			search(parsedQueryString, resultList);			
		}
		else{
			String q = parsedQueryString;
			search(q, resultList);
		}

//		if( retVal.size() < 10 ){
//			String q = QueryConverter.convertToFuzzyQueryWithTags(searchString);
//			search(q, retVal);
//		}

		return retVal;

		//reader.close();

		/*
		 * Iterator<Hit> it = hits.iterator(); while (it.hasNext()) { Hit hit =
		 * it.next(); Document document = hit.getDocument(); String path =
		 * document.get(FIELD_PATH); System.out.println("Hit: " + path); }
		 */

	}

}
