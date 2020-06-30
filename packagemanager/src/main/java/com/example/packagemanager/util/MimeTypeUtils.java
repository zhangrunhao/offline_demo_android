package com.example.packagemanager.util;

import java.util.ArrayList;
import java.util.List;

/**
 * MimeType工具类
 */
public class MimeTypeUtils {
    private static List<String> supportMimeTypeList = new ArrayList<>();
    static {
        supportMimeTypeList.add("application/x-javascript");
        supportMimeTypeList.add("image/jpeg");
        supportMimeTypeList.add("image/tiff");
        supportMimeTypeList.add("text/css");
        supportMimeTypeList.add("image/gif");
        supportMimeTypeList.add("image/png");
    }
    public static boolean checkIsSupportMimeType(String mime) {
        return supportMimeTypeList.contains(mime);
    }
}
