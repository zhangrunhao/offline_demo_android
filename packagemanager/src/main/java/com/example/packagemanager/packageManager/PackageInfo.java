package com.example.packagemanager.packageManager;

import android.text.TextUtils;

/**
 * 离线包信息
 */

public class PackageInfo {
    private String packageId; // 离线包Id
    private String downloadUrl; // 离线包下载地址
    private String version = "1.0"; // 离线包版本号
    private int status = PackageStatus.online; // 离线包状态
    private boolean isPath; // 是否为path包
    private String md5; // 离线包md5值, 由后端下发

    public String getPackageId() {return packageId;}
    public String getDownloadUrl() {return downloadUrl;}
    public String getVersion() {return  version;}
    public int getStatus() {return status;}
    public boolean isPath() {return isPath;}
    public String getMd5() {return md5;}

    public void setPackageId(String packageId) {this.packageId = packageId;}
    public void setVersion(String version) {this.version = version;}
    public void setStatus(int status) {this.status = status;}
    public void setMd5(String md5) {this.md5 = md5;}

    public boolean equals(Object obj) {
        if (!(obj instanceof  PackageInfo)) return false;
        PackageInfo other = (PackageInfo) obj;
        return TextUtils.equals(packageId, other.packageId);
    }

    public int hashCode() {
        int result = 17;
        result = result * 37 + (packageId == null ? 0 : packageId.hashCode());
        return result;
    }
}
