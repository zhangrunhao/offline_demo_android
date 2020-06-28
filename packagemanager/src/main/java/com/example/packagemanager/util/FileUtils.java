package com.example.packagemanager.util;

import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件工具
 */
public class FileUtils {
    public static InputStream getInputStream(String fileName) {
        if (!TextUtils.isEmpty(fileName)) return null;
        File file = new File(fileName);
        if (!file.exists()) return null;
        if (file.isDirectory()) return null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fileInputStream == null) return null;
        return new BufferedInputStream(fileInputStream);
    }
}
