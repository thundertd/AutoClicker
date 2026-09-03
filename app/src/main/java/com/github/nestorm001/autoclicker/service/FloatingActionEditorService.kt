package com.github.nestorm001.autoclicker.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.github.nestorm001.autoclicker.R
import com.github.nestorm001.autoclicker.TouchAndDragListener
import com.github.nestorm001.autoclicker.bean.Action
import com.github.nestorm001.autoclicker.bean.ClickPoint
import com.github.nestorm001.autoclicker.bean.TargetType
import com.github.nestorm001.autoclicker.database.ActionDatabaseHelper
import com.github.nestorm001.autoclicker.dp2px
import com.github.nestorm001.autoclicker.logd
import com.github.nestorm001.autoclicker.shortToast
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread

class FloatingActionEditorService : Service() {

    private lateinit var manager: WindowManager
    private lateinit var controlView: View
    private lateinit var controlParams: WindowManager.LayoutParams
    private lateinit var dbHelper: ActionDatabaseHelper
    
    private var actionId: Long = -1
    private var action: Action? = null
    private val targetViews = mutableListOf<TargetView>()
    private val targets = mutableListOf<ClickPoint>()
    private var isRunning = false
    private var isMenuHidden = false
    private var timer: Timer? = null
    
    // For creating swipe - tracks if we're in swipe creation mode
    private var swipeCreationMode = false
    private var swipeStartX = 0
    private var swipeStartY = 0
    
    data class TargetView(
        val startView: View?,
        val endView: View,
        val startParams: WindowManager.LayoutParams?,
        val endParams: WindowManager.LayoutParams
    )
    
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        dbHelper = ActionDatabaseHelper(this)
        manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            actionId = intent?.getLongExtra("ACTION_ID", -1) ?: -1
            "FloatingActionEditorService onStartCommand ACTION_ID: $actionId".logd()
            
            if (actionId == -1L) {
                "No ACTION_ID provided, stopping editor service".logd()
                stopSelf()
                return START_NOT_STICKY
            }
            
            action = dbHelper.getActionById(actionId)
            "Loaded action from DB: ${action?.name}".logd()
            
            if (action == null) {
                "Warning: Action is null for id $actionId, but will still show editor".logd()
                // Still show editor even if action is null - user can add click points
                // Create empty action object for display
                action = Action(id = actionId, name = "Unnamed Action", clickPoints = emptyList())
            }
            
            setupControlView()
            loadExistingTargets()
            
