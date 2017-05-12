package org.jaya.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class FileDownloader {

	private String mURL;
	private String mDestPath;
	private FutureTask<Integer> mTask;
	private WeakReference<ProgressCallback> mProgressCallback = new WeakReference<FileDownloader.ProgressCallback>(null);
	
	private static ScheduledThreadPoolExecutor mExecutor;
	
	public FileDownloader(String url, String destPath, ProgressCallback callback){
		mURL = url;
		mDestPath = destPath;
		mProgressCallback = new WeakReference<>(callback);
	}
	
	public FutureTask<Integer> getTask(){
		return mTask;
	}
	
	public FutureTask<Integer> loadAsync(){
		if( mExecutor == null ){
			mExecutor = new ScheduledThreadPoolExecutor(4);
		}
		if( mTask != null ){
			if( !mTask.isDone() )
				return mTask;
		}
		mTask = new FutureTask<Integer>(
	            new Callable<Integer>() {
	                @Override
	                public Integer call() throws Exception {
	                	Integer retVal = 0;
	                	InputStream stream = null;
	                	HttpURLConnection conn = null;
	                	FileOutputStream outputStream = null;
	                	try{
		                	URL url = new URL(mURL);
		                	conn = (HttpURLConnection)url.openConnection();
		                	int rc = conn.getResponseCode();
		                	if( rc != HttpURLConnection.HTTP_OK )
		                		throw new IOException();
		                	stream = conn.getInputStream();		                	
		                	int totalBytes = conn.getContentLength();
		                	
		                	if( totalBytes > 0 ){
		                		if( mProgressCallback.get() != null )
		                			mProgressCallback.get().onContentLength(totalBytes);		                		
		                	}
		                	
		                	long bytesSoFar = 0;
		                	int bytesRead = 0;
		                	byte[] buffer = new byte[1024*1024];
		                	outputStream = new FileOutputStream(new File(mDestPath));
		                	while( (bytesRead=stream.read(buffer)) >= 0 ){
		                		outputStream.write(buffer, 0, bytesRead);
		                		bytesSoFar += bytesRead;
		                		if( mProgressCallback.get() != null )
		                			mProgressCallback.get().onBytesDownloaded(bytesSoFar);
		                	}
	                	}catch(Exception ex){
	                		retVal = -1;
	                		ex.printStackTrace();
	                	}
	                	finally{
	                		Utils.closeSilently(stream); 
	                		Utils.closeSilently(outputStream);	                		
	                		if( conn != null )
	                			conn.disconnect();
	                	}
	                	if( mProgressCallback.get() != null )
	                		mProgressCallback.get().onComplete(retVal);
	                	return retVal;
	                }
	            });
	    //new Thread(mTask).start();
		mExecutor.submit(mTask);
	    return mTask;
	}
	
	public interface ProgressCallback{
		public void onContentLength(long contentLength);
		public void onBytesDownloaded(long totalBytesDownloaded);
		// error - 0 on success, else error
		public void onComplete(int error);
	}
	
}
