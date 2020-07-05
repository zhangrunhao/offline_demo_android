package com.example.packagemanager.packageManager;

import android.text.TextUtils;

import com.example.packagemanager.Constants;

/**
 * 模块信息
 */

public class PackageInfo {
    // 下发信息
    private String module_name; // 包名称 packageId
    // TODO: 区分包状态
//    private String type; // 包类型: module/plugin
    private String version; // 版本号
    private int status = PackageStatus.online; // 包状态
    private String origin_file_path; // 完整包下载地址
    private String origin_file_md5; // 完整包md5
    private String patch_file_path; // 差量包地址
    private String patch_file_md5; // 差量包md5

    // 安装信息
    private boolean isPatch = false; // 是否为patch包
    private String downloadUrl; // 实际下载地址


    public String getPackageId() {
        return module_name;
    }

    public String getOrigin_file_md5() {
        return origin_file_md5;
    }

    public String getOrigin_file_path() {
        return origin_file_path;
    }

    public String getPatch_file_md5() {
        return patch_file_md5;
    }

    public String getPatch_file_path() {
        return patch_file_path;
    }

    public String getVersion() {
        return version;
    }
    public String getDownloadUrl() {
        if (isPatch) {
            return Constants.BASE_URL + patch_file_path;
        } else {
            return Constants.BASE_URL + origin_file_path;
        }
    }

    public boolean isPatch() {
        return isPatch;
    }

    public int getStatus() {
        return status;
    }

    public void setIsPatch(boolean patch) {
        isPatch = patch;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = Constants.BASE_URL +  downloadUrl;
    }

    public void setPackageId(String packageId) {
        this.module_name = packageId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof  PackageInfo)) return false;
        PackageInfo other = (PackageInfo) obj;
        return TextUtils.equals(module_name, other.module_name);
    }

    public int hashCode() {
        int result = 17;
        result = result * 37 + (module_name == null ? 0 :  module_name.hashCode());
        return result;
    }
}
