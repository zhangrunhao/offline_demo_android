package com.example.packagemanager.downloader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.WebResourceResponse;

import com.example.packagemanager.Constants;
import com.example.packagemanager.packageManager.PackageEntry;
import com.example.packagemanager.packageManager.PackageInfo;
import com.example.packagemanager.packageManager.PackageInstaller;
import com.example.packagemanager.packageManager.PackageStatus;
import com.example.packagemanager.resource.AssetResourceLoader;
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
 * 下载/获取管理器
 */
public class DownloaderHandler extends Handler {
    private static  final int STATUS_PACKAGE_CAN_USE = 1;
    private Context context;
    private Lock resourceLock;
    private AssetResourceLoader assetResourceLoader;
    private PackageInstaller packageInstaller;
    private ResourceManager resourceManager;
    private PackageEntry localPackageEntry;
    private DownloadCallback callback;
    // 下载的资源
    private List<PackageInfo> willDownloadPackageInfoList;
    // 需要访问的资源
    private List<PackageInfo> onlyUpdatePackageInfoList;
    private Map<String, Integer> packageStatusMap = new HashMap<>();
//    private PackageConfig config = new PackageConfig();

    public DownloaderHandler(Context context, Looper looper, DownloadCallback callback) {
        super(looper);
        this.context = context;
        this.callback = callback;
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
    // init 加载本地静态资源
    private void performLoadAssets() {
        if (assetResourceLoader == null) return;
        // TODO: 本地应该支持多个包的加载
        // 加载本地离线包
        for (int i = 0; i < Constants.LOCAL_ASSET_LIST.length; i++) {
            PackageInfo packageInfo = assetResourceLoader.load(Constants.LOCAL_ASSET_LIST[i]);
            if (packageInfo == null) return;
            // 安装本地离线包
            installPackage(packageInfo.getPackageId(), packageInfo, true);
        }
    }

    // 开始更新
    private void performUpdate(String packageStr) {
        String packageIndexFileName = FileUtils.getPackageIndexFileName(context);
        File packageIndexFile = new File(packageIndexFileName);

        // 从服务器端获取所有需要加载的包, 并添加到加载列表
        PackageEntry nextEntry = GsonUtils.jsonFromString(packageStr, PackageEntry.class);
        willDownloadPackageInfoList = new ArrayList<>();
        if (nextEntry != null && nextEntry.getItems() != null) {
            willDownloadPackageInfoList.addAll(nextEntry.getItems());
        }

        // 非首次加载: 对比本地和线上离线包信息, 决定加载哪些
        if (packageIndexFile.exists()) initLocalEntry(packageIndexFile);

        List<PackageInfo> packageInfoList = new ArrayList<>(willDownloadPackageInfoList.size());
        // 去除status不为1的离线包, 也就是不需要更新的
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
            downloader.download(packageInfo, callback);
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
        // 下载成功后, 构建临时包信息, 并找到真正的离线包, 从下载列表中删除
        PackageInfo packageInfo = null;
        PackageInfo temp = new PackageInfo();
        temp.setPackageId(packageId);
        int pos = willDownloadPackageInfoList.indexOf(temp);
        if (pos >= 0) {
            packageInfo = willDownloadPackageInfoList.remove(pos);
        }
        allResourceFinished(); // 判断所有的资源是否下载完成
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
    // 安装离线包
    private void installPackage(String packageId, PackageInfo packageInfo, boolean isAssets) {
        if (packageInfo == null) return;
        // TODO: 验证资源
        resourceLock.lock();
        // 安装离线包
        boolean isInstallSuccess = packageInstaller.install(packageInfo, isAssets);
        resourceLock.unlock();
        if (!isInstallSuccess) return; // 安装失败不做处理


        resourceManager.updateResource(packageInfo.getPackageId(), packageInfo.getVersion());
        updateIndexFile(packageInfo.getPackageId(), packageInfo.getVersion());
        synchronized (packageStatusMap) {
            packageStatusMap.put(packageId, STATUS_PACKAGE_CAN_USE);
        }
    }
    // 更新本地索引文件
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

        // 获取本地所有离线包的信息, 找到当前的这个离线包是否存在, 存在就更新版本号, 不存在就加进去
        List<PackageInfo> packageInfoList = new ArrayList<>();
        if (localPackageEntry.getItems() != null) {
            packageInfoList.addAll(localPackageEntry.getItems());
        }
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.setPackageId(packageId);

        int index = packageInfoList.indexOf(packageInfo);
        if (index >= 0) {
            // TODO: 这里会不会把其他的信息干掉, 只保留一个版本号?
            packageInfoList.get(index).setVersion(version);
        } else {
            packageInfo.setStatus(PackageStatus.online);
            packageInfo.setVersion(version);
            packageInfoList.add(packageInfo);
        }
        localPackageEntry.setItems(packageInfoList);
        if (
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
    // 非首次下载, 对比本地离线包信息, 和线上离线包信息, 决定是否下载
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
            if ((index = willDownloadPackageInfoList.indexOf(localInfo)) < 0) { // 线上信息没有本地包
                continue;
            }
            PackageInfo onlineInfo = willDownloadPackageInfoList.get(index); // 在将要下载的离线包信息列表中, 找到本地包信息
            if (VersionUtils.compareVersion(onlineInfo.getVersion(), localInfo.getVersion()) <= 0) {
                // 本地版本号, 大于线上版本号: 从下载列表删除/并加入只更新列表
                if (!checkResourceFileValid(onlineInfo.getPackageId(), onlineInfo.getVersion())) return;
                willDownloadPackageInfoList.remove(index);
                if (onlyUpdatePackageInfoList == null) {
                    onlyUpdatePackageInfoList = new ArrayList<>();
                }
                if (onlineInfo.getStatus() == PackageStatus.online) {
                    onlyUpdatePackageInfoList.add(localInfo);
                }
                localInfo.setStatus(onlineInfo.getStatus());
            } else {
                // TODO: 应该设置md5, 以便检查下载正确性
                // 本地版本小于线上版本
                if ((Integer.parseInt(onlineInfo.getVersion())) - (Integer.parseInt(localInfo.getVersion())) > 1) {
                    // 超过离线包更新超过两位, 直接更新全量包
                    onlineInfo.setIsPatch(false);
                } else {
                    // 增量更新
                    onlineInfo.setIsPatch(true);
                }
            }
        }
    }
    //
    private boolean checkResourceFileValid(String packageId, String version) {
        File indexFile = FileUtils.getResourceIndexFile(context, packageId, version);
        return indexFile.exists() && indexFile.isFile();
    }
}
