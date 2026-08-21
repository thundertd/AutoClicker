package com.github.nestorm001.autoclicker.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.app.AlertDialog
import com.github.nestorm001.autoclicker.R
import com.github.nestorm001.autoclicker.TouchAndDragListener
import com.github.nestorm001.autoclicker.bean.ScriptSettings
import com.github.nestorm001.autoclicker.bean.Target
import com.github.nestorm001.autoclicker.dp2px
import com.github.nestorm001.autoclicker.logd
import com.github.nestorm001.autoclicker.shortToast
import com.github.nestorm001.autoclicker.service.autoClickService
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Service quản lý floating control panel và các target points
 */
class FloatingControlService : Service() {
    
    private lateinit var windowManager: WindowManager
    private lateinit var controlPanelView: View
    private lateinit var controlParams: WindowManager.LayoutParams
    
    // Danh sách các target (click points và swipe actions)
    private val targets = mutableListOf<Target>()
    private val targetViews = mutableMapOf<Int, View>()
    
    // ID counter cho targets
    private var nextTargetId = 1
    
    // Trạng thái
    private var isPlaying = false
    private var isControlPanelVisible = true
    
    // Settings
    private var scriptSettings = ScriptSettings()
    
    // Coroutine scope
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // SharedPreferences
    private lateinit var prefs: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "FloatingControlPrefs"
        private const val KEY_TARGETS = "targets"
        private const val KEY_NEXT_ID = "next_id"
        private const val KEY_SETTINGS = "settings"
        private const val KEY_POSITION_X = "position_x"
        private const val KEY_POSITION_Y = "position_y"
    }
    
    // Buttons
    private lateinit var btnPlay: ImageButton
    private lateinit var btnAddPoint: ImageButton
    private lateinit var btnAddSwipe: ImageButton
    private lateinit var btnRemove: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var btnSave: ImageButton
    private lateinit var btnMove: ImageButton
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Tải cấu hình đã lưu
        loadConfiguration()
        
        // Tạo control panel
        createControlPanel()
        
        // Hiển thị các target đã load
        targets.forEach { target ->
            addTargetView(target)
        }
    }
    
    private fun createControlPanel() {
        controlPanelView = LayoutInflater.from(this)
            .inflate(R.layout.floating_control_panel, null)
        
        // Setup window parameters
        val overlayParam = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        
        // Load saved position
        val savedX = prefs.getInt(KEY_POSITION_X, 0)
        val savedY = prefs.getInt(KEY_POSITION_Y, 0)
        
        controlParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }
        
        // Thêm vào window manager
        windowManager.addView(controlPanelView, controlParams)
        
        // Initialize buttons
        initializeButtons()
        
        // Setup touch listener để có thể kéo control panel
        setupDragListener()
    }
    
    private fun initializeButtons() {
        btnPlay = controlPanelView.findViewById(R.id.btnPlay)
        btnAddPoint = controlPanelView.findViewById(R.id.btnAddPoint)
        btnAddSwipe = controlPanelView.findViewById(R.id.btnAddSwipe)
        btnRemove = controlPanelView.findViewById(R.id.btnRemove)
        btnSettings = controlPanelView.findViewById(R.id.btnSettings)
        btnSave = controlPanelView.findViewById(R.id.btnSave)
        btnMove = controlPanelView.findViewById(R.id.btnMove)
        
        // Setup click listeners
        btnPlay.setOnClickListener { onPlayClicked() }
        btnAddPoint.setOnClickListener { onAddPointClicked() }
        btnAddSwipe.setOnClickListener { onAddSwipeClicked() }
        btnRemove.setOnClickListener { onRemoveClicked() }
        btnSettings.setOnClickListener { onSettingsClicked() }
        btnSave.setOnClickListener { onSaveClicked() }
        // Long press nút Move để đóng service, kéo nút Move để di chuyển control panel
        btnMove.setOnLongClickListener {
            onCloseService()
            true
        }
    }
    
    private fun setupDragListener() {
        val startDragDistance = dp2px(10f)
        
        // Áp dụng drag listener cho nút Move để kéo cả control panel
        btnMove.setOnTouchListener(TouchAndDragListener(
            controlParams,
            startDragDistance,
            { onMoveClicked() }, // onClick - toggle ẩn/hiện
            { 
                windowManager.updateViewLayout(controlPanelView, controlParams)
                savePosition()
            }
        ))
    }
    
    private fun onPlayClicked() {
        isPlaying = !isPlaying
        if (isPlaying) {
            btnPlay.setImageResource(android.R.drawable.ic_media_pause)
            shortToast(getString(R.string.play))
            // TODO: Chạy script
            runScript()
        } else {
            btnPlay.setImageResource(android.R.drawable.ic_media_play)
            shortToast(getString(R.string.pause))
            // TODO: Dừng script
        }
    }
    
    private fun onAddPointClicked() {
        // Tạo click point ở giữa màn hình
        val displayMetrics = resources.displayMetrics
        val centerX = displayMetrics.widthPixels / 2
        val centerY = displayMetrics.heightPixels / 2
        
        val clickPoint = Target.ClickPoint(
            id = nextTargetId++,
            position = Point(centerX, centerY)
        )
        
        targets.add(clickPoint)
        addTargetView(clickPoint)
        
        shortToast("Đã thêm điểm ${clickPoint.id}")
        "Added click point ${clickPoint.id} at ($centerX, $centerY)".logd()
    }
    
    private fun onAddSwipeClicked() {
        // Tạo swipe action ở giữa màn hình
        val displayMetrics = resources.displayMetrics
        val centerX = displayMetrics.widthPixels / 2
        val centerY = displayMetrics.heightPixels / 2
        
        val swipeAction = Target.SwipeAction(
            id = nextTargetId++,
            position = Point(centerX + 100, centerY), // Điểm kết thúc
            startPosition = Point(centerX - 100, centerY) // Điểm bắt đầu
        )
        
        targets.add(swipeAction)
        addTargetView(swipeAction)
        
        shortToast("Đã thêm vuốt ${swipeAction.id}")
        "Added swipe action ${swipeAction.id}".logd()
    }
    
    private fun onRemoveClicked() {
        if (targets.isEmpty()) {
            shortToast("Không có điểm nào để xóa")
            return
        }
        
        // Xóa target cuối cùng
        val lastTarget = targets.removeAt(targets.size - 1)
        removeTargetView(lastTarget.id)
        if(nextTargetId > 0)nextTargetId--;
        val targetType = if (lastTarget is Target.ClickPoint) "điểm" else "vuốt"
        shortToast("Đã xóa $targetType ${lastTarget.id}")
        "Removed target ${lastTarget.id}".logd()
    }
    
    private fun onSettingsClicked() {
        // Tạo dialog để hiển thị settings
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null)
        val etRepeatCount = dialogView.findViewById<EditText>(R.id.etRepeatCount)
        val etDelayBetweenActions = dialogView.findViewById<EditText>(R.id.etDelayBetweenActions)
        val etDelayBetweenRepeats = dialogView.findViewById<EditText>(R.id.etDelayBetweenRepeats)
        
        // Set giá trị hiện tại
        etRepeatCount.setText(scriptSettings.repeatCount.toString())
        etDelayBetweenActions.setText(scriptSettings.delayBetweenActions.toString())
        etDelayBetweenRepeats.setText(scriptSettings.delayBetweenRepeats.toString())
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        // Đặt window type để hiển thị trên các app khác
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        }
        
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
            try {
                scriptSettings.repeatCount = etRepeatCount.text.toString().toInt()
                scriptSettings.delayBetweenActions = etDelayBetweenActions.text.toString().toLong()
                scriptSettings.delayBetweenRepeats = etDelayBetweenRepeats.text.toString().toLong()
                shortToast("Đã lưu cài đặt")
                dialog.dismiss()
            } catch (e: Exception) {
                shortToast("Vui lòng nhập số hợp lệ")
            }
        }
        
        dialog.show()
    }
    
    private fun onSaveClicked() {
        saveConfiguration()
        shortToast("Đã lưu cấu hình")
        "Configuration saved".logd()
    }
    
    private fun onMoveClicked() {
        // Toggle ẩn/hiện các button khác, chỉ giữ lại nút Move
        isControlPanelVisible = !isControlPanelVisible
        
        btnPlay.visibility = if (isControlPanelVisible) View.VISIBLE else View.GONE
        btnAddPoint.visibility = if (isControlPanelVisible) View.VISIBLE else View.GONE
        btnAddSwipe.visibility = if (isControlPanelVisible) View.VISIBLE else View.GONE
        btnRemove.visibility = if (isControlPanelVisible) View.VISIBLE else View.GONE
        btnSettings.visibility = if (isControlPanelVisible) View.VISIBLE else View.GONE
        btnSave.visibility = if (isControlPanelVisible) View.VISIBLE else View.GONE
        
        val message = if (isControlPanelVisible) {
            "Đã hiện menu"
        } else {
            "Đã ẩn menu (Giữ lâu để đóng)"
        }
        shortToast(message)
    }
    
    private fun onCloseService() {
        shortToast("Đang đóng floating control...")
        stopSelf()
    }
    
    private fun addTargetView(target: Target) {
        val targetView: View
        val params: WindowManager.LayoutParams
        
        when (target) {
            is Target.ClickPoint -> {
                // Tạo view cho click point
                targetView = LayoutInflater.from(this)
                    .inflate(R.layout.floating_target_point, null)
                
                val tvNumber = targetView.findViewById<TextView>(R.id.tvNumber)
                tvNumber.text = target.id.toString()
                
                params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    },
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = target.position.x - 30 // Center the view
                    y = target.position.y - 30
                }
            }
            
            is Target.SwipeAction -> {
                // Tạo view cho swipe action
                targetView = LayoutInflater.from(this)
                    .inflate(R.layout.floating_swipe_action, null)
                
                val tvNumber = targetView.findViewById<TextView>(R.id.tvSwipeNumber)
                tvNumber.text = target.id.toString()
                
                params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    },
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = target.startPosition.x
                    y = target.startPosition.y - 50
                }
            }
        }
        
        // Cho phép kéo thả target
        setupTargetDragListener(targetView, params, target)
        
        // Thêm vào window manager
        windowManager.addView(targetView, params)
        targetViews[target.id] = targetView
    }
    
    private fun setupTargetDragListener(
        view: View,
        params: WindowManager.LayoutParams,
        target: Target
    ) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(view, params)
                    
                    // Cập nhật vị trí trong target
                    when (target) {
                        is Target.ClickPoint -> {
                            target.position.x = params.x + 30
                            target.position.y = params.y + 30
                        }
                        is Target.SwipeAction -> {
                            val deltaX = params.x + 30 - target.position.x
                            val deltaY = params.y + 50 - target.position.y
                            target.position.x += deltaX
                            target.position.y += deltaY
                            target.startPosition.x += deltaX
                            target.startPosition.y += deltaY
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }
    
    private fun removeTargetView(targetId: Int) {
        targetViews[targetId]?.let { view ->
            windowManager.removeView(view)
            targetViews.remove(targetId)
        }
    }
    
    private fun runScript() {
        if (targets.isEmpty()) {
            shortToast("Chưa có điểm nào để chạy")
            btnPlay.setImageResource(android.R.drawable.ic_media_play)
            return
        }
        
        // Chạy script trong coroutine
        serviceScope.launch {
            val repeatCount = scriptSettings.repeatCount
            var currentRepeat = 0
            
            while (isPlaying && (repeatCount == 0 || currentRepeat < repeatCount)) {
                // Chạy tất cả targets
                for (target in targets) {
                    if (!isPlaying) break
                    
                    when (target) {
                        is Target.ClickPoint -> {
                            autoClickService?.click(target.position.x, target.position.y)
                            "Clicking at (${target.position.x}, ${target.position.y})".logd()
                        }
                        is Target.SwipeAction -> {
                            autoClickService?.swipe(
                                target.startPosition.x,
                                target.startPosition.y,
                                target.position.x,
                                target.position.y
                            )
                            "Swiping from (${target.startPosition.x}, ${target.startPosition.y}) to (${target.position.x}, ${target.position.y})".logd()
                        }
                    }
                    
                    // Delay giữa các action
                    delay(scriptSettings.delayBetweenActions)
                }
                
                currentRepeat++
                
                // Delay giữa các lần lặp (nếu còn lặp)
                if (isPlaying && (repeatCount == 0 || currentRepeat < repeatCount)) {
                    delay(scriptSettings.delayBetweenRepeats)
                }
            }
            
            // Kết thúc
            if (isPlaying) {
                isPlaying = false
                btnPlay.setImageResource(android.R.drawable.ic_media_play)
                shortToast("Đã hoàn thành kịch bản")
            }
        }
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        "FloatingControlService onTaskRemoved".logd()
        
        // Dọn dẹp và dừng service
        stopSelf()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        "FloatingControlService onDestroy".logd()
        
        // Cancel coroutines
        serviceScope.cancel()
        
        // Dừng script nếu đang chạy
        isPlaying = false
        
        // Xóa tất cả target views
        try {
            targetViews.values.forEach { view ->
                try {
                    if (view.parent != null) {
                        windowManager.removeView(view)
                    }
                } catch (e: Exception) {
                    "Error removing target view: ${e.message}".logd()
                }
            }
            targetViews.clear()
        } catch (e: Exception) {
            "Error clearing target views: ${e.message}".logd()
        }
        
        // Xóa control panel
        try {
            if (::controlPanelView.isInitialized && controlPanelView.parent != null) {
                windowManager.removeView(controlPanelView)
            }
        } catch (e: Exception) {
            "Error removing control panel: ${e.message}".logd()
        }
    }
    
    private fun savePosition() {
        prefs.edit()
            .putInt(KEY_POSITION_X, controlParams.x)
            .putInt(KEY_POSITION_Y, controlParams.y)
            .apply()
    }
    
    private fun saveConfiguration() {
        try {
            val editor = prefs.edit()
            
            // Lưu targets
            val targetsJson = JSONArray()
            targets.forEach { target ->
                val targetJson = JSONObject()
                targetJson.put("id", target.id)
                when (target) {
                    is Target.ClickPoint -> {
                        targetJson.put("type", "click")
                        targetJson.put("x", target.position.x)
                        targetJson.put("y", target.position.y)
                    }
                    is Target.SwipeAction -> {
                        targetJson.put("type", "swipe")
                        targetJson.put("startX", target.startPosition.x)
                        targetJson.put("startY", target.startPosition.y)
                        targetJson.put("endX", target.position.x)
                        targetJson.put("endY", target.position.y)
                    }
                }
                targetsJson.put(targetJson)
            }
            editor.putString(KEY_TARGETS, targetsJson.toString())
            
            // Lưu next ID
            editor.putInt(KEY_NEXT_ID, nextTargetId)
            
            // Lưu settings
            val settingsJson = JSONObject()
            settingsJson.put("repeatCount", scriptSettings.repeatCount)
            settingsJson.put("delayBetweenActions", scriptSettings.delayBetweenActions)
            settingsJson.put("delayBetweenRepeats", scriptSettings.delayBetweenRepeats)
            editor.putString(KEY_SETTINGS, settingsJson.toString())
            
            editor.apply()
        } catch (e: Exception) {
            "Error saving configuration: ${e.message}".logd()
        }
    }
    
    private fun loadConfiguration() {
        try {
            // Load targets
            val targetsStr = prefs.getString(KEY_TARGETS, null)
            if (targetsStr != null) {
                val targetsJson = JSONArray(targetsStr)
                for (i in 0 until targetsJson.length()) {
                    val targetJson = targetsJson.getJSONObject(i)
                    val id = targetJson.getInt("id")
                    val type = targetJson.getString("type")
                    
                    val target = when (type) {
                        "click" -> Target.ClickPoint(
                            id = id,
                            position = Point(
                                targetJson.getInt("x"),
                                targetJson.getInt("y")
                            )
                        )
                        "swipe" -> Target.SwipeAction(
                            id = id,
                            position = Point(
                                targetJson.getInt("endX"),
                                targetJson.getInt("endY")
                            ),
                            startPosition = Point(
                                targetJson.getInt("startX"),
                                targetJson.getInt("startY")
                            )
                        )
                        else -> null
                    }
                    
                    if (target != null) {
                        targets.add(target)
                    }
                }
            }
            
            // Load next ID
            nextTargetId = prefs.getInt(KEY_NEXT_ID, 1)
            
            // Load settings
            val settingsStr = prefs.getString(KEY_SETTINGS, null)
            if (settingsStr != null) {
                val settingsJson = JSONObject(settingsStr)
                scriptSettings.repeatCount = settingsJson.getInt("repeatCount")
                scriptSettings.delayBetweenActions = settingsJson.getLong("delayBetweenActions")
                scriptSettings.delayBetweenRepeats = settingsJson.getLong("delayBetweenRepeats")
            }
            
            "Configuration loaded: ${targets.size} targets".logd()
        } catch (e: Exception) {
            "Error loading configuration: ${e.message}".logd()
        }
    }
}
