package com.github.nestorm001.autoclicker

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.nestorm001.autoclicker.bean.Action
import com.github.nestorm001.autoclicker.database.ActionDatabaseHelper
import com.github.nestorm001.autoclicker.service.FloatingActionRunnerService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import java.io.File
import java.io.FileWriter

class ActionListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActionListAdapter
    private lateinit var dbHelper: ActionDatabaseHelper
    private lateinit var tvSelectionInfo: TextView
    private lateinit var btnExportSelected: Button
    private lateinit var btnCombineActions: Button
    private lateinit var btnRunSequence: Button
    
    private var actions = mutableListOf<Action>()
    private val selectedActions = mutableSetOf<Long>()
    private var isSelectionMode = false
    private var permissionDialogShown = false
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_list)
        
        title = "Danh sách hành động"
        
        dbHelper = ActionDatabaseHelper(this)
        
        tvSelectionInfo = findViewById(R.id.tvSelectionInfo)
        btnExportSelected = findViewById(R.id.btnExportSelected)
        btnCombineActions = findViewById(R.id.btnCombineActions)
        btnRunSequence = findViewById(R.id.btnRunSequence)
        
        recyclerView = findViewById(R.id.recyclerActions)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = ActionListAdapter(
            actions = actions,
            selectedActions = selectedActions,
            isSelectionMode = { isSelectionMode },
            onDetailClick = { action -> openActionDetail(action) },
            onExportClick = { action -> exportSingleAction(action) },
            onRunClick = { action -> runSingleAction(action) },
            onLongClick = { action -> 
                if (!isSelectionMode) {
                    enterSelectionMode(action)
                }
            },
            onSelectionChanged = { updateSelectionInfo() }
        )
        
        recyclerView.adapter = adapter
        
        findViewById<FloatingActionButton>(R.id.fabAddAction).setOnClickListener {
            createNewAction()
        }
        
        btnExportSelected.setOnClickListener {
            if (isSelectionMode) {
                checkStoragePermissionAndExport()
            }
        }
        
        btnCombineActions.setOnClickListener {
            // TODO: Implement action combination screen
            shortToast("Tính năng đang phát triển")
        }
        
        btnRunSequence.setOnClickListener {
            // TODO: Implement sequence runner
            shortToast("Tính năng đang phát triển")
        }
        
        loadActions()
    }
    
    private fun checkStoragePermissionAndExport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                exportSelectedActions()
            }
        } else {
            exportSelectedActions()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportSelectedActions()
            } else {
                shortToast("Cần quyền truy cập bộ nhớ để xuất file")
            }
        }
    }
    
    private fun loadActions() {
        actions.clear()
        actions.addAll(dbHelper.getAllActions())
        adapter.notifyDataSetChanged()
    }
    
    private fun createNewAction() {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
        val editText = EditText(this).apply {
            hint = "Tên hành động"
        }
        
        AlertDialog.Builder(this)
            .setTitle("Tạo hành động mới")
            .setView(editText)
            .setPositiveButton("Tạo") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    "Creating new action with name: $name".logd()
                    val action = Action(
                        name = name,
                        clickPoints = emptyList()
                    )
                    "About to insert action into DB".logd()
                    val actionId = dbHelper.insertAction(action)
                    "insertAction() returned id: $actionId".logd()
                    
                    if (actionId <= 0) {
                        "ERROR: insertAction returned invalid id: $actionId".logd()
                        shortToast("Lỗi: Không thể lưu hành động")
                        return@setPositiveButton
                    }
                    
                    "Calling loadActions()".logd()
                    loadActions()
                    
                    "About to fetch action from DB with id=$actionId".logd()
                    val newAction = dbHelper.getActionById(actionId)
                    "getActionById returned: ${newAction?.name} (id=${newAction?.id})".logd()
                    
                    if (newAction == null) {
                        "ERROR: getActionById($actionId) returned null".logd()
                        shortToast("Lỗi: Không thể lấy hành động từ database")
                        return@setPositiveButton
                    }
                    
                    "About to call openActionDetail()".logd()
                    openActionDetail(newAction)
                } else {
                    shortToast("Vui lòng nhập tên hành động")
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun openActionDetail(action: Action) {
        "openActionDetail called for action: id=${action.id}, name=${action.name}".logd()
        shortToast("Đang mở chi tiết hành động: ${action.name}")
        val intent = Intent(this, ActionDetailActivity::class.java)
        intent.putExtra("ACTION_ID", action.id)
        "Starting ActionDetailActivity with ACTION_ID=${action.id}".logd()
        startActivity(intent)
    }
    
    private fun exportSingleAction(action: Action) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
                return
            }
        }
        
        try {
            val json = dbHelper.exportActionsToJson(listOf(action.id))
            val fileName = "${action.name}_${System.currentTimeMillis()}.json"
            saveJsonToFile(json, fileName)
            shortToast("Đã xuất: $fileName")
        } catch (e: Exception) {
            shortToast("Lỗi xuất file: ${e.message}")
        }
    }
    
    private fun exportSelectedActions() {
        if (selectedActions.isEmpty()) {
            shortToast("Chưa chọn hành động nào")
            return
        }
        
        try {
            val json = dbHelper.exportActionsToJson(selectedActions.toList())
            val fileName = "actions_${System.currentTimeMillis()}.json"
            saveJsonToFile(json, fileName)
            shortToast("Đã xuất ${selectedActions.size} hành động")
            exitSelectionMode()
        } catch (e: Exception) {
            shortToast("Lỗi xuất file: ${e.message}")
        }
    }
    
    private fun saveJsonToFile(json: String, fileName: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        FileWriter(file).use { it.write(json) }
    }
    
    private fun runSingleAction(action: Action) {
        if (action.clickPoints.isEmpty()) {
            shortToast("Hành động chưa có điểm click nào")
            return
        }
        
        val intent = Intent(this, FloatingActionRunnerService::class.java)
        intent.putExtra("ACTION_ID", action.id)
        startService(intent)
        shortToast("Bắt đầu chạy: ${action.name}")
    }
    
    private fun enterSelectionMode(firstAction: Action) {
        isSelectionMode = true
        selectedActions.clear()
        selectedActions.add(firstAction.id)
        tvSelectionInfo.visibility = View.VISIBLE
        updateSelectionInfo()
        adapter.notifyDataSetChanged()
    }
    
    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedActions.clear()
        tvSelectionInfo.visibility = View.GONE
        adapter.notifyDataSetChanged()
    }
    
    private fun updateSelectionInfo() {
        tvSelectionInfo.text = "Đã chọn: ${selectedActions.size}"
    }
    
    private fun checkAccessibilityPermission(): Boolean {
        val serviceId = getString(R.string.accessibility_service_id)
        val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        
        for (service in enabledServices) {
            if (serviceId == service.id) {
                return true
            }
        }
        return false
    }
    
    private fun showPermissionDialog() {
        if (permissionDialogShown) return
        permissionDialogShown = true
        
        AlertDialog.Builder(this)
            .setTitle("Yêu cầu cấp quyền")
            .setMessage(
                "Ứng dụng cần quyền Dịch vụ Hỗ trợ (Accessibility Service) để thực hiện thao tác tự động giả lập chạm (click) hoặc vuốt (swipe) trên màn hình.\n\n" +
                "Cam kết bảo mật: Nhà phát triển không thu thập dữ liệu cá nhân thông qua quyền này."
            )
            .setCancelable(false)
            .setNegativeButton("HỦY") { dialog, _ ->
                dialog.dismiss()
                shortToast("Ứng dụng cần quyền Accessibility để hoạt động")
            }
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNeutralButton("Xem hướng dẫn") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, GuideActivity::class.java)
                startActivity(intent)
            }
            .show()
    }
    override fun onResume() {
        super.onResume()
        loadActions()
        
        // Check all required permissions at app startup
        checkAndRequestAllPermissions()
    }
    
    private fun checkAndRequestAllPermissions() {
        val hasAccessibility = checkAccessibilityPermission()
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
        
        when {
            !hasAccessibility -> {
                "Missing Accessibility Service permission".logd()
                showPermissionDialog()
            }
            !hasOverlay -> {
                "Missing Overlay permission, requesting...".logd()
                requestOverlayPermission()
            }
            else -> {
                "All permissions granted".logd()
                permissionDialogShown = false
            }
        }
    }
    
    @TargetApi(Build.VERSION_CODES.M)
    private fun requestOverlayPermission() {
        AlertDialog.Builder(this)
            .setTitle("Yêu cầu quyền Overlay")
            .setMessage("Ứng dụng cần quyền hiển thị trên các ứng dụng khác để hiển thị công cụ điều khiển nổi khi chỉnh sửa hành động.")
            .setCancelable(false)
            .setPositiveButton("Mở cài đặt") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
            .setNegativeButton("Bỏ qua") { _, _ ->
                shortToast("Bạn có thể cấp quyền overlay sau trong Cài đặt ứng dụng")
            }
            .show()
    }
    
    override fun onBackPressed() {
        if (isSelectionMode) {
            exitSelectionMode()
        } else {
            moveTaskToBack(true)
        }
    }
}
