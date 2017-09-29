package org.jaya.scriptconverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import org.jaya.util.PathUtils;
import org.jaya.util.StringUtils;
import org.jaya.util.TimestampUtils;
import org.jaya.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import static org.apache.lucene.util.Version.LUCENE_47;

public class IndexerTest {
	
	private static final String FINAL_INDEX_NAME = "final_index_all";
	
	public static void main(String[] args){
		//System.out.println( Utils.getTagsBasedOnFilePath("/mahAbhArata/02-sabhA-parva.txt"));
		createMultipleIndexes("", "");
		createIndexZipFiles("", "");
		//mergeIndexes();
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
			String finalIndexDir = Constatants.INDEX_DIRECTORY + File.separator + FINAL_INDEX_NAME;
			LuceneUnicodeFileIndexer indexer = new LuceneUnicodeFileIndexer(finalIndexDir);
			List<String> indexPaths = new ArrayList<>();
			List<File> firstLevelDirs = Utils.getFirstLevelDirs(new File(Constatants.INDEX_DIRECTORY));
			int i=0;
			while(i<firstLevelDirs.size()){
				indexPaths.clear();
				for(int j=0;i<firstLevelDirs.size()&&j<2;i++,j++){
					String str = firstLevelDirs.get(i).getCanonicalPath();
					if( str.endsWith(FINAL_INDEX_NAME))
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
		
		new File(zipOutputPath).mkdirs();
		new File(rootDirPath).mkdirs();
		
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
		
		LastIndexMeta lmd = new LastIndexMeta();
		File rootDir = new File(rootDirPath);
		List<File> firstLevelDirs = Utils.getFirstLevelDirs(rootDir);
		for(File dir:firstLevelDirs){
			try{
				String path = dir.getCanonicalPath();
				if( Utils.getBaseName(path).endsWith(FINAL_INDEX_NAME))
					continue;
				String zipName = Utils.getBaseName(path)+".zip";
				String zipPath = Paths.get(zipOutputPath, zipName).toString();
				zipFolder(path, zipPath);
				
				String itemName = Utils.getBaseName(path);
				
				JayaIndexMetadata jmd = new JayaIndexMetadata(path);				
				String zipLastModified = lmd.getLastModified(zipName, jmd);
				String md = jmd.toString();
				JSONObject indexAdditionalInfoItem = new JSONObject();
				JSONObject indexCatalogueItem = new JSONObject();
				indexCatalogueItem.put("relPath", Utils.getFileName(zipPath));
				indexCatalogueItem.put("lastModified", zipLastModified);
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
			String indexCatalogJSON = StringUtils.prettyJSONString(indexCatalogue);
			catalogueFile = new FileWriter(Paths.get(zipOutputPath, IndexCatalogue.INDEX_CATALOG_FILE_NAME).toString());
			catalogueFile.write(indexCatalogJSON);
			catalogueFile.flush();
			
			String indexAdditionalInfoJSON = StringUtils.prettyJSONString(indexAdditionalInfo);
			additionalInfoFile = new FileWriter(Paths.get(zipOutputPath, IndexCatalogue.INDEX_CATALOG_DETAILS_FILE_NAME).toString());
			additionalInfoFile.write(indexAdditionalInfoJSON);
			additionalInfoFile.flush();
			
			ArrayList<String> updatedZipNames = lmd.getUpdatedZipNames();
			for(String zipName:updatedZipNames){
				FileUtils.copyFileToDirectory(new File(PathUtils.get(zipOutputPath, zipName)), new File(Constatants.JAYA_INDEX_FILES_V1_FOLDER));
			}
			
			FileUtils.write(new File(PathUtils.get(Constatants.JAYA_INDEX_FILES_V1_FOLDER, IndexCatalogue.INDEX_CATALOG_FILE_NAME)), indexCatalogJSON, Charset.forName("UTF-8"));
			FileUtils.write(new File(PathUtils.get(Constatants.JAYA_INDEX_FILES_V1_FOLDER, IndexCatalogue.INDEX_CATALOG_DETAILS_FILE_NAME)), indexAdditionalInfoJSON, Charset.forName("UTF-8"));
			
		}catch(IOException ex){
			ex.printStackTrace();
		}finally{
			Utils.closeSilently(catalogueFile);
			Utils.closeSilently(additionalInfoFile);
			lmd.commit();
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
	
	public static String getMd5HashForFile(String filePath){
		try(FileInputStream fis = new FileInputStream(new File(filePath))){
			String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			return md5;
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getMd5HashForString(String src){
		try(InputStream is = new ByteArrayInputStream( src.getBytes( "UTF-8" ))){
			String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
			return md5;
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	} 	
	
	static class LastIndexMeta{
		JSONObject mLastIndexMeta = new JSONObject();
		LastIndexMeta(){
			try{
				JSONParser lastIndexMetaParser = new JSONParser();
				mLastIndexMeta = (JSONObject)lastIndexMetaParser.parse(new FileReader(PathUtils.get(Constatants.JAYA_INDEX_FILES_V1_FOLDER, Constatants.LAST_INDEX_META_FILE_NAME)));
			}catch(Exception ex){
				ex.printStackTrace();
			}
			mLastIndexMeta.put("zipsUpdated", new JSONArray());
		}
		
		public String getLastModified(String zipName, JayaIndexMetadata jmd){
			boolean bZipHasNewContent = false;
			JSONObject zipItemMeta = (JSONObject)mLastIndexMeta.getOrDefault(zipName, new JSONObject());
			JSONArray zipsUpdated = (JSONArray)mLastIndexMeta.get("zipsUpdated");
			String lastModified = (String)zipItemMeta.getOrDefault("lastModified", TimestampUtils.nowAsString());
			String zipItemHash = (String)zipItemMeta.getOrDefault("hash", "");
			JSONObject zipItemContents = (JSONObject)zipItemMeta.getOrDefault("contents", new JSONObject());
			String zipItemNewHash = getMd5HashForString(jmd.toStringSortedList());
			if( zipItemNewHash.compareTo(zipItemHash) != 0 ){
				lastModified = TimestampUtils.nowAsString();
				zipItemContents = new JSONObject();
				bZipHasNewContent = true;
			}
			Set<String> filePathSet = jmd.getIndexedFilePathSet();
			for(String path:filePathSet){
				String absPath = PathUtils.get(Constatants.FILES_TO_INDEX_DIRECTORY, path);
				String zipContentItemNewHash = getMd5HashForFile(absPath);
				JSONObject zipItemContentItem = (JSONObject)zipItemContents.getOrDefault(path, new JSONObject());
				String zipContentItemOldHash = (String)zipItemContentItem.getOrDefault("hash", zipContentItemNewHash);
				if( !zipContentItemOldHash.equals(zipContentItemNewHash) ){
					lastModified = TimestampUtils.nowAsString();
					bZipHasNewContent = true;
				}
				zipItemContentItem.put("hash", zipContentItemNewHash);
				zipItemContents.put(path, zipItemContentItem);
			}
			zipItemMeta.put("lastModified", lastModified);
			zipItemMeta.put("hash", zipItemNewHash);		
			zipItemMeta.put("contents", zipItemContents);
			mLastIndexMeta.put(zipName, zipItemMeta);
			
			if( bZipHasNewContent ){
				zipsUpdated.add(zipName);
			}
			mLastIndexMeta.put("zipsUpdated", zipsUpdated);
			return lastModified;
		}
		
		public ArrayList<String> getUpdatedZipNames(){
			ArrayList<String> retVal = new ArrayList<>();
			JSONArray updatedZips = (JSONArray)mLastIndexMeta.getOrDefault("zipsUpdated", new JSONArray());
			for(int i=0;i<updatedZips.size();i++){
				retVal.add((String)updatedZips.get(i));
			}
			return retVal;
		}
		
		public void commit(){
			FileWriter lastIndexMetaFile = null;
			try{				
				if( mLastIndexMeta != null ){
					lastIndexMetaFile = new FileWriter(Paths.get(Constatants.JAYA_INDEX_FILES_V1_FOLDER, Constatants.LAST_INDEX_META_FILE_NAME).toString());
					lastIndexMetaFile.write(StringUtils.prettyJSONString(mLastIndexMeta));
					lastIndexMetaFile.flush();			
				}
			}catch(IOException ex){
				ex.printStackTrace();
			}finally{
				Utils.closeSilently(lastIndexMetaFile);
			}			
		}
	}
	
}
