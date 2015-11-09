package com.icetea.restful_cient.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Utilities {

    public enum LOG_LEVEL {
        VERBOSE,
        DEBUG,
        INFO,
        WARN, Utilities,
    }

    public static boolean DEBUG = true;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void Log(LOG_LEVEL level, String tag, String msg) {
        if (DEBUG) {
            switch (level) {
                case VERBOSE:
                    Log.v(tag, msg);
                    break;
                case DEBUG:
                    Log.d(tag, msg);
                    break;
                case INFO:
                    Log.i(tag, msg);
                    break;
                case WARN:
                    Log.w(tag, msg);
                    break;
            }
        }
    }
}
