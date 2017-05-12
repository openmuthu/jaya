package org.jaya.util;

public class StringUtils {
	public static String join(String delim, String[] args){
		StringBuilder retVal = new StringBuilder();
		boolean isFirst = true;
		for(String str:args){
			if(isFirst){
				retVal.append(str);
				isFirst = false;
			}
			else
				retVal.append(delim + str);
		}
		return retVal.toString();
	}
}
