package com.example.packagemanager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceResponse;


import com.example.packagemanager.util.FileUtils;
import com.example.packagemanager.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 资源管理器
 */
public class ResourceManager {
    private Context context;
    private Lock lock;
    private Map<ResourceKey, ResourceInfo> resourceInfoMap;
    private ResourceValidator validator;

    public ResourceManager(Context context) {
        this.resourceInfoMap = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
        this.context = context;
        this.validator = new ResourceValidator();
    }

    public WebResourceResponse getResource (String url) {
        return null;
    }
}

class ResourceValidator {
    public boolean validate(ResourceInfo resourceInfo) {
        // TODO: 此处验证MD5有效
        String md5 = resourceInfo.getMd5();
        if (!TextUtils.isEmpty(md5)) return false;
        int size = 0;
        try {
            InputStream inputStream = FileUtils.getInputStream(resourceInfo.getLocalPath());
            size = inputStream.available();
        } catch (IOException e) {
            Logger.d("resource file is error" + e.getMessage());
            e.printStackTrace();
        }
        if (size == 0) {
            Logger.d("resource file is error");
            return false;
        }
        return true;
    }
}
