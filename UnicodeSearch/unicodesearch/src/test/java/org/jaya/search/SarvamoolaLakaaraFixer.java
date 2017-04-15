package org.jaya.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jaya.util.Constatants;

public class SarvamoolaLakaaraFixer {
	
	public static void main(String[] args){
		convertFilesToUTF16LE(Constatants.FILES_TO_INDEX_DIRECTORY);
	}
	
	public static void convertFilesToUTF16LE(String dir){
		List<File> fileList = org.jaya.search.FileUtils.getListOfFiles(new File(Constatants.FILES_TO_INDEX_DIRECTORY + File.separator + "sarvamUla"));
		//List<File> fileList = new ArrayList<>();
		//File f1 = new File(Constatants.FILES_TO_INDEX_DIRECTORY + File.separator + "sarvamUla/aitarEya-bhAShya.txt");
		//fileList.add(f1);
		for (File file : fileList) {			
			try{
				String filePath = file.getCanonicalPath();
				//String newFilePath = file.getParent() + File.separator + FilenameUtils.getBaseName(filePath) + "_fixed." + FilenameUtils.getExtension(filePath);
				String newFilePath = filePath;
				byte[] bytes = FileUtils.readFileToByteArray(file);
				for(int i = 2;i<bytes.length;i+=2){
					if( bytes[i] == (byte)0x83 && bytes[i+1] == (byte)0x00 ){
						bytes[i] = 0x32;
						bytes[i+1] = 0x09;
					}
					else if( bytes[i] == (byte)0x80 && bytes[i+1] == (byte)0x00 ){
						bytes[i] = 0x3D;
						bytes[i+1] = 0x09;
					}
				}
				FileUtils.writeByteArrayToFile(new File(newFilePath), bytes);
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
}
