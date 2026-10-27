package com.templeseven.game;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
public class MainActivity extends Activity {
 private WebView view;
 @Override public void onCreate(Bundle state) {
 super.onCreate(state); view=new WebView(this); view.setWebViewClient(new WebViewClient());
 view.getSettings().setJavaScriptEnabled(true); view.getSettings().setDomStorageEnabled(true);
 view.loadUrl("file:///android_asset/index.html"); setContentView(view);
 }
 @Override public void onBackPressed(){if(view.canGoBack())view.goBack();else super.onBackPressed();}
 @Override protected void onDestroy(){view.destroy();super.onDestroy();}
}