package org.jaya.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;

/**
 * Created by murthy on 08/04/17.
 */

public class Utils {
	
	public static void main(String[] args){
		//String str = "/Volumes/Macintosh HD 2/github/jaya/UnicodeSearch/unicodesearch/to_be_indexed/sarvamUla/mANdUkya-bhAShya";
		String str = "/Volumes/Macintosh HD 2/github/jaya/UnicodeSearch/unicodesearch/to_be_indexed/purANa/bhaviShyapurANa/parva-01/bhaviShyapurANa-parva-01-001.txt";
		String basename = getBaseName(str);
		basename = basename.replaceAll("[\\s-]+", "");
		System.out.println(basename);		 
	}

    public static final String getFileExtension(final String filename) {
        if (filename == null) return null;
        final String afterLastSlash = filename.substring(filename.lastIndexOf('/') + 1);
        final int afterLastBackslash = afterLastSlash.lastIndexOf('\\') + 1;
        final int dotIndex = afterLastSlash.indexOf('.', afterLastBackslash);
        return (dotIndex == -1) ? "" : afterLastSlash.substring(dotIndex + 1);
    }
    
    public static final String getBaseName(final String filename) {
        if (filename == null) return null;
        int lastSlashIndex = filename.lastIndexOf(File.separatorChar);
        final String afterLastSlash = filename.substring(lastSlashIndex + 1);
        final int dotIndex = afterLastSlash.lastIndexOf('.');
        return (dotIndex == -1) ? afterLastSlash.substring(0) : afterLastSlash.substring(0, dotIndex);
    }    
    
    public static final String getTagsBasedOnFileName(String fileName){
		String basename = getBaseName(fileName);
		String retVal = basename.replaceAll("[\\s-]+", "");
		
		ScriptConverter it2dev = ScriptConverterFactory.getScriptConverter(ScriptConverter.ITRANS_SCRIPT,
				ScriptConverter.DEVANAGARI_SCRIPT);
		retVal += " " + it2dev.convert(retVal);
		
		return retVal;
    }
}
