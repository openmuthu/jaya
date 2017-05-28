package org.jaya.search;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.util.Constatants;
import org.jaya.util.PathUtils;
import org.jaya.util.Utils;

public class ITRANSFileToDevanagariFileConverter {

	public static void main(String[] args){
		//deleteGeneratedFiles(Constatants.FILES_TO_INDEX_DIRECTORY);
		try{
		List<File> files = Utils.getListOfFiles(new File(PathUtils.get(Constatants.FILES_TO_INDEX_DIRECTORY, "dAsasAhitya", "vijayadAsaru")));
			for(File file:files){
				String filePath = file.getCanonicalPath();
				if( filePath.endsWith("txt_i") ){				
					convertFile(file.getCanonicalPath());
				}
			}
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
	}
	
	public static void deleteGeneratedFiles(String path){
		try{
		List<File> files = Utils.getListOfFiles(new File(path));
			for(File file:files){
				String filePath = file.getCanonicalPath();
				if( filePath.endsWith("txt_i_d") ){
					FileUtils.deleteQuietly(file);
				}
			}
		}catch(IOException ex){
			ex.printStackTrace();
		}		
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
