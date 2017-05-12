package org.jaya.search;

import java.io.File;
import java.util.List;

import org.jaya.util.Constatants;
import org.jaya.util.Utils;

public class UtilsTest {
	
	public static void main(String[] args){
		testGuessFileEncoding();
	}
	
	public static void testGuessFileEncoding(){
		try{
			List<File> files = Utils.getListOfFiles(Constatants.getToBeIndexedDirectory());
			for(File file:files){
				System.out.println(Utils.getBaseName(file.getCanonicalPath()) + " - " + Utils.guessFileEncoding(file.getCanonicalPath()));
			}
		}catch(Exception ex){}
		
	}

}
