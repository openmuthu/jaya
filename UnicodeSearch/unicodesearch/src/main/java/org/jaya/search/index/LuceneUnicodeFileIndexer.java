package org.jaya.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.jaya.search.JayaIndexMetadata;
import org.jaya.util.Constatants;
import org.jaya.util.PathUtils;
import org.jaya.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
					document.add(new StringField(Constatants.FIELD_PATH, path, Field.Store.YES));
					
					document.add(new TextField(Constatants.FIELD_CONTENTS, lines, Field.Store.YES));
					
					mIndexWriter.addDocument(document);
					lines = "";
				}
			}
			if( !lines.isEmpty() ){
				Document document = new Document();
				document.add(new StringField(Constatants.FIELD_PATH, path, Field.Store.YES));
				
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
	
	private String mIndexStoragePath;
	private JayaIndexMetadata mIndexMetadata;
	private IndexWriter mIndexWriter;
	
	public LuceneUnicodeFileIndexer(String indexStoragePath) throws IOException{
		mIndexStoragePath = indexStoragePath;
		mIndexMetadata = new JayaIndexMetadata(mIndexStoragePath);
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
		mIndexWriter = new IndexWriter(FSDirectory.open(new File(mIndexStoragePath)), config);
		addADummyDocumentIfIndexIsEmpty();
	}

	public static void main(String[] args) throws IOException {
		// String source = args[0];
		// String destination = args[1];
		LuceneUnicodeFileIndexer fileIndexer = new LuceneUnicodeFileIndexer(Constatants.INDEX_DIRECTORY);
		fileIndexer.addFilesToIndex(Constatants.FILES_TO_INDEX_DIRECTORY);
	}
	
	public static void testMultipleIndexes(){
		try{
			List<File> firstLevelDirs = Utils.getFirstLevelDirs(new File(Constatants.INDEX_DIRECTORY));
			for(File dir:firstLevelDirs){			
				String indexDir = dir.getCanonicalPath();
				indexDir += "_index";
				
				LuceneUnicodeFileIndexer fileIndexer = new LuceneUnicodeFileIndexer(indexDir);
				fileIndexer.addFilesToIndex(dir.getCanonicalPath());				
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private String getTagFilePathForFile(String path){
		return path + ".kw";
	}
	
	private static String[] getIndexableExtensions(){
		return new String[]{".txt"};
	}
	
	public static boolean hasIndexableExtension(String path){
		boolean bIndexableExtFound = false;
		for(String ext:getIndexableExtensions()){
			if( path.endsWith(ext) ){
				bIndexableExtFound = true;
				break;
			}
		}		
		return bIndexableExtFound;
	}
	
	private String getTagsFromFilePath(String filePath){
		if( !hasIndexableExtension(filePath) )
			return "";
		
		return Utils.getTagsBasedOnFilePath(filePath);	
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
				retVal += " " + getTagsForFile(f.getParent());
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
	
	public void deleteIndexEntriesWithFilePath(String path){
		try{
			TermQuery query = new TermQuery(new Term(Constatants.FIELD_PATH, path));
			mIndexWriter.deleteDocuments(query);			
			mIndexWriter.commit();
			mIndexMetadata.remove(JayaIndexMetadata.getIndexedFilePathSet(path));
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	public void mergeIndexes(List<String> indexPaths){
		try{
			HashSet<String> indexedFileSetCumulative = new HashSet<>();
			List<Directory> indexList = new ArrayList<Directory>();		
			for(String indexPath: indexPaths){
				if( mIndexStoragePath == indexPath ){
					continue;
				}
				JayaIndexMetadata metadata = new JayaIndexMetadata(indexPath);
				Set<String> indexedFilePathSet = metadata.getIndexedFilePathSet();
				for(String path:indexedFilePathSet){
					if( mIndexMetadata.hasIndexedFile(path) ){
						deleteIndexEntriesWithFilePath(path);
					}
				}
				indexedFileSetCumulative.addAll(indexedFilePathSet);
				indexList.add(FSDirectory.open(new File(indexPath)));
			}
			
			mIndexWriter.addIndexes(indexList.toArray(new Directory[indexList.size()]));
			mIndexMetadata.append(indexedFileSetCumulative);			
			mIndexWriter.commit();
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
	}
	
	public void addADummyDocumentIfIndexIsEmpty(){
		try{
			File segmentsFile = new File(PathUtils.get(mIndexStoragePath, "segments.gen"));
			if( segmentsFile.exists() )
				return;
			Document document = new Document();
			document.add(new StringField(Constatants.FIELD_PATH, "", Field.Store.YES));
			TextField contentsField = new TextField(Constatants.FIELD_CONTENTS, "shrI rAma", Field.Store.YES);
			document.add(contentsField);
			TextField tagsField = new TextField(Constatants.FIELD_TAGS, "", Field.Store.YES);
			document.add(tagsField);								
			mIndexWriter.addDocument(document);
			mIndexWriter.commit();
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}	

	public void addFilesToIndexOld(String directoryToBeSearched) throws CorruptIndexException, LockObtainFailedException, IOException {
		
		File dir = new File(directoryToBeSearched);
		List<File> files;
		if( dir.isDirectory() )
			files = getListOfFilesToIndex(dir);
		else{
			files = new ArrayList<File>();
			files.add(dir);
		}
		Set<String> filePathSet = new HashSet<>();
		for (File file : files) {

			if( !hasIndexableExtension(file.getCanonicalPath()) ){
				continue;
			}
			
//			if(!file.getCanonicalPath().contains("_u16le"))
//				continue;

//			Reader reader = new InputStreamReader(new FileInputStream(path), "UTF-8");
//			document.add(new TextField(Constatants.FIELD_CONTENTS, reader));
			int maxCharsPerDoc = 512;
			int minCharsPerDoc = 128;
			int docLengthSofar = 0;
			String path = file.getCanonicalPath();
			
			String tags = getTagsForFile(getTagFilePathForFile(path));			
			String encoding = Utils.guessFileEncoding(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), encoding));
			String line = "";
			String lines = "";
			path = path.replace(Constatants.FILES_TO_INDEX_DIRECTORY, "");
			path = path.replace("\\", "/");
			filePathSet.add(path);
			tags += " " + getTagsFromFilePath(path);
			System.out.println("tags for file: " + path + " are : " + tags);			
			int docLocalId = 0;
			for(int i=0;(line=reader.readLine())!=null;i++){
				//if(line.trim().matches("^[\\s\\r\\n]+$")){
				if(line.trim().isEmpty()){
					//System.out.println("Skipping empty line");
					i--;
					continue;
				}
				boolean containsVirama = Utils.containsViramaChars(line);
				lines = lines + line + "\n";
				docLengthSofar += line.length();
				if( (docLengthSofar >= minCharsPerDoc && containsVirama)
						|| docLengthSofar > maxCharsPerDoc ){
					docLengthSofar = 0;
					Document document = new Document();
					document.add(new StringField(Constatants.FIELD_DOC_LOCAL_ID, new Integer(docLocalId++).toString(), Field.Store.YES));
					document.add(new StringField(Constatants.FIELD_PATH, path, Field.Store.YES));
					TextField contentsField = new TextField(Constatants.FIELD_CONTENTS, lines, Field.Store.YES);
					document.add(contentsField);
					TextField tagsField = new TextField(Constatants.FIELD_TAGS, tags, Field.Store.YES);
					document.add(tagsField);					
					
					mIndexWriter.addDocument(document);
					lines = "";
				}
			}
			if( !lines.isEmpty() ){
				Document document = new Document();
				document.add(new StringField(Constatants.FIELD_PATH, path, Field.Store.YES));
				document.add(new StringField(Constatants.FIELD_DOC_LOCAL_ID, new Integer(docLocalId++).toString(), Field.Store.YES));
				document.add(new TextField(Constatants.FIELD_CONTENTS, lines, Field.Store.YES));
				TextField tagsField = new TextField(Constatants.FIELD_TAGS, tags, Field.Store.YES);
				tagsField.setBoost(2.0f);
				document.add(tagsField);									
				mIndexWriter.addDocument(document);
			}
			mIndexMetadata.append(filePathSet);			
			reader.close();			
		}
		
		
		mIndexWriter.commit();
		System.out.println("Index is complete");
	}
	
	public void close(){
		if( mIndexWriter != null ){
			try{
				mIndexWriter.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	public void addFilesToIndex(String directoryToBeSearched) throws CorruptIndexException, LockObtainFailedException, IOException {
		//createIndexParallel(indexStorageDirectoryPath, directoryToBeSearched);
		addFilesToIndexOld(directoryToBeSearched);
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
