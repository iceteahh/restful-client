package com.icetea.icetealibrary.utilities;

import android.util.LruCache;

/**
 * Created by icetea on 10/7/15.
 */
public class CacheManager {
    private static CacheManager instance;
    private LruCache<String, String> cache;
    private CacheManager(){
        int cacheSize = 5 * 1024 * 1024;
        cache = new LruCache<>(cacheSize);
        instance = this;
    }
    public static CacheManager getInstance(){
        if(instance == null){
            new CacheManager();
        }
        return instance;
    }
    public void put(String key, String value){
        cache.put(key, value);
    }
    public void remove(String key){
        cache.remove(key);
    }
    public String get(String key){
        return cache.get(key);
    }
}
