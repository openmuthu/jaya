package org.jaya.search;

import java.util.ArrayList;

public class QueryConverter {
	
	public static void main(String[] args){
		test_convertToRegExQueryWithTags();
	}
	
	static void test_convertToRegExQueryWithTags(){
		String test1 = convertToRegExQueryWithTags("hari #amara #kOsha");
		System.out.println("test1: " + test1);
	}
	
	// This method will pre-process the query entered by user
	// Eg: converting # tags to equivalents in lucene, adding * at the end of each word etc
	public static String convertToRegExQueryWithTags(String originalQuery){
		return convertToQueryWithTags(originalQuery, "*");
	}
		
	public static String convertToFuzzyQueryWithTags(String originalQuery){
		return convertToQueryWithTags(originalQuery, "~2");
	}
		
	public static String convertToQueryWithTags(String originalQuery, String wordSuffix){		
		String retVal = "";
		String[] tokensBySpace = originalQuery.split(" ");
		ArrayList<String> tags = new ArrayList<>();
		ArrayList<String> words = new ArrayList<>();
		for( String token: tokensBySpace ){
			if(token.isEmpty())
				continue;
			if( token.startsWith("#") ){
				tags.add(token.substring(1));
			}
			else{
				if( !token.endsWith(wordSuffix) )
					words.add(token+wordSuffix);
				else
					words.add(token);
			}
		}
		for(String word:words){
			retVal += word + " ";
		}
		if( tags.size() > 0 ){
			retVal = "(" + retVal + ")&&(";
		}
		for(int i=0;i<tags.size();i++){
			if( i!=0 )
				retVal += " && ";
			retVal += "tags:" + tags.get(i) + wordSuffix;
		}
		if( tags.size() > 0 )
			retVal += ")";		
		return retVal;
	}
}
