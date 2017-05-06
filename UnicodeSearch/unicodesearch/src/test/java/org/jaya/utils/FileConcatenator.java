package org.jaya.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jaya.util.Constatants;
import org.jaya.util.Utils;

public class FileConcatenator {
	
	public static void main(String[] args){
		String folderPath = Constatants.FILES_TO_INDEX_DIRECTORY + "/kOsha/shabdakalpadruma";
		concatFilesFromFolder(folderPath, folderPath + File.separator + Utils.getBaseName(folderPath) + ".txt");
	}
	
	public static void concatFilesFromFolder(String folderPath, String destFilePath){
		List<File> files = Utils.getListOfFiles(new File(folderPath));
		files.sort(new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				try{
					return o1.getCanonicalPath().compareTo(o2.getCanonicalPath());
				}catch(IOException ex){
					ex.printStackTrace();
				}
				return 1;
			}
		});
		concatFiles(Arrays.stream(files.toArray()).toArray(File[]::new), destFilePath);
	}

	public static void concatFiles(File[] files, String destFilePath){
		try{
			File destFile = new File(destFilePath);
			String finalString = "";
			for(File file:files){
				if( !Utils.getFileExtension(file.getCanonicalPath()).equalsIgnoreCase("txt") )
					continue;
				String str = FileUtils.readFileToString(file, "UTF-16LE");
				if( str != null )
					finalString += str;
			}
			FileUtils.writeStringToFile(destFile, finalString, "UTF-16LE");
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}
}
