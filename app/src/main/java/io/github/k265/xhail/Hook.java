package io.github.k265.xhail;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.k265.xhail.utils.Helpers;

public class Hook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        String packageName = lpparam.packageName;
        Helpers.log("hooking " + packageName);

        if (!lpparam.isFirstApplication) {
            return;
        }

        Helpers.findAndHookMethod(
                Activity.class,
                "startActivity",
                Intent.class,
                Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param == null) {
                            return;
                        }

                        Intent intent = (Intent) param.args[0];
                        if (intent == null) {
                            return;
                        }

                        ComponentName componentName = intent.getComponent();
                        if (componentName == null) {
                            return;
                        }

                        String packageName = componentName.getPackageName();
                        if (HAIL_PACKAGE_NAME.equals(packageName)) {
                            return;
                        }

                        try {
                            PackageManager packageManager = (PackageManager) XposedHelpers.callMethod(param.thisObject, "getPackageManager");
                            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES);
                            if ((applicationInfo.flags & ApplicationInfo.FLAG_SUSPENDED) == 0) {
                                return;
                            }

                            Intent i = new Intent("com.aistra.hail.action.LAUNCH");
                            i.putExtra("package", packageName);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.setComponent(new ComponentName(HAIL_PACKAGE_NAME, "com.aistra.hail.ui.api.ApiActivity"));
                            XposedHelpers.callMethod(param.thisObject, "startActivity", i);
                            param.setResult(null);
                        } catch (Exception e) {
                            Helpers.log(e);
                        }
                    }
                }
        );

    }

    private static final String HAIL_PACKAGE_NAME = "com.aistra.hail";
}
