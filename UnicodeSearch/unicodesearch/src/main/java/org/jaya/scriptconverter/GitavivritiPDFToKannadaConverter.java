package org.jaya.scriptconverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.jaya.util.Constatants;
import org.jaya.util.Utils;

public class GitavivritiPDFToKannadaConverter implements ScriptConverter {
	
	private static Map<String, String> itransToSanskritMap;
	private static Map<String, String> prefixMap;

	static {
		itransToSanskritMap = new HashMap<String, String>();
		prefixMap = new HashMap<String, String>();
		itransToSanskritMap.put("A", "\u0C85"); // a
		//itransToSanskritMap.put("A", "\u0C86");
		itransToSanskritMap.put("aa", "\u0C86");
		itransToSanskritMap.put("i", "\u0C87");
		itransToSanskritMap.put("ii", "\u0C88");
		itransToSanskritMap.put("I", "\u0C88");
		itransToSanskritMap.put("C", "\u0C89"); // u
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
		//itransToSanskritMap.put("O", "\u0C93");
		itransToSanskritMap.put("au", "\u0C94");
		itransToSanskritMap.put("ou", "\u0C94");
		itransToSanskritMap.put("§", "\u0C82"); // M
		itransToSanskritMap.put("H", "\u0C83");
		itransToSanskritMap.put(":", "\u0C83");
		
		map("m", "\u0CBE"); // ಾ
		map("mm", "ಾ");
		itransToSanskritMap.put("{", "\u0CBF"); // ಿ
		itransToSanskritMap.put("r", "\u0CC0"); // ೀ
		itransToSanskritMap.put("w", "\u0CC1"); // ು
		itransToSanskritMap.put("y", "\u0CC2"); // ೂ
		itransToSanskritMap.put("¥", "\u0CC3"); // ೃ		
		itransToSanskritMap.put("o", "\u0CC7"); // ೇ
		itransToSanskritMap.put("¡", "\u0CC8"); // ೈ
		itransToSanskritMap.put("mo", "\u0CCB"); // ೋ
		itransToSanskritMap.put("m¡", "\u0CCC"); // ೌ		
		
		
		itransToSanskritMap.put("H", "\u0C95"); // k
		itransToSanskritMap.put("I", "\u0C96"); // kh
		// itransToSanskritMap.put("kh", "\u0C96");
		//itransToSanskritMap.put("Kh", "\u0C96");
		itransToSanskritMap.put("g", "\u0C97"); // g
		itransToSanskritMap.put("K", "\u0C98"); // G
		//itransToSanskritMap.put("gh", "\u0C98");
		//itransToSanskritMap.put("Gh", "\u0C98");
//		itransToSanskritMap.put("~", "~");
		//itransToSanskritMap.put("~N", "\u0C99");
		itransToSanskritMap.put("‘", "\u0C99");	// ~N		
		itransToSanskritMap.put("M", "\u0C9A"); // c
//		itransToSanskritMap.put("C", "\u0C9B");
//		itransToSanskritMap.put("Ch", "\u0C9B");
		itransToSanskritMap.put("O", "\u0C9C"); // j
//		itransToSanskritMap.put("J", "\u0C9D");
		itransToSanskritMap.put("jh", "\u0C9D");
		itransToSanskritMap.put("Jh", "\u0C9D");
		itransToSanskritMap.put("~n", "\u0C9E");
		itransToSanskritMap.put("²", "\u0CCD"); // halanth
		itransToSanskritMap.put("k", "\u0C9C\u0CCD\u0C9E"); // jn
		itransToSanskritMap.put(">", "\u0C9F"); // T
		itransToSanskritMap.put("R>", "\u0CA0"); // Th
		map("S>", "\u0CA1"); // D
		map("T", "\u0CA2"); // Dh
		map("T>", "\u0CA2"); // Dh
		itransToSanskritMap.put("U", "\u0CA3"); // N
		itransToSanskritMap.put("V", "\u0CA4"); // t
		itransToSanskritMap.put("W", "\u0CA5"); // th
		itransToSanskritMap.put("X", "\u0CA6"); // d
		itransToSanskritMap.put("Ú", "\u0CA6\u0CCD\u0CAF"); // dy
		itransToSanskritMap.put("Y", "\u0CA7"); // dh
		itransToSanskritMap.put("Z", "\u0CA8"); // n
		itransToSanskritMap.put("n", "\u0CAA"); // p
		itransToSanskritMap.put("P", "\u0CAB");
		itransToSanskritMap.put("ph", "\u0CAB");
		itransToSanskritMap.put("Ph", "\u0CAB");
		itransToSanskritMap.put("~", "\u0CAC"); // b
		itransToSanskritMap.put("B", "\u0CAD"); // B		
//		itransToSanskritMap.put("bh", "\u0CAD");
//		itransToSanskritMap.put("Bh", "\u0CAD");
		itransToSanskritMap.put("_", "\u0CAE"); // m		
		itransToSanskritMap.put("`", "\u0CAF"); // y
		itransToSanskritMap.put("a", "\u0CB0"); // r
		itransToSanskritMap.put("$a", "\u0CB0"); // r		
		itransToSanskritMap.put("b", "\u0CB2"); // l		
		itransToSanskritMap.put("L", "\u0CB3");
		itransToSanskritMap.put("v", "\u0CB5"); // v
		//itransToSanskritMap.put("w", "\u0CB5");
		itransToSanskritMap.put("e", "\u0CB6"); // sh
		itransToSanskritMap.put("f", "\u0CB7"); // Sh
		//itransToSanskritMap.put("S", "\u0CB7");
		itransToSanskritMap.put("s", "\u0CB8"); // s
		itransToSanskritMap.put("h", "\u0CB9"); // h
		itransToSanskritMap.put("j", "\u0C95\u0CCD\u0CB7"); // x
		itransToSanskritMap.put("›", "\u0C93\u0C82"); // OM
		itransToSanskritMap.put(".", ".");
		itransToSanskritMap.put("@", "\u0CBD"); // .a
		itransToSanskritMap.put("à", "\u0CAA\u0CCD\u0CB0"); // pra
		itransToSanskritMap.put("Ì", "\u0CA4\u0CCD\u0CB0");	// tra
		itransToSanskritMap.put("ó", "\u0CB8\u0CCD\u0CA4\u0CCD\u0CB0");	// stra		
		itransToSanskritMap.put("ñ", "\u0CB8\u0CCD");	// s with halanth
		itransToSanskritMap.put("ñ", "\u0CB8\u0CCD");	// s with halanth		
		itransToSanskritMap.put("î", "\u0CB7\u0CCD");	// Sh with halanth
		itransToSanskritMap.put("ï", "\u0CB7\u0CCD");	// Sh with halanth		
		itransToSanskritMap.put("å", "\u0CAE\u0CCD");	// m with halanth
		itransToSanskritMap.put("Ë", "\u0CA4\u0CCD");	// t with halanth
		itransToSanskritMap.put("Î", "\u0CA4\u0CCD\u0CA4");	// tt		
		itransToSanskritMap.put("Ï", "\u0CA5\u0CCD");	// th with halanth		
		itransToSanskritMap.put("Ê", "\u0CA3\u0CCD");	// N with halanth
		itransToSanskritMap.put("ì", "\u0CB5\u0CCD");	// v with halanth
		itransToSanskritMap.put("©", "\u0CB0\u0CCD");	// r with halanth
		itransToSanskritMap.put("ã", "\u0CAC\u0CCD");	// b with halanth
		itransToSanskritMap.put("Ý", "\u0CA8\u0CCD");	// n with halanth
		itransToSanskritMap.put("Š", "\u0C95\u0CCD");	// k with halanth		
		itransToSanskritMap.put("»", "\u0C96\u0CCD");	// kh with halanth
		itransToSanskritMap.put("«", "\u0CCD\u0CB0");	// halanth r
		itransToSanskritMap.put("÷", "\u0CB9\u0CCD\u0CAE");	// hm
		itransToSanskritMap.put("„", "\u0CB2\u0CCD\u0CB2");	// ll		
		itransToSanskritMap.put("é", "\u0CB0\u0CC1");	// ru
		itransToSanskritMap.put("º", "\u0C95\u0CCD\u0CA4");	// kt
		itransToSanskritMap.put("Õ", "ದ್ಧ");	// ದ್ಧ
		
		
		
		itransToSanskritMap.put("‚", ",");	// jj
		itransToSanskritMap.put("‚m", "\u0C9C\u0CCD\u0C9C");	// jj
		itransToSanskritMap.put("ö", "\u0CB9\u0CC3");	// hR
		itransToSanskritMap.put("á", "\u0CAA\u0CCD\u0CA4");	// pt
		itransToSanskritMap.put("á", "\u0CAA\u0CCD\u0CA4");	// pẗ
		itransToSanskritMap.put("Ü", "\u0CA7\u0CCD");	// dh with halanth
		itransToSanskritMap.put("ü", "\u0CB6\u0CCD\u0C9A");	// shca
		map("mü", "ಶ್ಚ");
		
		itransToSanskritMap.put("l", "\u0CB6\u0CCD\u0CB0");	// shr
		
		itransToSanskritMap.put("Û", "\u0CA6\u0CCD\u0CB5");	// dva
		itransToSanskritMap.put("‘>", "\u0C99\u0CCD\u0C95");	// ~Nk
		itransToSanskritMap.put("[", "[");	// ri
		itransToSanskritMap.put("[a", "ರಿ");	// ri		
		
		itransToSanskritMap.put("&", "|");	// |
		itransToSanskritMap.put("$", "");
		itransToSanskritMap.put("Ÿ", "");
		itransToSanskritMap.put("Q", "");
		
		
		// itransToSanskritMap.put("{", "{");
		// itransToSanskritMap.put("{#", "{#");
		// itransToSanskritMap.put("}", "}");
		// itransToSanskritMap.put("#}", "#}");
		
		
		//map("pñW", "ಸ್ಥಿ");	// ಸ್ಥಿ
		//map("pñV", "ಸ್ತಿ");	// ಸ್ತಿ
		map("F", "ಶ್ರೀ");		
		map("½", "ಗ್");
		map("Þ", "ನ್ನ");
		map("qY", "ಧಿಂ");
		map("Ù", "ದ್ಮ");
		map("#m", "ಂಚ");// "ಞ್ಚ");
		map("qU", "ಣಿಂ");
		map("“", "ಂಗ");
		map("ûc", "ಶ್ಲೋ");
		map("ß", "ಪ್");
		map("oe", "ಶ್ವ");
		map("í", "ಶ್");
		map("ä", "ಭ್");
		map("Îm", "ತ್ತ");
		//map("m}", "");
		map("Ø", "ದ್ಭ");
	}		
	
