package com.clj.fastble.utils;


import android.util.Log;

public final class BleLog {

    public static boolean isDebug = true;
    public static String defaultTag = "WZP_Vicky_BleHelper_";

    public static void d(String msg) {
        if (isDebug && msg != null)
            Log.d(defaultTag, msg);
    }

    public static void i(String msg) {
        if (isDebug && msg != null)
            Log.i(defaultTag, msg);
    }

    public static void w(String msg) {
        if (isDebug && msg != null)
            Log.w(defaultTag, msg);
    }

    public static void e(String msg) {
        if (isDebug && msg != null)
            Log.e(defaultTag, msg);
    }

}
