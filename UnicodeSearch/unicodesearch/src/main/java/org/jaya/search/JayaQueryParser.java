package org.jaya.search;

import java.util.ArrayList;
import java.util.List;

public class JayaQueryParser {
	
	String mSearchString = "";
	boolean mbIsFuzzyQuery = false;
	boolean mbIsLuceneQueryParserQuery = false;
	boolean mbIsRegExPrefixQuery = false; // Is this query some thing like *searchString* ?
	String mParsedQuery = "";
	List<String> mSearchWords = new ArrayList<>();
	List<String> mSearchTags = new ArrayList<>();
	
	public static void main(String[] args){
		test_convertToRegExQueryWithTags();
	}
	
	static void test_convertToRegExQueryWithTags(){
		String test1 = new JayaQueryParser("hari #amara #kOsha").getParsedQuery();
		System.out.println("test1: " + test1);
	}
	
	public JayaQueryParser(String searchString){
		mSearchString = searchString;
		parse();
	}
	
	public String getOriginalSearchString(){
		return mSearchString;
	}
	
	public boolean isFuzzyQuery(){
		return mbIsFuzzyQuery;
	}
	
	public boolean isLuceneQueryParserQuery(){
		return mbIsLuceneQueryParserQuery;
	}
	
	public boolean isRegExPrefixQuery(){
		return mbIsRegExPrefixQuery;
	}
	
	public List<String> getSearchWords(){
		return mSearchWords;
	}
	
	public List<String> getSearchTags(){
		return mSearchTags;
	}
	
	protected void parse(){
		if( mSearchString.startsWith("f/") ){
			mbIsFuzzyQuery = true;
			mSearchString.replace("f/", "");
		}
		else if(mSearchString.startsWith("q/")){
			mbIsLuceneQueryParserQuery = true;
			mSearchString.replace("q/", "");
		}
		else if(mSearchString.startsWith("~/")){
			mbIsRegExPrefixQuery = true;
			mSearchString.replace("~/", "");
		}
		if( mbIsFuzzyQuery ){
			mParsedQuery = convertToFuzzyQueryWithTags(mSearchString);
		}
		else if(mbIsLuceneQueryParserQuery){
			mParsedQuery = mSearchString;
		}
		else if( mbIsRegExPrefixQuery ){
			mParsedQuery = mSearchString;
		}
		else{
			mParsedQuery = convertToRegExQueryWithTags(mSearchString);
		}
		
	}
	
	public String getParsedQuery(){
		return mParsedQuery;
	}
	
	
	// This method will pre-process the query entered by user
	// Eg: converting # tags to equivalents in lucene, adding * at the end of each word etc
	protected String convertToRegExQueryWithTags(String originalQuery){
		return convertToQueryWithTags(originalQuery, "*");
	}
		
	protected String convertToFuzzyQueryWithTags(String originalQuery){
		return convertToQueryWithTags(originalQuery, "~2");
	}
		
	protected String convertToQueryWithTags(String originalQuery, String wordSuffix){		
		String retVal = "";
		String[] tokensBySpace = originalQuery.split(" ");
		for( String token: tokensBySpace ){
			if(token.isEmpty())
				continue;
			if( token.startsWith("#") ){
				mSearchTags.add(token.substring(1));
			}
			else{
				if( !token.endsWith(wordSuffix) )
					mSearchWords.add(token+wordSuffix);
				else
					mSearchWords.add(token);
			}
		}
		for(String word:mSearchWords){
			retVal += word + " ";
		}
		if( mSearchTags.size() > 0 ){
			retVal = "(" + retVal + ")&&(";
		}
		for(int i=0;i<mSearchTags.size();i++){
			if( i!=0 )
				retVal += " && ";
			retVal += "tags:" + mSearchTags.get(i) + wordSuffix;
		}
		if( mSearchTags.size() > 0 )
			retVal += ")";		
		return retVal;
	}
}
