package com.example.packagemanager;

import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

public class ResourceKey {
    private String host; // 地址
    private String scheme; // 协议
    private List<String> pathList; // 地址列表

    public ResourceKey(String url) {
        Uri uri = Uri.parse(url);
        host = uri.getHost();
        scheme = uri.getScheme();
        pathList = uri.getPathSegments();
    }

    public int hashCode() { // 获取uri的hashcode
        int result = 17;
        result = result * 37 + getHashCode(host);
        result = result * 37 + getHashCode(scheme);
        if (pathList != null) {
            for (String pa : pathList) {
                result = result * 37 + getHashCode(pa);
            }
        }
        return result;
    }
    public int getHashCode(Object object) { // 获取某非空对象的hashcode
        return object == null ? 0 : object.hashCode();
    }

    public boolean equals(Object obj) { // 判断是否相等
        if (!(obj instanceof ResourceKey)) return false;
        ResourceKey other = (ResourceKey) obj;
        if (!(TextUtils.equals(host, other.host))) return false;
        if (!(TextUtils.equals(scheme, other.scheme))) return false;
        if (pathList == other.pathList) return true;
        if (pathList == null || other.pathList == null) return false;
        boolean isEquals = true;
        for (String pa : pathList) {
            if (!other.pathList.contains(pa)) isEquals = false;
        }
        return isEquals;
    }
}
