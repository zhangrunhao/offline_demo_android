package com.example.packagemanager.util;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Gson工具
 */
public class GsonUtils {
    private static final Gson gson = new Gson();
    public static <T> T jsonFromString(String json, Class<T> classOfT) {
        try {
            return gson.fromJson(json, classOfT);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
    public static <T> T jsonFromFileStream(InputStream json, Class<T> classOfT) {
        Reader reader = null;
        BufferedReader bufferedReader = null;
        T entry = null;
        try {
            reader = new InputStreamReader(json);
            bufferedReader = new BufferedReader(reader);
            entry = gson.fromJson(bufferedReader, classOfT);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return  entry;
    }
}
