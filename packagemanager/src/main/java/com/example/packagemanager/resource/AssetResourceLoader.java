package com.example.packagemanager.resource;

import android.content.Context;
import android.text.TextUtils;

import com.example.packagemanager.packageManager.PackageInfo;
import com.example.packagemanager.packageManager.PackageStatus;
import com.example.packagemanager.util.FileUtils;
import com.example.packagemanager.util.GsonUtils;
import com.example.packagemanager.util.VersionUtils;
import com.example.packagemanager.util.ZipUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 本地资源加载类
 */
public class AssetResourceLoader {
    private Context context;

    public AssetResourceLoader(Context context) {
        this.context = context;
    }

    // 静态资源加载器, 只加载预安装的离线包
    public PackageInfo load(String path) {
        // TODO: 原始逻辑: 本地包只有一个; 而实际情况: 本地包可能有很多个..
        InputStream inputStream = openAssetInputStream(path);
        if (inputStream == null) return null;

        // 获取zip中的index.json
        String indexInfo = ZipUtils.getIndexJsonStringFromZip(inputStream);
        if (TextUtils.isEmpty(indexInfo)) return null;

        ResourceInfoEntry assetResourceEntry = GsonUtils.jsonFromString(indexInfo, ResourceInfoEntry.class);
        if (assetResourceEntry == null) return null;

//        File file = new File(FileUtils.getPackageUpdateName(context, assetEntry.getPackageId(), assetEntry.getVersion()));
//        ResourceInfoEntry localEntry = null;
//        FileInputStream fileInputStream = null;
//        if (file.exists()) {
//            try {
//                fileInputStream = new FileInputStream(file);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }

//        String lo = null;
//        if (fileInputStream != null) {
//            lo = ZipUtils.getStringFromZip(fileInputStream);
//        }
//        if (!TextUtils.isEmpty(lo)) {
//            localEntry = GsonUtils.jsonFromString(lo, ResourceInfoEntry.class);
//        }
//        if (
//                localEntry != null
//                &&
//                VersionUtils.compareVersion(assetEntry.getVersion(), localEntry.getVersion()) <= 0
//        ) {
//            return null;
//        }

        // 生成本地静态资源路径
        String assetResourcePath = FileUtils.getPackageAssetsName(context, assetResourceEntry.getPackageId(), assetResourceEntry.getVersion());
        // 将预安装的离线zip包放到本地静态资源应该在的地方
        inputStream = openAssetInputStream(path);
        if (!FileUtils.copyFile(inputStream, assetResourcePath)) return null;
        FileUtils.safeCloseFile(inputStream);
        
        // 本地加载的包, 需要进行设置
        PackageInfo info = new PackageInfo();
        info.setPackageId(assetResourceEntry.getPackageId());
        info.setStatus(PackageStatus.online);
        info.setVersion(assetResourceEntry.getVersion());
//        info.setMd5("");
        return info;
    }

    private InputStream openAssetInputStream(String path) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }
}
