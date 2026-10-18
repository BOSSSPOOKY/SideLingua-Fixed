package com.example.helloworld

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat

class FloatingTranslatorService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val notif = NotificationCompat.Builder(this, "sl")
            .setContentTitle("SideLingua Bubble ON")
            .setContentText("Look for red TR bubble on screen edge")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
        startForeground(1, notif)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Create bubble BY CODE - no XML needed, so can't crash
        val bubble = TextView(this).apply {
            text = "TR"
            setBackgroundColor(Color.parseColor("#FF2196F3"))
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(30, 60, 30, 60)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 300

        windowManager.addView(bubble, params)
        floatingView = bubble

        Toast.makeText(this, "Bubble added at 100,300 - drag it!", Toast.LENGTH_LONG).show()

        // Drag + click
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        bubble.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (Math.abs(event.rawX - initialTouchX) > 10 || Math.abs(event.rawY - initialTouchY) > 10) {
                        isClick = false
                    }
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(bubble, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        Toast.makeText(this, "Bubble WORKS! Now we add translator panel", Toast.LENGTH_LONG).show()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel("sl", "SideLingua", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try { windowManager.removeView(floatingView) } catch (_: Exception) {}
    }
}
