package org.jaya.util;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jaya.scriptconverter.ScriptConverter;
import org.jaya.scriptconverter.ScriptConverterFactory;
import org.jaya.scriptconverter.ScriptType;

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
	
	public static final String removeExtension(final String filename){
		if (filename == null) return null;
		int dotIndex = filename.lastIndexOf(".");
		return filename.substring(0, dotIndex);
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
    
    public static final String getFileName(final String filePath) {
        if (filePath == null) return null;
        int lastSlashIndex = filePath.lastIndexOf(File.separatorChar);
        final String afterLastSlash = filePath.substring(lastSlashIndex + 1);
        return afterLastSlash;
    }    
    
    public static final String getTagsBasedOnFileName(String fileName){
		String basename = getBaseName(fileName);
		String retVal = basename.replaceAll("[\\s-]+", "");
		
		ScriptConverter it2dev = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS,
				ScriptType.DEVANAGARI);
		retVal += " " + it2dev.convert(retVal);
		
		return retVal;
    }
    
	public static List<File> getListOfFiles(File sourceDir) {
		ArrayList<File> fileList = new ArrayList<>();
		File[] files = sourceDir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				fileList.addAll(getListOfFiles(file));
			} else {
				fileList.add(file);
			}
		}
		return fileList;
	}  
	
	public static List<File> getFirstLevelDirs(File sourceDir) {
		ArrayList<File> fileList = new ArrayList<>();
		File[] files = sourceDir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				fileList.add(file);
			}
		}
		return fileList;
	}	
	
	public static void closeSilently(Closeable closable){
		if( closable == null )
			return;
		try{
			closable.close();
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	public static void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}
	
	public static String getHumanReadableSize(long size) {
		int unit = 1024;
	    if (size < unit) return size + " B";
	    int exp = (int) (Math.log(size) / Math.log(unit));
	    String pre = "KMGTPE".charAt(exp-1) + "";
	    return String.format("%.1f %sB", size / Math.pow(unit, exp), pre);
	}	
	
	public static String guessFileEncoding(String path){
		String retVal = "UTF-8";
		try(FileInputStream fis = new FileInputStream(path)){			
			byte[] BOM = new byte[3];
			if( fis.read(BOM, 0, 3) >= 0 ){
				if( (BOM[0] == (byte)0xFE && BOM[1] == (byte)0xFF)
					|| (BOM[0] == (byte)0xFF && BOM[1] == (byte)0xFE) )
					return "UTF-16";
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retVal;
	}
	
	private static char nextChar(String str, int index){
		if( index+1 < str.length() )
			return str.charAt(index+1);
		return (char)0;
	}
	
	public static boolean containsViramaChars(String str){
		if( str == null || str.isEmpty() )
			return false;
		boolean retVal = false;
		char ch;
		for(int i=0;i<str.length();i++){
			switch(str.charAt(i)){
				case '\u0965':
					return true;
				case '\u0964':
					if( nextChar(str, i) == '\u0964' )
						return true;
					break;
				case '|':
					if( nextChar(str, i) == '|' && Character.isWhitespace(nextChar(str, i+1)) )
						return true;
					break;
				case '/':
					if( nextChar(str, i) == '/' && Character.isWhitespace(nextChar(str, i+1)) )
						return true;
					break;					
			}
		}
		return retVal;
	}
}
