package com.example.helloworld

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val NOTIF_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val btn = Button(this).apply { 
            text = "ENABLE SIDELINGUA BUBBLE"
            textSize = 20f
            setPadding(40, 200, 40, 200)
        }
        setContentView(btn)
        
        btn.setOnClickListener {
            // Step 1: Overlay permission
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
                Toast.makeText(this, "Step 1: Allow 'Display over other apps' then come back and tap again", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            // Step 2: Notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIF_CODE)
                    Toast.makeText(this, "Step 2: Allow notifications, then tap again", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            
            // Step 3: Start service
            try {
                val serviceIntent = Intent(this, FloatingTranslatorService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                Toast.makeText(this, "SUCCESS! Bubble should appear now - check top-left edge, drag it!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIF_CODE) {
            Toast.makeText(this, "Now tap ENABLE BUBBLE again!", Toast.LENGTH_LONG).show()
        }
    }
}
