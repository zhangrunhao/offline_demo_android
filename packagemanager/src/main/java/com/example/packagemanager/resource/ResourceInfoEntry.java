package com.example.packagemanager.resource;

import java.util.List;

/**
 * 单个包中所有的资源集合信息
 */
public class ResourceInfoEntry {
    private String version;
    private String packageId;
    private List<ResourceInfo> items;

    public String getVersion() {
        return version;
    }

    public String getPackageId() {
        return packageId;
    }

    public List<ResourceInfo> getItems(){
        return items;
    }
}