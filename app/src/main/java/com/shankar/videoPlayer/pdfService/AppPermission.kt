package com.shankar.videoPlayer.pdfService

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shankar.videoPlayer.MainActivity


class AppPermission {
    companion object {
        const val REQUEST_PERMISSION = 123
        val permissions =
            arrayOf(
                INTERNET
            )
        fun permissionGranted(context: Context): Boolean {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                    return false // If any permission is not granted, return false immediately
                }
            }
            return true // All permissions are granted
        }


        fun requestPermission(activity: MainActivity) {
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                REQUEST_PERMISSION
            )
        }
    }
}