            return START_STICKY
        } catch (e: Exception) {
            e.printStackTrace()
            applicationContext.shortToast("Lỗi khởi tạo: ${e.message}")
            "Exception in FloatingActionEditorService onStartCommand: ${e.message}".logd()
            stopSelf()
            return START_NOT_STICKY
        }
    }

    private fun setupControlView() {
        try {
            controlView = LayoutInflater.from(this).inflate(R.layout.floating_action_control, null)

            val overlayParam = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            controlParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayParam,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )

            // Position on right side of screen
            val displayMetrics = resources.displayMetrics
            controlParams.x = displayMetrics.widthPixels / 2 - dp2px(40f)
            controlParams.y = 0

            manager.addView(controlView, controlParams)
            
            "Displayed floating control view".logd()
            applicationContext.shortToast("Đã hiển thị menu điều khiển")

            val startDragDistance = dp2px(10f)
            controlView.setOnTouchListener(
                TouchAndDragListener(
                    controlParams,
                    startDragDistance,
                    { },
                    { manager.updateViewLayout(controlView, controlParams) }
                )
            )

            setupControlButtons()
        } catch (e: Exception) {
            e.printStackTrace()
            applicationContext.shortToast("Lỗi hiển thị menu: ${e.message}")
            throw e
        }
    }

    private fun setupControlButtons() {
        val btnPlay = controlView.findViewById<ImageButton>(R.id.btnPlay)
        val btnPause = controlView.findViewById<ImageButton>(R.id.btnPause)
        val btnAddClick = controlView.findViewById<ImageButton>(R.id.btnAddClick)
        val btnAddSwipe = controlView.findViewById<ImageButton>(R.id.btnAddSwipe)
        val btnRemove = controlView.findViewById<ImageButton>(R.id.btnRemove)
        val btnSettings = controlView.findViewById<ImageButton>(R.id.btnSettings)
        val btnToggleMenu = controlView.findViewById<ImageButton>(R.id.btnToggleMenu)

        btnPlay.setOnClickListener {
            if (!isRunning) {
                startRunning()
                btnPlay.visibility = View.GONE
                btnPause.visibility = View.VISIBLE
            }
        }

        btnPause.setOnClickListener {
            stopRunning()
            btnPlay.visibility = View.VISIBLE
            btnPause.visibility = View.GONE
        }

        btnAddClick.setOnClickListener {
            addNewClickPoint()
        }

        btnAddSwipe.setOnClickListener {
            if (!swipeCreationMode) {
                startSwipeCreation()
            } else {
                applicationContext.shortToast("Đặt điểm kết thúc vuốt trên màn hình")
            }
        }

        btnRemove.setOnClickListener {
            removeLastTarget()
        }

        btnSettings.setOnClickListener {
            showScenarioSettings()
        }

        btnToggleMenu.setOnClickListener {
            toggleMenuVisibility()
        }
    }

    private fun loadExistingTargets() {
        try {
            "Loading existing targets for action".logd()
            action?.clickPoints?.sortedBy { it.sequence }?.forEach { point ->
                try {
                    "Adding target seq=${point.sequence} type=${point.type} coords=(${point.x},${point.y})".logd()
                    targets.add(point)
                    when (point.type) {
                        TargetType.CLICK -> addClickMarker(point.x, point.y, point.sequence, point)
                        TargetType.SWIPE -> addSwipeMarker(
                            point.fromX, point.fromY,
                            point.toX, point.toY,
                            point.sequence, point
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    "Error adding target seq=${point.sequence}: ${e.message}".logd()
                    // Skip this target if error
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            applicationContext.shortToast("Lỗi tải mục tiêu: ${e.message}")
            "Exception in loadExistingTargets: ${e.message}".logd()
        }
    }

    private fun addNewClickPoint() {
        val sequence = targets.size + 1
        
        // Position marker at center of screen
        val displayMetrics = resources.displayMetrics
        val x = displayMetrics.widthPixels / 2
        val y = displayMetrics.heightPixels / 2
        
        val newPoint = ClickPoint(
            actionId = actionId,
            sequence = sequence,
            type = TargetType.CLICK,
            x = x,
            y = y,
            clickCount = 1,
            delayBefore = 0,
            holdDuration = 100
        )
        
        targets.add(newPoint)
        addClickMarker(x, y, sequence, newPoint)
        
        applicationContext.shortToast("Đã thêm điểm $sequence. Chạm để cấu hình")
    }

    private fun startSwipeCreation() {
        swipeCreationMode = true
        applicationContext.shortToast("Chạm vào màn hình để đặt điểm bắt đầu vuốt")
        
        // Create invisible overlay to catch touch
        val overlayView = View(this)
        
        val overlayParam = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        
        var swipeStart: View? = null
        var swipeStartParams: WindowManager.LayoutParams? = null
        
        overlayView.setOnTouchListener { v, event ->
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()
            
            if (swipeStart == null) {
                // First touch - set start point
                swipeStartX = x
                swipeStartY = y
                
                swipeStart = LayoutInflater.from(this).inflate(R.layout.swipe_start_marker, null)
                swipeStartParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    overlayParam,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT
                )
                swipeStartParams!!.x = x
                swipeStartParams!!.y = y
                manager.addView(swipeStart, swipeStartParams)
                
                applicationContext.shortToast("Đã đặt điểm bắt đầu. Chạm để đặt điểm kết thúc")
            } else {
                // Second touch - set end point and create swipe
                val sequence = targets.size + 1
                
                val newSwipe = ClickPoint(
                    actionId = actionId,
                    sequence = sequence,
                    type = TargetType.SWIPE,
                    fromX = swipeStartX,
                    fromY = swipeStartY,
                    toX = x,
                    toY = y,
                    delayBefore = 0,
                    swipeDuration = 300
                )
                
                targets.add(newSwipe)
                
                // Remove start marker and add full swipe markers
                manager.removeView(swipeStart)
                addSwipeMarker(swipeStartX, swipeStartY, x, y, sequence, newSwipe)
                
                // Remove overlay
                manager.removeView(overlayView)
                swipeCreationMode = false
                
                applicationContext.shortToast("Đã tạo vuốt $sequence. Chạm để cấu hình")
            }
            
            true
        }
        
        manager.addView(overlayView, overlayParams)
    }

    private fun addClickMarker(x: Int, y: Int, sequence: Int, clickPoint: ClickPoint) {
        val markerView = LayoutInflater.from(this).inflate(R.layout.click_point_marker, null)
        val tvSequence = markerView.findViewById<TextView>(R.id.tvSequenceNumber)
        tvSequence.text = sequence.toString()

        val overlayParam = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val markerParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        markerParams.x = x
        markerParams.y = y

        manager.addView(markerView, markerParams)
        targetViews.add(TargetView(null, markerView, null, markerParams))

        val startDragDistance = dp2px(10f)
        markerView.setOnTouchListener(
            TouchAndDragListener(
                markerParams,
                startDragDistance,
                { 
                    // On click - show settings
                    showClickPointSettings(sequence - 1)
                },
                { 
                    manager.updateViewLayout(markerView, markerParams)
                    // Update click point coordinates
                    updateTargetPosition(sequence - 1, markerParams.x, markerParams.y, null, null)
                }
            )
        )
    }

    private fun addSwipeMarker(
        fromX: Int, fromY: Int,
        toX: Int, toY: Int,
        sequence: Int,
        swipePoint: ClickPoint
    ) {
        val overlayParam = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Start marker
        val startView = LayoutInflater.from(this).inflate(R.layout.swipe_start_marker, null)
        val startParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        startParams.x = fromX
        startParams.y = fromY
        manager.addView(startView, startParams)

        // End marker with sequence number
        val endView = LayoutInflater.from(this).inflate(R.layout.swipe_end_marker, null)
        val tvSequence = endView.findViewById<TextView>(R.id.tvSequenceNumber)
        tvSequence.text = sequence.toString()
        
        val endParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        endParams.x = toX
        endParams.y = toY
        manager.addView(endView, endParams)

        targetViews.add(TargetView(startView, endView, startParams, endParams))

        // Make both markers draggable
        val startDragDistance = dp2px(10f)
        
        startView.setOnTouchListener(
            TouchAndDragListener(
                startParams,
                startDragDistance,
                { showSwipeSettings(sequence - 1) },
                { 
                    manager.updateViewLayout(startView, startParams)
                    updateTargetPosition(sequence - 1, null, null, startParams.x, startParams.y)
                }
            )
        )

        endView.setOnTouchListener(
            TouchAndDragListener(
                endParams,
                startDragDistance,
                { showSwipeSettings(sequence - 1) },
                { 
                    manager.updateViewLayout(endView, endParams)
                    updateTargetPosition(sequence - 1, endParams.x, endParams.y, null, null)
                }
            )
        )
    }

    private fun updateTargetPosition(
        index: Int,
        endX: Int?,
        endY: Int?,
        startX: Int?,
        startY: Int?
    ) {
        if (index >= 0 && index < targets.size) {
            val target = targets[index]
            
            when (target.type) {
                TargetType.CLICK -> {
                    if (endX != null && endY != null) {
                        targets[index] = target.copy(x = endX, y = endY)
                    }
                }
                TargetType.SWIPE -> {
                    targets[index] = target.copy(
                        fromX = startX ?: target.fromX,
                        fromY = startY ?: target.fromY,
                        toX = endX ?: target.toX,
                        toY = endY ?: target.toY
                    )
                }
            }
        }
    }

    private fun showClickPointSettings(index: Int) {
        if (index < 0 || index >= targets.size) return
        
        val clickPoint = targets[index]
        if (clickPoint.type != TargetType.CLICK) return
        
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_click_settings, null)
        val etDelayBefore = dialogView.findViewById<EditText>(R.id.etDelayBefore)
        val etHoldDuration = dialogView.findViewById<EditText>(R.id.etHoldDuration)
        val etClickCount = dialogView.findViewById<EditText>(R.id.etClickCount)
        
        etDelayBefore.setText(clickPoint.delayBefore.toString())
        etHoldDuration.setText(clickPoint.holdDuration.toString())
        etClickCount.setText(clickPoint.clickCount.toString())
        
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
            .setView(dialogView)
            .setPositiveButton("Lưu") { dialog, _ ->
                val delayBefore = etDelayBefore.text.toString().toLongOrNull() ?: 0
                val holdDuration = etHoldDuration.text.toString().toLongOrNull() ?: 100
                val clickCount = etClickCount.text.toString().toIntOrNull() ?: 1
                
                targets[index] = clickPoint.copy(
                    delayBefore = delayBefore,
                    holdDuration = holdDuration,
                    clickCount = clickCount
                )
                
                applicationContext.shortToast("Đã cập nhật cài đặt điểm ${index + 1}")
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .create()
            .apply {
                window?.setType(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        WindowManager.LayoutParams.TYPE_PHONE
                    }
                )
            }
            .show()
    }

    private fun showSwipeSettings(index: Int) {
        if (index < 0 || index >= targets.size) return
        
        val swipePoint = targets[index]
        if (swipePoint.type != TargetType.SWIPE) return
        
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_swipe_settings, null)
        val etDelayBefore = dialogView.findViewById<EditText>(R.id.etDelayBefore)
        val etSwipeDuration = dialogView.findViewById<EditText>(R.id.etSwipeDuration)
        
        etDelayBefore.setText(swipePoint.delayBefore.toString())
        etSwipeDuration.setText(swipePoint.swipeDuration.toString())
        
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
            .setView(dialogView)
            .setPositiveButton("Lưu") { dialog, _ ->
                val delayBefore = etDelayBefore.text.toString().toLongOrNull() ?: 0
                val swipeDuration = etSwipeDuration.text.toString().toLongOrNull() ?: 300
                
                targets[index] = swipePoint.copy(
                    delayBefore = delayBefore,
                    swipeDuration = swipeDuration
                )
                
                applicationContext.shortToast("Đã cập nhật cài đặt vuốt ${index + 1}")
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .create()
            .apply {
                window?.setType(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        WindowManager.LayoutParams.TYPE_PHONE
                    }
                )
            }
            .show()
    }

    private fun showScenarioSettings() {
        val currentAction = action ?: return
        
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_scenario_settings, null)
        val etLoopCount = dialogView.findViewById<EditText>(R.id.etLoopCount)
        val etDelayBetweenLoops = dialogView.findViewById<EditText>(R.id.etDelayBetweenLoops)
        
        etLoopCount.setText(currentAction.loopCount.toString())
        etDelayBetweenLoops.setText(currentAction.delayBetweenLoops.toString())
        
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
            .setView(dialogView)
            .setPositiveButton("Lưu & Thoát") { dialog, _ ->
                val loopCount = etLoopCount.text.toString().toIntOrNull() ?: 1
                val delayBetweenLoops = etDelayBetweenLoops.text.toString().toLongOrNull() ?: 1000
                
                action = currentAction.copy(
                    loopCount = loopCount,
                    delayBetweenLoops = delayBetweenLoops
                )
                
                saveAndExit()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .create()
            .apply {
                window?.setType(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        WindowManager.LayoutParams.TYPE_PHONE
                    }
                )
            }
            .show()
    }

    private fun removeLastTarget() {
        if (targets.isEmpty()) {
            applicationContext.shortToast("Không có mục tiêu nào để xóa")
            return
        }
        
        // Remove last target
        targets.removeAt(targets.size - 1)
        
        // Remove views
        val lastView = targetViews.removeAt(targetViews.size - 1)
        lastView.startView?.let { manager.removeView(it) }
        manager.removeView(lastView.endView)
        
        // Renumber remaining targets
        renumberTargets()
        
        applicationContext.shortToast("Đã xóa mục tiêu cuối")
    }

    private fun renumberTargets() {
        targets.forEachIndexed { index, target ->
            targets[index] = target.copy(sequence = index + 1)
            
            // Update displayed sequence number
            val targetView = targetViews[index]
            val tvSequence = targetView.endView.findViewById<TextView>(R.id.tvSequenceNumber)
            tvSequence?.text = (index + 1).toString()
        }
    }

    private fun toggleMenuVisibility() {
        val buttonsContainer = controlView.findViewById<LinearLayout>(R.id.buttonsContainer)
        
        if (isMenuHidden) {
            controlView.findViewById<ImageButton>(R.id.btnPlay).visibility = View.VISIBLE
            controlView.findViewById<ImageButton>(R.id.btnPause).visibility = 
                if (isRunning) View.VISIBLE else View.GONE
            controlView.findViewById<ImageButton>(R.id.btnAddClick).visibility = View.VISIBLE
            controlView.findViewById<ImageButton>(R.id.btnAddSwipe).visibility = View.VISIBLE
            controlView.findViewById<ImageButton>(R.id.btnRemove).visibility = View.VISIBLE
            controlView.findViewById<ImageButton>(R.id.btnSettings).visibility = View.VISIBLE
            isMenuHidden = false
        } else {
            controlView.findViewById<ImageButton>(R.id.btnPlay).visibility = View.GONE
            controlView.findViewById<ImageButton>(R.id.btnPause).visibility = View.GONE
            controlView.findViewById<ImageButton>(R.id.btnAddClick).visibility = View.GONE
            controlView.findViewById<ImageButton>(R.id.btnAddSwipe).visibility = View.GONE
            controlView.findViewById<ImageButton>(R.id.btnRemove).visibility = View.GONE
            controlView.findViewById<ImageButton>(R.id.btnSettings).visibility = View.GONE
            isMenuHidden = true
        }
    }

    private fun startRunning() {
        if (targets.isEmpty()) {
            applicationContext.shortToast("Chưa có hành động nào")
            return
        }

        isRunning = true
        val currentAction = this.action ?: return
        val loopCount = currentAction.loopCount
        
        thread {
            var currentLoop = 0
            
            while (isRunning && (loopCount == 0 || currentLoop < loopCount)) {
                // Execute all targets in sequence
                targets.forEach { target ->
                    if (!isRunning) return@thread
                    
                    // Delay before action
                    if (target.delayBefore > 0) {
                        Thread.sleep(target.delayBefore)
                    }
                    
                    when (target.type) {
                        TargetType.CLICK -> {
                            repeat(target.clickCount) {
                                autoClickService?.clickWithDuration(
                                    target.x,
                                    target.y,
                                    target.holdDuration
                                )
                                if (it < target.clickCount - 1) {
                                    Thread.sleep(50) // Small delay between multiple clicks
                                }
                            }
                        }
                        TargetType.SWIPE -> {
                            autoClickService?.swipe(
                                target.fromX,
                                target.fromY,
                                target.toX,
                                target.toY,
                                target.swipeDuration
                            )
                        }
                    }
                }
                
                currentLoop++
                
                // Delay between loops (if not last loop)
                if (isRunning && (loopCount == 0 || currentLoop < loopCount)) {
                    Thread.sleep(currentAction.delayBetweenLoops)
                }
            }
            
            // Auto stop when done
            if (isRunning && loopCount > 0) {
                stopRunning()
            }
        }
        
        applicationContext.shortToast("Bắt đầu chạy")
    }

    private fun stopRunning() {
        isRunning = false
        applicationContext.shortToast("Đã dừng")
    }

    private fun saveAndExit() {
        // Save all targets to database
        val updatedAction = action?.copy(clickPoints = targets)
        updatedAction?.let {
            dbHelper.updateAction(it)
            applicationContext.shortToast("Đã lưu ${targets.size} hành động")
        }
        
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        "FloatingActionEditorService onDestroy".logd()
        
        stopRunning()
        
        // Remove all views
        try {
            manager.removeView(controlView)
            targetViews.forEach { targetView ->
                targetView.startView?.let { manager.removeView(it) }
                manager.removeView(targetView.endView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        targetViews.clear()
    }
}
