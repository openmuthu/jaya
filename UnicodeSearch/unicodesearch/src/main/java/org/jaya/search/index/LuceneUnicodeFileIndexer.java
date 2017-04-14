package org.jaya.search.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.reverse.ReverseStringFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Filter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.jaya.util.Constatants;
import org.jaya.util.Utils;

import static org.apache.lucene.util.Version.LUCENE_47;

class SingleFileIndexerThread extends Thread{

	private IndexWriter mIndexWriter;
	private File mFileToIndex;
    
    public SingleFileIndexerThread(IndexWriter indexWriter, File fileToIndex){
        this.mIndexWriter = indexWriter;
        mFileToIndex = fileToIndex;
    }	
    
    @Override
    public void run() {
    	try {
			String path = mFileToIndex.getCanonicalPath();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
			String line = "";
			String lines = "";
			for(int i=0;(line=reader.readLine())!=null;i++){
				lines = lines + line + "\n";
				if( (i+1)%4 == 0 ){
					Document document = new Document();
					document.add(new TextField(Constatants.FIELD_PATH, path, Field.Store.YES));
					
					document.add(new TextField(Constatants.FIELD_CONTENTS, lines, Field.Store.YES));
					
					mIndexWriter.addDocument(document);
					lines = "";
				}
			}
			if( !lines.isEmpty() ){
				Document document = new Document();
				document.add(new TextField(Constatants.FIELD_PATH, path, Field.Store.YES));
				
				document.add(new TextField(Constatants.FIELD_CONTENTS, lines, Field.Store.YES));
				
				mIndexWriter.addDocument(document);
			}
			reader.close();    	
    	}catch(IOException ex){
    		ex.printStackTrace();
    	}
    }
}

public class LuceneUnicodeFileIndexer {
	// private static final Logger LOG =
	// LoggerFactory.getLogger(CreateIndex.class);

	public static void main(String[] args) throws IOException {
		// String source = args[0];
		// String destination = args[1];
		LuceneUnicodeFileIndexer fileIndexer = new LuceneUnicodeFileIndexer();
		fileIndexer.createIndex(Constatants.INDEX_DIRECTORY, Constatants.FILES_TO_INDEX_DIRECTORY);
	}
	
	private String getTagFilePathForFile(String path){
		return path + ".kw";
	}
	
