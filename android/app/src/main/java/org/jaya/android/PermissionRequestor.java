package org.jaya.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by murthy on 12/03/17.
 */

public class PermissionRequestor {

    public static Map<Integer, OnPermissionResultCallback> mPermissionResultCallbackMap = new TreeMap<>();
    public static int mRequestId = 0;
    private static ArrayList<PermissionRequest> mPendingRequests = new ArrayList<>();

    private static int nextRequestId(){
        return ++mRequestId;
    }

    static class PermissionRequest{
        private Activity activity;
        private String permission;
        private OnPermissionResultCallback callback;
        PermissionRequest(Activity a, String p, OnPermissionResultCallback c){
            activity = a;
            permission = p;
            callback = c;
        }

        public String getPermission(){ return permission; }
        public OnPermissionResultCallback getCallback(){ return callback; }

        public Activity getActivity(){ return activity;}
    }
    public static boolean queuePermissionRequest(final Activity activity, final String permission, OnPermissionResultCallback callback) {
        mPendingRequests.add(new PermissionRequest(activity, permission, callback));
        if (mPendingRequests.size() > 1) {
            return false;
        }
        return requestPermissionIfRequired(activity, permission, callback);
    }

    public static boolean requestPermissionIfRequired(final Activity activity, final String permission, OnPermissionResultCallback callback){
        final int requestId = nextRequestId();
        if( callback != null )
            mPermissionResultCallbackMap.put(requestId, callback);
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permission)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showMessageOKCancel(activity, "This app needs permissions: " + permission,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{permission},
                                        requestId);
                            }
                        });

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
            if( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.onRequestPermissionsResult(requestId, new String[]{permission}, new int[]{PackageManager.PERMISSION_GRANTED});
            }
            else{
                dispatchPermissionResult(requestId, new String[]{permission}, new int[]{PackageManager.PERMISSION_GRANTED});
            }
            return true;
        }
    }

    public static void dispatchPermissionResult(int requestId, String[] permissions, int[] grantResults){
        try {
            if (!mPermissionResultCallbackMap.containsKey(requestId))
                return;
            OnPermissionResultCallback callback = mPermissionResultCallbackMap.get(requestId);
            mPermissionResultCallbackMap.remove(requestId);
            if (callback != null) {
                for (int i = 0; i < permissions.length; i++) {
                    callback.onPermissionResult(requestId, permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        if( mPendingRequests.size() == 0 )
            return;
        PermissionRequest pr = mPendingRequests.remove(0);
        try {
            requestPermissionIfRequired(pr.getActivity(), pr.getPermission(), pr.getCallback());
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private static void showMessageOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setCancelable(false)
                .create()
                .show();
    }

    interface OnPermissionResultCallback{
        public void onPermissionResult(int requestId, String permission, boolean bGranted);
    }

}
