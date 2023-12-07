package io.github.k265.xhail.utils;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Helpers {
    private static final String TAG = "io.github.k265.xhail";

    public static void log(String text) {
        Log.i(TAG, text);
    }

    public static void log(Throwable t) {
        XposedBridge.log(t);
    }

    public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clz, String methodName, Object... parameterTypesAndCallback) {
        try {
            return XposedHelpers.findAndHookMethod(clz, methodName, parameterTypesAndCallback);
        } catch (Throwable t) {
            log("failed to hook " + clz + "." + methodName);
            log(t);
            return null;
        }
    }

    public static XC_MethodHook.Unhook findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        Class<?> clz = XposedHelpers.findClassIfExists(className, classLoader);
        if (clz == null) {
            log(className + " not found, skip");
            return null;
        }
        return findAndHookMethod(clz, methodName, parameterTypesAndCallback);
    }

}
