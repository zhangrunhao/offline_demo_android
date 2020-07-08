package com.example.packagemanager.resource;

import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

public class ResourceKey {
    private String packageId; // 模块名称
    private String path; // 相对路径

    // 通过packageId + filename进行访问
    public ResourceKey(String packageId, String path) {
        this.packageId = packageId;
        this.path = path;
    }

    // TODO: 不清楚hashCode的用途
    public int hashCode() { // 获取uri的hashcode
        int result = 17;
        result = result * 37 + getHashCode(packageId);
        result = result * 37 + getHashCode(path);
        return result;
    }
    public int getHashCode(Object object) { // 获取某非空对象的hashcode
        return object == null ? 0 : object.hashCode();
    }

    public boolean equals(Object obj) { // 判断两个ResourceKey是否相等
        if (!(obj instanceof ResourceKey)) return false;
        ResourceKey other = (ResourceKey) obj;

        if (!(TextUtils.equals(packageId, other.packageId))) return false;
        if (!(TextUtils.equals(path, other.path))) return false;
        return true;
    }
}
