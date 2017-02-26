package org.jaya.search;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.jaya.util.Constatants;

public class LuceneUnicodeSearcher {

	public static void main(String[] args) throws Exception {
		LuceneUnicodeSearcher searcher = new LuceneUnicodeSearcher();
		searcher.searchIndex("रायणं");
	}

	public void searchIndex(String searchString) throws IOException, ParseException {
		System.out.println("Searching for '" + searchString + "'");
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(Constatants.INDEX_DIRECTORY)));
		IndexSearcher indexSearcher = new IndexSearcher(reader);

		Analyzer analyzer = new StandardAnalyzer();
		QueryParser queryParser = new QueryParser(Constatants.FIELD_CONTENTS, analyzer);
		Query query = queryParser.parse(searchString + "~");
		TopDocs result = indexSearcher.search(query, Constatants.MAX_RESULTS);
		System.out.println("Number of hits: " + result.totalHits);
		for (ScoreDoc topdoc : result.scoreDocs) {
			String fp = indexSearcher.doc(topdoc.doc).get(Constatants.FIELD_PATH);
			// String fp1 =
			// indexSearcher.doc(topdoc.doc).get(Constatants.FIELD_CONTENTS);
			System.out.println("String :" + searchString + "matched in : " + fp);
			// System.out.println("String :" + searchString + "matched is : " +
			// fp1);
		}

		reader.close();

		/*
		 * Iterator<Hit> it = hits.iterator(); while (it.hasNext()) { Hit hit =
		 * it.next(); Document document = hit.getDocument(); String path =
		 * document.get(FIELD_PATH); System.out.println("Hit: " + path); }
		 */

	}

}
