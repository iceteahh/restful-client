package com.icetea.icetealibrary.api;

import android.app.LoaderManager;

/**
 * Created by icetea on 10/7/15.
 */
public interface ApiCallback extends LoaderManager.LoaderCallbacks {
    void onNetworkError();
    void onError(int type, String msg);
    void onPostExecute();
}
