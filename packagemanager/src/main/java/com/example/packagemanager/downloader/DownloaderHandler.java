package com.example.packagemanager.downloader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.WebResourceResponse;

import com.example.packagemanager.PackageConfig;
import com.example.packagemanager.packageManager.PackageEntry;
import com.example.packagemanager.packageManager.PackageInfo;
import com.example.packagemanager.packageManager.PackageInstaller;
import com.example.packagemanager.packageManager.PackageStatus;
import com.example.packagemanager.resource.AssetResourceLoader;
import com.example.packagemanager.resource.ResourceInfo;
import com.example.packagemanager.resource.ResourceManager;
import com.example.packagemanager.util.FileUtils;
import com.example.packagemanager.util.GsonUtils;
import com.example.packagemanager.util.Logger;
import com.example.packagemanager.util.VersionUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.NonNull;

/**
 * Created by zhangrh on 2020/6/30
 */
public class DownloaderHandler extends Handler {
    private static  final int STATUS_PACKAGE_CAN_USE = 1;
    private Context context;
    private Lock resourceLock;
    private AssetResourceLoader assetResourceLoader;
    private PackageInstaller packageInstaller;
    private ResourceManager resourceManager;
    private PackageEntry localPackageEntry;
    // 下载的资源
    private List<PackageInfo> willDownloadPackageInfoList;
    // 需要访问的资源
    private List<PackageInfo> onlyUpdatePackageInfoList;
    private Map<String, Integer> packageStatusMap = new HashMap<>();
    private PackageConfig config = new PackageConfig();

