package com.icetea.icetealibrary.api;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.icetea.icetealibrary.utilities.CacheManager;
import com.icetea.icetealibrary.utilities.RequestUtils;
import com.icetea.icetealibrary.utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by icetea on 10/7/15.
 */
public class ApiLoader extends AsyncTaskLoader<String> {

    private Bundle bundle;
    private Context context;
    private ApiCallback callback;

    public ApiLoader(Context context, Bundle bundle, ApiCallback apiCallback) {
        super(context);
        this.bundle = bundle;
        this.context = context;
        this.callback = apiCallback;
    }

    @Override
    public String loadInBackground() {
        final String url = bundle.getString(RequestUtils.URL_KEY);
        final boolean cache = bundle.getBoolean(RequestUtils.CACHE_KEY);
        final HashMap<String, String> params =
                (HashMap<String, String>) bundle.getSerializable(RequestUtils.PARAMS_KEY);
        if (cache && params != null) {
            String value = CacheManager.getInstance().get(url + params.toString());
            if (value != null)
                return value;
        }
        if (Utilities.isNetworkAvailable(context)) {
            int method = bundle.getInt(RequestUtils.METHOD_KEY);
            switch (method) {
                case RequestUtils.METHOD_GET:
                    try {
                        JSONObject response = doGetRequest(url, params);
                        int returnCode = response.getInt("returnCode");
                        String result = response.getJSONObject("data").getString("results");
                        if (returnCode == 1) {
                            if (cache && params != null) {
                                CacheManager.getInstance().put(url + params.toString(), result);
                            }
                            return result;
                        } else {
                            callback.onError(returnCode, "Error");
                        }
                    } catch (Exception e) {
                        if (Utilities.DEBUG)
                            Log.e("Request Error", e.getMessage());
                        e.printStackTrace();
                        callback.onError(0, "Unknown error");
                    }
                    callback.onPostExecute();
                    break;
                case RequestUtils.METHOD_POST:
                    try {
                        JSONObject response = doPostRequest(url, params);
                        int returnCode = response.getInt("returnCode");
                        String result = response.getJSONObject("data").getString("results");
                        if (returnCode == 1) {
                            if (cache && params != null) {
                                CacheManager.getInstance().put(url + params.toString(), result);
                            }
                            return result;
                        } else {
                            callback.onError(returnCode, "Error");
                        }
                    } catch (IOException | JSONException | IllegalStateException | JsonSyntaxException e) {
                        e.printStackTrace();
                        callback.onError(0, "Unknown error");
                    }
                    callback.onPostExecute();
                    break;
                case RequestUtils.METHOD_GET_RAW:
                    try {
                        return doGetRawRequest(url, params);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onError(0, "Unknown error");
                    }
                    callback.onPostExecute();
                    break;
                case RequestUtils.METHOD_GET_1:

                    try {
                        JSONObject response = doGetRequest(url, params);
                        int returnCode = response.getInt("returnCode");
                        String result = response.getString("data");
                        if (returnCode == 1) {
                            if (cache && params != null) {
                                CacheManager.getInstance().put(url + params.toString(), result);
                            }
                            return result;
                        } else {
                            callback.onError(returnCode, "Error");
                        }
                    } catch (Exception e) {
                        if (Utilities.DEBUG)
                            Log.e("Request Error", e.getMessage());
                        e.printStackTrace();
                        callback.onError(0, "Unknown error");
                    }
                    callback.onPostExecute();
            }
        } else {
            callback.onNetworkError();
            callback.onPostExecute();
        }
        return null;
    }

