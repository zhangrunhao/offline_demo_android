package com.example.offlinedemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.io.FileInputStream;
import java.io.InputStream;

import wendu.dsbridge.DWebView;

public class WebViewActivity extends AppCompatActivity {
    private DWebView dwebView;

    @Override
    public  void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        dwebView = findViewById(R.id.dwebview);
        dwebView.addJavascriptObject(new JsApi(), null);
        dwebView.loadUrl("http://192.168.1.47:6622/offline_demo_fe/index.html");
        dwebView.setWebContentsDebuggingEnabled(true);
        // 拦截请求, 并更改css
        dwebView.setWebViewClient(new WebViewClient(){
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                WebResourceResponse response = null;
                if (request.getUrl().toString().equals("http://192.168.1.47:6622/offline_demo_fe/css/index.css")) {
                    try {
                        InputStream is = new FileInputStream("/data/user/0/com.example.offlinedemo/files/css/index.css");
                        response = new WebResourceResponse("text/css", "UTF-8", is);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return response;
            }
        });
        Button btn = (Button) findViewById(R.id.button3);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadUrl();
            }
        });
    }

    public void reloadUrl() {
        dwebView.reload();
    }
}