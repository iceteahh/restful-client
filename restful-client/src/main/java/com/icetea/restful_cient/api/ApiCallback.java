package com.icetea.restful_cient.api;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

/**
 * Created by icetea on 10/7/15.
 */

public abstract class ApiCallback implements LoaderManager.LoaderCallbacks<String> {

    public static String NETWORK_ERROR_INDICATOR = "network_error";

    public void onNetworkError(Loader<String> loader){}
    public void onError(Loader<String> loader){}
    public void onPostExecute(Loader<String> loader){}
    public void onSuccess(Loader<String> loader, String data){}

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if(data != null){
            if(data.equals(NETWORK_ERROR_INDICATOR)){
                onNetworkError(loader);
            }
            else {
                onSuccess(loader, data);
            }
        } else {
            onError(loader);
        }
        onPostExecute(loader);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {}
}
