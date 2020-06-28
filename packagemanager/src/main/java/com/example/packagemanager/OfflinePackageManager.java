package com.example.packagemanager;

import android.content.Context;

import java.util.Map;

/**
 * 离线包管理器
 */
public class OfflinePackageManager {
    private Context context;
    public  void init(Context context) {

        this.context = context; // 需要使用离线包的activity
    }
}
