package com.example.packagemanager.downloader;

public interface DownloadCallback {
    void onSuccess(String packageId);
    void onFailure(String packageId);
}

