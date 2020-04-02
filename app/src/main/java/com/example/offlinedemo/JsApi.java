package com.example.offlinedemo;

import android.webkit.JavascriptInterface;

import wendu.dsbridge.CompletionHandler;

public class JsApi{
    //同步API
    @JavascriptInterface
    public String testSyn(Object msg)  {
        System.out.println(msg);
        return msg + "［syn call］";
    }

    //异步API
    @JavascriptInterface
    public void testAsyn(Object msg, CompletionHandler<String> handler) {
        handler.complete(msg+" [ asyn call]");
    }
}
