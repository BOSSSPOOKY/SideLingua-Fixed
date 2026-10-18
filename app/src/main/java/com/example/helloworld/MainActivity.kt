
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
            text = "Enable SideLingua Bubble\n(Tap to activate)"
            textSize = 18f
        }
        setContentView(btn)
        btn.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
                Toast.makeText(this, "Allow overlay permission then tap again", Toast.LENGTH_LONG).show()
            } else {
                val serviceIntent = Intent(this, FloatingTranslatorService::class.java)
                startForegroundService(serviceIntent)
                Toast.makeText(this, "SideLingua bubble active! Check your screen edge", Toast.LENGTH_LONG).show()
            }
        }
    }
}
