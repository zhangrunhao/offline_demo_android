package com.example.packagemanager;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;

import com.example.packagemanager.downloader.DownloadCallback;
import com.example.packagemanager.downloader.DownloaderHandler;
import com.example.packagemanager.downloader.DownloaderState;


/**
 * 离线包管理器
 */
public class OfflinePackageManager {
    private Context context;
    private static DownloaderHandler packageHandler;
    private static HandlerThread packageThread;
    private static OfflinePackageManager instance;

    public OfflinePackageManager() {
    }

    public static OfflinePackageManager getInstance() {
        if (instance == null) {
            instance = new OfflinePackageManager();
        }
        return instance;
    }

    public void init (Context context) {
        this.context = context;
        ensurePackageThread();
        packageHandler.sendEmptyMessage(DownloaderState.INIT_ASSETS);
    }

    // 更新离线包信息
    public void update(String packageJsonStr) {
        if (packageJsonStr == null) packageJsonStr = "";
        ensurePackageThread();
        Message message = Message.obtain();
        message.what = DownloaderState.START_UPDATE;
        message.obj = packageJsonStr;
        packageHandler.sendMessage(message);
    }

    // 获取网络响应资源资源
    public WebResourceResponse getWebResponseResource(String packageId, String path) {
        return this.packageHandler.getWebResourceResponseResource(packageId, path);
    }


    private void ensurePackageThread() {
        if (packageThread == null)  {
            packageThread = new HandlerThread("offline_package_thread");
            packageThread.start();
            packageHandler = new DownloaderHandler(context, packageThread.getLooper(), new DownloadCallback() {
                @Override
                public void onSuccess(String packageId) {
                    Message message = Message.obtain();
                    message.what = DownloaderState.DOWNLOAD_SUCCESS;
                    message.obj = packageId;
                    packageHandler.sendMessage(message);
                }

                @Override
                public void onFailure(String packageId) {
                    Message message = Message.obtain();
                    message.what = DownloaderState.DOWNLOAD_FAILED;
                    message.obj = packageId;
                    packageHandler.sendMessage(message);
                }
            });
        }
    }
}
