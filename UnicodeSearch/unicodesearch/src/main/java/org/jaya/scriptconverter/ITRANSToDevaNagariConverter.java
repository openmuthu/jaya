package org.jaya.scriptconverter;

import java.util.HashMap;
import java.util.Map;

public class ITRANSToDevaNagariConverter implements ScriptConverter {
	
	private static Map<String, String> itransToSanskritMap;

	static {
		itransToSanskritMap = new HashMap<String, String>();
		itransToSanskritMap.put("a", "\u0905");
		itransToSanskritMap.put("A", "\u0906");
		itransToSanskritMap.put("aa", "\u0906");
		itransToSanskritMap.put("i", "\u0907");
		itransToSanskritMap.put("ii", "\u0908");
		itransToSanskritMap.put("I", "\u0908");
		itransToSanskritMap.put("u", "\u0909");
		itransToSanskritMap.put("U", "\u090A");
		itransToSanskritMap.put("uu", "\u090A");
		itransToSanskritMap.put("R", "\u090B");
		itransToSanskritMap.put("RRi", "\u090B");
		itransToSanskritMap.put("R^i", "\u090B");
		itransToSanskritMap.put("RR", "\u0960");
		itransToSanskritMap.put("RRI", "\u0960");
		itransToSanskritMap.put("R^I", "\u0960");
		itransToSanskritMap.put("LLi", "\u090C");
		itransToSanskritMap.put("L^i", "\u090C");
		itransToSanskritMap.put("LLI", "\u0961");
		itransToSanskritMap.put("L^I", "\u0961");
		itransToSanskritMap.put("e", "\u090F");
		itransToSanskritMap.put("E", "\u090F");
		itransToSanskritMap.put("ai", "\u0910");
		itransToSanskritMap.put("o", "\u0913");
		itransToSanskritMap.put("O", "\u0913");
		itransToSanskritMap.put("au", "\u0914");
		itransToSanskritMap.put("M", "\u0902");
		itransToSanskritMap.put("H", "\u0903");
		itransToSanskritMap.put("k", "\u0915");
		itransToSanskritMap.put("kh", "\u0916");
		itransToSanskritMap.put("g", "\u0917");
		itransToSanskritMap.put("gh", "\u0918");
		itransToSanskritMap.put("~", "~");
		itransToSanskritMap.put("~N", "\u0919");
		itransToSanskritMap.put("c", "\u091A");
		itransToSanskritMap.put("ch", "\u091A");
		itransToSanskritMap.put("C", "\u091B");
		itransToSanskritMap.put("Ch", "\u091B");
		itransToSanskritMap.put("j", "\u091C");
		itransToSanskritMap.put("jh", "\u091D");
		itransToSanskritMap.put("~n", "\u091E");
		itransToSanskritMap.put("jn", "\u091C\u094D\u091E");
		itransToSanskritMap.put("T", "\u091F");
		itransToSanskritMap.put("Th", "\u0920");
		itransToSanskritMap.put("D", "\u0921");
		itransToSanskritMap.put("Dh", "\u0922");
		itransToSanskritMap.put("N", "\u0923");
		itransToSanskritMap.put("t", "\u0924");
		itransToSanskritMap.put("th", "\u0925");
		itransToSanskritMap.put("d", "\u0926");
		itransToSanskritMap.put("dh", "\u0927");
		itransToSanskritMap.put("n", "\u0928");
		itransToSanskritMap.put("p", "\u092A");
		itransToSanskritMap.put("ph", "\u092B");
		itransToSanskritMap.put("b", "\u092C");
		itransToSanskritMap.put("bh", "\u092D");
		itransToSanskritMap.put("m", "\u092E");
		itransToSanskritMap.put("y", "\u092F");
		itransToSanskritMap.put("r", "\u0930");
		itransToSanskritMap.put("l", "\u0932");
		itransToSanskritMap.put("L", "\u0933");
		itransToSanskritMap.put("v", "\u0935");
		itransToSanskritMap.put("sh", "\u0936");
		itransToSanskritMap.put("Sh", "\u0937");
		itransToSanskritMap.put("S", "\u0937");
		itransToSanskritMap.put("s", "\u0938");
		itransToSanskritMap.put("h", "\u0939");
		itransToSanskritMap.put("x", "\u0915\u094D\u0937");
		itransToSanskritMap.put("om", "\u0950");
		itransToSanskritMap.put("OM", "\u0950");
		itransToSanskritMap.put(".", ".");
		itransToSanskritMap.put(".a", "\u093D");
		
		itransToSanskritMap.put("{", "{");
		itransToSanskritMap.put("{#", "{#");
		itransToSanskritMap.put("}", "}");
		itransToSanskritMap.put("#}", "#}");
	}	
	
