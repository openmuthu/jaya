package org.jaya.util;

import java.io.Writer;

import org.json.simple.JSONObject;

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
	
	public static String prettyJSONString(JSONObject obj){
		String retVal = "";
		try{
			Writer writer = new JSONWriter();
			obj.writeJSONString(writer);
			retVal = writer.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		return retVal;
	}
}
