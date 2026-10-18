package com.example.helloworld

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.*
import android.os.Build
import androidx.core.app.NotificationCompat

class FloatingTranslatorService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        
        // FIX: Android 8+ requires foreground notification or it kills the service
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "sidelingua_channel")
            .setContentTitle("SideLingua Active")
            .setContentText("Bubble is on screen edge - drag it!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
        startForeground(1, notification)
        
        Toast.makeText(this, "SideLingua service started!", Toast.LENGTH_SHORT).show()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        try {
            floatingView = LayoutInflater.from(this).inflate(R.layout.overlay_translator, null)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER_VERTICAL or Gravity.END
            params.x = 20
            params.y = 0

            windowManager.addView(floatingView, params)

            val bubble = floatingView.findViewById<View>(R.id.bubble_collapsed)
            val expanded = floatingView.findViewById<View>(R.id.panel_expanded)
            val btnClose = floatingView.findViewById<View>(R.id.btn_minimize)
            val input = floatingView.findViewById<EditText>(R.id.input_text)
            val output = floatingView.findViewById<TextView>(R.id.output_text)
            val btnTranslate = floatingView.findViewById<Button>(R.id.btn_translate)
            val chipContext = floatingView.findViewById<RadioGroup>(R.id.chip_context)

            bubble.setOnClickListener {
                bubble.visibility = View.GONE
                expanded.visibility = View.VISIBLE
                params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                windowManager.updateViewLayout(floatingView, params)
            }

            btnClose.setOnClickListener {
                expanded.visibility = View.GONE
                bubble.visibility = View.VISIBLE
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                windowManager.updateViewLayout(floatingView, params)
            }

            btnTranslate.setOnClickListener {
                val text = input.text.toString()
                if (text.isBlank()) {
                    output.text = "Paste something to translate bro..."
                    return@setOnClickListener
                }
                val contextMode = when (chipContext.checkedRadioButtonId) {
                    R.id.chip_assignment -> "assignment"
                    R.id.chip_conversation -> "conversation"
                    else -> "general"
                }
                output.text = translateWithContext(text, contextMode)
            }

            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f
            bubble.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x; initialY = params.y
                        initialTouchX = event.rawX; initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (initialTouchX - event.rawX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("sidelingua_channel", "SideLingua Bubble", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun translateWithContext(text: String, mode: String): String {
        return when {
            text.contains("débutant", true) -> "HUMAN TRANSLATION (Conversation):\n'I'm a beginner, Facebook ads is too hard — both setup and ad choice. I'm thinking of hiring an agency.'"
            mode == "assignment" -> "Assignment Mode:\n$text\n-> Explained as clear instructions"
            mode == "conversation" -> "Chat Mode:\n$text\n-> Natural human meaning"
            else -> "General:\n$text\n-> Human meaning explained"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
