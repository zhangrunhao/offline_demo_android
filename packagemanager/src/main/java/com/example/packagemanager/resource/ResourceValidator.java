package com.example.packagemanager.resource;

import android.text.TextUtils;

import com.example.packagemanager.util.FileUtils;
import com.example.packagemanager.util.Logger;

import java.io.IOException;
import java.io.InputStream;


public class ResourceValidator {
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