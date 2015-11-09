package com.icetea.restful_cient.api;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.icetea.restful_cient.utilities.CacheManager;
import com.icetea.restful_cient.utilities.Method;
import com.icetea.restful_cient.utilities.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by icetea on 10/7/15.
 */
public class ApiLoader extends AsyncTaskLoader<String> {

    private int connectTimeout = 30000;
    private int readTimeout = 30000;

    private Context context;
    private Method method;
    private HashMap<String, String> params;
    private HashMap<String, String> headers;
    private String url;
    private boolean cache;
    private boolean dataIsReady;
    private String data;
    private boolean enableHttps;

    public ApiLoader(Context context, Method method, String url) {
        super(context);
        this.context = context;
        this.method = method;
        this.url = url;
    }

    public ApiLoader putParam(String key, String value){
        if(params == null){
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }

    public ApiLoader removeParam(String key){
        if(params != null){
            params.remove(key);
        }
        return this;
    }

    public ApiLoader putHeader(String key, String value){
        if(headers == null){
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }

    public ApiLoader removeHeader(String key){
        if(headers != null){
            headers.remove(key);
        }
        return this;
    }

    public ApiLoader setCache(boolean cache){
        this.cache = cache;
        return this;
    }

    public ApiLoader setConnectTimeout(int connectTimeout){
        this.connectTimeout = connectTimeout;
        return this;
    }

    public ApiLoader setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public ApiLoader setEnableHttps(boolean enable){
        enableHttps = enable;
        return this;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (dataIsReady) {
            deliverResult(data);
        } else {
            forceLoad();
        }
    }


    @Override
    public String loadInBackground() {
        data = null;
        dataIsReady = false;
        if (cache && params != null) {
            data = CacheManager.getInstance().get(url + params.hashCode());
            if (data != null)
                return data;
        }
        if (Utilities.isNetworkAvailable(context)) {
            switch (method) {
                case GET:
                    try {
                        data = doGetRequest();
                        if(cache){
                            CacheManager.getInstance().put(url + params.hashCode(), data);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case POST:
                    try {
                        data = doPostRequest();
                        if(cache){
                            CacheManager.getInstance().put(url + params.hashCode(), data);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        } else {
            data = ApiCallback.NETWORK_ERROR_INDICATOR;
        }
        dataIsReady = true;
        return data;
    }

    @Override
    public void deliverResult(String data) {
        super.deliverResult(data);
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

    private String doPostRequest() throws
            IOException {

        HttpURLConnection con;
        int postDataLength = -1;
        byte[] postData = null;
        if(params != null) {
            StringBuilder postParam = new StringBuilder();
            for (String key : params.keySet()) {
                postParam.append(key).append("=").append(params.get(key)).append("&");
            }
            String urlParameters = postParam.toString();

            postData = urlParameters.getBytes(Charset.forName("UTF-8"));
            postDataLength = postData.length;
        }

        URL toUrl = new URL(url);

        if(enableHttps){
            TrustManager[] trustManager = new TrustManager[] {new TrustEverythingTrustManager()};

            // Let us create the factory where we can set some parameters for the connection
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustManager, new java.security.SecureRandom());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                // do nothing
            }

            if (sslContext != null) {
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                con = (HttpURLConnection) toUrl.openConnection();
                ((HttpsURLConnection)con).setHostnameVerifier(new VerifyEverythingHostnameVerifier());
            }
            else {
                return null;
            }
        } else {
            con = (HttpURLConnection) toUrl.openConnection();
        }

        con.setConnectTimeout(connectTimeout);
        con.setReadTimeout(readTimeout);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("charset", "utf-8");
        if(headers != null){
            for(String key : headers.keySet()){
                con.setRequestProperty(key, headers.get(key));
            }
        }
        if(postDataLength != -1) {
            con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        }

        OutputStream wr = con.getOutputStream();
        if(postData != null)
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

        return response.toString();
    }

    public String doGetRequest() throws
            IOException {
        HttpURLConnection con;
        Uri.Builder builder = Uri.parse(url)
                .buildUpon();
        if(params != null) {
            for (String key : params.keySet()) {
                builder.appendQueryParameter(key, params.get(key));
            }
        }
        url = builder.build().toString();
        Utilities.Log(Utilities.LOG_LEVEL.DEBUG, "url", url);

        URL toUrl = new URL(url);


        if(enableHttps){
            TrustManager[] trustManager = new TrustManager[] {new TrustEverythingTrustManager()};

            // Let us create the factory where we can set some parameters for the connection
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustManager, new java.security.SecureRandom());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                // do nothing
            }

            if (sslContext != null) {
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                con = (HttpURLConnection) toUrl.openConnection();
                ((HttpsURLConnection)con).setHostnameVerifier(new VerifyEverythingHostnameVerifier());
            }
            else {
                return null;
            }
        } else {
            con = (HttpURLConnection) toUrl.openConnection();
        }

        con.setConnectTimeout(connectTimeout);
        con.setReadTimeout(readTimeout);
        con.setRequestMethod("GET");
        if(headers != null){
            for(String key : headers.keySet()){
                con.setRequestProperty(key, headers.get(key));
            }
        }
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



    public class TrustEverythingTrustManager implements X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {   }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {   }
    }

    public class VerifyEverythingHostnameVerifier implements HostnameVerifier {

        public boolean verify(String string, SSLSession sslSession) {
            return true;
        }
    }
}
