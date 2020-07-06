package com.example.packagemanager.packageManager;

import android.content.Context;
import android.text.TextUtils;

import com.example.packagemanager.util.FileUtils;
import com.example.packagemanager.util.GsonUtils;
import com.example.packagemanager.util.Logger;
import com.example.packagemanager.util.PatchUtils;
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
     * 下载文件: download.zip 或者预置在asset中的package.zip
     * 如果是patch文件: merge.zip
     * 更新后的zip目录: update.zip
     */
    public boolean install(PackageInfo packageInfo, boolean isAssets) {
        // 获取刚下载的离线包download.zip的路径, 或者预先加载到assets中的离线包的路径
        String downloadFile = isAssets ?
                FileUtils.getPackageAssetsName(context, packageInfo.getPackageId(), packageInfo.getVersion())
                :
                FileUtils.getPackageDownloadName(context, packageInfo.getPackageId(), packageInfo.getVersion());
        // 需要复制的文件
        String willCopyFile = downloadFile;

        // 获取update.zip文件的路径
        String updateFile = FileUtils.getPackageUpdateName(context, packageInfo.getPackageId(), packageInfo.getVersion());


        String lastVersion = getLastVersion(packageInfo.getPackageId());
        if (packageInfo.isPatch() && TextUtils.isEmpty(lastVersion)) {
            Logger.e("资源为path,  但上个版本信息没有数据, 无法path!");
            return false;
        }

        // merge离线增量
        if (packageInfo.isPatch()) {
            String baseFile = FileUtils.getPackageUpdateName(context, packageInfo.getPackageId(), lastVersion);
            String mergePath = FileUtils.getPackageMergeName(context, packageInfo.getPackageId(), packageInfo.getVersion());
            int diffStatus = -1;
            try {
                diffStatus = PatchUtils.getInstance().bsPatch(baseFile, downloadFile, mergePath);
            } catch (Exception e) {
                Logger.e("pach error: " + e.getMessage());
                e.printStackTrace();
            }
            if (diffStatus == 0) {
                //  增量成功, 增量成功后的merge.zip复制到update.zip, 然后解压等一系列操作
                willCopyFile = mergePath;
                FileUtils.deleteFile(downloadFile);
                // TODO: 需要验证刚刚生成的merge.zip和线上全量包的md5是否相同
//                return true;
            } else {
                Logger.e("merge error");
                return false;
            }
        } else {
            // TODO: 这里处理非path文件的情况
        }

        // 复制zip, 复制的过程中将下载下来的文件, 更名为update.zip
        if (!FileUtils.copyFileCover(willCopyFile, updateFile)) {
            Logger.e(packageInfo.getPackageId() + " : copy file error");
            return false;
        }
        if (!FileUtils.deleteFile(willCopyFile)) {
            Logger.e(packageInfo.getPackageId() + " : delete will copy file error");
            return false;
        }

        // 获取离线包应该所在的工作目录
        String workPath = FileUtils.getPackageWorkName(context, packageInfo.getPackageId(), packageInfo.getVersion());
        try {
            // 尝试解压文件, 将update.zip解压到工作目录
            if (!ZipUtils.unZipFolder(updateFile, workPath)) return false;
        } catch (Exception e) {
            return false;
        }
        FileUtils.deleteFile(willCopyFile);
        cleanOldFileIfNeed(packageInfo.getPackageId(), packageInfo.getVersion(), lastVersion);
        return true;
    }
    // 清除旧包, 清除与当前版本不同, 或者上一个版本的旧包
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
    // 获取上一次, 本地存储的packageId对应的离线包的版本号
    private String getLastVersion(String packageId) {
        String packageIndexFile = FileUtils.getPackageIndexFileName(context);
        FileInputStream indexFileStream = null;
        try {
            indexFileStream = new FileInputStream(packageIndexFile);
        } catch (FileNotFoundException e) {
            Logger.d("getLastVersion: " + e.getMessage());
            return "";
        }

        if (indexFileStream == null) return "";
        // 第一次加载静态资源的时候, 这里是空的.
        PackageEntry localPackageEntry = GsonUtils.jsonFromFileStream(indexFileStream, PackageEntry.class);
        if (localPackageEntry == null || localPackageEntry.getItems() == null) return "";

        List<PackageInfo> list = localPackageEntry.getItems();
        PackageInfo info = new PackageInfo();
        info.setPackageId(packageId);
        int index = list.indexOf(info);
        if (index >= 0) return  list.get(index).getVersion();
        return "";
    }
}
