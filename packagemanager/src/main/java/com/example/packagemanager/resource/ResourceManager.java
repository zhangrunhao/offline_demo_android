package com.example.packagemanager.resource;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;

import com.example.packagemanager.Constants;
import com.example.packagemanager.util.FileUtils;
import com.example.packagemanager.util.GsonUtils;
import com.example.packagemanager.util.Logger;
import com.example.packagemanager.util.MimeTypeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 资源管理器
 */
public class ResourceManager {
    private Context context;
    private Lock lock;
    private Map<ResourceKey, ResourceInfo> resourceInfoMap;
    private ResourceValidator validator;

    public ResourceManager(Context context) {
        this.context = context;
        this.resourceInfoMap = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
        this.validator = new ResourceValidator();
    }

    // 更新资源
    public boolean updateResource(String packageId, String version) {
        // index.json
        String indexFileName = FileUtils.getPackageWorkName(context, packageId, version)
            + File.separator
            + Constants.RESOURCE_MIDDLE_PATH
            + File.separator
            + Constants.RESOURCE_INDEX_NAME;
        Logger.d("updateResource indexFileName: " + indexFileName);

        File indexFile = new File(indexFileName);
        if (!indexFile.exists()) {
            Logger.e("updateResource indexFile is not exists, update Resource error");
            return false;
        }
        if (!indexFile.isFile()) {
            Logger.e("updateResource indexFile is not file, update Resource error");
            return false;
        }

        FileInputStream indexFileStream = null;
        try {
            indexFileStream = new FileInputStream(indexFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (indexFileStream == null) {
            Logger.e("update resource stream is null, update resource error");
            return false;
        }

        // index.json => ResourceInfoEntry.class
        ResourceInfoEntry entry = GsonUtils.jsonFromFileStream(indexFileStream, ResourceInfoEntry.class);
        if (indexFileStream != null) {
            try {
                indexFileStream.close();
            } catch (Exception e) {
            }
        }
        if (entry == null) return false;

        // 获取所有的资源列表
        List<ResourceInfo> resourceInfos = entry.getItems();
        if (resourceInfos == null) return true;


        String workPath = FileUtils.getPackageWorkName(context, packageId, version);
        for (ResourceInfo resourceInfo: resourceInfos) {
            if (TextUtils.isEmpty(resourceInfo.getPath())) continue;
            resourceInfo.setPackageId(packageId);
            String path = resourceInfo.getPath();
            path = path.startsWith(File.separator) ? path.substring(1) : path;
            resourceInfo.setLocalPath(
                    workPath
                    + File.separator
                    + Constants.RESOURCE_MIDDLE_PATH
                    + File.separator
                    + path
            );
            lock.lock();
            resourceInfoMap.put(new ResourceKey(resourceInfo.getPackageId(), resourceInfo.getPath()), resourceInfo);
            lock.unlock();
        }
        return true;
    }
    // 获取网络响应资源
    public WebResourceResponse getWebResponseResource (String packageId, String path) {
        ResourceKey key = new ResourceKey(packageId, path);
        if (!(lock.tryLock())) return null;
        ResourceInfo resourceInfo = resourceInfoMap.get(key);
        lock.unlock();
        if (resourceInfo == null) return null;
        if (!(MimeTypeUtils.checkIsSupportMimeType(resourceInfo.getMimeType()))) {
            Logger.d("getWebResponseResource: " + packageId + ": " + path + " not support mime type");
            safeRemoveResource(key);
            return null;
        }

        InputStream inputStream = FileUtils.getInputStream(resourceInfo.getLocalPath());
        if (inputStream == null) {
            Logger.e("getWebResponseResource: " + packageId + ": " + path + " inputStream is null");
            return null;
        }
        // TODO: 资源验证

        WebResourceResponse response;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Map<String, String> header = new HashMap<>();
            header.put("Access-Control-Allow-Origin", "*");
            header.put("Access-Control-Allow-Headers", "Content-Type");
            response = new WebResourceResponse(
                    resourceInfo.getMimeType(),
                    "UTF-8",
                    200,
                    "ok",
                    header,
                    inputStream
            );
        } else {
            response = new WebResourceResponse(
              resourceInfo.getMimeType(),
              "UTF-8",
              inputStream
            );
        }
        return response;
    }
    // 安全删除资源
    public void safeRemoveResource(ResourceKey key) {
        if (lock.tryLock()) {
            resourceInfoMap.remove(key);
            lock.unlock();
        }
    }
}
