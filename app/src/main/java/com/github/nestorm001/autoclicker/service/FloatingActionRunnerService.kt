package com.github.nestorm001.autoclicker.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import com.github.nestorm001.autoclicker.R
import com.github.nestorm001.autoclicker.TouchAndDragListener
import com.github.nestorm001.autoclicker.bean.Action
import com.github.nestorm001.autoclicker.database.ActionDatabaseHelper
import com.github.nestorm001.autoclicker.dp2px
import com.github.nestorm001.autoclicker.logd
import com.github.nestorm001.autoclicker.shortToast
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

class FloatingActionRunnerService : Service() {

    private lateinit var manager: WindowManager
    private lateinit var runnerView: View
    private lateinit var runnerParams: WindowManager.LayoutParams
    private lateinit var dbHelper: ActionDatabaseHelper
    
    private lateinit var tvActionInfo: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvClickCount: TextView
    private lateinit var tvDelay: TextView
    private lateinit var btnPlay: ImageButton
    private lateinit var btnStop: ImageButton
    
    private var actionId: Long = -1
    private var action: Action? = null
    private var isRunning = false
    private var timer: Timer? = null
    private var totalClicks = 0
    private var currentPointIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        dbHelper = ActionDatabaseHelper(this)
        manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        actionId = intent?.getLongExtra("ACTION_ID", -1) ?: -1
        
        if (actionId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        action = dbHelper.getActionById(actionId)
        if (action == null) {
            applicationContext.shortToast("Không tìm thấy hành động")
            stopSelf()
            return START_NOT_STICKY
        }
        
        setupRunnerView()
        updateDisplay()
        
        return START_STICKY
    }

    private fun setupRunnerView() {
        runnerView = LayoutInflater.from(this).inflate(R.layout.floating_action_runner, null)

        val overlayParam = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        runnerParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        manager.addView(runnerView, runnerParams)

        val startDragDistance = dp2px(10f)
        runnerView.setOnTouchListener(
            TouchAndDragListener(
                runnerParams,
                startDragDistance,
                { },
                { manager.updateViewLayout(runnerView, runnerParams) }
            )
        )

        initViews()
        setupButtons()
    }

    private fun initViews() {
        tvActionInfo = runnerView.findViewById(R.id.tvActionInfo)
        tvProgress = runnerView.findViewById(R.id.tvProgress)
        tvClickCount = runnerView.findViewById(R.id.tvClickCount)
        tvDelay = runnerView.findViewById(R.id.tvDelay)
        btnPlay = runnerView.findViewById(R.id.btnPlay)
        btnStop = runnerView.findViewById(R.id.btnStop)
    }

    private fun setupButtons() {
        btnPlay.setOnClickListener {
            if (!isRunning) {
                startRunning()
            }
        }

        btnStop.setOnClickListener {
            if (isRunning) {
                stopRunning()
            }
        }

        runnerView.findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            stopSelf()
        }
    }

    private fun updateDisplay() {
        val action = this.action ?: return
        
        handler.post {
            tvActionInfo.text = "Hành động: ${action.name}"
            tvProgress.text = "Điểm: ${currentPointIndex + 1}/${action.clickPoints.size}"
            tvClickCount.text = "Clicks: $totalClicks"
            tvDelay.text = "Delay: ${action.delayBetweenClicks}ms"
        }
    }

    private fun startRunning() {
        val action = this.action ?: return
        
        if (action.clickPoints.isEmpty()) {
            applicationContext.shortToast("Hành động chưa có điểm click nào")
            return
        }

        isRunning = true
        handler.post {
            btnPlay.visibility = View.GONE
            btnStop.visibility = View.VISIBLE
        }
        
        applicationContext.shortToast("Bắt đầu chạy")
        
        // Run in a separate thread
        Thread {
            repeat(action.repeatCount) { repeatIndex ->
                if (!isRunning) return@Thread
                
                action.clickPoints.forEachIndexed { index, point ->
                    if (!isRunning) return@Thread
                    
                    currentPointIndex = index
                    handler.post { updateDisplay() }
                    
                    // Perform clicks for this point
                    repeat(point.clickCount) {
                        if (!isRunning) return@Thread
                        autoClickService?.click(point.x, point.y)
                        totalClicks++
                        handler.post { updateDisplay() }
                        Thread.sleep(action.delayBetweenClicks)
                    }
                    
                    // Delay after this point if specified
                    if (point.delayAfter > 0) {
                        Thread.sleep(point.delayAfter)
                    }
                }
                
                "Completed repeat ${repeatIndex + 1}/${action.repeatCount}".logd()
            }
            
            // Finished all repeats
            if (isRunning) {
                handler.post {
                    applicationContext.shortToast("Hoàn thành! Total clicks: $totalClicks")
                    stopRunning()
                }
            }
        }.start()
    }

    private fun stopRunning() {
        isRunning = false
        handler.post {
            btnPlay.visibility = View.VISIBLE
            btnStop.visibility = View.GONE
        }
        applicationContext.shortToast("Đã dừng")
    }

    override fun onDestroy() {
        super.onDestroy()
        "FloatingActionRunnerService onDestroy".logd()
        
        stopRunning()
        
        try {
            manager.removeView(runnerView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
