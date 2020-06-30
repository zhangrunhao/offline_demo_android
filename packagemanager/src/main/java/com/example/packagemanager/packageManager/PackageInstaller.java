package com.example.packagemanager.packageManager;

import android.content.Context;
import android.text.TextUtils;

import com.example.packagemanager.util.DiffUtils;
import com.example.packagemanager.util.FileUtils;
import com.example.packagemanager.util.GsonUtils;
import com.example.packagemanager.util.Logger;
import com.example.packagemanager.util.ZipUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PackageInstaller {
    private Context context;

    public PackageInstaller(Context context) {
        this.context = context;
    }

    /**
     * 安卓离线包
     * 下载文件: download.zip
     * 如果是patch文件: merge.zip
     * 更新后的zip目录: update.zip
     */
    public boolean install(PackageInfo packageInfo, boolean isAssets) {
        // 获取下载目录
        String downloadFile = isAssets ?
                FileUtils.getPackageAssetsName(context, packageInfo.getPackageId(), packageInfo.getVersion())
                :
                FileUtils.getPackageDownloadName(context, packageInfo.getPackageId(), packageInfo.getVersion());
        String willCopyFile = downloadFile;

        // 获取update.zip目录
        String updateFile = FileUtils.getPackageUpdateName(context, packageInfo.getPackageId(), packageInfo.getVersion());
        String lastVersion = getLastVersion(packageInfo.getPackageId());
        if (packageInfo.isPath() && TextUtils.isEmpty(lastVersion)) {
            Logger.e("资源为path,  但上个版本信息没有数据, 无法path!");
            return false;
        }

        // merge离线增量
        if (packageInfo.isPath()) {
            String baseFile = FileUtils.getPackageUpdateName(context, packageInfo.getPackageId(), lastVersion);
            String mergePath = FileUtils.getPackageMergeName(context, packageInfo.getPackageId(), packageInfo.getVersion());
            int diffStatus = -1;
            try {
                diffStatus = DiffUtils.patch(baseFile, mergePath, downloadFile);
            } catch (Exception e) {
                Logger.e("pach error: " + e.getMessage());
                e.printStackTrace();
            }
            if (diffStatus == 0) {
                willCopyFile = mergePath;
                FileUtils.deleteFile(downloadFile);
                return true;
            } else {
                Logger.e("merge error");
                return false;
            }
        }

        // 复制zip
        if (!FileUtils.copyFileCover(willCopyFile, updateFile)) {
            Logger.e(packageInfo.getPackageId() + " : copy file error");
            return false;
        }
        if (!FileUtils.deleteFile(willCopyFile)) {
            Logger.e(packageInfo.getPackageId() + " : delete will copy file error");
            return false;
        }

        // 解压成功
        String workPath = FileUtils.getPackageWorkName(context, packageInfo.getPackageId(), packageInfo.getVersion());
        try {
            if (!ZipUtils.unZipFolder(updateFile, workPath)) return false;
        } catch (Exception e) {
            return false;
        }
        FileUtils.deleteFile(willCopyFile);
        cleanOldFileIfNeed(packageInfo.getPackageId(), packageInfo.getVersion(), lastVersion);
        return true;
    }
    // 清除旧包
    private void cleanOldFileIfNeed(String packageId, String version, String lastVersion) {
        String path = FileUtils.getPackageRootByPackageId(context, packageId);
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) return;

        File[] versionList = file.listFiles();
        if (versionList == null || versionList.length == 0) return;

        List<File> deleteFiles = new ArrayList<>();
        for (File item : versionList) {
            if (TextUtils.equals(version, item.getName()) || TextUtils.equals(lastVersion, item.getName())) {
                continue;
            }
            deleteFiles.add(item);
        }
        for (File deleteFile : deleteFiles) {
            FileUtils.deleteDir(deleteFile);
        }
    }
    // 获取最后一个版本号
    private String getLastVersion(String packageId) {
        String packageIndexFile = FileUtils.getPackageIndexFileName(context);
        FileInputStream indexFileStream = null;
        try {
            indexFileStream = new FileInputStream(packageIndexFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (indexFileStream == null) return "";

        PackageEntry localPackageEntry = GsonUtils.jsonFromFileStream(indexFileStream, PackageEntry.class);
        if (localPackageEntry == null || localPackageEntry.getItems() == null) return "";

        List<PackageInfo> list = localPackageEntry.getItems();
        PackageInfo info = new PackageInfo();
        info.setPackageId(packageId);
        int index = list.indexOf(info);
        // TODO: 难道这里是任取一个包, 作为了最后的Version?
        if (index >= 0) return  list.get(index).getVersion();
        return "";
    }
}
