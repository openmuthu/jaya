package org.jaya.util;

import java.io.File;

public class Constatants {

//	public static final String FILES_TO_INDEX_DIRECTORY = "C:\\Users\\hjuturi\\Downloads\\Sarvamoola";
//	public static final String INDEX_DIRECTORY = "C:\\Users\\hjuturi\\Downloads\\lucenIndex";
	public static final String JAYA_HOME = "C:\\Users\\vjutur\\Documents\\GitHub\\jaya";
	public static final String UNICODE_SEARCH_HOME = PathUtils.get(JAYA_HOME, "UnicodeSearch", "unicodesearch");
	public static final String FILES_TO_INDEX_DIRECTORY = PathUtils.get(UNICODE_SEARCH_HOME, "to_be_indexed");// "C:\\Users\\vjutur\\Documents\\GitHub\\jaya\\UnicodeSearch\\unicodesearch\\to_be_indexed";
	public static final String INDEX_DIRECTORY =  PathUtils.get(UNICODE_SEARCH_HOME, "index_output");
	public static final String INDEX_ZIP_OUTPUT_DIRECTORY = PathUtils.get(UNICODE_SEARCH_HOME, "index-zip-output-temp");
	//public static final String FILES_TO_INDEX_DIRECTORY = "/Volumes/Macintosh HD 2/github/jaya/UnicodeSearch/unicodesearch/to_be_indexed";
	//public static final String INDEX_DIRECTORY = "/Volumes/Macintosh HD 2/github/jaya/UnicodeSearch/unicodesearch/index_output";
	//public static final String INDEX_ZIP_OUTPUT_DIRECTORY = "/Volumes/Macintosh HD 2/github/jaya/UnicodeSearch/unicodesearch/index-zip-output-temp";
	public static final String INDEX_CATALOGUE_BASE_URL = "https://raw.githubusercontent.com/openmuthu/jaya-index-files/v1";
	//public static final String INDEX_CATALOGUE_BASE_URL = "http://192.168.1.149:8080";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_DOC_LOCAL_ID = "docLocalId";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_TAGS = "tags";
	public static final int MAX_RESULTS = 100; 
	public static final int MAX_INDEX_SIZE = 30 * 1024 * 1024;
	
	public static File getToBeIndexedDirectory(){
		return new File(FILES_TO_INDEX_DIRECTORY);
	}
	
	public static String getIndexCatalogBaseUrl(){
		return INDEX_CATALOGUE_BASE_URL;
	}

}
