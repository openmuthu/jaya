package org.jaya.scriptconverter;

//ScriptConverterUtils
public class SCUtils {
	
	static final int MAX_CHARACTERS_TO_TRY = 15;  
	
	public static ScriptType guessScript(String str){
		ScriptType type = ScriptType.ITRANS;
		int skippableCharCount = 0;
		for(int i=0; (i<str.length()) && (i < MAX_CHARACTERS_TO_TRY+skippableCharCount);i++){
			char ch = str.charAt(i);
			int chInt = (int)ch;
			// 0x0964 is '|' and 0x0965 is '||'. Don't consider these chars and whitespace while guessing script
			if(Character.isWhitespace(ch) || chInt == 0x0964 || chInt == 0x0965 ){
				skippableCharCount++;
				continue;
			}
			type = guessScript(ch);
			if( type != ScriptType.ITRANS )
				break;
		}
		return type;
	}
	
	public static ScriptType guessScript(char ch){
		ScriptType type = ScriptType.ITRANS;
		if( isDevanagari(ch) )
			return ScriptType.DEVANAGARI;
		else if( isKannada(ch) )
			return ScriptType.KANNADA;
		return type;
	}
	
	public static boolean isDevanagari(char ch){
		int chInt = (int)ch;
		return (chInt >= 0x0900) && (chInt <= 0x097F);		
	}
	
	public static boolean isKannada(char ch){
		int chInt = (int)ch;
		return (chInt >= 0x0C80) && (chInt <= 0x0CFF);
	}
	
	public static boolean isDependentCharacter(char ch){
		boolean retVal = false;
		ScriptType type = guessScript(ch);
		switch(type){
		case KANNADA:
			return isKannadaDependentCharacter(ch);
		case DEVANAGARI:
			return isDevanagariDependentCharacter(ch);
		default:
			break;
		}
		return retVal;
	}
	
	public static boolean isDevanagariDependentCharacter(char ch){
		int c1 = (int)ch;
		if( (c1 >= 0x093A && c1 <= 0x094F) || (c1 >= 0x0951 && c1 <= 0x0957 ) 
				|| ( c1 >= 0x0962 && c1 <= 0x0963 ) || (c1 >= 0x0900 && c1 <= 0x0903) )
			return true;
		return false;
	}
	
	public static boolean isKannadaDependentCharacter(char ch){
		int c1 = (int)ch;
		if( (c1 >= 0x0C82 && c1 <= 0x0C83) || (c1 >= 0x0CBC && c1 <= 0x0CDD )  )
			return true;
		return false;
	}	
	
	public static String convertStringToScript(String str, ScriptType destScriptType){
		if( destScriptType == null )
			return str;
		ScriptType sourceScriptType = guessScript(str);
		if( destScriptType == sourceScriptType )
			return str;
		ScriptConverter converter = ScriptConverterFactory.getScriptConverter(sourceScriptType, destScriptType);
		if( converter == null )
			return str;
		return converter.convert(str);
	}
}
