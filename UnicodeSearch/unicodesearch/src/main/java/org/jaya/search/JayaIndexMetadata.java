package org.jaya.search;

import org.jaya.util.StringUtils;
import org.jaya.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JayaIndexMetadata {
	
	public static final String MD_REC_DELEMITER = "\r\n";
	public static final String MD_FILE_NAME = ".jaya-index-md.txt";
	
	private String mIndexStorageDirectoryPath;
	private HashSet<String> mIndexedFilePathSet = null;
	
	
	public JayaIndexMetadata(String indexStorageDirectoryPath){
		mIndexStorageDirectoryPath = indexStorageDirectoryPath;
		read();
	}
	
	@Override
	public String toString(){
		if( mIndexedFilePathSet == null )
			return "";
		return StringUtils.join(MD_REC_DELEMITER, mIndexedFilePathSet.toArray(new String[mIndexedFilePathSet.size()]));
	}
	
	public String toStringSortedList(){
		if( mIndexedFilePathSet == null )
			return "";
		String[] paths = mIndexedFilePathSet.toArray(new String[mIndexedFilePathSet.size()]);
		Arrays.sort(paths, 0, paths.length);
		return StringUtils.join(MD_REC_DELEMITER, paths);
	}	
	
	public void read(){
		if( mIndexedFilePathSet != null )
			return;
		File mdFile = getMetadataFile();
		if(!mdFile.exists())
			return;
		mIndexedFilePathSet = new HashSet<>();
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(mdFile));
			String line = null;
			while((line=br.readLine()) != null){
				mIndexedFilePathSet.add(line);				
			}
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		finally{
			Utils.closeSilently(br);			
		}
	}

	public static Set<String> getIndexedFilePathSet(String filePaths){
		Set<String> retVal = new HashSet<>();
		if( filePaths == null || filePaths.isEmpty() )
			return retVal;
		String[] pathsArray = filePaths.split(MD_REC_DELEMITER);
		for(String path:pathsArray){
			if( !path.isEmpty() )
				retVal.add(path);
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	public Set<String> getIndexedFilePathSet(){
		if( mIndexedFilePathSet == null ){
			mIndexedFilePathSet = new HashSet<>();
		}
		return (Set<String>) mIndexedFilePathSet.clone();
	}
	
	public boolean hasIndexedFile(String path){
		return mIndexedFilePathSet!=null && mIndexedFilePathSet.contains(path);
	}
	
	public File getMetadataFile(){
		File metadataFile = new File(mIndexStorageDirectoryPath + File.separator + MD_FILE_NAME);;
		return metadataFile;
	}

	public void append(Set<String> filePathSet){
		if( mIndexedFilePathSet == null ){
			mIndexedFilePathSet = new HashSet<>();
		}
		for( String file: filePathSet ){
			mIndexedFilePathSet.add(file);
		}
		writeToFile();
	}	
	
	private void writeToFile(){
		String indexedFilePaths = StringUtils.join(MD_REC_DELEMITER, mIndexedFilePathSet.toArray(new String[mIndexedFilePathSet.size()]));
		FileWriter fw = null;
		try{
			fw = new FileWriter(getMetadataFile());
			if(fw != null){
				fw.write(indexedFilePaths);
			}
		}catch(IOException ex){
			ex.printStackTrace();
		}
		finally{
			Utils.closeSilently(fw);
		}		
	}
	
	public void remove(Set<String> pathsToRemove){
		if( mIndexedFilePathSet == null || pathsToRemove == null ){
			return;
		}
		if( mIndexedFilePathSet.removeAll(pathsToRemove) )
			writeToFile();
	}
}