    public DownloaderHandler(Context context, Looper looper) {
        super(looper);
        this.context = context;
        this.resourceLock = new ReentrantLock();
        this.resourceManager = new ResourceManager(context);
        this.packageInstaller = new PackageInstaller(context);
        this.assetResourceLoader = new AssetResourceLoader(context);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case DownloaderState.INIT_ASSETS:
                performLoadAssets();
                break;
            case DownloaderState.START_UPDATE:
                performUpdate((String) msg.obj);
                break;
            case DownloaderState.DOWNLOAD_SUCCESS:
                performDownloadSuccess((String) msg.obj);
                break;
            case DownloaderState.DOWNLOAD_FAILED:
                performDownloadFailed((String) msg.obj);
                break;
        }
        super.handleMessage(msg);
    }

    // 获取资源
    public WebResourceResponse getResource(String url) {
        synchronized (packageStatusMap) {
            String packageId = resourceManager.getPackageId(url);
            Integer status = packageStatusMap.get(packageId);
            if (status == null ) return null;
            if (status != STATUS_PACKAGE_CAN_USE) return null;
            WebResourceResponse response = null;
            if (!resourceLock.tryLock()) return null;
            response = resourceManager.getWebResponseResource(url);
            resourceLock.unlock();
            return response;
        }
    }
    // init的时候就可以加载一部分静态资源
    private void performLoadAssets() {
        if (assetResourceLoader == null) return;
        PackageInfo packageInfo = assetResourceLoader.load(config.getAssetPath());
        if (packageInfo == null) return;
        installPackage(packageInfo.getPackageId(), packageInfo, true);
    }
    // 开始更新
    private void performUpdate(String packageStr) {
        String packageIndexFileName = FileUtils.getPackageIndexFileName(context);
        File packageIndexFile = new File(packageIndexFileName);
        if (!packageIndexFile.exists()) initLocalEntry(packageIndexFile);

        PackageEntry netEntry = GsonUtils.jsonFromString(packageStr, PackageEntry.class);
        willDownloadPackageInfoList = new ArrayList<>();
        if (netEntry != null && netEntry.getItems() != null) {
            willDownloadPackageInfoList.addAll(netEntry.getItems());
        }

        List<PackageInfo> packageInfoList = new ArrayList<>(willDownloadPackageInfoList.size());
        for (PackageInfo packageInfo : willDownloadPackageInfoList) {
            if (packageInfo.getStatus() == PackageStatus.offline) {
                continue;
            }
            packageInfoList.add(packageInfo);
        }

        willDownloadPackageInfoList.clear();
        willDownloadPackageInfoList.addAll(packageInfoList);

        for (PackageInfo packageInfo : willDownloadPackageInfoList) {
            Downloader downloader = new Downloader(context);
            downloader.download(packageInfo, new DownloadCallback() {
                @Override
                public void onSuccess(String packageId) {
                    performDownloadSuccess(packageId);
                }

                @Override
                public void onFailure(String packageId) {
                    performDownloadFailed(packageId);
                }
            });
        }

        if (onlyUpdatePackageInfoList != null && onlyUpdatePackageInfoList.size() > 0) {
            for (PackageInfo packageInfo : onlyUpdatePackageInfoList) {
                resourceManager.updateResource(packageInfo.getPackageId(), packageInfo.getVersion());
                updateIndexFile(packageInfo.getPackageId(), packageInfo.getVersion());
                synchronized (packageStatusMap) {
                    packageStatusMap.put(packageInfo.getPackageId(), STATUS_PACKAGE_CAN_USE);
                }
            }
        }

    }
    private void performDownloadSuccess(String packageId) {
        if (willDownloadPackageInfoList == null)return;
        PackageInfo packageInfo = null;
        PackageInfo temp = new PackageInfo();
        temp.setPackageId(packageId);
        int pos = willDownloadPackageInfoList.indexOf(temp);
        if (pos >= 0) {
            packageInfo = willDownloadPackageInfoList.remove(pos);
        }
        allResourceFinished();
        installPackage(packageId, packageInfo, false);
    }
    private void performDownloadFailed(String packageId) {
        if (willDownloadPackageInfoList == null) return;
        int pos = willDownloadPackageInfoList.indexOf(packageId);
        if (pos >= 0) willDownloadPackageInfoList.remove(pos);
        allResourceFinished();
    }
    // 全部资源加载完成
    private void allResourceFinished() {
        if (willDownloadPackageInfoList.size() == 0) {
            // 全部加载完成
        }
    }
    // 安装包
    private void installPackage(String packageId, PackageInfo packageInfo, boolean isAssets) {
        if (packageInfo == null) return;
        // TODO: 验证资源
        resourceLock.lock();
        boolean isInstallSuccess = packageInstaller.install(packageInfo, isAssets);
        resourceLock.unlock();
        if (!isInstallSuccess) return; // 安装失败不做处理
        resourceManager.updateResource(packageInfo.getPackageId(), packageInfo.getVersion());
        updateIndexFile(packageInfo.getPackageId(), packageInfo.getVersion());
        synchronized (packageStatusMap) {
            packageStatusMap.put(packageId, STATUS_PACKAGE_CAN_USE);
        }

    }
    // 更新索引文件 TODO: 讲真没看懂这一段是在干什么
    private void updateIndexFile(String packageId, String version) {
        String packageIndexFileName = FileUtils.getPackageIndexFileName(context);
        File packageIndexFile = new File(packageIndexFileName);
        if (!packageIndexFile.exists()) {
            try {
                if (!packageIndexFile.createNewFile()) return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        if (localPackageEntry == null) {
            FileInputStream indexFis = null;
            try {
                indexFis = new FileInputStream(packageIndexFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
            if (indexFis == null) return;
            localPackageEntry = GsonUtils.jsonFromFileStream(indexFis, PackageEntry.class);
        }
        if (localPackageEntry == null) {
            localPackageEntry = new PackageEntry();
        }

        List<PackageInfo> packageInfoList = new ArrayList<>();
        if (localPackageEntry.getItems() != null) {
            packageInfoList.addAll(localPackageEntry.getItems());
        }
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.setPackageId(packageId);

        int index = packageInfoList.indexOf(packageInfo);
        if (index >= 0) {
            packageInfoList.get(index).setVersion(version);
        } else {
            packageInfo.setStatus(PackageStatus.online);
            packageInfo.setVersion(version);
            packageInfoList.add(packageInfo);
        }
        localPackageEntry.setItems(packageInfoList);
        if (
                localPackageEntry == null
                ||
                localPackageEntry.getItems() == null
                ||
                localPackageEntry.getItems().size() == 0
        ) {
            return;
        }

        String updateStr = new Gson().toJson(localPackageEntry);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(packageIndexFile);
            outputStream.write(updateStr.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e("write packageIndex file error");
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // 初始化本地引导文件
    private void initLocalEntry(File packageIndexFile) {
        FileInputStream indexFis = null;
        try {
            indexFis = new FileInputStream(packageIndexFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (indexFis == null) return;

        localPackageEntry = GsonUtils.jsonFromFileStream(indexFis, PackageEntry.class);
        if (localPackageEntry == null || localPackageEntry.getItems() == null) {
            return;
        }

        int index = 0;
        for (PackageInfo localInfo: localPackageEntry.getItems()) {
            if ((index = willDownloadPackageInfoList.indexOf(localInfo)) < 0) {
                continue;
            }
            PackageInfo info = willDownloadPackageInfoList.get(index);
            if (VersionUtils.compareVersion(info.getVersion(), localInfo.getVersion()) <= 0) {
                if (!checkResourceFileValid(info.getPackageId(), info.getVersion())) return;
                willDownloadPackageInfoList.remove(index);
                if (onlyUpdatePackageInfoList == null) {
                    onlyUpdatePackageInfoList = new ArrayList<>();
                }
                if (info.getStatus() == PackageStatus.online) {
                    onlyUpdatePackageInfoList.add(localInfo);
                }
                localInfo.setStatus(info.getStatus());
            } else {
                localInfo.setStatus(info.getStatus());
                localInfo.setVersion(info.getVersion());
            }
        }
    }
    //
    private boolean checkResourceFileValid(String packageId, String version) {
        File indexFile = FileUtils.getResourceIndexFile(context, packageId, version);
        return indexFile.exists() && indexFile.isFile();
    }
}
