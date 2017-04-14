package org.jaya.search;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.apache.lucene.util.ArrayUtil;
import org.jaya.util.Constatants;
import org.jaya.search.*;

public class FileEncodingConverter {
	
	public static void main(String[] args){
		convertFilesToUTF16LE(Constatants.FILES_TO_INDEX_DIRECTORY);
		//deleteGeneratedFiles(Constatants.FILES_TO_INDEX_DIRECTORY);
	}
	
	public static void deleteGeneratedFiles(String dir){
		List<File> fileList = org.jaya.search.FileUtils.getListOfFiles(new File(dir));
		for (File file : fileList) {
			try{
				String absPath = file.getAbsolutePath();
				if( absPath.contains("_u16le") ){
					FileUtils.deleteQuietly(file);
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		System.out.println("delete complete.");
	}

	public static void convertFilesToUTF16LE(String dir){
		List<File> fileList = org.jaya.search.FileUtils.getListOfFiles(new File(dir));
		for (File file : fileList) {
			try{
				String newFilePath = file.getAbsolutePath();
				System.out.println("Path: " + newFilePath);				
				File f1 = new File(newFilePath);				
				if( !FilenameUtils.getExtension(newFilePath).equals("txt") )
					continue;
				if( newFilePath.contains("brahmAMDa purANa.txt")){
					System.out.println("brahmAMDa purANa.txt");
				}
				//newFilePath = f1.getParent() + File.separator + FilenameUtils.getBaseName(newFilePath) + "_u16le." + FilenameUtils.getExtension(newFilePath);
				byte[] bytes = FileUtils.readFileToByteArray(file);
				if( bytes[0] == (byte)0xFE && bytes[1] == (byte)0xFF ){					
					// UTF-16BE
					String str = new String(bytes, 2, bytes.length-2, Charset.forName("UTF-16BE"));
					System.out.println("Writing File UTF-16BE to UTF-16LE: " + newFilePath);
					FileUtils.write(new File(newFilePath), str, "UTF-16LE");
				}
				else if( bytes[0] == (byte)0xFF && bytes[1] == (byte)0xFE ){
					//UTF-16LE
					System.out.println("Skipping as file is already UTF-16LE: " + newFilePath);
				}
				else if( (bytes[0] == (byte)0xEF) && (bytes[1] == (byte)0xBB) && (bytes[2] == (byte)0xBF) ){
					//UTF-8 BOM
					System.out.println("Writing File UTF-8BOM to UTF-16LE: " + newFilePath);
					String str = new String(bytes, 0, bytes.length, Charset.forName("UTF-8"));
					FileUtils.write(new File(newFilePath), str, "UTF-16LE");					
				}
				else{
					System.out.println("Writing File UTF-8 to UTF-16LE: " + newFilePath);
					byte[] utf8WithBOM = new byte[3+bytes.length];//{(byte)0xEF, (byte)0xBB, (byte)0xFE};
					utf8WithBOM[0] = (byte)0xEF;
					utf8WithBOM[1] = (byte)0xBB;
					utf8WithBOM[2] = (byte)0xBF;
					//utf8WithBOM = bytes;
					System.arraycopy(bytes, 0, utf8WithBOM, 3, bytes.length);
					String str = new String(utf8WithBOM, 0, utf8WithBOM.length, Charset.forName("UTF-8"));
					FileUtils.write(new File(newFilePath), str, "UTF-16LE");
					//FileUtils.write(new File(newFilePath), str, "UTF-8");
				}
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}	
}
