package com.zm.ek;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private WebView mBtnWv;
    private TextToSpeech textToSpeech; // TTS对象
    private MixSpeakUtil mixSpeakUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);



        mBtnWv = new WebView(this);
        mBtnWv.getSettings().setJavaScriptEnabled(true);
        mBtnWv.setWebChromeClient(new MyWebChromeClient());

        Window window = getWindow();
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏状态栏
        //定义全屏参数
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        mBtnWv.addJavascriptInterface(MainActivity.this,"android");

        mBtnWv.loadUrl("file:///android_asset/screen.html?deptid=001");
        setContentView(mBtnWv);
        initPermission();
        mixSpeakUtil=MixSpeakUtil.getInstance(this);

    }



    @JavascriptInterface
    public void call(final String text){
        mixSpeakUtil.speak(text);
    }
    @JavascriptInterface
    public void upd(final String url){
        mixSpeakUtil.onDestroy();
        Intent i = new Intent();
        i.setClass(MainActivity.this,UpdateActivity.class);
        i.putExtra("url",url);
        startActivity(i);
        MainActivity.this.finish();

    }
    class MyWebChromeClient extends WebChromeClient
    {
        @Override
        public void onProgressChanged(WebView view, int newProgress)
        {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title)
        {
            super.onReceivedTitle(view, title);
            setTitle(title);
        }
    }
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && mBtnWv.canGoBack())
        {
            mBtnWv.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //  下面是android 6.0以上的动态授权

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);

                // 进入到这里代表没有权限.

            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }
}
