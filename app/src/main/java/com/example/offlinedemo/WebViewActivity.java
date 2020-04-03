package com.example.offlinedemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import wendu.dsbridge.DWebView;

public class WebViewActivity extends AppCompatActivity {
    private DWebView dwebView;

    @Override
    public  void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        dwebView = findViewById(R.id.dwebview);
        dwebView.addJavascriptObject(new JsApi(), null);
        dwebView.loadUrl("http://10.2.153.102:6622/offline_demo_fe/index.html");

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