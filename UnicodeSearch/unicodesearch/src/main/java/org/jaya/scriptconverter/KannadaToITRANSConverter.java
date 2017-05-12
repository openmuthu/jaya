package org.jaya.scriptconverter;

import java.util.HashMap;
import java.util.Map;

public class KannadaToITRANSConverter implements ScriptConverter {
	
	private static Map<String, String> kannadaToITRANSMap;

	static {
		kannadaToITRANSMap = new HashMap<String, String>();
		kannadaToITRANSMap.put("\u0C85", "a");
		kannadaToITRANSMap.put("\u0C86", "A");
		kannadaToITRANSMap.put("\u0C87", "i");
		kannadaToITRANSMap.put("\u0C88", "I");
		kannadaToITRANSMap.put("\u0C89", "u");
		kannadaToITRANSMap.put("\u0C8A", "U");
		kannadaToITRANSMap.put("\u0C8B", "R");
		//kannadaToITRANSMap.put("\u090B", "RRi");
		//kannadaToITRANSMap.put("\u090B", "R^i");
		kannadaToITRANSMap.put("\u0CE0", "RR");
		//kannadaToITRANSMap.put("\u0960", "RRI");
		//kannadaToITRANSMap.put("\u0960", "R^I");
		kannadaToITRANSMap.put("\u0C8C", "LLi");
		//kannadaToITRANSMap.put("\u090C", "L^i");
		kannadaToITRANSMap.put("\u0CE1", "LLI");
		//kannadaToITRANSMap.put("\u0961", "L^I");
		kannadaToITRANSMap.put("\u0C8E", "e");
		kannadaToITRANSMap.put("\u0C8F", "E");
		kannadaToITRANSMap.put("\u0C90", "ai");
		kannadaToITRANSMap.put("\u0C92", "o");
		kannadaToITRANSMap.put("\u0C93", "O");
		kannadaToITRANSMap.put("\u0C94", "au");
		kannadaToITRANSMap.put("\u0C82", "M");
		kannadaToITRANSMap.put("\u0C83", "H");
		
		kannadaToITRANSMap.put("\u0CBE", "A");
		kannadaToITRANSMap.put("\u0CBF", "i");
		kannadaToITRANSMap.put("\u0CC0", "I");
		kannadaToITRANSMap.put("\u0CC1", "u");
		kannadaToITRANSMap.put("\u0CC2", "U");
		kannadaToITRANSMap.put("\u0CC3", "R");
		kannadaToITRANSMap.put("\u0CC4", "RR");
		kannadaToITRANSMap.put("\u0CC6", "e");
		kannadaToITRANSMap.put("\u0CC7", "E");
		kannadaToITRANSMap.put("\u0CC8", "ai");
		kannadaToITRANSMap.put("\u0CCA", "o");
		kannadaToITRANSMap.put("\u0CCB", "O");
		kannadaToITRANSMap.put("\u0CCC", "au");
		
		kannadaToITRANSMap.put("\u0C95", "k");
		kannadaToITRANSMap.put("\u0C96", "kh");
		kannadaToITRANSMap.put("\u0C97", "g");
		kannadaToITRANSMap.put("\u0C98", "gh");
		kannadaToITRANSMap.put("\u0C99", "~N");
		kannadaToITRANSMap.put("\u0C9A", "ch");
		kannadaToITRANSMap.put("\u0C9B", "Ch");
		kannadaToITRANSMap.put("\u0C9C", "j");
		kannadaToITRANSMap.put("\u0C9D", "jh");
		kannadaToITRANSMap.put("\u0C9E", "~n");
		kannadaToITRANSMap.put("\u0C9C\u0CCD", "j");
		kannadaToITRANSMap.put("\u0C9C\u0CCD\u0C9E", "jn");
		kannadaToITRANSMap.put("\u0C9F", "T");
		kannadaToITRANSMap.put("\u0CA0", "Th");
		kannadaToITRANSMap.put("\u0CA1", "D");
		kannadaToITRANSMap.put("\u0CA2", "Dh");
		kannadaToITRANSMap.put("\u0CA3", "N");
		kannadaToITRANSMap.put("\u0CA4", "t");
		kannadaToITRANSMap.put("\u0CA5", "th");
		kannadaToITRANSMap.put("\u0CA6", "d");
		kannadaToITRANSMap.put("\u0CA7", "dh");
		kannadaToITRANSMap.put("\u0CA8", "n");
		kannadaToITRANSMap.put("\u0CAA", "p");
		kannadaToITRANSMap.put("\u0CAB", "ph");
		kannadaToITRANSMap.put("\u0CAC", "b");
		kannadaToITRANSMap.put("\u0CAD", "bh");
		kannadaToITRANSMap.put("\u0CAE", "m");
		kannadaToITRANSMap.put("\u0CAF", "y");
		kannadaToITRANSMap.put("\u0CB0", "r");
		kannadaToITRANSMap.put("\u0CB2", "l");
		kannadaToITRANSMap.put("\u0CB3", "L");
		kannadaToITRANSMap.put("\u0CB5", "v");
		kannadaToITRANSMap.put("\u0CB6", "sh");
		kannadaToITRANSMap.put("\u0CB7", "Sh");
		//kannadaToITRANSMap.put("\u0937", "S");
		kannadaToITRANSMap.put("\u0CB8", "s");
		kannadaToITRANSMap.put("\u0CB9", "h");
		kannadaToITRANSMap.put("\u0C95\u0CCD", "k");
		kannadaToITRANSMap.put("\u0C95\u0CCD\u0CB7", "x");
		//kannadaToITRANSMap.put("\u0950", "om");
		kannadaToITRANSMap.put("\u0950", "OM");
		kannadaToITRANSMap.put("\u0CBD", ".a");
		
		kannadaToITRANSMap.put("\u0CCD", "");
	}		

	@Override
	public ScriptType getSourceScript() {
		return ScriptType.KANNADA;
	}

	@Override
	public ScriptType getDestinationScript() {
		return ScriptType.ITRANS;
	}
	
	private boolean isConsonent(char ch){
		int numVal = (int)ch;
		return numVal >= 0x0C95 && numVal <= 0x0CB9;
	}	
	
	private boolean isDependentVowel(char ch){
		int numVal = (int)ch;
		return numVal >= 0x0CBE && numVal <= 0x0CE3;		
	}
	
	private boolean isDependentVowelOrHalanth(char ch){
		return isDependentVowel(ch) || ch == '\u0CCD';
	}

	@Override
	public String convert(String devanagari) {
		String retVal = "";
		int pos=0;
		int length = devanagari.length(), i; 
		String  curKey;
		String tempChar = null, itransChar = null;
		while(pos < length)
		{
			itransChar = null;			
			curKey = Character.toString( devanagari.charAt(pos) );
			for(i = 1;(curKey != null) && ((tempChar = kannadaToITRANSMap.get(curKey)) != null);i++)
			{
				itransChar = tempChar;
				if( pos+i < length)
					curKey = devanagari.substring(pos, pos+i+1);
				else
					curKey = null;
			}
			
			pos += (i-1);
			
			if( itransChar == null )
			{
				retVal += curKey;
				pos += curKey.length();
			}
			else
			{
				retVal += itransChar;
				if( pos>0 && isConsonent(devanagari.charAt(pos-1)) && (pos >= length || !isDependentVowelOrHalanth(devanagari.charAt(pos)) ) )
					retVal += "a";
			}
		}
		return retVal;
	}

}
