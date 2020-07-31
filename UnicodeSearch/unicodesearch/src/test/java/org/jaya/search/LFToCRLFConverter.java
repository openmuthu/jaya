package org.jaya.search;

import org.jaya.util.Constatants;
import org.jaya.util.PathUtils;
import org.jaya.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

public class LFToCRLFConverter {
	public static void main(String[] args){
//		List<File> files = FileUtils.getListOfFiles(new File(Constatants.FILES_TO_INDEX_DIRECTORY));
		List<File> files = FileUtils.getListOfFiles(new File(PathUtils.get(Constatants.FILES_TO_INDEX_DIRECTORY, "mAdhva/sarvamUla/sudhA samagra/")));
		for(File file:files){
			String path = file.getAbsolutePath();
			if( !Utils.getFileExtension(path).equals("txt") )
				continue;
			String encoding = Utils.guessFileEncoding(path);
			if( encoding.equals("UTF-8") ){
				try{
					StringBuilder sb = new StringBuilder();
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
					//String line = reader.readLine();
					//sb.append(line);
					int ch;
					boolean isPrevCR = false;
					boolean isCurLF = false;
					for(;(ch=reader.read())!=-1;){
						isCurLF = ( ch == (int)'\n');
						if( isPrevCR || isCurLF ){
							if( !isCurLF )
								sb.append((char)ch);
							sb.append("\r\n");
						}
						isPrevCR = ( ch == (int)'\r' );
						if( !isPrevCR && !isCurLF ){
							sb.append((char)ch);
						}
					}
					reader.close();

					byte[] bom = new byte[2];
					bom[0] = (byte)0xFF;
					bom[1] = (byte)0xFE;
					org.apache.commons.io.FileUtils.writeByteArrayToFile(file, bom);
					org.apache.commons.io.FileUtils.writeStringToFile(file, sb.toString(), "UTF-16LE", true);
				}catch(Exception ex){
					ex.printStackTrace();
				}
				
			}
		}
	}
}
