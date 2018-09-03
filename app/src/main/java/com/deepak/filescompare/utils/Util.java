package com.deepak.filescompare.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Util {

    /**
     * Method to check the permission used to access the application
     * @param context	context for the activity
     * @param permission	permission to check the access
     * @return return the true or false
     */
    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Method to check the permission used to request permission
     * @param activity	activity for the UI
     * @param permissionArray	permission array to get access
     * @param permissionIndex	index for the permission
     */
    public static void accessPermission(Activity activity, String[] permissionArray, int permissionIndex) {
        ActivityCompat.requestPermissions(activity,permissionArray, permissionIndex);
    }
}
