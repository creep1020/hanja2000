package kr.hanja.study;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import android.content.*;
import android.net.Uri;
import android.print.PrintManager;
import java.io.*;
public class MainActivity extends Activity {
 WebView web; ValueCallback<Uri[]> chooser; String pending;
 @Override public void onCreate(Bundle b){super.onCreate(b);web=new WebView(this);setContentView(web);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);web.getSettings().setAllowFileAccess(false);web.getSettings().setAllowContentAccess(true);web.addJavascriptInterface(new Bridge(),"NativeHanja");web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return true;} @Override public void onPageFinished(WebView v,String u){v.evaluateJavascript("window.print=()=>NativeHanja.print();document.getElementById('backup').onclick=()=>NativeHanja.backup(JSON.stringify(state));",null);}});web.setWebChromeClient(new WebChromeClient(){@Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> c,FileChooserParams p){if(chooser!=null)chooser.onReceiveValue(null);chooser=c;try{startActivityForResult(p.createIntent(),1);}catch(Exception e){chooser=null;c.onReceiveValue(null);}return true;}});web.loadUrl("file:///android_asset/index.html");}
 class Bridge { @JavascriptInterface public void print(){runOnUiThread(()->((PrintManager)getSystemService(PRINT_SERVICE)).print("한자 학습지",web.createPrintDocumentAdapter("한자 학습지"),null));} @JavascriptInterface public void backup(String s){runOnUiThread(()->{pending=s;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"hanja-progress.json");startActivityForResult(i,2);});}}
 @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==1&&chooser!=null){chooser.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(c,d));chooser=null;}if(r==2){if(c==RESULT_OK&&d!=null&&pending!=null){try(OutputStream o=getContentResolver().openOutputStream(d.getData())){o.write(pending.getBytes("UTF-8"));android.widget.Toast.makeText(this,"백업 저장 완료",0).show();}catch(Exception e){android.widget.Toast.makeText(this,"백업 저장 실패",1).show();}}pending=null;}}
}
