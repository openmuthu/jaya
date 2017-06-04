package org.jaya.android;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import junit.framework.AssertionFailedError;

import org.jaya.indexsync.IndexCatalogue;
import org.jaya.search.LuceneUnicodeSearcher;
import org.jaya.util.Constatants;
import org.jaya.util.Utils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by murthy on 12/03/17.
 */

public class JayaApp extends Application{
    public static final String APP_NAME = "Jaya";
    public static final String INTENT_OPEN_DOCUMENT_ID = "org.jaya.INTENT_OPEN_DOCUMENT_ID";

    public enum Version{
        JAYA_1_0
    }

    public static final String gVersionStr = "1.0.0.0";
    public static Version VERSION = Version.JAYA_1_0;
    private static Context mContext;
    private static Handler mJayaAppHandler = null;

    private static LuceneUnicodeSearcher mSearcher = null;
    private static IndexCatalogue mIndexCatalog = null;

    public void onCreate(){
        super.onCreate();
        JayaApp.mContext = getApplicationContext();
        mJayaAppHandler = new Handler();
    }

    public static Context getAppContext() {
        return JayaApp.mContext;
    }

    public static LuceneUnicodeSearcher getSearcher(){
        if (mSearcher == null) {
            mSearcher = new LuceneUnicodeSearcher(JayaApp.getSearchIndexFolder());
        }
        return mSearcher;
    }

    public static String getAppExtStorageFolder(){
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_NAME;
    }

    public static String getToBeIndexedFolder(){
        return getAppExtStorageFolder() + "/to_be_indexed/";
    }

    public static String getDocumentsFolder(){
        //return "/sdcard/"+APP_NAME+"/Documents/";
        return getAppExtStorageFolder() + "/Documents/";
    }

    public static String getSearchIndexFolder(){
        //return "/sdcard/"+APP_NAME+"/Index/";
        return getAppExtStorageFolder() + "/Index/";
    }

    public static String getIndexMetadataFolder(){
        return getAppExtStorageFolder() + "/Index_md/";
    }

    public static IndexCatalogue getIndexCatalog(){
        if( mIndexCatalog == null ) {
            mIndexCatalog = IndexCatalogue.getInstance();
            mIndexCatalog.initialize(JayaApp.getIndexMetadataFolder(), JayaApp.getIndexCatalogueBaseUrl(), JayaApp.getSearchIndexFolder());
        }
        return mIndexCatalog;
    }

    public static boolean isIndexPresent(){
        return isIndexingRequired();
    }

    public static String getIndexCatalogueBaseUrl(){
        //return "http://10.193.123.248:8080";
        //return "https://raw.githubusercontent.com/openmuthu/jaya/indexing-revamp/UnicodeSearch/unicodesearch/index-zip-output";
        File iniFile = new File(getDocumentsFolder()+".jaya.ini");
        if( iniFile.exists() ){
            try{
                JSONParser parser = new JSONParser();
                JSONObject iniObj = (JSONObject) parser.parse(new FileReader(iniFile));
                if( iniObj.containsKey("indexCatalogueBaseUrl") ){
                    return (String)iniObj.get("indexCatalogueBaseUrl");
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        else{
            JSONObject iniObj = new JSONObject();
            iniObj.put("indexCatalogueBaseUrl", Constatants.getIndexCatalogBaseUrl());
            FileWriter fw = null;
            try{
                fw = new FileWriter(iniFile);
                fw.write(iniObj.toJSONString());
            }catch (Exception ex){
                ex.printStackTrace();
            }
            finally {
                Utils.closeSilently(fw);
            }
        }
        return Constatants.getIndexCatalogBaseUrl();
    }

    public static boolean isIndexingRequired(){
        String sigFilePath = getSearchIndexFolder() + "/segments.gen";
        File sigFile = new File(sigFilePath);

        return !sigFile.exists();
    }

    public static void runOnUiThread(Runnable r) {
        runOnUiThreadAfterDelay(r, 0);
    }

    public static void runOnUiThreadAfterDelay(Runnable r, long delayMillis) {
        if (mJayaAppHandler == null) {
            throw new AssertionFailedError("ReaderApplication is not yet initilized");
        }

        if(delayMillis == 0 && mJayaAppHandler.getLooper() == Looper.myLooper()){
            // If we are already on the main UI thread, just run the runnable.
            r.run();
        }
        else {
            if(delayMillis == 0)
                mJayaAppHandler.post(r);
            else
                mJayaAppHandler.postDelayed(r, delayMillis);
        }
    }
}
