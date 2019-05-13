package com.e.dudusgram.Utils;


import android.Manifest;

public class Permissions {

    public static final String[] PERMISSIONS = {
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    };

    public static final String CAMERA_PERMISSION =
            Manifest.permission.CAMERA;

}
