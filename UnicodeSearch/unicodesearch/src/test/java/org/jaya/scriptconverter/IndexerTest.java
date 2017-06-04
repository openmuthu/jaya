package org.jaya.scriptconverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jaya.indexsync.IndexCatalogue;
import org.jaya.search.JayaIndexMetadata;
import org.jaya.search.index.LuceneUnicodeFileIndexer;
import org.jaya.util.Constatants;
import org.jaya.util.TimestampUtils;
import org.jaya.util.Utils;
import org.json.simple.JSONObject;

import static org.apache.lucene.util.Version.LUCENE_47;

public class IndexerTest {
	
	public static void main(String[] args){
		//System.out.println( Utils.getTagsBasedOnFilePath("/mahAbhArata/02-sabhA-parva.txt"));
		createMultipleIndexes("", "");
		//createIndexZipFiles("", "");
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
	
	public static void createMultipleIndexes(String rootDirPath, String prevPrefix){
		try{
			String prefix = "";
			if( rootDirPath == null || rootDirPath.isEmpty() )
				rootDirPath = Constatants.FILES_TO_INDEX_DIRECTORY;
			else
				prefix = Utils.getBaseName(rootDirPath);
			List<File> alreadyIndexedFilesInThisDir = new ArrayList<>();
			File rootDir = new File(rootDirPath);
			
			if( rootDir.isDirectory() && FileUtils.sizeOfDirectory(rootDir) > Constatants.MAX_INDEX_SIZE ){
				List<File> firstLevelDirs = Utils.getFirstLevelDirs(rootDir);
				for(File dir:firstLevelDirs){
					alreadyIndexedFilesInThisDir.add(dir);
					createMultipleIndexes(dir.getCanonicalPath(), prefix);
				}
			}
			
			String indexDir = Constatants.INDEX_DIRECTORY + File.separator + prevPrefix + ((prevPrefix.isEmpty())?"":"_") + prefix;			
			LuceneUnicodeFileIndexer fileIndexer = null;			
			int totalSizeSoFar = 0;
			int currentIndex = 0;
			String suffix = "";
			String indexName = "";
			File[] filesInThisDir = rootDir.listFiles();
			for(File file:filesInThisDir){
				if( alreadyIndexedFilesInThisDir.contains(file) ){
					System.out.println("Skipping file as it is indexed seperately: " + file.getCanonicalPath());
					continue;
				}
				if( !file.isDirectory() && !LuceneUnicodeFileIndexer.hasIndexableExtension(file.getCanonicalPath()) )
					continue;
				if( totalSizeSoFar + FileUtils.sizeOf(file) > Constatants.MAX_INDEX_SIZE ){
					currentIndex++;
					suffix = String.format("%d", currentIndex);
					if( fileIndexer != null )
						fileIndexer.close();
					fileIndexer = null;
					totalSizeSoFar = 0;
				}
				if( fileIndexer == null ){
					indexName = (suffix.isEmpty())?indexDir:indexDir + "_" + suffix;
					fileIndexer = new LuceneUnicodeFileIndexer(indexName);
				}
				System.out.println("Adding file: " + file.getCanonicalPath() + " to index: " + indexName);
				fileIndexer.addFilesToIndex(file.getCanonicalPath());
				totalSizeSoFar += FileUtils.sizeOf(file);
			}
			if( fileIndexer != null )
				fileIndexer.close();			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static void createIndexZipFiles(String rootDirPath, String zipOutputPath){
		if( rootDirPath == null || rootDirPath.isEmpty() )
			rootDirPath = Constatants.INDEX_DIRECTORY;
		if( zipOutputPath == null || zipOutputPath.isEmpty() )
			zipOutputPath = Constatants.INDEX_ZIP_OUTPUT_DIRECTORY;		
		
		JSONObject indexCatalogue = new JSONObject();
		JSONObject indexAdditionalInfo = new JSONObject();
		JSONObject indexCatalogueItemList = new JSONObject();
		JSONObject indexAdditionalInfoItemList = new JSONObject();
		String timestamp = TimestampUtils.nowAsString();
		
		indexCatalogue.put("version", IndexCatalogue.INDEX_CATALOGUE_VERSION);
		indexCatalogue.put("lastModified", timestamp);
		indexCatalogue.put("baseUrl", Constatants.INDEX_CATALOGUE_BASE_URL);
		
		indexAdditionalInfo.put("version", IndexCatalogue.INDEX_CATALOGUE_VERSION);
		indexAdditionalInfo.put("lastModified", timestamp);
		
		File rootDir = new File(rootDirPath);
		List<File> firstLevelDirs = Utils.getFirstLevelDirs(rootDir);
		for(File dir:firstLevelDirs){
			try{
				String path = dir.getCanonicalPath();
				String zipPath = Paths.get(zipOutputPath, Utils.getBaseName(path)+".zip").toString();
				zipFolder(path, zipPath);
				
				String itemName = Utils.getBaseName(path);
				
				String md = new JayaIndexMetadata(path).toString();
				JSONObject indexAdditionalInfoItem = new JSONObject();
				JSONObject indexCatalogueItem = new JSONObject();
				indexCatalogueItem.put("relPath", Utils.getFileName(zipPath));
				indexCatalogueItem.put("lastModified", timestamp);
				indexCatalogueItem.put("size", FileUtils.sizeOf(new File(zipPath)));				
				indexCatalogueItemList.put(itemName, indexCatalogueItem);
				
				indexAdditionalInfoItem.put("files", md);	
				indexAdditionalInfoItemList.put(itemName, indexAdditionalInfoItem);
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
		indexAdditionalInfo.put("items", indexAdditionalInfoItemList);
		indexCatalogue.put("items", indexCatalogueItemList);
		FileWriter catalogueFile = null;
		FileWriter additionalInfoFile = null;
		try{
			catalogueFile = new FileWriter(Paths.get(zipOutputPath, IndexCatalogue.INDEX_CATALOG_FILE_NAME).toString());
			catalogueFile.write(indexCatalogue.toJSONString());
			catalogueFile.flush();
			
			additionalInfoFile = new FileWriter(Paths.get(zipOutputPath, IndexCatalogue.INDEX_CATALOG_DETAILS_FILE_NAME).toString());
			additionalInfoFile.write(indexAdditionalInfo.toJSONString());
			additionalInfoFile.flush();			
		}catch(IOException ex){
			ex.printStackTrace();
		}finally{
			Utils.closeSilently(catalogueFile);
			Utils.closeSilently(additionalInfoFile);
		}
	}
	
	public static void zipFolder(String sourceDirPath, String zipFilePath) throws IOException {
		Files.deleteIfExists(Paths.get(zipFilePath));
	    Path p = Files.createFile(Paths.get(zipFilePath));
	    try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
	        Path pp = Paths.get(sourceDirPath);
	        Files.walk(pp)
	          .filter(path -> !Files.isDirectory(path))
	          .forEach(path -> {
	              ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
	              try {
	                  zs.putNextEntry(zipEntry);
	                  zs.write(Files.readAllBytes(path));
	                  zs.closeEntry();
	            } catch (Exception e) {
	                System.err.println(e);
	            }
	          });
	    }
	}
	
}
