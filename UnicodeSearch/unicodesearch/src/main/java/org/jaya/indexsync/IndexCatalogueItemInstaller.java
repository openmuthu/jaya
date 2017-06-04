package org.jaya.indexsync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.lucene.util.CollectionUtil;
import org.jaya.search.JayaIndexMetadata;
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
	
	public void removeObsoleteFilesFromIndex(final IndexCatalogue indexCatalog, final String indexFolder, final OnFilesRemovedCallback callback){
		final Thread currentThread = Thread.currentThread();
		JayaIndexMetadata jayaIndexMetadata = new JayaIndexMetadata(indexFolder);
		final Set<String> indexedFilePathSet = jayaIndexMetadata.getIndexedFilePathSet();
		indexCatalog.getAllIncludedFiles(new IndexCatalogue.ItemDetailsCallback() {			
			@Override
			public void onDataArrived(final String allValidFiles, final int error1) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						int error = error1;
						if( error != 0 ){
							if( callback != null )
								callback.onFilesRemoved(error, new String[]{});
							return;
						}
						Set<String> allValidFilePathSet = JayaIndexMetadata.getIndexedFilePathSet(allValidFiles);
						if( !indexedFilePathSet.removeAll(allValidFilePathSet) ){
							if( callback != null )
								callback.onFilesRemoved(error, new String[]{});
							return;
						}

						synchronized(mInstance){
							LuceneUnicodeFileIndexer indexer = null;
							try {
								indexer = new LuceneUnicodeFileIndexer(indexFolder);
								for(String path:indexedFilePathSet){
									indexer.deleteIndexEntriesWithFilePath(path);
								}
							}catch(Exception ex){
								ex.printStackTrace();
								error = 1;
							}finally {
								if( indexer != null )
									indexer.close();
								if( callback != null )
									callback.onFilesRemoved(error, indexedFilePathSet.toArray(new String[indexedFilePathSet.size()]));
							}
						}
					}
				};
				if( currentThread.getId() == Thread.currentThread().getId() )
					new Thread(r).start();
				else
					r.run();
			}
		});
	}

	public void unistallItem(final IndexCatalogue.Item item, final String indexFolder, final OnUninstalledCallback callback){
		item.getIncludedFiles(new IndexCatalogue.ItemDetailsCallback() {
			@Override
			public void onDataArrived(String data, int error) {
				synchronized(mInstance){
					LuceneUnicodeFileIndexer indexer = null;
					try {
						if( error == 0 ){
							indexer = new LuceneUnicodeFileIndexer(indexFolder);
							Set<String> filePathSet = JayaIndexMetadata.getIndexedFilePathSet(data);
							for(String path:filePathSet){
								indexer.deleteIndexEntriesWithFilePath(path);
							}
							item.setIsInstalled(false);
						}
					}catch(Exception ex){
						ex.printStackTrace();
					}finally {
						if( indexer != null )
							indexer.close();
						if( callback != null )
							callback.onUninstalled(error, item);
					}
				}
			}
		});
	}
	
	public boolean installToIndex(String indexZipPath, String destIndexFolder){
		File zipFile = new File(indexZipPath);
		if( !zipFile.exists() )
			return false;
		synchronized(mInstance){
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
		}
		return true;
	}

	public interface OnUninstalledCallback{
		void onUninstalled(int error, IndexCatalogue.Item item);
	}
	
	public interface OnFilesRemovedCallback{
		void onFilesRemoved(int error, String[] filesRemoved);
	}
}
