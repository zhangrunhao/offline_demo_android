package com.example.packagemanager;

/**
 * Created by zhangrh on 2020/6/30
 * 配置信息
 */
public class PackageConfig {
    private boolean enableAssets = true;
    private boolean enableBsDiff = true;
    private String assetPath = "packageApp.zip";

    public boolean isEnableAssets() {return enableAssets;}
    public boolean isEnableBsDiff() {return enableBsDiff;}
    public String getAssetPath() {return assetPath;}

    public void setEnableAssets(boolean enableAssets) {
        this.enableAssets = enableAssets;
    }
    public void  setEnableBsDiff(boolean enableBsDiff) {
        this.enableBsDiff = enableBsDiff;
    }
    public void setAssetPath(String assetPath) {
        this.assetPath = assetPath;
    }

}
