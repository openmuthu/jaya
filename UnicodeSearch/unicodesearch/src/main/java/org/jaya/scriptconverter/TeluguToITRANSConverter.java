package org.jaya.scriptconverter;

import java.util.HashMap;
import java.util.Map;

public class TeluguToITRANSConverter implements ScriptConverter {
	
	private static Map<String, String> teluguToITRANSMap;

	static {
		teluguToITRANSMap = new HashMap<String, String>();
		teluguToITRANSMap.put("\u0C05", "a");
		teluguToITRANSMap.put("\u0C06", "A");
		teluguToITRANSMap.put("\u0C07", "i");
		teluguToITRANSMap.put("\u0C08", "I");
		teluguToITRANSMap.put("\u0C09", "u");
		teluguToITRANSMap.put("\u0C0A", "U");
		teluguToITRANSMap.put("\u0C0B", "R");
		//teluguToITRANSMap.put("\u090B", "RRi");
		//teluguToITRANSMap.put("\u090B", "R^i");
		teluguToITRANSMap.put("\u0C60", "RR");
		//teluguToITRANSMap.put("\u0960", "RRI");
		//teluguToITRANSMap.put("\u0960", "R^I");
		teluguToITRANSMap.put("\u0C0C", "LLi");
		//teluguToITRANSMap.put("\u090C", "L^i");
		teluguToITRANSMap.put("\u0C61", "LLI");
		//teluguToITRANSMap.put("\u0961", "L^I");
		teluguToITRANSMap.put("\u0C0E", "e");
		teluguToITRANSMap.put("\u0C0F", "E");
		teluguToITRANSMap.put("\u0C10", "ai");
		teluguToITRANSMap.put("\u0C12", "o");
		teluguToITRANSMap.put("\u0C13", "O");
		teluguToITRANSMap.put("\u0C14", "au");
		teluguToITRANSMap.put("\u0C02", "M");
		teluguToITRANSMap.put("\u0C03", "H");
		
		teluguToITRANSMap.put("\u0C3E", "A");
		teluguToITRANSMap.put("\u0C3F", "i");
		teluguToITRANSMap.put("\u0C40", "I");
		teluguToITRANSMap.put("\u0C41", "u");
		teluguToITRANSMap.put("\u0C42", "U");
		teluguToITRANSMap.put("\u0C43", "R");
		teluguToITRANSMap.put("\u0C44", "RR");
		teluguToITRANSMap.put("\u0C46", "e");
		teluguToITRANSMap.put("\u0C47", "E");
		teluguToITRANSMap.put("\u0C48", "ai");
		teluguToITRANSMap.put("\u0C4A", "o");
		teluguToITRANSMap.put("\u0C4B", "O");
		teluguToITRANSMap.put("\u0C4C", "au");
		
		teluguToITRANSMap.put("\u0C15", "k");
		teluguToITRANSMap.put("\u0C16", "kh");
		teluguToITRANSMap.put("\u0C17", "g");
		teluguToITRANSMap.put("\u0C18", "gh");
		teluguToITRANSMap.put("\u0C19", "~N");
		teluguToITRANSMap.put("\u0C1A", "ch");
		teluguToITRANSMap.put("\u0C1B", "Ch");
		teluguToITRANSMap.put("\u0C1C", "j");
		teluguToITRANSMap.put("\u0C1D", "jh");
		teluguToITRANSMap.put("\u0C1E", "~n");
		teluguToITRANSMap.put("\u0C1C\u0C4D", "j");
		teluguToITRANSMap.put("\u0C1C\u0C4D\u0C1E", "jn");
		teluguToITRANSMap.put("\u0C1F", "T");
		teluguToITRANSMap.put("\u0C20", "Th");
		teluguToITRANSMap.put("\u0C21", "D");
		teluguToITRANSMap.put("\u0C22", "Dh");
		teluguToITRANSMap.put("\u0C23", "N");
		teluguToITRANSMap.put("\u0C24", "t");
		teluguToITRANSMap.put("\u0C25", "th");
		teluguToITRANSMap.put("\u0C26", "d");
		teluguToITRANSMap.put("\u0C27", "dh");
		teluguToITRANSMap.put("\u0C28", "n");
		teluguToITRANSMap.put("\u0C2A", "p");
		teluguToITRANSMap.put("\u0C2B", "ph");
		teluguToITRANSMap.put("\u0C2C", "b");
		teluguToITRANSMap.put("\u0C2D", "bh");
		teluguToITRANSMap.put("\u0C2E", "m");
		teluguToITRANSMap.put("\u0C2F", "y");
		teluguToITRANSMap.put("\u0C30", "r");
		teluguToITRANSMap.put("\u0C32", "l");
		teluguToITRANSMap.put("\u0C33", "L");
		teluguToITRANSMap.put("\u0C35", "v");
		teluguToITRANSMap.put("\u0C36", "sh");
		teluguToITRANSMap.put("\u0C37", "Sh");
		//teluguToITRANSMap.put("\u0937", "S");
		teluguToITRANSMap.put("\u0C38", "s");
		teluguToITRANSMap.put("\u0C39", "h");
		teluguToITRANSMap.put("\u0C15\u0C4D", "k");
		teluguToITRANSMap.put("\u0C15\u0C4D\u0C37", "x");
		//teluguToITRANSMap.put("\u0950", "om");
		teluguToITRANSMap.put("\u0950", "OM");
		teluguToITRANSMap.put("\u0C3D", ".a");
		
		teluguToITRANSMap.put("\u0C4D", "");
	}		

	@Override
	public ScriptType getSourceScript() {
		return ScriptType.TELUGU;
	}

	@Override
	public ScriptType getDestinationScript() {
		return ScriptType.ITRANS;
	}
	
	private boolean isConsonent(char ch){
		int numVal = (int)ch;
		return numVal >= 0x0C15 && numVal <= 0x0C39;
	}	
	
	private boolean isDependentVowel(char ch){
		int numVal = (int)ch;
		return numVal >= 0x0C3E && numVal <= 0x0C63 && numVal != 0x0C60 && numVal != 0x0C61;		
	}
	
	private boolean isDependentVowelOrHalanth(char ch){
		return isDependentVowel(ch) || ch == '\u0C4D';
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
			for(i = 1;(curKey != null) && ((tempChar = teluguToITRANSMap.get(curKey)) != null);i++)
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
