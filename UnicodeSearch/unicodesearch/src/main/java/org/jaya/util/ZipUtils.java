package org.jaya.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
	
	public static void unzipFile(String zipFilePath, String destDirPath) throws IOException {
		
		File outDir = new File(PathUtils.get(destDirPath));
		Utils.deleteDir(outDir);
		outDir.mkdirs();
		
		
		byte[] buffer = new byte[1024*64];
		
		ZipInputStream zis = null;
		ZipEntry ze = null;
		FileOutputStream fos = null;

	     try{
	    	//get the zip file content
	    	zis =
	    		new ZipInputStream(new FileInputStream(zipFilePath));
	    	//get the zipped file list entry
	    	ze = zis.getNextEntry();

	    	while(ze!=null){

	    	   String fileName = ze.getName();
	           File newFile = new File(PathUtils.get(destDirPath, fileName));

	           //System.out.println("file unzip : "+ newFile.getAbsoluteFile());

	            //create all non exists folders
	            //else you will hit FileNotFoundException for compressed folder
	            new File(newFile.getParent()).mkdirs();

	            fos = new FileOutputStream(newFile);

	            int len;
	            while ((len = zis.read(buffer)) > 0) {
	            	fos.write(buffer, 0, len);
	            }

	            fos.close();
	            ze = zis.getNextEntry();
	    	}

	    	//System.out.println("Done");

	    }catch(IOException ex){
	       ex.printStackTrace();
	    }
	    finally{
	    	if( zis != null )
	    		zis.closeEntry();	    	
	    	Utils.closeSilently(zis);
	    	Utils.closeSilently(fos);
	    }
	}
}
