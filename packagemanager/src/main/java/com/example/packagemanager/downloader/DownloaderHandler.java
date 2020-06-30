package com.example.packagemanager.downloader;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

/**
 * Created by zhangrh on 2020/6/30
 */
public class DownloaderHandler extends Handler {
    public DownloaderHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case DownloaderState.DOWNLOAD_SUCCESS:
                performDownloadSuccess((String) msg.obj);
                break;
            case DownloaderState.DOWNLOAD_FAILED:
                performDownloadFailed((String) msg.obj);
                break;
            case DownloaderState.START_UPDATE:
                performUpdate((String) msg.obj);
                break;
            case DownloaderState.INIT_ASSETS:
                performLoadAssets((String) msg.obj);
                break;

        }
        super.handleMessage(msg);
    }

    private void performDownloadSuccess(String msg) {
    }
    private void performDownloadFailed(String msg) {
    }
    private void performUpdate(String msg) {
    }
    private void performLoadAssets(String msg) {
    }
}
