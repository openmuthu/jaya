package org.jaya.search;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jaya.util.Constatants;

public class NumericCharacterTransformer {
	
	public static void main(String[] args){
		try{
//			File file = new File(Constatants.FILES_TO_INDEX_DIRECTORY + File.separator + "vyAkaraNa/kAshikA-1.txt");
//			String filePath = file.getCanonicalPath();
//			String newFilePath = file.getParent() + File.separator + FilenameUtils.getBaseName(filePath) + "_fixed." + FilenameUtils.getExtension(filePath);		
//			convertNumericCharacters(file, newFilePath);
			convertNumericCharactersInAllFiles(Constatants.FILES_TO_INDEX_DIRECTORY);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static void convertNumericCharactersInAllFiles(String dir){
		List<File> fileList = org.jaya.search.FileUtils.getListOfFiles(new File(dir));
		try{
			for (File file : fileList) {		
				String newFilePath = file.getCanonicalPath();
				if(newFilePath.endsWith(".txt"))
					convertNumericCharacters(file, newFilePath);
			}
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}	
	
	public static void convertNumericCharacters(File file, String destFilePath){
		try{
			String filePath = file.getCanonicalPath();
			String newFilePath = (destFilePath==null || destFilePath.isEmpty())?filePath:destFilePath;
			byte[] bytes = FileUtils.readFileToByteArray(file);
			if( !(bytes[0] == (byte)0xFF && bytes[1] == (byte)0xFE) ){
				System.out.println("convertNumericCharacters() error: file not in UTF16LE format. " + filePath);
				return;
			}
			for(int i = 2;i<bytes.length;i+=2){
				if( bytes[i] >= (byte)0x66 && bytes[i] <= (byte)0x6F && (bytes[i+1] == (byte)0x09 || bytes[i+1] == (byte)0x0C) ){
					// Convert devanagari or telugu numbers to ascii numbers
					bytes[i] = (byte)(0x30 + (int)bytes[i] - 0x66);
					bytes[i+1] = 0x00;
					if( i+3 < bytes.length && bytes[i+2] == (byte)',' ){
						bytes[i+2] = (byte)'.';
					}
				}
				else if( bytes[i] >= (byte)0xE6 && bytes[i] <= (byte)0xEF && bytes[i+1] == (byte)0x0C  ){
					// Convert kannada numbers to ascii numbers
					bytes[i] = (byte)(0x30 + (int)bytes[i] - 0xE6);
					bytes[i+1] = 0x00;	
					if( i+3 < bytes.length && bytes[i+2] == (byte)',' ){
						bytes[i+2] = (byte)'.';
					}					
				}				
			}
			FileUtils.writeByteArrayToFile(new File(newFilePath), bytes);
		}
		catch(IOException ex){
			ex.printStackTrace();
		}		
	}
}
