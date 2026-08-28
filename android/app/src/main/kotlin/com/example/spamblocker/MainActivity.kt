package com.example.spamblocker

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.example.spamblocker/prefs"
    private val ROLE_REQUEST_ID = 1
    private val PERMISSION_REQUEST_ID = 2
    
    private var pendingResult: MethodChannel.Result? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "updatePrefixes" -> {
                    val prefixes = call.argument<String>("prefixes") ?: ""
                    val sharedPref = getSharedPreferences("spam_prefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("blocked_prefixes", prefixes)
                        apply()
                    }
                    result.success(null)
                }
                "isDefaultApp" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                        val isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
                        if (isRoleHeld) {
                            try {
                                ProtectionService.startService(this)
                            } catch (e: Exception) {
                                android.util.Log.e("SpamBlocker", "Failed to start ProtectionService", e)
                            }
                        }
                        result.success(isRoleHeld)
                    } else {
                        result.success(false)
                    }
                }
                "requestDefaultApp" -> {
                    requestRuntimePermissions()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                        if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                            pendingResult = result
                            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                            startActivityForResult(intent, ROLE_REQUEST_ID)
                        } else {
                            try {
                                ProtectionService.startService(this)
                            } catch (e: Exception) {
                                android.util.Log.e("SpamBlocker", "Failed to start ProtectionService", e)
                            }
                            result.success(true)
                        }
                    } else {
                        result.success(false)
                    }
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun requestRuntimePermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CALL_LOG)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_ID)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ROLE_REQUEST_ID) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    ProtectionService.startService(this)
                } catch (e: Exception) {
                    android.util.Log.e("SpamBlocker", "Failed to start ProtectionService", e)
                }
                pendingResult?.success(true)
            } else {
                pendingResult?.success(false)
            }
            pendingResult = null
        }
    }
}
