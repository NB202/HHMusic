package com.hhmusic.permissions;

public interface PermissionListener {

    void permissionsChanged(String permissionChanged);


    void permissionsGranted(String permissionGranted);


    void permissionsRemoved(String permissionRemoved);
}