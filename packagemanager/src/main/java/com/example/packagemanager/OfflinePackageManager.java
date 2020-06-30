package com.example.packagemanager;

import android.content.Context;

import com.example.packagemanager.packageManager.PackageInstaller;
import com.example.packagemanager.resource.ResourceManager;
import com.liulishuo.filedownloader.FileDownloader;

/**
 * 离线包管理器
 */
public class OfflinePackageManager {
    private Context context;
    private ResourceManager resourceManager;
    private PackageInstaller packageInstaller;

    public  void init(Context context) {
        this.context = context;
        this.resourceManager = new ResourceManager(context);
        this.packageInstaller = new PackageInstaller(context);
        FileDownloader.init(context);
    }
}
