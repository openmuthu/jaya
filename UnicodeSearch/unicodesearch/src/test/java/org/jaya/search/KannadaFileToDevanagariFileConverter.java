package org.jaya.search;

import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.util.Constatants;
import org.jaya.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class KannadaFileToDevanagariFileConverter {
	public static void main(String[] args){
		String filePath = Constatants.FILES_TO_INDEX_DIRECTORY + File.separator + "mAdhva/sarvamUla/test.txt";
		convertFile(filePath);
	}

	public static void convertFile(String path){
		try{
//			ScriptConverter converter = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.DEVANAGARI);
//			ScriptConverter converter = ScriptConverterFactory.getScriptConverter(ScriptType.DEVANAGARI, ScriptType.TELUGU);
			ScriptConverter converter = ScriptConverterFactory.getScriptConverter(ScriptType.DEVANAGARI, ScriptType.KANNADA);
//			String itrans = FileUtils.readFileToString(new File(path), "UTF16-LE");
//			String devanagari = itransToDevanagariConverter.convert(itrans);
			String outPath = path + "_d";
//			FileUtils.writeStringToFile(new File(outPath), devanagari, "UTF16-LE");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), Utils.guessFileEncoding(path)));
			FileWriter fw = new FileWriter(new File(outPath));
			String str;
			while( (str=reader.readLine()) != null ){
				fw.write(converter.convert(str)+"\r\n");
			}
			fw.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
}
