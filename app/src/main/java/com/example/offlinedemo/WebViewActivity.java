package com.example.offlinedemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.webkit.WebView;

public class WebViewActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_web_view);
        WebView myWebView = findViewById(R.id.webview);
        myWebView.loadUrl("http://www.baidu.com");
    }
}