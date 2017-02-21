package org.jaya.scriptconverter;

import java.util.HashMap;
import java.util.Map;

public class DevaNagariToITRANSConverter implements ScriptConverter {
	
	private static Map<String, String> devaNagariToITRANSMap;

	static {
		devaNagariToITRANSMap = new HashMap<String, String>();
		devaNagariToITRANSMap.put("\u0905", "a");
		devaNagariToITRANSMap.put("\u0906", "A");
		devaNagariToITRANSMap.put("\u0907", "i");
		devaNagariToITRANSMap.put("\u0908", "I");
		devaNagariToITRANSMap.put("\u0909", "u");
		devaNagariToITRANSMap.put("\u090A", "U");
		devaNagariToITRANSMap.put("\u090B", "R");
		//devaNagariToITRANSMap.put("\u090B", "RRi");
		//devaNagariToITRANSMap.put("\u090B", "R^i");
		devaNagariToITRANSMap.put("\u0960", "RR");
		//devaNagariToITRANSMap.put("\u0960", "RRI");
		//devaNagariToITRANSMap.put("\u0960", "R^I");
		devaNagariToITRANSMap.put("\u090C", "LLi");
		//devaNagariToITRANSMap.put("\u090C", "L^i");
		devaNagariToITRANSMap.put("\u0961", "LLI");
		//devaNagariToITRANSMap.put("\u0961", "L^I");
		//devaNagariToITRANSMap.put("\u090F", "e");
		devaNagariToITRANSMap.put("\u090F", "E");
		devaNagariToITRANSMap.put("\u0910", "ai");
		//devaNagariToITRANSMap.put("\u0913", "o");
		devaNagariToITRANSMap.put("\u0913", "O");
		devaNagariToITRANSMap.put("\u0914", "au");
		devaNagariToITRANSMap.put("\u0902", "M");
		devaNagariToITRANSMap.put("\u0903", "H");
		
		devaNagariToITRANSMap.put("\u093E", "A");
		devaNagariToITRANSMap.put("\u093F", "i");
		devaNagariToITRANSMap.put("\u0940", "I");
		devaNagariToITRANSMap.put("\u0941", "u");
		devaNagariToITRANSMap.put("\u0942", "U");
		devaNagariToITRANSMap.put("\u0943", "R");
		devaNagariToITRANSMap.put("\u0944", "RR");
		devaNagariToITRANSMap.put("\u0947", "E");
		devaNagariToITRANSMap.put("\u0948", "ai");
		devaNagariToITRANSMap.put("\u094B", "O");
		devaNagariToITRANSMap.put("\u094C", "au");
		
		devaNagariToITRANSMap.put("\u0915", "k");
		devaNagariToITRANSMap.put("\u0916", "kh");
		devaNagariToITRANSMap.put("\u0917", "g");
		devaNagariToITRANSMap.put("\u0918", "gh");
		devaNagariToITRANSMap.put("\u0919", "~N");
		devaNagariToITRANSMap.put("\u091A", "ch");
		devaNagariToITRANSMap.put("\u091B", "Ch");
		devaNagariToITRANSMap.put("\u091C", "j");
		devaNagariToITRANSMap.put("\u091D", "jh");
		devaNagariToITRANSMap.put("\u091E", "~n");
		devaNagariToITRANSMap.put("\u091C\u094D", "j");
		devaNagariToITRANSMap.put("\u091C\u094D\u091E", "jn");
		devaNagariToITRANSMap.put("\u091F", "T");
		devaNagariToITRANSMap.put("\u0920", "Th");
		devaNagariToITRANSMap.put("\u0921", "D");
		devaNagariToITRANSMap.put("\u0922", "Dh");
		devaNagariToITRANSMap.put("\u0923", "N");
		devaNagariToITRANSMap.put("\u0924", "t");
		devaNagariToITRANSMap.put("\u0925", "th");
		devaNagariToITRANSMap.put("\u0926", "d");
		devaNagariToITRANSMap.put("\u0927", "dh");
		devaNagariToITRANSMap.put("\u0928", "n");
		devaNagariToITRANSMap.put("\u092A", "p");
		devaNagariToITRANSMap.put("\u092B", "ph");
		devaNagariToITRANSMap.put("\u092C", "b");
		devaNagariToITRANSMap.put("\u092D", "bh");
		devaNagariToITRANSMap.put("\u092E", "m");
		devaNagariToITRANSMap.put("\u092F", "y");
		devaNagariToITRANSMap.put("\u0930", "r");
		devaNagariToITRANSMap.put("\u0932", "l");
		devaNagariToITRANSMap.put("\u0933", "L");
		devaNagariToITRANSMap.put("\u0935", "v");
		devaNagariToITRANSMap.put("\u0936", "sh");
		devaNagariToITRANSMap.put("\u0937", "Sh");
		//devaNagariToITRANSMap.put("\u0937", "S");
		devaNagariToITRANSMap.put("\u0938", "s");
		devaNagariToITRANSMap.put("\u0939", "h");
		devaNagariToITRANSMap.put("\u0915\u094D", "k");
		devaNagariToITRANSMap.put("\u0915\u094D\u0937", "x");
		//devaNagariToITRANSMap.put("\u0950", "om");
		devaNagariToITRANSMap.put("\u0950", "OM");
		devaNagariToITRANSMap.put("\u093D", ".a");
		
		devaNagariToITRANSMap.put("\u094D", "");
	}		

	@Override
	public String getSourceScript() {
		return ScriptConverter.DEVANAGARI_SCRIPT;
	}

	@Override
	public String getDestinationScript() {
		return ScriptConverter.ITRANS_SCRIPT;
	}
	
	private boolean isConsonent(char ch){
		int numVal = (int)ch;
		return numVal >= 0x0915 && numVal <= 0x0939;
	}	
	
	private boolean isDependentVowel(char ch){
		int numVal = (int)ch;
		return numVal >= 0x093E && numVal <= 0x094F;		
	}
	
	private boolean isDependentVowelOrHalanth(char ch){
		return isDependentVowel(ch) || ch == '\u094D';
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
			for(i = 1;(curKey != null) && ((tempChar = devaNagariToITRANSMap.get(curKey)) != null);i++)
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
				pos++;
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
