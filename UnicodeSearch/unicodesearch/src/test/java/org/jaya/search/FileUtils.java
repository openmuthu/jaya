package org.jaya.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	
	public static List<File> getListOfFiles(File sourceDir) {
		ArrayList<File> fileList = new ArrayList<>();
		File[] files = sourceDir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				fileList.addAll(getListOfFiles(file));
			} else {
				fileList.add(file);
			}
		}
		return fileList;
	}
}
