package org.jaya.search;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.util.Constatants;

public class ITRANSFileToDevanagariFileConverter {

	public static void main(String[] args){
		String filePath = Constatants.FILES_TO_INDEX_DIRECTORY + File.separator + "madhvavijaya/prameyaphalamAlikA.txt_itrans";		
		convertFile(filePath);
	}

	public static void convertFile(String path){
		try{
			ScriptConverter itransToDevanagariConverter = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.DEVANAGARI);
			String itrans = FileUtils.readFileToString(new File(path), "utf8");
			String devanagari = itransToDevanagariConverter.convert(itrans);
			String outPath = path + "_d";
			FileUtils.writeStringToFile(new File(outPath), devanagari, "utf8");
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
}
