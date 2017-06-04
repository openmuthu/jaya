package org.jaya.indexsync;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jaya.scriptconverter.SCUtils;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.search.JayaIndexMetadata;
import org.jaya.util.FileDownloader;
import org.jaya.util.FileDownloader.ProgressCallback;
import org.jaya.util.PathUtils;
import org.jaya.util.TimestampUtils;
import org.jaya.util.Utils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class IndexCatalogue {
	
	public static final String INDEX_CATALOG_FILE_NAME = "catalogue.txt";
	public static final String INDEX_CATALOG_DETAILS_FILE_NAME = "cat-details.txt";
	public static final String INDEX_CATALOGUE_VERSION = "1.0";
	
	private static IndexCatalogue mInstance;
	private static boolean mIsInitialized = false;
	
	private String mLocalCatalogFolder;
	private String mAppSearchIndexFolder;
	private String mRemoteCatalogBaseURL;
	private JSONObject mCatalogue;
	private JSONObject mCatalogueDetails;
	
	private static List<EventListener> mEventListeners = Collections.synchronizedList(new ArrayList<EventListener>());
	private static boolean sbCatalogueUpdateInProgress = false;
	private static boolean sbCatalogueDetailsUpdateInProgress = false;
	private IndexCatalogueItemDownloader mCatalogueItemDownloader;

	public static IndexCatalogue getInstance(){
		if( mInstance == null ){
			mInstance = new IndexCatalogue();
			//mInstance.initialize(localCatalogueFolder, remoteCatalogBaseURL);
		}
		return mInstance;
	}
	
	private IndexCatalogue(){}
	
	public String getAppIndexFolderPath(){
		return mAppSearchIndexFolder;
	}
	
	public boolean initialize(String localCatalogueFolder, String remoteCatalogBaseURL, String searchIndexFolder){
		if( mIsInitialized )
			return false;
		mAppSearchIndexFolder = searchIndexFolder;
		mLocalCatalogFolder = localCatalogueFolder;
		mRemoteCatalogBaseURL = remoteCatalogBaseURL;
		File catalogFile = getLocalCatalogFile();
		if( !catalogFile.exists() ){
			writeCatalogue();
		}
		mIsInitialized = true;
		readCatalog(false);
		return true;
		//readCatalogFile(catalogFile);
	}
	
	private File getLocalCatalogFile(){
		String catalogFilePath = PathUtils.get(mLocalCatalogFolder, INDEX_CATALOG_FILE_NAME);
		File catalogFile = new File(catalogFilePath);
		return catalogFile;
	}
	
	private File getLocalCatalogDetailsFile(){
		String catalogDetailsFilePath = PathUtils.get(mLocalCatalogFolder, INDEX_CATALOG_DETAILS_FILE_NAME);
		File catalogDetailsFile = new File(catalogDetailsFilePath);
		return catalogDetailsFile;		
	}
	
	public void addEventListener(EventListener listener){
		try{
			Iterator<EventListener> iterator = mEventListeners.iterator();
			while( iterator.hasNext() ){
				EventListener p = iterator.next();
				if( p == null )
					iterator.remove();
			}	
		}catch(Exception ex){
			ex.printStackTrace();
		}
		for(EventListener p:mEventListeners){
			if( p == listener ){
				return;
			}
		}
		mEventListeners.add(listener);
	}
	
	private void removeEventListener(EventListener listener){
		try{
			Iterator<EventListener> iterator = mEventListeners.iterator();
			while( iterator.hasNext() ){
				EventListener p = iterator.next();
				if( p == listener )
					iterator.remove();
			}	
		}catch(Exception ex){
			ex.printStackTrace();
		}		
	}
	
	public void readCatalog(boolean force){
		try{
			File catalogFile = getLocalCatalogFile();
			JSONParser parser = new JSONParser();
			mCatalogue = (JSONObject)parser.parse(new FileReader(catalogFile));
			if( sbCatalogueUpdateInProgress )
				return;			
			long seconds = TimestampUtils.diffInSeconds(new Date(), getLastSyncDate());
			if( seconds > TimestampUtils.SECONDS_IN_DAY*7 || force){
//				System.out.println("syncCatalogueFromRemote"); //temp
				syncCatalogueFromRemote();
			}
			else{
				notifyCatalogueUpdate(0);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void loadCatalogDetailsFromFileIfRequired(boolean forceReload){
		if( forceReload )
			mCatalogueDetails = null;
		try{
			if( mCatalogueDetails != null )
				return;
			File catalogDetailsFile = getLocalCatalogDetailsFile();
			if( !catalogDetailsFile.exists() )
				return;
			JSONParser parser = new JSONParser();
			mCatalogueDetails = (JSONObject)parser.parse(new FileReader(catalogDetailsFile));
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	Date getLastModifedDate(){
		if( mCatalogue == null )
			return new Date(0L);
		try{
			return TimestampUtils.getDateFromISO8601String((String)mCatalogue.get("lastModified"));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return new Date(0L);
	}

    Date getLastSyncDate(){
        if( mCatalogue == null )
            return new Date(0L);
        try{
            Date lastSyncDate = TimestampUtils.getDateFromISO8601String((String)mCatalogue.get("lastSyncDate"));
            if(lastSyncDate == null){
                return new Date(0L);
            }
            return lastSyncDate;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new Date(0L);
    }
	
	Date getCatalogDetailsLastModifedDate(){
		if( mCatalogueDetails == null )
			return new Date(0L);
		try{
			return TimestampUtils.getDateFromISO8601String((String)mCatalogueDetails.get("lastModified"));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return new Date(0L);
	}	
	
	public void syncCatalogueFromRemote(){
		if( sbCatalogueUpdateInProgress )
			return;
		//System.out.println("Inside syncCatalogueFromRemote"); //temp
		sbCatalogueUpdateInProgress = true;
		final String downloadedCatalogFilePath = PathUtils.get(mLocalCatalogFolder, INDEX_CATALOG_FILE_NAME+"_r");
		FileDownloader fd = new FileDownloader(
				mRemoteCatalogBaseURL+"/"+INDEX_CATALOG_FILE_NAME, 
				downloadedCatalogFilePath,
				new FileDownloader.ProgressCallback(){
					@Override
					public void onComplete(int error) {
						try{
							//System.out.println("onComplete 1"); //temp
							if( error == 0 ){
								JSONParser parser = new JSONParser();
								JSONObject newCatalog = (JSONObject)parser.parse(new FileReader(new File(downloadedCatalogFilePath)));
								mergeWithCatalog(newCatalog);
								//System.out.println("onComplete 2"); //temp
							}
						}catch(Exception ex){
							ex.printStackTrace();
						}finally {
							notifyCatalogueUpdate(error);
						}
					}
					@Override
					public void onContentLength(long contentLength) {}
					@Override
					public void onBytesDownloaded(long totalBytesDownloaded) {}					
				}
				);		
		fd.loadAsync();
	}
	
	public void syncCatalogueAdditionalDetailsFromRemoteIfRequired(EventListener listener){
		addEventListener(listener);
		if( sbCatalogueDetailsUpdateInProgress ) {
			System.out.println("Catalogue Details Update In Progress");
			return;
		}
		loadCatalogDetailsFromFileIfRequired(false);
		if( getCatalogDetailsLastModifedDate().equals(getLastModifedDate()) ){
			notifyCatalogueDetailsUpdate(0);
			return;
		}
		sbCatalogueDetailsUpdateInProgress = true;
		String downloadedCatalogDetailsFilePath = PathUtils.get(mLocalCatalogFolder, INDEX_CATALOG_DETAILS_FILE_NAME);
		FileDownloader fd = new FileDownloader(
				mRemoteCatalogBaseURL+"/"+INDEX_CATALOG_DETAILS_FILE_NAME, 
				downloadedCatalogDetailsFilePath,
				new ProgressCallback(){
					@Override
					public void onComplete(int error) {
						try{
							if( error == 0 )
								loadCatalogDetailsFromFileIfRequired(true);
							notifyCatalogueDetailsUpdate(error);
						}catch(Exception ex){
							ex.printStackTrace();
						}finally{
							sbCatalogueDetailsUpdateInProgress = false;
						}
					}

					@Override
					public void onContentLength(long contentLength) {}
					@Override
					public void onBytesDownloaded(long totalBytesDownloaded) {}
				}
				);		
		fd.loadAsync();
	}	
	
	private void notifyCatalogueUpdate(int error){
		try {
			for (EventListener p : mEventListeners) {
				if (p != null) {
					p.onCatalogueUpdated(error);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		finally {
			sbCatalogueUpdateInProgress = false;
		}
	}
	
	private void notifyCatalogueDetailsUpdate(int error){
		try {
			for (EventListener p : mEventListeners) {
				if (p != null) {
					p.onCatalogueDetailsUpdated(error);
				}
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
		finally {
			sbCatalogueDetailsUpdateInProgress = false;
		}
	}	
	
	private void writeCatalogue(){
		String catalogJSONStr = "{\"version\":\"1.0\", \"lastModified\": \"2010-05-06T15:37:58+0300\", \"items\":{}}";
		if( mCatalogue != null ){
			catalogJSONStr = mCatalogue.toJSONString();
		}
		FileWriter catalogueFileWriter = null;
		File catalogFile = getLocalCatalogFile();
		try{
			catalogueFileWriter = new FileWriter(catalogFile);
			catalogueFileWriter.write(catalogJSONStr);
			catalogueFileWriter.flush();			
		}catch(Exception ex){
			
		}
		finally{
			Utils.closeSilently(catalogueFileWriter);
		}
	}
	
	public synchronized boolean mergeWithCatalog(JSONObject newCatalog){
		boolean retVal = false;
		try{
			if( !mCatalogue.get("version").equals(newCatalog.get("version")) )
					return false;
			JSONObject oldCatalogueItems = (JSONObject)mCatalogue.get("items");
			JSONObject newCatalogItems = (JSONObject)newCatalog.get("items");
			
			for(Object key:newCatalogItems.keySet()){
				JSONObject item = (JSONObject)newCatalogItems.get(key);
				if( !oldCatalogueItems.containsKey(key) ){
					oldCatalogueItems.put(key, item.clone());
				}
				updateItem(item, (JSONObject)oldCatalogueItems.get(key));				
			}
			// Remove items from existing catalog if they are not present in the newly downloaded one.
			Set<Object> keySet = oldCatalogueItems.keySet();
			boolean bNeedsIndexOptimization = false;
			for(Object key:keySet){
				if( !newCatalogItems.containsKey(key) ) {
					// We should also remove the corresponding docs from the index, in this case
					bNeedsIndexOptimization = true;
					oldCatalogueItems.remove(key);
				}
			}
			mCatalogue.put("lastModified", newCatalog.get("lastModified"));
			mCatalogue.put("lastSyncDate", TimestampUtils.nowAsString());
			mCatalogue.put("baseUrl", newCatalog.get("baseUrl"));
			if(bNeedsIndexOptimization){
				IndexCatalogueItemInstaller.getInstance().removeObsoleteFilesFromIndex(this, mAppSearchIndexFolder, new IndexCatalogueItemInstaller.OnFilesRemovedCallback() {
					@Override
					public void onFilesRemoved(int error, String[] filesRemoved) {
						if( error != 0 )
							System.err.println("mergeWithCatalog(): removeObsoleteFilesFromIndex failed");
					}
				});
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		writeCatalogue();
		return retVal;
	}
	
	private void updateItem(JSONObject src, JSONObject dest){
		for(Object key:src.keySet()){
			dest.put(key, src.get(key));
			if( dest.containsKey("installed") ){
				Date srcInstant = TimestampUtils.getDateFromISO8601String((String)src.get("lastModified"));
				Date destInstant = TimestampUtils.getDateFromISO8601String((String)dest.get("lastModified"));
				if( destInstant.before(srcInstant) )
					dest.put("updateAvailable", "true");
				dest.put("installed", dest.get("installed"));
			}
			else
				dest.put("installed", "false");
		}
	}
	
	public int getNumItems(){
		try{
			return ((JSONObject)mCatalogue.get("items")).size();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return 0;
	}
	
	public Set<String> getItemNames(){
		try{
			Set<String> obj = (Set<String>)((JSONObject)mCatalogue.get("items")).keySet();
			return obj;
		}catch(Exception ex){
			ex.printStackTrace();
		}	
		return null;
	}
	
	public Item getItemByName(String name){
		if( !getItemNames().contains(name) )
			return null;
		return new Item(this, name);
	}
	
	public void getAllIncludedFiles(final ItemDetailsCallback callback){
		final Set<String> itemNames = getItemNames();
		if( itemNames == null || itemNames.size() == 0 ){
			if( callback != null ){
				callback.onDataArrived("", 1);
			}
			return;
		}
		Item item = null;
		for(String name:itemNames){
			item = getItemByName(name);
			break;
		}
		item.getIncludedFiles(new IndexCatalogue.ItemDetailsCallback() {
			@Override
			public void onDataArrived(String data, int error) {
				if( error != 0 || mCatalogueDetails == null ){
					if( callback != null )
						callback.onDataArrived(data, 1);
					return;
				}
				StringBuffer includedFiles = new StringBuffer(12*1024);
				JSONObject items = (JSONObject)mCatalogueDetails.get("items");
				for(String name:itemNames){
					includedFiles.append((String)((JSONObject)(items.get(name))).get("files"));
					includedFiles.append(JayaIndexMetadata.MD_REC_DELEMITER);
				}
				if( callback != null )
					callback.onDataArrived(includedFiles.toString(), error);				
			}
		});
	}
	
	public class Item{
		
		private WeakReference<IndexCatalogue> mParentCatalogue;
		private ItemDetails mItemDetails;
		private String mName;
		private String mNameInPreferredScript = null;
		private ScriptType mCurrentScriptType = ScriptType.ITRANS;

		public Item(IndexCatalogue parent, String name){
			mParentCatalogue = new WeakReference<IndexCatalogue>(parent);
			mName = name;
			mItemDetails = new ItemDetails(parent, this);
		}

		public String getNameInPreferredScript(ScriptType scriptType){
			if( scriptType == ScriptType.ITRANS )
				return mName;
			if( mCurrentScriptType == scriptType && mNameInPreferredScript != null )
				return mNameInPreferredScript;
			mNameInPreferredScript = SCUtils.convertStringToScript(mName, scriptType);
			mCurrentScriptType = scriptType;
			return mNameInPreferredScript;
		}

		public String getName() {
			return mName;
		}

		public String getURL() {
			try{
				if( mParentCatalogue.get() == null )
					return "";
				JSONObject catalogue = mParentCatalogue.get().mCatalogue;
				JSONObject obj = (JSONObject)((JSONObject)catalogue.get("items")).get(mName);
				return (String)catalogue.get("baseUrl") + "/" + (String)obj.get("relPath");
					
			}catch(Exception ex){
				
			}
			return "";
		}

		public Date getLastModified() {
			try{
				if( mParentCatalogue.get() == null )
					return new Date();
				JSONObject obj = (JSONObject)((JSONObject)mParentCatalogue.get().mCatalogue.get("items")).get(mName);
				return TimestampUtils.getDateFromISO8601String((String)obj.get("lastModified"));
					
			}catch(Exception ex){
				
			}
			return new Date();
		}

		public long getSize() {
			try{
				if( mParentCatalogue.get() == null )
					return 0;
				JSONObject obj = (JSONObject)((JSONObject)mParentCatalogue.get().mCatalogue.get("items")).get(mName);
				return (Long)obj.get("size");
					
			}catch(Exception ex){
				
			}
			return 0;
		}
		
		public boolean getIsInstalled(){
			try{
				if( mParentCatalogue.get() == null )
					return false;
				JSONObject obj = (JSONObject)((JSONObject)mParentCatalogue.get().mCatalogue.get("items")).get(mName);
				return ((String)obj.get("installed")).equals("true");
					
			}catch(Exception ex){
				
			}
			return false;
		}
		
		public void setIsInstalled(boolean bInstalled){
			try{
				if( mParentCatalogue.get() == null )
					return;
				JSONObject obj = (JSONObject)((JSONObject)mParentCatalogue.get().mCatalogue.get("items")).get(mName);
				obj.put("installed", (bInstalled)?"true":"false");
				if( bInstalled )
					obj.put("updateAvailable", "false");
				writeCatalogue();
					
			}catch(Exception ex){
				
			}
		}		
		
		public boolean getIsUpdateAvailable(){
			try{
				if( mParentCatalogue.get() == null )
					return false;
				JSONObject obj = (JSONObject)((JSONObject)mParentCatalogue.get().mCatalogue.get("items")).get(mName);
				return ((String)obj.get("updateAvailable")).equals("true");
					
			}catch(Exception ex){
				
			}
			return false;
		}		
		
		public void getIncludedFiles(ItemDetailsCallback callback){
			mItemDetails.getIncludedFiles(callback);
		}
	}
	
	public class ItemDetails{
		private WeakReference<IndexCatalogue> mParentCatalogue;
		private WeakReference<Item> mItem;
		private ItemDetailsCallback mCallback;
		public ItemDetails(IndexCatalogue parent, Item item){
			mParentCatalogue = new WeakReference<IndexCatalogue>(parent);
			mItem = new WeakReference<IndexCatalogue.Item>(item);
		}
		
		public void getIncludedFiles(ItemDetailsCallback callback){
			mCallback = callback;
			mParentCatalogue.get().syncCatalogueAdditionalDetailsFromRemoteIfRequired(new EventListener() {
				@Override
				public void onCatalogueUpdated(int error) {
				}
				@Override
				public void onCatalogueDetailsUpdated(int error) {
					if( mCallback != null ){
						String files = "Data unavailable";
						try{
							JSONObject items = (JSONObject)mCatalogueDetails.get("items");
							files = (String)((JSONObject)(items.get(mItem.get().getName()))).get("files");
						}catch(Exception ex){
							ex.printStackTrace();
						}finally{
							mCallback.onDataArrived(files, error);
						}
					}
					else{
						System.out.println("IndexCatalogue.getIncludedFiles(): callback is null");
					}
					removeEventListener(this);
				}
			});
		}
	}

	public interface ItemDetailsCallback{
		void onDataArrived(String data, int error);
	}
	
	public interface EventListener{
		// error 0 on success,
		public void onCatalogueUpdated(int error);
		public void onCatalogueDetailsUpdated(int error);
	}
}
