package org.jaya.scriptconverter;

import java.util.HashMap;
import java.util.Map;

public class ITRANSToTeluguConverter implements ScriptConverter {
	
	private static Map<String, String> itransToTeluguMap;

	static {
		itransToTeluguMap = new HashMap<String, String>();
		itransToTeluguMap.put("a", "\u0C05");
		itransToTeluguMap.put("A", "\u0C06");
		itransToTeluguMap.put("aa", "\u0C06");
		itransToTeluguMap.put("i", "\u0C07");
		itransToTeluguMap.put("ii", "\u0C08");
		itransToTeluguMap.put("I", "\u0C08");
		itransToTeluguMap.put("u", "\u0C09");
		itransToTeluguMap.put("U", "\u0C0A");
		itransToTeluguMap.put("uu", "\u0C0A");
		itransToTeluguMap.put("R", "\u0C0B");
		itransToTeluguMap.put("RRi", "\u0C0B");
		itransToTeluguMap.put("Ri", "\u0C0B");
		itransToTeluguMap.put("Ru", "\u0C0B");
		itransToTeluguMap.put("R^i", "\u0C0B");
		itransToTeluguMap.put("RR", "\u0C60");
		itransToTeluguMap.put("RRI", "\u0C60");
		itransToTeluguMap.put("R^I", "\u0C60");
		itransToTeluguMap.put("LLi", "\u0C0C");
		itransToTeluguMap.put("L^i", "\u0C0C");
		itransToTeluguMap.put("LLI", "\u0C61");
		itransToTeluguMap.put("L^I", "\u0C61");
		itransToTeluguMap.put("e", "\u0C0E");
		itransToTeluguMap.put("E", "\u0C0F");
		itransToTeluguMap.put("ai", "\u0C10");
		itransToTeluguMap.put("o", "\u0C12");
		itransToTeluguMap.put("O", "\u0C13");
		itransToTeluguMap.put("au", "\u0C14");
		itransToTeluguMap.put("M", "\u0C02");
		itransToTeluguMap.put("H", "\u0C03");
		itransToTeluguMap.put(":", "\u0C03");
		itransToTeluguMap.put("k", "\u0C15");
		itransToTeluguMap.put("K", "\u0C16");
		itransToTeluguMap.put("kh", "\u0C16");
		itransToTeluguMap.put("g", "\u0C17");
		itransToTeluguMap.put("G", "\u0C18");
		itransToTeluguMap.put("gh", "\u0C18");
		itransToTeluguMap.put("~", "~");
		itransToTeluguMap.put("~N", "\u0C19");
		itransToTeluguMap.put("c", "\u0C1A");
		itransToTeluguMap.put("ch", "\u0C1A");
		itransToTeluguMap.put("C", "\u0C1B");
		itransToTeluguMap.put("Ch", "\u0C1B");
		itransToTeluguMap.put("j", "\u0C1C");
		itransToTeluguMap.put("J", "\u0C1D");
		itransToTeluguMap.put("jh", "\u0C1D");
		itransToTeluguMap.put("~n", "\u0C1E");
		itransToTeluguMap.put("jn", "\u0C1C\u0C4D\u0C1E");
		itransToTeluguMap.put("T", "\u0C1F");
		itransToTeluguMap.put("Th", "\u0C20");
		itransToTeluguMap.put("D", "\u0C21");
		itransToTeluguMap.put("Dh", "\u0C22");
		itransToTeluguMap.put("N", "\u0C23");
		itransToTeluguMap.put("t", "\u0C24");
		itransToTeluguMap.put("th", "\u0C25");
		itransToTeluguMap.put("d", "\u0C26");
		itransToTeluguMap.put("dh", "\u0C27");
		itransToTeluguMap.put("n", "\u0C28");
		itransToTeluguMap.put("p", "\u0C2A");
		itransToTeluguMap.put("P", "\u0C2B");
		itransToTeluguMap.put("ph", "\u0C2B");
		itransToTeluguMap.put("b", "\u0C2C");
		itransToTeluguMap.put("B", "\u0C2D");
		itransToTeluguMap.put("bh", "\u0C2D");
		itransToTeluguMap.put("m", "\u0C2E");
		itransToTeluguMap.put("y", "\u0C2F");
		itransToTeluguMap.put("r", "\u0C30");
		itransToTeluguMap.put("l", "\u0C32");
		itransToTeluguMap.put("L", "\u0C33");
		itransToTeluguMap.put("v", "\u0C35");
		itransToTeluguMap.put("sh", "\u0C36");
		itransToTeluguMap.put("Sh", "\u0C37");
		itransToTeluguMap.put("S", "\u0C37");
		itransToTeluguMap.put("s", "\u0C38");
		itransToTeluguMap.put("h", "\u0C39");
		itransToTeluguMap.put("x", "\u0C15\u0C4D\u0C37");
		itransToTeluguMap.put("OM", "\u0C50");
		itransToTeluguMap.put(".", ".");
		itransToTeluguMap.put(".a", "\u0C3D");
		
		itransToTeluguMap.put("{", "{");
		itransToTeluguMap.put("{#", "{#");
		itransToTeluguMap.put("}", "}");
		itransToTeluguMap.put("#}", "#}");
	}	
	
	private boolean isConsonent(char ch){
		int numVal = (int)ch;
		return numVal >= 0x0C15 && numVal <= 0x0C39;
	}
	
	private boolean isVowel(char ch){
		int numVal = (int)ch;
		return numVal >= 0x0C05 && numVal <= 0x0C14;
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
			for(i = 1;(curKey != null) && ((tempChar = itransToTeluguMap.get(curKey)) != null);i++)
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
					sanskritChar = "\u0C4D"+curKey;
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
						retVal += "\u0C4D";
					else if(isCurCharVowel)
					{
						int curCharNumVal = (int)curChar;
						if( curCharNumVal != 0x0C05 )
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
			retVal += "\u0C4D";		
		
		return retVal;
	}

	@Override
	public ScriptType getSourceScript() {
		return ScriptType.ITRANS;
	}

	@Override
	public ScriptType getDestinationScript() {
		return ScriptType.TELUGU;
	}
}
