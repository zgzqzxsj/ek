package com.zm.ek;

/**
 * Created by Administrator on 2019/6/22 0022.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.zm.ek.getVersion.getURL;

public class UpdateActivity extends Activity {

    private int progress;
    private ProgressBar updateProgress;
    private String downLoadPath = "";
    private String versionPath = "";
    private int preVersion = 100;
    private int newVersion = 0;
    private String saveFileDir = "";
    private String apkName = "";
    private Dialog noticeDialog;

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        versionPath = getIntent().getStringExtra("url");
        checkUpdate();
    }

    //获取当前版本号
    private int getPreVersionCode(Context context) {
        try {
            if (preVersion != 100) {
                return preVersion;
            }
            preVersion = context.getPackageManager().getPackageInfo(UpdateActivity.this.getPackageName(), 0).versionCode;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
            finish();
        }
        return preVersion;
    }

    //检查更新
    public void checkUpdate() {
        new Thread(){
            @Override
            public void run(){
                try {
                    String result = "";
                    // 让读写操作和网络操作从主线程中脱离
//                    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
                    System.out.println(versionPath);
                    result = getURL(versionPath,"UTF-8", 5*1000, 1);
                    JSONObject jo = new JSONObject(result);

                    newVersion = Integer.parseInt(jo.getString("version"));
                    downLoadPath = jo.getString("url");

                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("newVersion",newVersion+"");
                    data.putString("downLoadPath",downLoadPath);
                    msg.setData(data);
                    handler.sendMessage(msg);
//                    InputStream inStream = conn.getInputStream();
                    //解析XML文件
//                    parseXml(inStream);

                } catch (Exception e) {
                    Toast.makeText(UpdateActivity.this, "更新出错,无法获知新版本！", Toast.LENGTH_SHORT).show();

                }
            }
        }.start();
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Bundle data = msg.getData();
            //从data中拿出存的数据
//            String val = data.getString("value");
            //将数据进行显示到界面等操作

            if (newVersion > getPreVersionCode(UpdateActivity.this)) {
                //询问是否更新

                showNoticeDialog();
            } else {
                //不更新，登录
                UpdateActivity.this.finish();
//                        showPre();
            }
        }
    };
    //询问是否更新窗口
    private void showNoticeDialog() {
        Builder builder = new Builder(UpdateActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle("软件更新");
        builder.setMessage("检测到新版本，是否更新？");
        builder.setPositiveButton("更新", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showUpdateProgress();
            }
        });
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //此处选择取消，直接退出App，可根据流程做修改
//                ExitApplication.getInstance().exit(UpdateActivity.this);
                UpdateActivity.this.finish();
            }
        });
        noticeDialog = builder.create();
        noticeDialog.setCancelable(false);
        noticeDialog.show();
    }

    OnKeyListener onKey = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // TODO Auto-generated method stub
            if (keyCode == KeyEvent.KEYCODE_ENTER && noticeDialog.isShowing()) {
                Button PositiveButton = ((AlertDialog) noticeDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                PositiveButton.callOnClick();
            }
            return true;
        }
    };

    //显示进度条
    private void showUpdateProgress() {
        updateProgress = (ProgressBar) findViewById(R.id.update_progress);
        updateProgress.setVisibility(View.VISIBLE);
        downloadApk();
    }

    //新建一个进程，下载Apk
    private void downloadApk() {
        new downloadApkThread().start();
    }

    //下载Apk文件
    private class downloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    System.out.println("App的下载路径为：" + downLoadPath);

                    URL url = new URL(downLoadPath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(8 * 1000);
                    conn.setReadTimeout(8 * 1000);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    int length = conn.getContentLength();
                    //获取App文件名
                    apkName = downLoadPath.substring(downLoadPath.lastIndexOf("=") + 1, downLoadPath.length());
                    //下载后保存的路径
                    saveFileDir = Environment.getExternalStorageDirectory() + "/APP";
                    File apkFileDir = new File(saveFileDir);
                    if (!apkFileDir.exists()) {
                        //如果/App文件夹不存在就创建一个新的App子文件夹
                        apkFileDir.mkdir();
                    }
                    File apkFile = new File(apkFileDir, apkName);
                    FileOutputStream fos = new FileOutputStream(apkFile);

                    int count = 0;
                    byte buf[] = new byte[10 * 1024];
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        progress = (int) (((float) count / length) * 100);
                        updateProgress.setProgress(progress);
                        if (numread <= 0) {

                            installAPk(apkFile);
                            break;
                        }
                        fos.write(buf, 0, numread);
                    } while (true);
                    fos.close();
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ;

    //安装Apk文件
    private void installAPk(File apkFile) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //安装路径
//            Uri downloadUriUri = Uri.parse("file://" + saveFileDir + "/" + apkName);
            Uri downloadUriUri = Uri.fromFile(apkFile);
            intent.setDataAndType(downloadUriUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            UpdateActivity.this.startActivity(intent);

            UpdateActivity.this.finish();
        } catch (Exception e) {

            Toast.makeText(UpdateActivity.this, "安装失败，请手动安装或重新更新！", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


}

