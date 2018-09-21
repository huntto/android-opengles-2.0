package me.huntto.gl.common;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;

public final class GLESChecker {
    public static boolean checkSupportES2(Context context) {
        final ActivityManager activityManager
                = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            final ConfigurationInfo configurationInfo
                    = activityManager.getDeviceConfigurationInfo();

            return configurationInfo.reqGlEsVersion >= 0x20000
                    || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                    && (Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86"))
            );
        }
        return false;
    }

    private GLESChecker() {
    }
}
