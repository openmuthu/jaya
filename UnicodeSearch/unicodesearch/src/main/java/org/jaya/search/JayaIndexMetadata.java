package org.jaya.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jaya.util.StringUtils;
import org.jaya.util.Utils;

public class JayaIndexMetadata {
	
	private static final String MD_REC_DELEMITER = "\r\n";
	private static final String MD_FILE_NAME = ".jaya-index-md.txt";
	
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
	
	public Set<String> getIndexedFilePathSet(){
		return mIndexedFilePathSet;
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
}
