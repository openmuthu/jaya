package org.jaya.android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by murthy on 12/03/17.
 */

public class PermissionRequestor {

    public static final int WRITE_EXTERNAL_STORAGE_PERMISSSION_REQUEST_ID = 1;
    public static final int INTERNET_PERMISSSION_REQUEST_ID = 2;


    public static boolean requestPermissionIfRequired(Activity activity, String permission, int requestId){
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permission)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(activity,
                        new String[]{permission},
                        requestId);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false;
        }
        else {
            activity.onRequestPermissionsResult(requestId, new String[]{permission}, new int[]{1});
            return true;
        }
    }

}
