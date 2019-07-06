package com.hhmusic.permissions;

public interface PermissionCallback {
    void permissionGranted();

    void permissionRefused();
}