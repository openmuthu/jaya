package org.jaya.android;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        getActionBar().setIcon(android.R.color.transparent);
        WebView wv;
        wv = (WebView) findViewById(R.id.help_webview);
        wv.loadUrl("file:///android_asset/help.html");
    }
}
