package com.example.packagemanager.resource;

/**
 * 每个url对应的资源管理信息
 */
public class ResourceInfo {
    private String packageId; // 关联的包id
    private String remoteUrl; // 关联的远程地址
    private String path; // 相对路径
    private String localPath; // 本地加载地址
    private String mimeType; // 类型
    private String md5;

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }
    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public String getPackageId() {return this.packageId;}
    public String getRemoteUrl() {return this.remoteUrl;}
    public String getLocalPath() {return this.localPath;}
    public String getMimeType() {return this.localPath;}
    public String getPath() {return this.path;}
    public String getMd5() {return this.md5;}
}