	private String getTagsForFile(String path){
		if( path == null )
			return "";
		String retVal = "";
		String tagsFilePath = path;
		BufferedReader reader = null;
		File f = new File(path);
		try{
			if( f.isDirectory() ){
				tagsFilePath = f.getCanonicalPath() + File.separator + "tags.kw";
			}
			File tagsFile = new File(tagsFilePath);
			if( tagsFile.exists() ){		
				String line = "";
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(tagsFilePath), "UTF-16"));
				if( reader != null ){				
					while((line=reader.readLine()) != null){
						retVal += line + "\n";
					}
				}
			}
			if(!f.equals(Constatants.getToBeIndexedDirectory()))
				retVal += getTagsForFile(f.getParent());
		}catch(IOException ex){
			ex.printStackTrace();
		}
		finally{
			try{
				if( reader != null )
					reader.close();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
		return retVal;
	}

	public void createIndexOld(String indexStorageDirectoryPath, String directoryToBeSearched) throws CorruptIndexException, LockObtainFailedException, IOException {
		Analyzer analyzer = new StandardAnalyzer(LUCENE_47);
//		Analyzer analyzer = new Analyzer() {
//			@Override
//			protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
//				StandardTokenizer source = new StandardTokenizer(LUCENE_47, reader);
//				//KeywordTokenizer source = new KeywordTokenizer(reader);
//				//WhitespaceTokenizer wst = new WhitespaceTokenizer(LUCENE_47, reader);
//				TokenStream filter = new NGramTokenFilter(LUCENE_47, source, 3, 6);
//				return new TokenStreamComponents(source, filter);
//			}
//		};
		IndexWriterConfig config = new IndexWriterConfig(LUCENE_47, analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		IndexWriter indexWriter = new IndexWriter(FSDirectory.open(new File(indexStorageDirectoryPath)), config);
		
		File dir = new File(directoryToBeSearched);
		List<File> files = getListOfFilesToIndex(dir);
		for (File file : files) {

			if( !Utils.getFileExtension(file.getCanonicalPath()).equals("txt") ){
				continue;
			}
			
//			if(!file.getCanonicalPath().contains("_u16le"))
//				continue;

//			Reader reader = new InputStreamReader(new FileInputStream(path), "UTF-8");
//			document.add(new TextField(Constatants.FIELD_CONTENTS, reader));
			String path = file.getCanonicalPath();
			
			String tags = getTagsForFile(getTagFilePathForFile(path));
			System.out.println("tags for file: " + path + " are : " + tags);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-16"));
			String line = "";
			String lines = "";
			path = path.replace(directoryToBeSearched, "");
			for(int i=0;(line=reader.readLine())!=null;i++){
				lines = lines + line + "\n";
				if( (i+1)%4 == 0 ){
					Document document = new Document();
					document.add(new TextField(Constatants.FIELD_PATH, path, Field.Store.YES));
					TextField contentsField = new TextField(Constatants.FIELD_CONTENTS, lines, Field.Store.YES);
					document.add(contentsField);
					TextField tagsField = new TextField(Constatants.FIELD_TAGS, tags, Field.Store.YES);
					document.add(tagsField);					
					
					indexWriter.addDocument(document);
					lines = "";
				}
			}
			if( !lines.isEmpty() ){
				Document document = new Document();
				document.add(new TextField(Constatants.FIELD_PATH, path, Field.Store.YES));
				
				document.add(new TextField(Constatants.FIELD_CONTENTS, lines, Field.Store.YES));
				TextField tagsField = new TextField(Constatants.FIELD_TAGS, tags, Field.Store.YES);
				tagsField.setBoost(2.0f);
				document.add(tagsField);									
				indexWriter.addDocument(document);
			}
			reader.close();

			
		}
		
		
		indexWriter.close();
		System.out.println("Index is complete");
	}
	
	public void createIndex(String indexStorageDirectoryPath, String directoryToBeSearched) throws CorruptIndexException, LockObtainFailedException, IOException {
		//createIndexParallel(indexStorageDirectoryPath, directoryToBeSearched);
		createIndexOld(indexStorageDirectoryPath, directoryToBeSearched);
	}
	
	public void createIndexParallel(String indexStorageDirectoryPath, String directoryToBeSearched) throws CorruptIndexException, LockObtainFailedException, IOException {
		//Analyzer analyzer = new StandardAnalyzer(LUCENE_47);
		Analyzer analyzer = new Analyzer() {
			@Override
			protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
				StandardTokenizer source = new StandardTokenizer(LUCENE_47, reader);
				//KeywordTokenizer source = new KeywordTokenizer(reader);
				//WhitespaceTokenizer wst = new WhitespaceTokenizer(LUCENE_47, source);
				TokenStream filter = new NGramTokenFilter(LUCENE_47, source, 2, 10);
				return new TokenStreamComponents(source, filter);
			}
		};
		IndexWriterConfig config = new IndexWriterConfig(LUCENE_47, analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		IndexWriter indexWriter = new IndexWriter(FSDirectory.open(new File(indexStorageDirectoryPath)), config);
		
		ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(10);
		try{
			
			File dir = new File(directoryToBeSearched);
			List<File> files = getListOfFilesToIndex(dir);
			for (File file : files) {
				SingleFileIndexerThread worker = new SingleFileIndexerThread(indexWriter, file);
				scheduledThreadPool.schedule(worker, 1, TimeUnit.MILLISECONDS);			
			}
			
			Thread.sleep(3000);
				
		}catch(InterruptedException ex){
			ex.printStackTrace();
		}
		finally{
			scheduledThreadPool.shutdown();
			while(!scheduledThreadPool.isTerminated()){
				//wait for all tasks to finish
			}		
			indexWriter.close();
		}
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
