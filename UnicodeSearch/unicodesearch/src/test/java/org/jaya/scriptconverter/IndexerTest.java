package org.jaya.scriptconverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jaya.search.index.LuceneUnicodeFileIndexer;
import org.jaya.util.Constatants;
import org.jaya.util.Utils;
import static org.apache.lucene.util.Version.LUCENE_47;

public class IndexerTest {
	
	public static void main(String[] args){
		//testMultipleIndexes();
		mergeIndexes();
		//deleteIndexFiles();
	}

	public static void deleteIndexFiles(){
		try{
			List<File> firstLevelDirs = Utils.getFirstLevelDirs(new File(Constatants.FILES_TO_INDEX_DIRECTORY));
			for(File dir:firstLevelDirs){			
				String dirPath = dir.getCanonicalPath();
				if( dirPath.endsWith("_index"))
					FileUtils.deleteDirectory(dir);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}		
	}
	
	public static void mergeIndexes(){
		try{
			String finalIndexDir = Constatants.INDEX_DIRECTORY + File.separator +"final_index_all";
			LuceneUnicodeFileIndexer indexer = new LuceneUnicodeFileIndexer(finalIndexDir);
			List<String> indexPaths = new ArrayList<>();
			List<File> firstLevelDirs = Utils.getFirstLevelDirs(new File(Constatants.INDEX_DIRECTORY));
			int i=0;
			while(i<firstLevelDirs.size()){
				indexPaths.clear();
				for(int j=0;i<firstLevelDirs.size()&&j<2;i++,j++){
					String str = firstLevelDirs.get(i).getCanonicalPath();
					if( str.endsWith("final_index_all"))
						continue;
					indexPaths.add(str);
				}
				if( indexPaths.size() > 0 ){
					System.out.println("Merging paths: " + indexPaths);
					indexer.mergeIndexes(indexPaths);
				}
			}
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	public static void mergeIndexes1(){
		try{
			String finalIndexDir = Constatants.FILES_TO_INDEX_DIRECTORY + "/final_index";
			Analyzer analyzer = new StandardAnalyzer(LUCENE_47);
			IndexWriterConfig config = new IndexWriterConfig(LUCENE_47, analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			IndexWriter indexWriter = new IndexWriter(FSDirectory.open(new File(finalIndexDir)), config);
			
			List<File> firstLevelDirs = Utils.getFirstLevelDirs(new File(Constatants.FILES_TO_INDEX_DIRECTORY));
			List<File> indexFileList = new ArrayList<File>();
			for(File dir:firstLevelDirs){			
				String dirPath = dir.getCanonicalPath();
				if( dirPath.endsWith("_index") && !dirPath.contains("final_index") ){
					indexFileList.add(dir);
				}
			}
			Directory[] indexList = new Directory[indexFileList.size()]; 
			for(int i=0;i < indexFileList.size();i++){
				indexList[i] = FSDirectory.open(indexFileList.get(i));
			}
			
			indexWriter.addIndexes(indexList);
			indexWriter.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	public static void testMultipleIndexes(){
		try{
			List<File> firstLevelDirs = Utils.getFirstLevelDirs(new File(Constatants.FILES_TO_INDEX_DIRECTORY));			
			for(File dir:firstLevelDirs){					
				String indexDir = Constatants.INDEX_DIRECTORY + File.separator + Utils.getBaseName(dir.getCanonicalPath());
				//indexDir += "_index";
				
				LuceneUnicodeFileIndexer fileIndexer = new LuceneUnicodeFileIndexer(indexDir);
				fileIndexer.addFilesToIndex(dir.getCanonicalPath());				
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}	
	
}
