package org.jaya.android;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import org.jaya.annotation.Annotation;
import org.jaya.search.ResultDocument;
import org.jaya.util.TimestampUtils;

import java.lang.ref.WeakReference;
import java.util.Date;

class MainActivityActionBarCallBack implements ActionMode.Callback {

    private ResultDocument mResultDocument;

        public MainActivityActionBarCallBack(ResultDocument rd){
            mResultDocument = rd;
        }
  
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            try {
                ResultDocument doc = (ResultDocument) mode.getTag();
                switch (item.getItemId()) {
                    case R.id.action_bookmark:
                    {
                        String addBookmarkString = JayaApp.getAppContext().getResources().getString(R.string.action_bookmark_add);
                        String deleteBookmarkString = JayaApp.getAppContext().getResources().getString(R.string.action_bookmark_delete);
                        if (item.getTitle().equals(addBookmarkString)){
                            item.setTitle(deleteBookmarkString);
                            JayaApp.getAnnotationManager().addAnnotation(doc, TimestampUtils.nowAsString());
                        }
                        else{
                            item.setTitle(addBookmarkString);
                            JayaApp.getAnnotationManager().removeAnnotation(new Annotation(doc, TimestampUtils.nowAsString(), new Date()));
                        }
                    }
                            break;
                    case R.id.action_copy:
                    {
                        ClipboardManager clipboard = (ClipboardManager) JayaApp.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("", doc.getDocContentsForScriptType(PreferencesManager.getPreferredOutputScriptType()));
                        clipboard.setPrimaryClip(clip);
                    }
                        break;
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return false;
        }
  
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            mode.getMenuInflater().inflate(R.menu.main_activity_list_item_context_menu, menu);
            return true;
        }
  
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // TODO Auto-generated method stub
  
        }
  
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            String addBookmarkString = JayaApp.getAppContext().getResources().getString(R.string.action_bookmark_add);
            String deleteBookmarkString = JayaApp.getAppContext().getResources().getString(R.string.action_bookmark_delete);
            MenuItem bookmarkMenu = menu.findItem(R.id.action_bookmark);
            ResultDocument doc = mResultDocument;
            if( JayaApp.getAnnotationManager().annotationExists(doc) )
                bookmarkMenu.setTitle(deleteBookmarkString);
            else
                bookmarkMenu.setTitle(addBookmarkString);
            mode.setTitle("");
            return false;
        }
}