    @Override
    public void deliverResult(String data) {
        super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(String data) {
        super.onCanceled(data);
    }

    @Override
    protected void onReset() {
        super.onReset();
        cancelLoad();
    }

    private JSONObject doPostRequest(String url,
                                     Map<String, String> params) throws
            IOException, JSONException {

        HttpURLConnection con;
        StringBuilder postParam = new StringBuilder();
        for (String key : params.keySet()) {
            postParam.append(key).append("=").append(params.get(key)).append("&");
        }
        String urlParameters = postParam.toString();
        byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
        int postDataLength = postData.length;

        URL toUrl = new URL(url);

        con = (HttpURLConnection) toUrl.openConnection();
        con.setConnectTimeout(RequestUtils.TIME_OUT);
        con.setReadTimeout(RequestUtils.TIME_OUT);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("charset", "utf-8");
        con.setRequestProperty("Content-Length", Integer.toString(postDataLength));

        OutputStream wr = con.getOutputStream();
        wr.write(postData);
        wr.flush();
        wr.close();

        con.connect();

        InputStream in;
        try {
            in = con.getInputStream();
        } catch (IOException e) {
            in = con.getErrorStream();
        }
        byte[] buffer = new byte[128];
        int lent;
        StringBuilder response = new StringBuilder();
        while ((lent = in.read(buffer)) != -1) {
            response.append(new String(buffer, 0, lent));
        }
        in.close();
        con.disconnect();

        return new JSONObject(response.toString());
    }

    public static JSONObject doGetRequest(String url,
                                          Map<String, String> params) throws
            IOException, JSONException {
        HttpURLConnection con;
        Uri.Builder builder = Uri.parse(url)
                .buildUpon();
        for (String key : params.keySet()) {
            builder.appendQueryParameter(key, params.get(key));
        }

        url = builder.build().toString();
        if (Utilities.DEBUG)
            Log.e("url", url);

        URL toUrl = new URL(url);

        con = (HttpURLConnection) toUrl.openConnection();
        con.setConnectTimeout(RequestUtils.TIME_OUT);
        con.setReadTimeout(RequestUtils.TIME_OUT);
        con.setRequestMethod("GET");
        con.connect();
        InputStream in;
        try {
            in = con.getInputStream();
        } catch (IOException e) {
            in = con.getErrorStream();
        }
        byte[] buffer = new byte[128];
        int lent;
        StringBuilder response = new StringBuilder();
        while ((lent = in.read(buffer)) != -1) {
            response.append(new String(buffer, 0, lent));
        }
        in.close();
        con.disconnect();

        return new JSONObject(response.toString());
    }

    public static String doGetRawRequest(String url,
                                         Map<String, String> params) throws
            IOException, JSONException {
        HttpURLConnection con;
        Uri.Builder builder = Uri.parse(url)
                .buildUpon();
        for (String key : params.keySet()) {
            builder.appendQueryParameter(key, params.get(key));
        }

        url = builder.build().toString();
        URL toUrl = new URL(url);

        con = (HttpURLConnection) toUrl.openConnection();
        con.setInstanceFollowRedirects(false);
        con.setConnectTimeout(RequestUtils.TIME_OUT);
        con.setReadTimeout(RequestUtils.TIME_OUT);
        con.setRequestMethod("GET");
        con.connect();
        InputStream in;
        try {
            in = con.getInputStream();
        } catch (IOException e) {
            in = con.getErrorStream();
        }
        byte[] buffer = new byte[128];
        int lent;
        StringBuilder response = new StringBuilder();
        while ((lent = in.read(buffer)) != -1) {
            response.append(new String(buffer, 0, lent));
        }
        in.close();
        con.disconnect();

        return response.toString();
    }

    public String doGetDataRequest(String url,
                                   Map<String, String> params) throws
            IOException, JSONException {
        HttpURLConnection con;
        Uri.Builder builder = Uri.parse(url)
                .buildUpon();
        for (String key : params.keySet()) {
            builder.appendQueryParameter(key, params.get(key));
        }

        url = builder.build().toString();
        URL toUrl = new URL(url);

        con = (HttpURLConnection) toUrl.openConnection();
        con.setConnectTimeout(RequestUtils.TIME_OUT);
        con.setReadTimeout(RequestUtils.TIME_OUT);
        con.setRequestMethod("GET");
        con.setRequestProperty("charset", "utf-8");
        con.connect();
        InputStream in;
        try {
            in = con.getInputStream();
        } catch (IOException e) {
            in = con.getErrorStream();
        }

        String data;
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte[] abInBuffer = new byte[128];
        int nBytesRead = 0;
        while (nBytesRead != -1) {
            nBytesRead = in.read(abInBuffer);
            if (nBytesRead != -1) bao.write(abInBuffer, 0, nBytesRead);
        }
        byte[] arr = bao.toByteArray();
        if (arr[0] == (byte) 0xEF && arr[1] == (byte) 0xBB && arr[2] == (byte) 0xBF)
            data = new String(arr, "UTF-8");
        else if (arr[0] == (byte) 0xFE && arr[1] == (byte) 0xFF)
            data = new String(arr, "UTF-16");
        else if (arr[0] == (byte) 0xFF && arr[1] == (byte) 0xFE)
            data = new String(arr, "UTF-16");
        else
            data = new String(arr);
        in.close();
        con.disconnect();
        return data;
    }
}
