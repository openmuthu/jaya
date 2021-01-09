package org.jaya.android;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.jaya.annotation.Annotation;
import org.jaya.search.ResultDocument;
import org.jaya.search.index.LuceneUnicodeFileIndexer;

import java.io.IOException;

/**
 * Created by murthy on 22/04/17.
 */

public class JayaAppUtils {

    private static int[] sListItemBackgroundColors = new int[]{
            //http://www.quackit.com/css/css_color_codes.cfm
            0xFFFFFAFA,
            0xFFF0FFF0,
            0xFFF5FFFA,
            0xFFF0FFFF,
            0xFFF0F8FF,
            0xFFF8F8FF,
            0xFFF5F5F5,
            0xFFFFF5EE,
            0xFFF5F5DC,
            0xFFFDF5E6,
            0xFFFFFAF0,
            0xFFFFFFF0,
            0xFFFAEBD7,
            0xFFFAF0E6,
            0xFFFFF0F5,
            0xFFFFE4E1
    };

    public static int getColorForDoc(int docId){
        int iBgColorIndex = docId % sListItemBackgroundColors.length;
        return sListItemBackgroundColors[iBgColorIndex];
    }

    public static int getRandomColor(){
        int iBgColorIndex = ((int)(Math.random()*100)) % sListItemBackgroundColors.length;
        return sListItemBackgroundColors[iBgColorIndex];
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) JayaApp.getAppContext().getSystemService(JayaApp.getAppContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static ResultDocument getDoc(Annotation item){
        ResultDocument doc = null;
        try {
            doc = JayaApp.getSearcher().getDoc(item.getDocPath(), item.getDocLocalId());
        }catch (IOException ex){
            ex.printStackTrace();
        }
        return doc;
    }

    public static void indexFiles(boolean bForceReIndexing)
    {
        LuceneUnicodeFileIndexer indexer = null;
        try {
            indexer = new LuceneUnicodeFileIndexer(JayaApp.getSearchIndexFolder());
            indexer.addADummyDocumentIfIndexIsEmpty();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            if( indexer != null )
                indexer.close();
        }
        return;
//        if( !JayaApp.isIndexingRequired() && !bForceReIndexing)
//            return;
//        if( mProgress != null )
//        {
//            // Indexing is already in progress.. just return
//            return;
//        }
//        mProgress = new ProgressDialog(this);
//        mProgress.setIndeterminate(true);
//        mProgress.setMessage("Please wait while indexing is in progress... This may take a few minutes.");
//        mProgress.show();
//        Thread t =  new Thread(new Runnable() {
//            public void run() {
//                try {
//                    LuceneUnicodeFileIndexer indexer = new LuceneUnicodeFileIndexer();
//                    indexer.createIndex(JayaApp.getSearchIndexFolder(), JayaApp.getToBeIndexedFolder());
//                }
//                catch(IOException ex){
//                    Log.d("Indexer", "IOException in indexFiles");
//                }
//                finally {
//                    // Update the progress bar
//                    mHandler.post(new Runnable() {
//                        public void run() {
//                            mProgress.dismiss();
//                            mProgress = null;
//                            showDocumentId(JayaApp.getSearcher().getRandomDoc());
//                        }
//                    });
//                }
//            }
//        });
//        t.start();
    }

}
