package org.jaya.android;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by murthy on 12/03/17.
 */

public class JayaApp extends Application{
    public static final String APP_NAME = "Jaya";

    public enum Version{
        JAYA_1_0
    }

    public static Version VERSION = Version.JAYA_1_0;
    private static Context mContext;

    public void onCreate(){
        super.onCreate();
        JayaApp.mContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return JayaApp.mContext;
    }

    public static String getDocumentsFolder(){
        //return "/sdcard/"+APP_NAME+"/Documents/";
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_NAME + "/Documents/";
    }

    public static String getSearchIndexFolder(){
        //return "/sdcard/"+APP_NAME+"/Index/";
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_NAME + "/Index/";
    }

    public static boolean isIndexingRequired(){
        String sigFilePath = getSearchIndexFolder() + "/segments.gen";
        File sigFile = new File(sigFilePath);

        return !sigFile.exists();
    }
}
