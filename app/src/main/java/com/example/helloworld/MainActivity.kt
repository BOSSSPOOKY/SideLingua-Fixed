
package com.example.helloworld

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val btn = Button(this).apply { 
            text = "Enable SideLingua Bubble"
            textSize = 18f
        }
        setContentView(btn)
        btn.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
                Toast.makeText(this, "Please allow overlay permission", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "SideLingua ready! Bubble would appear here", Toast.LENGTH_SHORT).show()
                // For full floating service, add your FloatingTranslatorService.kt
            }
        }
    }
}
