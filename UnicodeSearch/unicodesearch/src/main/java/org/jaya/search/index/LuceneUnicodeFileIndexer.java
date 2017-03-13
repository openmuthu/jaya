package org.jaya.search.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.jaya.util.Constatants;

import static org.apache.lucene.util.Version.LUCENE_47;

public class LuceneUnicodeFileIndexer {
	// private static final Logger LOG =
	// LoggerFactory.getLogger(CreateIndex.class);

	public static void main(String[] args) throws IOException {
		// String source = args[0];
		// String destination = args[1];
		LuceneUnicodeFileIndexer fileIndexer = new LuceneUnicodeFileIndexer();
		fileIndexer.createIndex(Constatants.INDEX_DIRECTORY, Constatants.FILES_TO_INDEX_DIRECTORY);
	}

	public void createIndex(String indexStorageDirectoryPath, String directoryToBeSearched) throws CorruptIndexException, LockObtainFailedException, IOException {
		Analyzer analyzer = new StandardAnalyzer(LUCENE_47);
		IndexWriterConfig config = new IndexWriterConfig(LUCENE_47, analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		IndexWriter indexWriter = new IndexWriter(FSDirectory.open(new File(indexStorageDirectoryPath)), config);
		File dir = new File(directoryToBeSearched);
		List<File> files = getListOfFilesToIndex(dir);
		for (File file : files) {
			Document document = new Document();

			String path = file.getCanonicalPath();
			document.add(new TextField(Constatants.FIELD_PATH, path, Field.Store.YES));

			Reader reader = new InputStreamReader(new FileInputStream(path), "UTF-8");
			document.add(new TextField(Constatants.FIELD_CONTENTS, reader));

			indexWriter.addDocument(document);
		}
		indexWriter.close();
		System.out.println("Index is complete");
	}

	public List<File> getListOfFilesToIndex(File sourceDir) {
		ArrayList<File> fileList = new ArrayList<>();
		File[] files = sourceDir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				fileList.addAll(getListOfFilesToIndex(file));
			} else {
				fileList.add(file);
			}
		}
		return fileList;

	}

}
