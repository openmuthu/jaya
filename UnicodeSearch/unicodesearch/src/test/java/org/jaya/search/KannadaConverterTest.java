package org.jaya.search;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.util.Constatants;

public class KannadaConverterTest {
	
	public static void main(String[] args){
		String filePath = Constatants.FILES_TO_INDEX_DIRECTORY + File.separator + "others/sAtvata taMtra.txt";
		convertFile(filePath);
	}

	public static void convertFile(String path){
		try{
			String devanagariString = FileUtils.readFileToString(new File(path), "UTF-16");
			ScriptConverter devanagariToITransConverter = ScriptConverterFactory.getScriptConverter(ScriptType.DEVANAGARI, ScriptType.ITRANS);
			String itransString = devanagariToITransConverter.convert(devanagariString);
			ScriptConverter itransToKannadaConverter = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.KANNADA);
			System.out.println("itrans: " + itransString);			
			String kannadaString = itransToKannadaConverter.convert(itransString);
			System.out.println("kannada: " + kannadaString);
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
}
