package com.icetea.restful_cient.utilities;

import android.os.Build;
import android.util.LruCache;

/**
 * Created by icetea on 10/7/15.
 */
public class CacheManager {
    private static CacheManager instance;
    private LruCache<String, String> cache;
    private CacheManager(){
        int cacheSize = 5 * 1024 * 1024;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            cache = new LruCache<>(cacheSize);
        }
        instance = this;
    }
    public static CacheManager getInstance(){
        if(instance == null){
            new CacheManager();
        }
        return instance;
    }
    public void put(String key, String value){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            cache.put(key, value);
        }
    }
    public void remove(String key){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            cache.remove(key);
        }
    }
    public String get(String key){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return cache.get(key);
        }
        return null;
    }
}
