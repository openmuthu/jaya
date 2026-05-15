package org.jaya.scriptconverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
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
import org.junit.Test;

import static org.apache.lucene.util.Version.LUCENE_47;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IndexerTest {
	
	private static final String FINAL_INDEX_NAME = "final_index_all";

	@Test
	public void runIndexer() {
		main(new String[0]);
	}

	/**
	 * Re-packages existing Lucene indexes into ZIPs without re-indexing source
	 * texts.  Use this when only the ZIP contents changed (e.g. after the fix
	 * that adds {@code .jaya-index-md.txt} to each ZIP) and {@code index_output/}
	 * is already up-to-date.  Much faster than {@link #runIndexer()}.
	 */
	@Test
	public void repackageZips() {
		createIndexZipFiles("", "");
	}

	/**
	 * Re-indexes only the {@code mahAbhArata_mAdhvapATha} folder and regenerates
	 * its ZIP.  Use this after updating verse-number formatting in those source
	 * files without re-indexing the entire library.
	 */
	@Test
	public void reindexMahAbhArataMadhvapATha() {
		String srcDir = Constatants.FILES_TO_INDEX_DIRECTORY
				+ File.separator + "mahAbhArata_mAdhvapATha";
		createMultipleIndexes(srcDir, "");
		createIndexZipFiles("", "");
	}

	/**
	 * Verifies that {@link #zipFolder} includes {@code .jaya-index-md.txt} in the
	 * output ZIP even though the name starts with {@code '.'} (which would normally
	 * be excluded by {@link #isExcluded}).  Also confirms that other dot-files and
	 * the "app" folder are still excluded.
	 *
	 * <p>Backward-compatibility note: deployed builds have always contained the
	 * {@code mergeIndexes()} call that reads this file from the unzipped folder.
	 * Previously the file was absent from the ZIP so an empty set was merged.
	 * Old APKs will automatically benefit from regenerated ZIPs because
	 * {@code mergeIndexes()} has been present since the first release — no app
	 * update is required, only a re-download of the category.
	 */
	@Test
	public void zipFolder_metadataFileIncluded_otherDotFilesExcluded() throws Exception {
		Path sourceDir = Files.createTempDirectory("ziptest_src_");
		Path zipPath   = Files.createTempFile("ziptest_out_", ".zip");
		Files.delete(zipPath); // zipFolder recreates it
		try {
			// Files that must end up IN the ZIP
			Files.write(sourceDir.resolve(JayaIndexMetadata.MD_FILE_NAME),
					"AgamAH/file.txt\r\n".getBytes(StandardCharsets.UTF_8));
			Files.write(sourceDir.resolve("normal.txt"),
					"content".getBytes(StandardCharsets.UTF_8));

			// Files that must be EXCLUDED
			Files.write(sourceDir.resolve(".hidden"),
					"secret".getBytes(StandardCharsets.UTF_8));
			Path appDir = Files.createDirectory(sourceDir.resolve("app"));
			Files.write(appDir.resolve("data.txt"),
					"app data".getBytes(StandardCharsets.UTF_8));

			zipFolder(sourceDir.toString(), zipPath.toString());

			Set<String> entries = new HashSet<String>();
			ZipFile zf = new ZipFile(zipPath.toFile());
			try {
				Enumeration<? extends ZipEntry> e = zf.entries();
				while (e.hasMoreElements()) {
					entries.add(e.nextElement().getName());
				}
			} finally {
				zf.close();
			}

			assertTrue("Metadata file must be present in ZIP",
					entries.contains(JayaIndexMetadata.MD_FILE_NAME));
			assertTrue("Normal files must be included",
					entries.contains("normal.txt"));
			assertFalse("Other dot-files must remain excluded",
					entries.contains(".hidden"));
			assertFalse("'app' folder contents must remain excluded",
					entries.contains("app" + File.separator + "data.txt")
					|| entries.contains("app/data.txt"));
		} finally {
			Files.deleteIfExists(zipPath);
			FileUtils.deleteDirectory(sourceDir.toFile());
		}
	}

	public static void main(String[] args){
		//System.out.println( Utils.getTagsBasedOnFilePath("/mahAbhArata/02-sabhA-parva.txt"));
		createMultipleIndexes("", "");
		createIndexZipFiles("", "");
		//mergeIndexes();
		//deleteIndexFiles();
	}

	/**
	 * Centralized exclusion logic for files and folders.
	 */
	private static boolean isExcluded(String name) {
		return name.startsWith(".") || name.equals("app") || name.endsWith(".py");
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
				if( !indexPaths.isEmpty() ){
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
			List<File> indexFileList = new ArrayList<>();
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
					if (isExcluded(dir.getName())) continue;
					alreadyIndexedFilesInThisDir.add(dir);
					createMultipleIndexes(dir.getCanonicalPath(), prefix);
				}
			}
			
			String indexDir = Constatants.INDEX_DIRECTORY + File.separator + prevPrefix + ((prevPrefix.isEmpty())?"":"_") + prefix;			
			LuceneUnicodeFileIndexer fileIndexer = null;			
			long totalSizeSoFar = 0;
			int currentIndex = 0;
			String suffix = "";
			String indexName = "";
			File[] filesInThisDir = rootDir.listFiles();
			if (filesInThisDir != null) {
				for(File file:filesInThisDir){
					if (isExcluded(file.getName())) continue;
					
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
						// Delete the existing index directory so CREATE_OR_APPEND
						// starts with a clean slate.  Without this, repeated runs
						// of createMultipleIndexes() accumulate duplicate Lucene
						// segments, bloating each ZIP by one full copy per run.
						FileUtils.deleteDirectory(new File(indexName));
						fileIndexer = new LuceneUnicodeFileIndexer(indexName);
					}
					System.out.println("Adding file: " + file.getCanonicalPath() + " to index: " + indexName);
					fileIndexer.addFilesToIndex(file.getCanonicalPath());
					totalSizeSoFar += FileUtils.sizeOf(file);
				}
			}
			if( fileIndexer != null )
				fileIndexer.close();			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
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
			if (isExcluded(dir.getName())) continue;
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
				String md = jmd.toStringSortedList();
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
			
			FileUtils.write(new File(PathUtils.get(Constatants.JAYA_INDEX_FILES_V1_FOLDER, IndexCatalogue.INDEX_CATALOG_FILE_NAME)), indexCatalogJSON, StandardCharsets.UTF_8);
			FileUtils.write(new File(PathUtils.get(Constatants.JAYA_INDEX_FILES_V1_FOLDER, IndexCatalogue.INDEX_CATALOG_DETAILS_FILE_NAME)), indexAdditionalInfoJSON, StandardCharsets.UTF_8);
			
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
	          .filter(path -> {
	              // Check every part of the relative path for exclusions.
	              // Exception: always include the metadata file so that
	              // mergeIndexes() on the device can update its path set.
	              for (Path part : pp.relativize(path)) {
	                  String partName = part.getFileName().toString();
	                  if (partName.equals(JayaIndexMetadata.MD_FILE_NAME)) continue;
	                  if (isExcluded(partName)) return false;
	              }
	              return true;
	          })
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
		try(FileInputStream fis = new FileInputStream(filePath)){
			return DigestUtils.md5Hex(fis);
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getMd5HashForString(String src){
		try(InputStream is = new ByteArrayInputStream( src.getBytes( StandardCharsets.UTF_8 ))){
			return DigestUtils.md5Hex(is);
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
		
		@SuppressWarnings("unchecked")
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
				if( zipContentItemOldHash != null && !zipContentItemOldHash.equals(zipContentItemNewHash) ){
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
			
			if( bZipHasNewContent && zipsUpdated != null ){
				zipsUpdated.add(zipName);
			}
			mLastIndexMeta.put("zipsUpdated", zipsUpdated);
			return lastModified;
		}
		
		public ArrayList<String> getUpdatedZipNames(){
			ArrayList<String> retVal = new ArrayList<>();
			JSONArray updatedZips = (JSONArray)mLastIndexMeta.getOrDefault("zipsUpdated", new JSONArray());
			if (updatedZips != null) {
				for(int i=0;i<updatedZips.size();i++){
					retVal.add((String)updatedZips.get(i));
				}
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
