package org.jaya.search;

import org.jaya.scriptconverter.SCUtils;
import org.jaya.scriptconverter.ScriptType;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
	
	JayaQueryParser mJayaQueryParser;
	List<ResultDocument> mResultDocs;
	
	
	class Highlight{
		public int startIndex;
		public int length;
		
		public Highlight(int start, int length){
			startIndex = start;
			this.length = length;
		}
	}
	
	public SearchResult(JayaQueryParser jqp, List<ResultDocument> resultDocs){
		mJayaQueryParser = jqp;
		mResultDocs = resultDocs;
	}
	
	public JayaQueryParser getQueryParser(){
		return mJayaQueryParser;
	}
	
	public List<ResultDocument> getResultDocs(){
		return mResultDocs;
	}
	
	public int getDependentCharSequenceLength(String str, int startIndex){
		int retVal = 0;
		int length = str.length();
		for(int i=startIndex;(i<length) && SCUtils.isDependentCharacter(str.charAt(i));i++){
			retVal++; 
		}
		return retVal;
	}
	
	public String getSpannedString(String str, List<Highlight> highlights){
		String retVal = "";
		int index = 0;
		for(Highlight h:highlights){
			retVal += str.substring(index, h.startIndex);
			index = h.startIndex+h.length;
			// include dependent characters also in the highlight to avoid weird rendering of such characters
			index += getDependentCharSequenceLength(str, index);
			retVal += "<b><span style=\"color:#FF00FF\">" + str.substring(h.startIndex, index) + "</span></b>";
		}
		
		if( index < str.length() )
			retVal += str.substring(index);
		return retVal;
	}
	
	public Highlight getClosestMatch(String str, String word1){
		Highlight retVal = new Highlight(-1, 0);
		String word = word1;
		int index = -1;
		while(word.length()>0 && (index=str.indexOf(word)) == -1){
			word = word.substring(0, word.length()-1);
		}
		retVal.startIndex = index;
		retVal.length = word.length();
		return retVal;
	}
	
	public String getSpannedStringBasedOnCurrentQuery(String str, ScriptType scriptType){
		List<Highlight> matchedIndicesList = new ArrayList<Highlight>();
		List<String> words = mJayaQueryParser.getSearchWords();
		String retVal = "";
		int lastMatchedIndex = -1;
		str = SCUtils.convertStringToScript(str, scriptType);
		for(String word:words){
			word = SCUtils.convertStringToScript(word, scriptType);
			Highlight match = getClosestMatch(str, word);
			if( match.startIndex > lastMatchedIndex ){
				int size = matchedIndicesList.size();
				int insertAt = size;
				for(int i=0;i<size;i++){
					if( matchedIndicesList.get(i).startIndex >= match.startIndex ){
						insertAt = i;
						break;
					}
				}
				matchedIndicesList.add(insertAt, new Highlight(match.startIndex, match.length));
				Highlight finalHighlight = matchedIndicesList.get(matchedIndicesList.size()-1);
				lastMatchedIndex = finalHighlight.startIndex + finalHighlight.length-1;
			}
		}
		retVal = getSpannedString(str, matchedIndicesList);
		return retVal;
	}
}
