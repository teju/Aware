package com.watch.aware.app.callback;

public interface PermissionListener {
    void onCheckPermission(String permission, Boolean isGranted);
    void onPermissionAlreadyGranted();
    void onUserNotGrantedThePermission();
}
