package org.jaya.android;

import android.app.Activity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class AssetsManager {

    private Context context;
    private String resFolderPath;
    private final ArrayList<Pair<String,String>> mFilesToCopy = new ArrayList<Pair<String,String>>();

    public AssetsManager(Context c){
        context = c;
        //resFolderPath = context.getFilesDir() + "/" + JayaApp.VERSION + "/";
        resFolderPath = JayaApp.getDocumentsFolder();

        mFilesToCopy.add(new Pair("sarvamoola/aitareya.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/anuvyakhyana.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/atharvana-up.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/bhagavata-t-n.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/bhagavata.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/brh-up.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/chand-up.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/gita.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/gitabhashya.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/gitatatparya.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/isha-up.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/katha-up.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/mandukya-up.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/mbtn.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/nyayavivarana.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/rigbhashya.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/sankirna.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/shatprashna-up.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/sutra.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/sutrabhashya.docx", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/sutrabhashya.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/taittiriya.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/talavakara-up.txt", "sarvamoola"));
        mFilesToCopy.add(new Pair("sarvamoola/yamaka.txt", "sarvamoola"));

    }

    public String getResFolderPath(){
        return resFolderPath;
    }

    public void copyResourcesToCacheIfRequired(Activity activity){
        if( ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED )
            return;
        String sigFilePath = resFolderPath + "/sarvamoola/aitareya.txt";
        File sigFile = new File(sigFilePath);

        if(!sigFile.exists())
        {

            for (Pair<String, String> item : mFilesToCopy) {
                String fileName ;

                if(item.first.indexOf('/')!= -1)
                    fileName = item.first.substring(item.first.lastIndexOf("/")+1);
                else
                    fileName = item.first;

                String destUrl = resFolderPath;
                if(!item.second.isEmpty()) {
//                  if(new File(item.second).isAbsolute()){
                    if(File.separator.equals(item.second.substring(0,1))){ // Shortcut to check isAbsolute()
                        // Its an absolute url. set the destUrl to empty so that item.second is taken as is
                        destUrl = "";
                    }
                    destUrl = destUrl + item.second + '/';
                }
                destUrl = destUrl + fileName;

                copyFile(item.first, destUrl);
            }

        }
    }

    private void copyFile(String srcPath, String dstPath){
        InputStream srcStream = null;
        FileOutputStream dstStream = null;
        try {
            try {
                srcStream = context.getAssets().open(srcPath);
                File dstFile = new File(dstPath);
                boolean bDirsCreated =  dstFile.getParentFile().mkdirs();
                dstStream = new FileOutputStream(dstPath, false);
                if (srcStream != null && dstStream != null) {
                    byte[] buffer = new byte[1024 * 64]; // Adjust if you want
                    int bytesRead;
                    while ((bytesRead = srcStream.read(buffer)) != -1) {
                        dstStream.write(buffer, 0, bytesRead);
                    }
                } else {
                    Log.d(JayaApp.APP_NAME, "JayaApp.copyFile(): Error reading one of the files. src:"
                            + srcPath + ", dst:" + dstPath);
                }
            } catch (IOException ex) {
                Log.e(JayaApp.APP_NAME, "JayaApp.copyFile() exception : " + ex.toString());
            } finally {
                if (srcStream != null)
                    srcStream.close();
                if (dstStream != null)
                    dstStream.close();
            }
        }catch (IOException ex){
            Log.e(JayaApp.APP_NAME, "JayaApp.copyFile() Stream close failed : " + ex.toString());
        }
    }


}
