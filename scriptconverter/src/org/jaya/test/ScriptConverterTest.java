package org.jaya.test;

import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;

public class ScriptConverterTest {
	public static void main(String[] args)
	{
		ScriptConverter sc = ScriptConverterFactory.getScriptConverter(ScriptConverter.ITRANS_SCRIPT, ScriptConverter.DEVANAGARI_SCRIPT);
		String sanskrit =  sc.convert("bRhaddhaatukOSa");
		System.out.println("t:" + sanskrit);
	}
}
