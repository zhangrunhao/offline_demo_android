package com.example.packagemanager.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.example.packagemanager.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.FileChannel;

/**
 * 文件工具
 */
public class FileUtils {
    // inputStream to String
    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    // 获取资源中的索引文件
    public  static File getResourceIndexFile(Context context, String packageId, String version) {
        String indexPath = getPackageWorkName(context, packageId, version)
                + File.separator + Constants.RESOURCE_MIDDLE_PATH
                + File.separator + Constants.RESOURCE_INDEX_NAME;
        return new File(indexPath);
    }
    // 复制文件
    public static boolean copyFile(InputStream inputStream, String newPath) {
        try {
            int byteRead = 0;
            File file = new File(newPath);
            if (file.exists()) {
                if (!file.delete()) return false;
            }
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) return false;
            }
            if (!file.createNewFile()) return false;

            FileOutputStream fs = new FileOutputStream(file);
            byte[] buffer = new byte[1024 * 16];
            while ((byteRead = inputStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteRead);
            }
            fs.flush();
            safeCloseFile(inputStream);
            safeCloseFile(fs);
        } catch (Exception e) {
            Logger.e("copyFile error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
    // 安全关闭文件
    public static boolean safeCloseFile(Closeable file) {
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // 复制单个文件
    public static boolean copyFileCover(String srcFileName, String descFileName) {
        File srcFile = new File(srcFileName);
        if (!srcFile.exists() || !srcFile.isFile()) return false;

        File descFile = new File(descFileName);
        if (descFile.exists()) {
            if (!FileUtils.deleteFile(descFileName)) return false;
        } else if (descFile.getParentFile() != null) {
            if (!descFile.getParentFile().exists() && !descFile.getParentFile().mkdirs()) return false;
        } else {
            return false;
        }

        try {
            return FileUtils.copyFileByChannel(srcFile, descFile);
        } catch (Exception e) {
            return false;
        }
    }
    public static boolean copyFileByChannel(File src, File desc) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;

        try {
            fi = new FileInputStream(src);
            fo = new FileOutputStream(desc);
            in = fi.getChannel();
            out= fo.getChannel();
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                fi.close();
                fo.close();
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    // 删除文件, 可以删除单个文件, 或者文件夹
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    // 根据packageId获取merge目录地址
    public static String getPackageMergeName(Context context, String packageId, String version) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) return null;
        return root + File.separator
                + packageId + File.separator
                + version + File.separator
                + Constants.PACKAGE_MERGE;
    }
    // 获取update.zip目录地址
    public static String getPackageUpdateName(Context context, String packageId, String version) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) return null;
        return root + File.separator
                + packageId + File.separator
                + version + File.separator
                + Constants.PACKAGE_UPDATE;
    }
    // 根据packageId获取下载目录
    public static String getPackageDownloadName(Context context, String packageId, String version) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) return null;
        return root + File.separator
                + packageId + File.separator
                + version + File.separator
                + Constants.PACKAGE_DOWNLOAD;
    }
    // 获取静态资源目录
    public static String getPackageAssetsName(Context context, String packageId, String version) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) return null;
        return root + File.separator
                + "assets" + File.separator
                + packageId + File.separator
                + version + File.separator
                + Constants.PACKAGE_ASSETS;

    }
    // 删除某个目录, 以及目录下所有的子文件
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean isDelete = deleteDir(new File(dir, child));
                }
            }
        }
        return dir.delete();
    }
    // 根据packageId获得package根路径
    public static String getPackageRootByPackageId(Context context, String packageId) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) return null;
        return root + File.separator + packageId;
    }
    // 根据packageId获取packageIndex.json地址
    public static String getPackageIndexFileName(Context context) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) return null;
        makeDir(root);
        return root + File.separator + Constants.PACKAGE_FILE_INDEX;
    }
    // 创建文件夹
    public static boolean makeDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }
    // 根据fileName获取inputStream
    public static InputStream getInputStream(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        File file = new File(fileName);
        if (!file.exists()) return null;
        if (file.isDirectory()) return null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fileInputStream == null) return null;
        return new BufferedInputStream(fileInputStream);
    }
    // 根据packageId获取工作目录
    public static String getPackageWorkName(Context context, String packageId, String version) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) return null;
        return root + File.separator
                + packageId + File.separator
                + version + File.separator
                + Constants.PACKAGE_WORK;
    }
    // 获取根地址
    public static String getPackageRootPath(Context context) {
        File fileDir = getFileDirectory(context, false);
        if (fileDir == null) return null;
        String path = fileDir + File.separator + Constants.PACKAGE_FILE_ROOT_PATH;
        File file;
        if (!(file = new File(path)).exists()) file.mkdir();
        return path;
    }
    // 获取缓存目录
    public static File getFileDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        if (preferExternal && isExternalStorageMounted()) appCacheDir = getExternalCacheDir(context);
        if (appCacheDir == null) appCacheDir = context.getFilesDir();
        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/file";
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }
    // 获取外部缓存目录
    public static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"),  "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "file");
        if (!appCacheDir.exists() && !appCacheDir.mkdir()) return null;
        return dataDir;
    }
    // TODO: 不明白外部实例化存储是在做什么?
    // 是否为外部存储实例化
    public static boolean isExternalStorageMounted() {
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(getExternalStorageState());
    }
    // 获取外部存储状态
    public static String getExternalStorageState() {
        String externalStorageState = "";
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) {
            Logger.d("Environment.getExternalStorageState() 捕获异常");
            externalStorageState = "";
        }
        return externalStorageState;
    }
}
