package com.example.packagemanager.packageManager;

import java.util.List;

/**
 * 离线包索引信息 (所有离线包的集合信息)
 */
public class PackageEntry {
    private int errorCode;
    private List<PackageInfo> data;
    public void setItems (List<PackageInfo> data) {this.data = data;}
    public List<PackageInfo> getItems() {return this.data;}
}
