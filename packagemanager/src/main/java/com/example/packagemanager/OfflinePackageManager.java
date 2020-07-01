package com.example.packagemanager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;

import com.example.packagemanager.downloader.DownloaderHandler;
import com.example.packagemanager.downloader.DownloaderState;
import com.example.packagemanager.packageManager.PackageInstaller;
import com.example.packagemanager.resource.AssetResourceLoader;
import com.example.packagemanager.resource.ResourceManager;

/**
 * 离线包管理器
 */
public class OfflinePackageManager {
    private Context context;
    private PackageConfig config = new PackageConfig();
    private DownloaderHandler packageHandler;
    private HandlerThread packageThread;

    // 初始化离线包
    public  void init(Context context) {
        this.context = context;
        if (config.isEnableAssets() && !TextUtils.isEmpty(config.getAssetPath())) {
            ensurePackageeThread();
            packageHandler.sendEmptyMessage(DownloaderState.INIT_ASSETS);
        }
    }

    // 更新离线包信息
    public void update(String packageJsonStr) {
        if (packageJsonStr == null) packageJsonStr = "";
        ensurePackageeThread();
        Message message = Message.obtain();
        message.what = DownloaderState.START_UPDATE;
        message.obj = packageJsonStr;
        packageHandler.sendMessage(message);
    }

    // 获取资源
    public WebResourceResponse getResource(String url) {
        return this.packageHandler.getResource(url);
    }

    private void ensurePackageeThread() {
        if (packageThread == null)  {
            packageThread = new HandlerThread("offline_package_thread");
            packageThread.start();
            packageHandler = new DownloaderHandler(this.context, packageThread.getLooper());
        }
    }
}