	public static void map(String key, String val)
	{
		for(int i=0;i<key.length()-1;i++)
		{
			String subKey = key.substring(0,i+1);
			String subKeyVal = itransToSanskritMap.get(subKey);
			if( subKeyVal == null && itransToSanskritMap.get(key.substring(i, i+1)) == null )
				itransToSanskritMap.put(subKey, subKey);
			prefixMap.put(subKey, subKey);
		}
		itransToSanskritMap.put(key, val);
	}
	
	public static void main(String[] args){
//		String[] inputs = new String[]{ 	
//				//"pñW{Vì`m©d¥{Îmu"
//				"lr_‚m`VrW© w{Z{da{MVm"
//				,"lr_X²~«÷gyÌmZwì`m»`mZñ` Q>rH$m"
//				, "lr_XmZÝXVrW©^JdËnmXmMm`©{da{MVñ`"
//				,"lr_Z²Ý`m`gwYm"
//				,"› && {l`: nË`o {ZË`mJ{UVJwU_m{UŠ`{deXà^"
//				,"OJ‚mÝ_ñWo àb`aMZmerbdnwfo"
//				,"ì`m{á`©ñ` {ZOo {ZOoZ _hgm njo gnjo pñW{Vì`m©d¥{Îmü {dnjVmo@W {df`o g{º$Z©d¡ ~m{YVoŸ&"
//				,"Z¡dmpñV à{Vnj`w{º$aVwb§ ewÕ§ à_mU§ g _o"
//				,"^y`mV² VËd{d{ZU©`m` ^JdmZmZÝXVrWm} _w{Z: Ÿ&&Ÿ3Ÿ&&"
//				,"^d{V `XZw^mdmXoS> yH$mo@{n dm½_r"
//				,"OS> {Va{n OÝVwOm©`Vo àmk_m¡{b:Ÿ&"
//				,"gH$bdMZMoVmoXodVm ^maVr gm"
//				,"d¡am½`^m½`mo __ nÙZm^-"
//				,"__ dM{g {ZYÎmm§ g{ÞqY _mZgo M Ÿ&&Ÿ4Ÿ&&"
//				,"J«mh{`Vw§ J«ÝWmXm¡ {Z~ÜZm{VŸ Zmam`U{_{VŸ&"
//				,"lr_XmZÝXVrWm©`©gÝ_Z:gagr^w{dŸ&"
//				,"AZwì`m»`mZZ{bZo M#marH${V _o _Z:Ÿ Ÿ&&Ÿ7Ÿ&&"
//				,"Z eãXmãYm¡ JmT>m ZM {ZJ_MMm©gw MVwam"
//				,"ZM Ý`m`o àm¡T>m ZM {d{XVdoÚm A{n d`_²Ÿ&"
//				,"na§ lr_ËnyU©à_{VJwéH$méÊ`gaqU"
//				,"ànÞm _mÝ`m: ñ_: {H$_{n M dXÝVmo@{n _hVm_² Ÿ&&Ÿ8Ÿ&&"
//				,"[ _“bûcmoH$ì`m»`mZ_² ]"
//				,"^JdVm ~mXam`UoZ àm{UZm§ {Z:lo`gm` àUrV_{n ~«÷_r_m§gmemó_gmYw{Z~ÝYmÝYV_gm"
//				,"AdHw$pÊR>VËdoZmàUrV{_d _Ý`_mZmo ^JdmZmZÝXVrW© w{Z`©WmMm`m©{^àm`_ñ` ^mî`§ {dYm` AZw^mî`_{n H$[aî`Z²"
//				,"{dbrZàH¥${VV`m ñd`_ÝVam`{dYwamo@ß`ZdaV_roeaàdUH$m`H$aUd¥{Îm-a{n Zmam`UàUm_m{XH§$"
//				,"àm[apßgVñ`mZÝVam`n[ag_máo: àM`ñ` M hoVwV`m@{dJrV{eï>mMma-naånam{XZm@dJV_dí`§ H$aUr`§ {eî`mZ²"
//				,"› && Zmam`U§ {Z{IbnyU©JwU¡H$Xoh§"
//				};
		
		int startLine = 1;
		int endLine = 10;
		Vector<String> inputs = new Vector<String>();
		String path = Constatants.FILES_TO_INDEX_DIRECTORY + File.separator + "gItA vivRti/gItAvivRti-10.txt_pdf";;				
		try( BufferedReader reader = 
				new BufferedReader(new InputStreamReader(new FileInputStream(path), Utils.guessFileEncoding(path))) ){
			
			String str;
			int lineNum = 0;
			while( (str=reader.readLine()) != null ){
				lineNum++;
				if( lineNum >= startLine && lineNum <= endLine )
					inputs.add(str);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		VishvanandiniPDFToKannadaConverter conv = new VishvanandiniPDFToKannadaConverter();
		int i = 0;
		for(String input: inputs)
		{
			String kannadaStr = conv.convert(input);
			System.out.println(startLine+i + ": " + input + "\n" + kannadaStr);			
			i++;
		}
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
	
	class Token
	{
		public String token;
		public int length;
		public int offset;
	}
	
	Token getNextToken(String str, int pos)
	{
		Token retVal = new Token();
		int length = str.length();
		String curKey = Character.toString( str.charAt(pos) );
		String tempChar = null, lastValidKey = curKey;
		int i;
		for(i = 1;(curKey != null) && (((tempChar = itransToSanskritMap.get(curKey)) != null) || prefixMap.get(curKey) != null) ;i++)
		{
			if( tempChar != null )
				lastValidKey = curKey;
			if( pos+i < length)
				curKey = str.substring(pos, pos+i+1);
			else
				curKey = null;
		}		
		//retVal.token = itransToSanskritMap.get(str.substring(pos, pos+i-1));
		retVal.token = itransToSanskritMap.get(lastValidKey);
		retVal.length = lastValidKey.length();		
		if( retVal.token == null )
		{
			retVal.token = curKey;
			retVal.length = curKey.length();
		}
		retVal.offset = pos;
		return retVal;
	}
	
	Token getPrevToken(String str, int pos)
	{
		Token retVal = new Token();
		int length = str.length();
		String curKey = Character.toString( str.charAt(pos) );
		String tempChar;
		int i;
		for(i = 1;(curKey != null) && ((tempChar = itransToSanskritMap.get(curKey)) != null);i++)
		{
			if( pos-i >= 0 )
				curKey = str.substring(pos-i, pos+1);
			else
				curKey = null;
		}		
		retVal.token = itransToSanskritMap.get(str.substring(pos-i+2, pos+1));
		retVal.length = i-1;
		retVal.offset = pos-i+2;
		return retVal;
	}	
	
	public int getNextSamyuktaxraEndPos(String str, int startOffset)
	{
		String  curKey = null;
		String tempChar = null;
		int length = str.length();
		int pos = startOffset;
		int i = 0;
		do
		{
			Token token = getNextToken(str, pos);
			curKey = token.token;
			pos += token.length;
		}while(curKey != null && curKey.charAt(curKey.length()-1) == '\u0CCD' );	
		return pos;
	}
	
	public int getPrevSamyuktaxraEndPos(String str, int startOffset)
	{
		String  curKey = null;
		String tempChar = null;
		int length = str.length();
		int pos = startOffset;
		int i = pos - 1;
		for(;i>0;)
		{
			Token token = getPrevToken(str, i);
			curKey = token.token;			
			if( (token.length == 1 && SCUtils.isKannadaDependentCharacter(curKey.charAt(0))) )
			{
				i -= token.length;
				continue;
			}
			else
			{
				Token prevToken = getPrevToken(str, token.offset-1);
				if( prevToken.token != null && prevToken.token.length() > 0 && SCUtils.isKannadaHalanth(prevToken.token.charAt(prevToken.token.length()-1)))
				{
					i -= token.length;
					continue;
				}
				else
				break;
			}
		}
		return i;
	}	
	
	public String preprocess(String itrans1)
	{
		String itrans = itrans1.toString();
		StringBuffer retVal = new StringBuffer(itrans);
		int length = itrans.length(), i = 0;
		for(;i< length;i++)
		{
			char ch = itrans.charAt(i);
			if( ch == '{' || ch == 'p' )
			{
				int nextSamyuktaxaraPos = getNextSamyuktaxraEndPos(itrans, i+1);
				while( i < nextSamyuktaxaraPos-1 )
				{
					retVal.setCharAt(i, itrans.charAt(++i));
				}
				if( nextSamyuktaxaraPos-1 == i )
				{
					retVal.setCharAt(i, '{');
				}
			}
		}
		itrans = retVal.toString();
		for(i = 0;i< length;i++)
		{
			char ch = retVal.charAt(i);
			if( ch == '©' )
			{
				int prevSamyuktaxaraPos = getPrevSamyuktaxraEndPos(retVal.toString(), i);
				for(int j=i;j>=prevSamyuktaxaraPos+1;j--)
				{
					retVal.setCharAt(j, retVal.charAt(j-1));
				}
				retVal.setCharAt(prevSamyuktaxaraPos, '©');
			}
		}		
		return retVal.toString();
	}

	@Override
	public String convert(String itrans){
		String retVal = "";
		int pos=0;
		itrans = preprocess(itrans);
		//System.out.println("preproceesed string: " + itrans);
		int length = itrans.length(), i;
		boolean bInLiteralMode = false;
		String  curKey;
		String tempChar = null, sanskritChar = null;
		boolean isPreviousCharConsonent = false;
		while(pos < length)
		{
			sanskritChar = null;
			boolean skip = false;			
//			curKey = Character.toString( itrans.charAt(pos) );
//			for(i = 1;(curKey != null) && ((tempChar = itransToSanskritMap.get(curKey)) != null);i++)
//			{
//				sanskritChar = tempChar;
//				if( pos+i < length)
//					curKey = itrans.substring(pos, pos+i+1);
//				else
//					curKey = null;
//			}
			Token token = getNextToken(itrans, pos);
			sanskritChar = token.token;
			i = token.length + 1;
			
			if(sanskritChar == null)
			{
				if(isPreviousCharConsonent)
				{
					//sanskritChar = "\u0CCD"+curKey;
					//sanskritChar = curKey;
					isPreviousCharConsonent = false;
				}
				else
					//sanskritChar = curKey;				
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
				//sanskritChar = curKey;
				i++;
			}
			else
			{
//				char curChar = sanskritChar.charAt(0);
//				boolean isCurCharConsonent = isConsonent(curChar);
//				boolean isCurCharVowel = isVowel(curChar);
//				if(isPreviousCharConsonent)
//				{
//					if(isCurCharConsonent)
//						retVal += "\u0CCD";
//					else if(isCurCharVowel)
//					{
//						int curCharNumVal = (int)curChar;
//						if( curCharNumVal != 0x0C85 )
//							sanskritChar =  Character.toString((char)(curCharNumVal+56));
//						else
//							skip = true;
//					}
//				}
//				isPreviousCharConsonent = isCurCharConsonent;				
			}
			if(!skip)
				retVal += sanskritChar;
			pos += (i-1);			
		}
		if(isPreviousCharConsonent)
			retVal += "\u0CCD";		
		
		return retVal;
	}

}
