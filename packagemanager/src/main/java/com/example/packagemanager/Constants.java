package com.example.packagemanager;

/**
 * 存放常量信息
 */
public class Constants {
    // 本地预置安装包列表
    public static String[] LOCAL_ASSET_LIST = {
            "package.zip"
    };
    // 服务器地址
    public static final String BASE_URL = "http://10.2.155.99:5003";
    // 配置信息所在文件
    public static final String PACKAGE_FILE_INDEX = "packageIndex.json";

    // 工作目录
    public static final String PACKAGE_WORK = "work";

    // 离线包缓存根目录
    public static final String PACKAGE_FILE_ROOT_PATH = "offline_package";

    // 中间路径: 每个离线包的文件夹名称
    public static final String RESOURCE_MIDDLE_PATH = "package";

    // 离线包: 索引信息文件
    public static final String RESOURCE_INDEX_NAME = "index.json";

    // assets文件名称
    public static final String PACKAGE_ASSETS = "package_assets.zip";

    // 下载文件名称
    public static final String PACKAGE_DOWNLOAD = "download.zip";

    // 更新文件名称
    public static final String PACKAGE_UPDATE = "update.zip";

    // merge文件名称
    public static final String PACKAGE_MERGE = "merge.zip";
}
