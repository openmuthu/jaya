package org.jaya.indexsync;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;

import org.jaya.util.FileDownloader;
import org.jaya.util.FileDownloader.ProgressCallback;

public class IndexCatalogueItemDownloader {
	
	private static IndexCatalogueItemDownloader mInstance;
	private WeakReference<IndexCatalogue> mIndexCatalogue;
	private static Map<String, DownloadTaskInfo> mCatalogueItemDownloadQueue = Collections.synchronizedMap(new HashMap<String, DownloadTaskInfo>());
	
	public static IndexCatalogueItemDownloader getInstance(){
		if( mInstance == null ){
			mInstance = new IndexCatalogueItemDownloader();
		}
		return mInstance;
	}
	
	private IndexCatalogueItemDownloader(){
		mIndexCatalogue = new WeakReference<IndexCatalogue>(IndexCatalogue.getInstance());
	}
	
	public boolean addItemToDownloadQueue(String name, ProgressCallback callback){
		if( mIndexCatalogue.get() == null )
			return false;
		if( mCatalogueItemDownloadQueue.containsKey(name) )
			return true;
		DownloadTaskInfo d = new DownloadTaskInfo(mIndexCatalogue.get().getItemByName(name), callback);
		mCatalogueItemDownloadQueue.put(name, d);
		return true;
	}
	
	public boolean removeItemFromDownloadQueue(String name){
		if( !mCatalogueItemDownloadQueue.containsKey(name) )
			return false;	
		mCatalogueItemDownloadQueue.remove(name);
		return true;
	}
	
	public boolean cancelItemDownload(String name){
		if( !mCatalogueItemDownloadQueue.containsKey(name) ){
			return false;
		}
		
		DownloadTaskInfo d = mCatalogueItemDownloadQueue.get(name);
		if( d != null ){
			d.cancelTask();
			mCatalogueItemDownloadQueue.remove(name);
		}			
		return true;
	}
	
	public long getDownloadedBytesForItem(String itemName){
		if( !isItemDownloading(itemName) )
			return 0;
		DownloadTaskInfo d = mCatalogueItemDownloadQueue.get(itemName);
		if( d != null ){
			return d.getDownloadedBytes();
		}	
		return 0;
	}
	
	public boolean isItemDownloading(String itemName){
		return mCatalogueItemDownloadQueue.containsKey(itemName);
	}
	
	public class DownloadTaskInfo{
		private IndexCatalogue.Item mItem;
		private FileDownloader mDownloader;
		private long mDownloadedBytes;
		
		public DownloadTaskInfo(IndexCatalogue.Item item, final ProgressCallback callback){
			try{
				final File destFile = File.createTempFile(item.getName(), ".zip");
				mItem = item;
				mDownloader = new FileDownloader(item.getURL(), destFile.getCanonicalPath(), new ProgressCallback() {
					
					@Override
					public void onContentLength(long contentLength) {
						if( callback != null )
							callback.onContentLength(contentLength);
					}
					
					@Override
					public void onComplete(int error) {
						if( error == 0 ){
							try{
							// Install the index item
								IndexCatalogueItemInstaller installer = IndexCatalogueItemInstaller.getInstance();
								installer.installToIndex(destFile.getCanonicalPath(), IndexCatalogue.getInstance().getAppIndexFolderPath());
								mItem.setIsInstalled(true);
							}catch(IOException ex){
								ex.printStackTrace();
							}
							finally {
								destFile.delete();
							}
						}
						removeItemFromDownloadQueue(mItem.getName());
						if( callback != null)
							callback.onComplete(error);
					}
					
					@Override
					public void onBytesDownloaded(long totalBytesDownloaded) {
						mDownloadedBytes = totalBytesDownloaded;
						if( callback != null )
							callback.onBytesDownloaded(totalBytesDownloaded);
					}
				});
				mDownloader.loadAsync();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
		
		public long getDownloadedBytes(){
			return mDownloadedBytes;
		}
		
		public String getName(){
			return mItem.getName();
		}
		
		public void cancelTask(){
			FutureTask<Integer> task = mDownloader.getTask();
			if( task != null && !task.isDone() && !task.isCancelled() )
				task.cancel(true);
		}
	}
}
