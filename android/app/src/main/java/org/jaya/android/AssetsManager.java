package org.jaya.android;

import android.app.Activity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AssetsManager {

    private Context context;
    private String resFolderPath;
    private final ArrayList<Pair<String,String>> mFilesToCopy = new ArrayList<Pair<String,String>>();

    public AssetsManager(Context c){
        context = c;
        //resFolderPath = context.getFilesDir() + "/" + JayaApp.VERSION + "/";
        resFolderPath = JayaApp.getDocumentsFolder();

    }

    public String getResFolderPath(){
        return resFolderPath;
    }

    private boolean listAssetFiles(AssetManager assets, String path, List<String> recursiveFilePaths) {

        String [] list;
        try {
            list = assets.list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (!listAssetFiles(assets, path + "/" + file, recursiveFilePaths))
                        return false;
                }
            } else {
                recursiveFilePaths.add(path);
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public void copyResourcesToCacheIfRequired(Activity activity){
        if( ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED )
            return;
        String sigFilePath = JayaApp.getSearchIndexFolder() + "/segments.gen";
        File sigFile = new File(sigFilePath);

        if(!sigFile.exists())
        {
            InputStream stream = null;
            OutputStream output = null;

            try {
                ArrayList<String> assetFiles = new ArrayList<>();
                listAssetFiles(JayaApp.getAppContext().getAssets(), "to_be_indexed", assetFiles);
                //listAssetFiles(JayaApp.getAppContext().getAssets(), "index", assetFiles);
                for (String fileName : assetFiles) {
                    String dstPath = JayaApp.getAppExtStorageFolder() + "/" + fileName;
                    File dstFile = new File(dstPath);
                    boolean bDirsCreated =  dstFile.getParentFile().mkdirs();
                    stream = JayaApp.getAppContext().getAssets().open(fileName);
                    output = new BufferedOutputStream(new FileOutputStream(dstPath));

                    byte data[] = new byte[16*1024];
                    int count;

                    while ((count = stream.read(data)) != -1) {
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    stream.close();

                    stream = null;
                    output = null;
                }
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    public void copyResourcesToCacheIfRequired1(Activity activity){
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
