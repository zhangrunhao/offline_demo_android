package com.example.packagemanager.util;

import com.example.packagemanager.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
    /**
     * 获取zip中的字符串
     * @param zipFileStream zip流
     * @return 字符串
     */
    public static String getStringFromZip(InputStream zipFileStream) {
        ZipInputStream inZip = null;
        try {
            inZip = new ZipInputStream(zipFileStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        ZipEntry zipEntry = readZipNextEntry(inZip);
        while (zipEntry != null) {
            String szName = zipEntry.getName();
            // index.json
            if (szName.equals(Constants.RESOURCE_MIDDLE_PATH + File.separator + Constants.RESOURCE_INDEX_NAME)) break;
            zipEntry = readZipNextEntry(inZip);
        }
        if (zipEntry == null) {
            FileUtils.safeCloseFile(inZip);
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int len = -1;
        byte[] buffer = new byte[2048];
        len = readZipFile(inZip, buffer);
        while (len != -1) {
            if (len == -2) break;
            sb.append(new String(buffer, 0, len));
            len = readZipFile(inZip, buffer);
        }
        if (FileUtils.safeCloseFile(inZip)) return sb.toString();
        return "";

    }
    /**
     * 解压zip到指定的目录
     * @param zipFileString 需要解压的zip目录
     * @param outPathString 解压到指定目录
     * @return 解压是否成功
     */
    public static boolean unZipFolder(String zipFileString, String outPathString) {
        ZipInputStream inZip = deleteOutUnZipFileIfNeed(zipFileString, outPathString);
        if (inZip == null) return false;

        ZipEntry zipEntry = readZipNextEntry(inZip);
        if (zipEntry == null) return false;

        while (zipEntry != null) {
            String szName = zipEntry.getName();
            // 不是 package 开头, 认定是无效数据
            if (!szName.startsWith(Constants.RESOURCE_MIDDLE_PATH)) {
                zipEntry = readZipNextEntry(inZip);
                continue;
            }
            // 判断是否为文件夹
            if (zipEntry.isDirectory()) {
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                if (!folder.mkdirs()) break;
            } else {
                File file = new File(outPathString + File.separator + szName);
                if (!file.exists()) {
                    if (!makeUnZipFile(outPathString, szName)) break;
                }
                if (!writeUnZipFileToFile(inZip, file)) break;
            }
        }

        return FileUtils.safeCloseFile(inZip);
    }

    private static int readZipFile(ZipInputStream inZip, byte[] buffer) {
        int len = -1;
        try {
            len = inZip.read(buffer);
        } catch (IOException e) {
            len = -2;
        }
        return len;
    }


    private static boolean writeUnZipFileToFile(ZipInputStream inZip, File file) {
        boolean isSuccess = true;
        // 获取文件的输出流
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            isSuccess = false;
        }
        int len = -1;
        byte[] buffer = new byte[1024];
        len = readZipFile(inZip, buffer);
        if (len == -1 || len == -2) {
            isSuccess = false;
        }
        if (!isSuccess) {
            return false;
        }
        // 读取（字节）字节到缓冲区
        while (len != -1) {
            // 从缓冲区（0）位置写入（字节）字节
            try {
                out.write(buffer, 0, len);
            } catch (IOException e) {
                isSuccess = false;
            }
            if (!isSuccess) {
                break;
            }
            try {
                out.flush();
            } catch (IOException e) {
                isSuccess = false;
            }
            if (!isSuccess) {
                break;
            }
            len = readZipFile(inZip, buffer);
            if (len == -2) {
                isSuccess = false;
                break;
            }
        }
        try {
            out.close();
        } catch (IOException e) {

        }
        return isSuccess;
    }

    private static boolean makeUnZipFile(String outPathString, String szName) {
        File file = new File(outPathString + File.separator + szName);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) return false;
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static ZipEntry readZipNextEntry(ZipInputStream inZip) {
        ZipEntry zipEntry = null;
        try {
            zipEntry = inZip.getNextEntry();
        } catch (IOException e) {
            return null;
        }
        return zipEntry;
    }

    private static ZipInputStream deleteOutUnZipFileIfNeed(String zipFileString, String outPathString) {
        ZipInputStream inZip = null;
        try {
            inZip = new ZipInputStream(new FileInputStream(zipFileString));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        File outPath = new File(outPathString);
        if (outPath.exists()) {
            if (!FileUtils.deleteDir(outPath)) return null;
        }
        return inZip;
    }
}
