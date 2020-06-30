package com.example.packagemanager.downloader;

import android.content.Context;

import com.example.packagemanager.packageManager.PackageInfo;
import com.example.packagemanager.util.FileUtils;
import com.example.packagemanager.util.Logger;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

/**
 * 下载器实现类
 */
public class Downloader {
    private Context context;

    public Downloader(Context context) {
        this.context = context;
    }

    public void download(PackageInfo packageInfo, final DownloadCallback callback) {
        BaseDownloadTask downloadTask = FileDownloader.getImpl()
                .create(packageInfo.getDownloadUrl())
                .setTag(packageInfo.getPackageId())
                .setPath(FileUtils.getPackageDownloadName(context, packageInfo.getPackageId(), packageInfo.getVersion()))
                .setListener(new FileDownloadSampleListener() {
                    @Override
                    protected void completed(BaseDownloadTask task) {
                        super.completed(task);
                        if (callback != null && task.getStatus() == FileDownloadStatus.completed) {
                            callback.onSuccess((String) task.getTag());
                        } else if (callback != null) {
                            callback.onFailure((String) task.getTag());
                        }
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        super.error(task, e);
                        Logger.e("packageResource download error: " + e.getMessage());
                        if (callback != null) callback.onFailure((String) task.getTag());
                    }
                });
        downloadTask.start();
    }
}