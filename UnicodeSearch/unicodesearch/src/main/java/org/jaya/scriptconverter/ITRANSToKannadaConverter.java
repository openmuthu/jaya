package org.jaya.scriptconverter;

import java.util.HashMap;
import java.util.Map;

public class ITRANSToKannadaConverter implements ScriptConverter {
	
	private static Map<String, String> itransToSanskritMap;

	static {
		itransToSanskritMap = new HashMap<String, String>();
		itransToSanskritMap.put("a", "\u0C85");
		itransToSanskritMap.put("A", "\u0C86");
		itransToSanskritMap.put("aa", "\u0C86");
		itransToSanskritMap.put("i", "\u0C87");
		itransToSanskritMap.put("ii", "\u0C88");
		itransToSanskritMap.put("I", "\u0C88");
		itransToSanskritMap.put("u", "\u0C89");
		itransToSanskritMap.put("U", "\u0C8A");
		itransToSanskritMap.put("uu", "\u0C8A");
		itransToSanskritMap.put("R", "\u0C8B");
		itransToSanskritMap.put("Ru", "\u0C8B");
		itransToSanskritMap.put("Ri", "\u0C8B");
		itransToSanskritMap.put("RRi", "\u0C8B");
		itransToSanskritMap.put("R^i", "\u0C8B");
		itransToSanskritMap.put("RR", "\u0CE0");
		itransToSanskritMap.put("RRI", "\u0CE0");
		itransToSanskritMap.put("R^I", "\u0CE0");
		itransToSanskritMap.put("LLi", "\u0C8C");
		itransToSanskritMap.put("L^i", "\u0C8C");
		itransToSanskritMap.put("LLI", "\u0CE1");
		itransToSanskritMap.put("L^I", "\u0CE1");
		itransToSanskritMap.put("e", "\u0C8E");
		itransToSanskritMap.put("E", "\u0C8F");
		itransToSanskritMap.put("ai", "\u0C90");
		itransToSanskritMap.put("o", "\u0C92");
		itransToSanskritMap.put("O", "\u0C93");
		itransToSanskritMap.put("au", "\u0C94");
		itransToSanskritMap.put("ou", "\u0C94");
		itransToSanskritMap.put("M", "\u0C82");
		itransToSanskritMap.put("H", "\u0C83");
		itransToSanskritMap.put(":", "\u0C83");
		itransToSanskritMap.put("k", "\u0C95");
		itransToSanskritMap.put("K", "\u0C96");
		itransToSanskritMap.put("kh", "\u0C96");
		itransToSanskritMap.put("Kh", "\u0C96");
		itransToSanskritMap.put("g", "\u0C97");
		itransToSanskritMap.put("G", "\u0C98");
		itransToSanskritMap.put("gh", "\u0C98");
		itransToSanskritMap.put("Gh", "\u0C98");
		itransToSanskritMap.put("~", "~");
		itransToSanskritMap.put("~N", "\u0C99");
		itransToSanskritMap.put("c", "\u0C9A");
		itransToSanskritMap.put("ch", "\u0C9A");
		itransToSanskritMap.put("C", "\u0C9B");
		itransToSanskritMap.put("Ch", "\u0C9B");
		itransToSanskritMap.put("j", "\u0C9C");
		itransToSanskritMap.put("J", "\u0C9D");
		itransToSanskritMap.put("jh", "\u0C9D");
		itransToSanskritMap.put("Jh", "\u0C9D");
		itransToSanskritMap.put("~n", "\u0C9E");
		itransToSanskritMap.put("jn", "\u0C9C\u0CCD\u0C9E");
		itransToSanskritMap.put("T", "\u0C9F");
		itransToSanskritMap.put("Th", "\u0CA0");
		itransToSanskritMap.put("D", "\u0CA1");
		itransToSanskritMap.put("Dh", "\u0CA2");
		itransToSanskritMap.put("N", "\u0CA3");
		itransToSanskritMap.put("t", "\u0CA4");
		itransToSanskritMap.put("th", "\u0CA5");
		itransToSanskritMap.put("d", "\u0CA6");
		itransToSanskritMap.put("dh", "\u0CA7");
		itransToSanskritMap.put("n", "\u0CA8");
		itransToSanskritMap.put("p", "\u0CAA");
		itransToSanskritMap.put("P", "\u0CAB");
		itransToSanskritMap.put("ph", "\u0CAB");
		itransToSanskritMap.put("Ph", "\u0CAB");
		itransToSanskritMap.put("b", "\u0CAC");
		itransToSanskritMap.put("B", "\u0CAD");		
		itransToSanskritMap.put("bh", "\u0CAD");
		itransToSanskritMap.put("Bh", "\u0CAD");
		itransToSanskritMap.put("m", "\u0CAE");
		itransToSanskritMap.put("y", "\u0CAF");
		itransToSanskritMap.put("r", "\u0CB0");
		itransToSanskritMap.put("l", "\u0CB2");
		itransToSanskritMap.put("L", "\u0CB3");
		itransToSanskritMap.put("v", "\u0CB5");
		itransToSanskritMap.put("w", "\u0CB5");
		itransToSanskritMap.put("sh", "\u0CB6");
		itransToSanskritMap.put("Sh", "\u0CB7");
		itransToSanskritMap.put("S", "\u0CB7");
		itransToSanskritMap.put("s", "\u0CB8");
		itransToSanskritMap.put("h", "\u0CB9");
		itransToSanskritMap.put("x", "\u0C95\u0CCD\u0CB7");
		itransToSanskritMap.put("OM", "\u0C93\u0C82");
		itransToSanskritMap.put(".", ".");
		itransToSanskritMap.put(".a", "\u0CBD");
		
		itransToSanskritMap.put("{", "{");
		itransToSanskritMap.put("{#", "{#");
		itransToSanskritMap.put("}", "}");
		itransToSanskritMap.put("#}", "#}");
	}		
	
	private boolean isConsonent(char ch){
		int numVal = (int)ch;
		return numVal >= 0x0C95 && numVal <= 0x0CB9;
	}
	
	private boolean isVowel(char ch){
		int numVal = (int)ch;
		return numVal >= 0x0C85 && numVal <= 0x0C94;
	}	

	@Override
	public ScriptType getSourceScript() {
		return ScriptType.ITRANS;
	}

	@Override
	public ScriptType getDestinationScript() {
		return ScriptType.KANNADA;
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
					sanskritChar = "\u0CCD"+curKey;
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
						retVal += "\u0CCD";
					else if(isCurCharVowel)
					{
						int curCharNumVal = (int)curChar;
						if( curCharNumVal != 0x0C85 )
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
			retVal += "\u0CCD";		
		
		return retVal;
	}
	
	
	public static void main(String[] args){
		String itrans = "naaraayaNaM namaskRtya naraM chaiva narOttamam | dEvIM sarasvatIM vyaasaM tatO jayamudIrayE ||";
		ITRANSToKannadaConverter conv = new ITRANSToKannadaConverter();
		String kannadaStr = conv.convert(itrans);
		System.out.println(kannadaStr);
	}

}
