package com.icetea.icetealibrary.api;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;

import com.google.gson.JsonSyntaxException;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by icetea on 3/22/2015.
 */
public class ApiService {

    public interface ServiceCallback {
        void onPreExecute();

        void onSuccess(String result);

        void onError(int code, String msg);

        void onNetworkError();

        void onPostExecute();
    }


    public static final int TIME_OUT = 30000;

    private ExecutorService executor;

    private LruCache<String, String> lruCache;

    private Context context;

    public ApiService(Context context){
        this.context = context;
        executor = Executors.newFixedThreadPool(3);
        int cacheSize = 5 * 1024 * 1024;
        lruCache = new LruCache<>(cacheSize);
    }

    public Future requestAPI(Bundle bundle, final ServiceCallback callback, final boolean cache) {
        final String url = bundle.getString(RequestUtils.URL_KEY);
        final HashMap<String, String> params =
                (HashMap<String, String>) bundle.getSerializable(RequestUtils.PARAMS_KEY);
        if (cache && params != null) {
            String value = lruCache.get(url + params.toString());
            if (value != null) {
                try {
                    callback.onPreExecute();
                    callback.onSuccess(value);
                    callback.onPostExecute();
                } catch (Exception e){
                    e.printStackTrace();
                    callback.onError(0, "Unknown");
                    callback.onPostExecute();
                }
                return null;
            }
        }
        callback.onPreExecute();
        if (Utilities.isNetworkAvailable(context)) {
            int method = bundle.getInt(RequestUtils.METHOD_KEY);
            switch (method) {
                case RequestUtils.METHOD_GET:
                    return executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject response = doGetRequest(url, params);
                                int returnCode = response.getInt("returnCode");
                                String result = response.getJSONObject("data").getString("results");
                                if (returnCode == 1) {
                                    callback.onSuccess(result);
                                    if (cache && params != null) {
                                        lruCache.put(url + params.toString(), result);
                                    }
                                } else {
                                    callback.onError(returnCode, "Error");
                                }
                            } catch (Exception e) {
                                if(Utilities.DEBUG)
                                    Log.e("Request Error", e.getMessage());
                                e.printStackTrace();
                                callback.onError(0, "Unknown error");
                            }
                            callback.onPostExecute();
                        }
                    });
                case RequestUtils.METHOD_POST:
                    return executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject response = doPostRequest(url, params);
                                int returnCode = response.getInt("returnCode");
                                String result = response.getJSONObject("data").getString("results");
                                if (returnCode == 1) {
                                    callback.onSuccess(result);
                                    if (cache && params != null) {
                                        lruCache.put(url + params.toString(), result);
                                    }
                                } else {
                                    callback.onError(returnCode, "Error");
                                }
                            } catch (IOException | JSONException | IllegalStateException | JsonSyntaxException e) {
                                e.printStackTrace();
                                callback.onError(0, "Unknown error");
                            }
                            callback.onPostExecute();
                        }
                    });
                case RequestUtils.METHOD_GET_RAW:
                    return executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String response = doGetRawRequest(url, params);
                                callback.onSuccess(response);
                            } catch (Exception e) {
                                e.printStackTrace();
                                callback.onError(0, "Unknown error");
                            }
                            callback.onPostExecute();
                        }
                    });
                case RequestUtils.METHOD_GET_1:
                    return executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject response = doGetRequest(url, params);
                                int returnCode = response.getInt("returnCode");
                                String result = response.getString("data");
                                if (returnCode == 1) {
                                    callback.onSuccess(result);
                                    if (cache && params != null) {
                                        lruCache.put(url + params.toString(), result);
                                    }
                                } else {
                                    callback.onError(returnCode, "Error");
                                }
                            } catch (Exception e) {
                                if(Utilities.DEBUG)
                                    Log.e("Request Error", e.getMessage());
                                e.printStackTrace();
                                callback.onError(0, "Unknown error");
                            }
                            callback.onPostExecute();
                        }
                    });
            }
        } else {
            callback.onNetworkError();
            callback.onPostExecute();
        }
        return null;
    }

    public Future requestAPIData(final Bundle bundle, final ServiceCallback callback, final boolean cache) {
        final String url = bundle.getString(RequestUtils.URL_KEY);
        final HashMap<String, String> params =
                (HashMap<String, String>) bundle.getSerializable(RequestUtils.PARAMS_KEY);
        if(cache && params != null){
            String value = lruCache.get(url + params.toString());
            if (value != null) {
                callback.onPreExecute();
                callback.onSuccess(value);
                callback.onPostExecute();
                return null;
            }
        }
        callback.onPreExecute();
        if (Utilities.isNetworkAvailable(context)) {
            int method = bundle.getInt(RequestUtils.METHOD_KEY);
            switch (method) {
                case RequestUtils.METHOD_GET:
                    return executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String response = doGetDataRequest(url, params);
                                callback.onSuccess(response);
                                if (cache && params != null) {
                                    lruCache.put(url + params.toString(), response);
                                }
                                callback.onPostExecute();
                            } catch (Exception e) {
                                e.printStackTrace();
                                callback.onError(0, "Unknown error");
                            }
                        }
                    });
                case RequestUtils.METHOD_POST:

            }
        } else {
            callback.onNetworkError();
        }
        return null;
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
        con.setConnectTimeout(TIME_OUT);
        con.setReadTimeout(TIME_OUT);
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
        if(Utilities.DEBUG)
            Log.e("url", url);

        URL toUrl = new URL(url);

        con = (HttpURLConnection) toUrl.openConnection();
        con.setConnectTimeout(TIME_OUT);
        con.setReadTimeout(TIME_OUT);
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
        con.setConnectTimeout(TIME_OUT);
        con.setReadTimeout(TIME_OUT);
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
        con.setConnectTimeout(TIME_OUT);
        con.setReadTimeout(TIME_OUT);
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
