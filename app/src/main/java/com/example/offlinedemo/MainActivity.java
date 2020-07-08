package com.example.offlinedemo;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.widget.EditText;

import com.example.packagemanager.Constants;
import com.example.packagemanager.OfflinePackageManager;
import com.example.packagemanager.util.Logger;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.expamle.myfirestapp.MESSAGE";
    private Object  that;
    private OfflinePackageManager packageManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = getApplicationContext();
        OfflinePackageManager.getInstance().init(context);
    }

    public void  sendMessage(View view) {
        // 首先要使用Context参数, 因为Activity类是Context类的子类.
        // Class参数是app组件, 也就是传入intent的地方, 换句话, 这是这个activity开始的地方
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        EditText editText = findViewById(R.id.editText);
        String message = editText.getText().toString();
        // intent把值添加进去
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void downloadZip(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    String url = Constants.BASE_URL + "/api/getPackageIndex?appName=sohu";
                    Request request = new Request.Builder().url(url).build();
                    try (Response response = client.newCall(request).execute()) {
                        String data =  response.body().string();
                        OfflinePackageManager.getInstance().update(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void openMyWebView(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivity(intent);
    }

    private void readFile() throws IOException {
        File f = new File("/data/user/0/com.example.offlinedemo/files/css/index.css");
        int length = (int) f.length();
        byte[] buff = new byte[length];
        FileInputStream fin = new FileInputStream(f);
        fin.read(buff);
        fin.close();
    }
}
