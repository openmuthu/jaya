package org.jaya.util;

import java.io.File;

public interface Constatants {

//	public static final String FILES_TO_INDEX_DIRECTORY = "C:\\Users\\hjuturi\\Downloads\\Sarvamoola";
//	public static final String INDEX_DIRECTORY = "C:\\Users\\hjuturi\\Downloads\\lucenIndex";
	public static final String FILES_TO_INDEX_DIRECTORY = "/Volumes/Macintosh HD 2/github/jaya/UnicodeSearch/unicodesearch/to_be_indexed";
	public static final String INDEX_DIRECTORY = "/Volumes/Macintosh HD 2/github/jaya/android/app/src/main/assets/index";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_TAGS = "tags";
	public static final int MAX_RESULTS = 100; 
	
	public static File getToBeIndexedDirectory(){
		return new File(FILES_TO_INDEX_DIRECTORY);
	}

}
