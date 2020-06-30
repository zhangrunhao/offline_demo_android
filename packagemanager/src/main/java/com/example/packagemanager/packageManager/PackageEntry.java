package com.example.packagemanager.packageManager;

import java.util.List;

/**
 * 离线包, index(索引)文件信息
 */
public class PackageEntry {
    private List<PackageInfo> items;
    public void setItems (List<PackageInfo> items) {this.items = items;}
    public List<PackageInfo> getItems() {return this.items;}
}
