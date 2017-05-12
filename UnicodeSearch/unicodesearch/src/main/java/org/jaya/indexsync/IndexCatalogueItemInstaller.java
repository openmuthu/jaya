package org.jaya.indexsync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jaya.search.index.LuceneUnicodeFileIndexer;
import org.jaya.util.PathUtils;
import org.jaya.util.Utils;
import org.jaya.util.ZipUtils;

public class IndexCatalogueItemInstaller {

//	private String mIndexItemZipPath;
//	private boolean mIndexMergeInProgress = false;
//	private List<String> mIndexesToBeMergedToMain = Collections.synchronizedList(new ArrayList<>());
	
	private static IndexCatalogueItemInstaller mInstance;
	
	public static IndexCatalogueItemInstaller getInstance(){
		if( mInstance == null ){
			mInstance = new IndexCatalogueItemInstaller();
		}
		return mInstance;
	}
	
	private IndexCatalogueItemInstaller(){
		//mIndexItemZipPath = indexZipPath;
	}
	
	public synchronized boolean installToIndex(String indexZipPath, String destIndexFolder){
		File zipFile = new File(indexZipPath);
		if( !zipFile.exists() )
			return false;
		LuceneUnicodeFileIndexer indexer = null;
		File unzipFolder = null;
		try{
//			mIndexMergeInProgress = true;
			String unzipPath = PathUtils.get(zipFile.getParent(), Utils.getBaseName(indexZipPath));
			unzipFolder = new File(unzipPath);
			Utils.deleteDir(unzipFolder);
			unzipFolder.mkdir();
			ZipUtils.unzipFile(indexZipPath, unzipPath);
			indexer = new LuceneUnicodeFileIndexer(destIndexFolder);
			List<String> indexPaths = new ArrayList<>();
			indexPaths.add(unzipPath);
			indexer.mergeIndexes(indexPaths);
		}catch(IOException ex){
			ex.printStackTrace();
			return false;
		}
		finally{
			if( indexer != null )
				indexer.close();
			if( unzipFolder != null )
				Utils.deleteDir(unzipFolder);
//			mIndexMergeInProgress = false;
		}
		return true;
	}
}