	private boolean isConsonent(char ch){
		int numVal = (int)ch;
		return numVal >= 0x0915 && numVal <= 0x0939;
	}
	
	private boolean isVowel(char ch){
		int numVal = (int)ch;
		return numVal >= 0x0905 && numVal <= 0x0914;
	}	

	@Override
	public String convert(String itrans){
		String retVal = "";
		int pos=0;
		int length = itrans.length(), i;
		boolean bInLiteralMode = false;
		String  curKey;
		String tempChar = null, sanskritChar = null;
		boolean isPreviousCharConsonent = false;
		while(pos < length)
		{
			sanskritChar = null;
			boolean skip = false;			
			curKey = Character.toString( itrans.charAt(pos) );
			for(i = 1;(curKey != null) && ((tempChar = itransToSanskritMap.get(curKey)) != null);i++)
			{
				sanskritChar = tempChar;
				if( pos+i < length)
					curKey = itrans.substring(pos, pos+i+1);
				else
					curKey = null;
			}
			
			if(sanskritChar == null)
			{
				if(isPreviousCharConsonent)
				{
					sanskritChar = "\u094D"+curKey;
					isPreviousCharConsonent = false;
				}
				else
					sanskritChar = curKey;				
				i++;
			}
			else if( LITERAL_MODE_START_STRING == sanskritChar || LITERAL_MODE_END_STRING == sanskritChar ){
				if( !bInLiteralMode )
					bInLiteralMode = sanskritChar == LITERAL_MODE_START_STRING;
				else
					bInLiteralMode = sanskritChar != LITERAL_MODE_END_STRING;
				sanskritChar = "";
			}
			else if(bInLiteralMode){
				sanskritChar = curKey;
				i++;
			}
			else
			{
				char curChar = sanskritChar.charAt(0);
				boolean isCurCharConsonent = isConsonent(curChar);
				boolean isCurCharVowel = isVowel(curChar);
				if(isPreviousCharConsonent)
				{
					if(isCurCharConsonent)
						retVal += "\u094D";
					else if(isCurCharVowel)
					{
						int curCharNumVal = (int)curChar;
						if( curCharNumVal != 0x0905 )
							sanskritChar =  Character.toString((char)(curCharNumVal+56));
						else
							skip = true;
					}
				}
				isPreviousCharConsonent = isCurCharConsonent;				
			}
			if(!skip)
				retVal += sanskritChar;
			pos += (i-1);			
		}
		if(isPreviousCharConsonent)
			retVal += "\u094D";		
		
		return retVal;
	}

	@Override
	public String getSourceScript() {
		return ScriptConverter.ITRANS_SCRIPT;
	}

	@Override
	public String getDestinationScript() {
		return ScriptConverter.DEVANAGARI_SCRIPT;
	}
	
	public static boolean isDevanagariDependentCharacter(char ch){
		int c1 = (int)ch;
		if( (c1 >= 0x093A && c1 <= 0x094F) || (c1 >= 0x0951 && c1 <= 0x0957 ) 
				|| ( c1 >= 0x0962 && c1 <= 0x0963 ) || (c1 >= 0x0900 && c1 <= 0x0903) )
			return true;
		return false;
	}
}
