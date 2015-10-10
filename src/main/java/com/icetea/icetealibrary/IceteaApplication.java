package com.icetea.icetealibrary;

import android.app.Application;
import android.graphics.Bitmap;

import com.facebook.FacebookSdk;
import com.icetea.icetealibrary.api.ApiService;
import com.icetea.icetealibrary.utilities.Utilities;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by iceteahh on 9/16/2015.
 */
public class IceteaApplication extends Application {
    private static ApiService apiService;
    private static RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.default_bg).cacheOnDisk(true)
                .cacheInMemory(true)
                .bitmapConfig((Bitmap.Config.RGB_565)).imageScaleType(ImageScaleType.EXACTLY).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(imageOptions)
                .diskCacheSize(50 * 1024 * 1024)
                .memoryCacheSize(20 * 1024 * 1024)
                .build();
        ImageLoader.getInstance().init(config);
        Utilities.DEBUG = true;
        apiService = new ApiService(this);
        refWatcher = LeakCanary.install(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    public static RefWatcher getRefWatcher() {
        return refWatcher;
    }

    public static ApiService getApiService() {
        return apiService;
    }
}
