package org.jaya.util;

import java.io.File;

public class PathUtils {
	public static String get(String pathPrefix, String... pathComponents){
		String retVal = pathPrefix;
		for(String pathComponent:pathComponents){
			if( !retVal.endsWith(File.separator) )
				retVal += File.separator;			
			retVal += pathComponent;
		}
		return retVal;
	}
}
