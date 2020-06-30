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

    public PackageInfo load(String path) {
        InputStream inputStream = openAssetInputStream(path);
        if (inputStream == null) return null;

        String indexInfo = ZipUtils.getStringFromZip(inputStream);
        if (TextUtils.isEmpty(indexInfo)) return null;

        ResourceInfoEntry assetEntry = GsonUtils.jsonFromString(indexInfo, ResourceInfoEntry.class);
        if (assetEntry == null) return null;

        File file = new File(FileUtils.getPackageUpdateName(context, assetEntry.getPackageId(), assetEntry.getVersion()));
        ResourceInfoEntry localEntry = null;
        FileInputStream fileInputStream = null;
        if (file.exists()) {
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        String lo = null;
        if (fileInputStream != null) {
            lo = ZipUtils.getStringFromZip(fileInputStream);
        }
        if (!TextUtils.isEmpty(lo)) {
            localEntry = GsonUtils.jsonFromString(lo, ResourceInfoEntry.class);
        }
        if (
                localEntry != null
                &&
                VersionUtils.compareVersion(assetEntry.getVersion(), localEntry.getVersion()) <= 0
        ) {
            return null;
        }

        String assetPath = FileUtils.getPackageAssetsName(context, assetEntry.getPackageId(), assetEntry.getVersion());
        if (!FileUtils.copyFile(inputStream, assetPath)) return null;
        FileUtils.safeCloseFile(inputStream);

        // TODO: 验证md5
        PackageInfo info = new PackageInfo();
        info.setPackageId(assetEntry.getPackageId());
        info.setStatus(PackageStatus.online);
        info.setVersion(assetEntry.getVersion());
        info.setMd5("");
        return null;
    }

    private InputStream openAssetInputStream(String path) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(path);
        } catch (IOException e) {
        }
        return inputStream;
    }
}
