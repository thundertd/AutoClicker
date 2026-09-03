package com.github.nestorm001.autoclicker

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.nestorm001.autoclicker.database.ActionDatabaseHelper
import com.github.nestorm001.autoclicker.service.FloatingActionEditorService

class ActionDetailActivity : AppCompatActivity() {

    private var actionId: Long = -1
    private lateinit var dbHelper: ActionDatabaseHelper
    private val OVERLAY_PERMISSION_REQUEST_CODE = 1001
    private val ACCESSIBILITY_PERMISSION_REQUEST_CODE = 1002
    private var editorStarted = false
    private var needsAccessibility = false
    private var needsOverlay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_detail)

        "ActionDetailActivity onCreate - intent extras: ${intent.extras}".logd()

        try {
            actionId = intent.getLongExtra("ACTION_ID", -1)
            "Received ACTION_ID: $actionId (type: Long)".logd()

            if (actionId == -1L) {
                "ERROR: ACTION_ID is -1 (not found in intent)".logd()
                shortToast("Lỗi: Không tìm thấy hành động")
                finish()
                return
            }

            dbHelper = ActionDatabaseHelper(this)
            val action = dbHelper.getActionById(actionId)

            "Query DB for id=$actionId, result: ${action?.name} (clickPoints: ${action?.clickPoints?.size})".logd()

            if (action == null) {
                "ERROR: Action is null from DB query for id=$actionId".logd()
                shortToast("Lỗi: Không tìm thấy hành động trong database")
                finish()
                return
            }

            title = action.name
            
            // Don't close activity, just check permissions
            "Action found, checking permissions...".logd()
            checkRequiredPermissions()
        } catch (e: Exception) {
            e.printStackTrace()
            "Exception in onCreate: ${e.message}".logd()
            e.message?.let { shortToast("Lỗi khởi tạo: $it") }
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // If returned from permission screens, try to start editor again
        if (actionId != -1L && !editorStarted) {
            "onResume: checking permissions again".logd()
            checkRequiredPermissions()
        }
    }

    private fun checkRequiredPermissions() {
        val hasAccessibility = hasAccessibilityPermission()
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }

        "checkRequiredPermissions: hasAccessibility=$hasAccessibility, hasOverlay=$hasOverlay".logd()

        when {
            !hasAccessibility -> {
                "Missing Accessibility permission".logd()
                needsAccessibility = true
                showAccessibilityPermissionDialog()
            }
            !hasOverlay -> {
                "Missing Overlay permission".logd()
                needsOverlay = true
                requestOverlayPermission()
            }
            else -> {
                "All permissions granted, starting editor".logd()
                startEditorService()
            }
        }
    }

    private fun hasAccessibilityPermission(): Boolean {
        val serviceId = getString(R.string.accessibility_service_id)
        val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)

        return enabledServices.any { serviceId == it.id }
    }

    private fun showAccessibilityPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Yêu cầu quyền Accessibility")
            .setMessage(
                "Ứng dụng cần quyền Accessibility để thực hiện thao tác tự động trên màn hình. " +
                    "Bật quyền ngay để sử dụng chức năng chỉnh sửa hành động."
            )
            .setCancelable(false)
            .setPositiveButton("Mở cài đặt") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Hủy") { _, _ ->
                shortToast("Bạn cần bật Accessibility để mở trình chỉnh sửa")
                finish()
            }
            .show()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        shortToast("Vui lòng cấp quyền hiển thị trên các ứng dụng khác")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        "onActivityResult requestCode: $requestCode".logd()
        
        when (requestCode) {
            OVERLAY_PERMISSION_REQUEST_CODE -> {
                "Returned from Overlay permission dialog".logd()
                checkRequiredPermissions()
            }
            ACCESSIBILITY_PERMISSION_REQUEST_CODE -> {
                "Returned from Accessibility permission dialog".logd()
                checkRequiredPermissions()
            }
        }
    }

    private fun startEditorService() {
        if (editorStarted) {
            "ERROR: startEditorService called but editorStarted already true".logd()
            return
        }

        try {
            "Starting FloatingActionEditorService for ACTION_ID: $actionId".logd()
            shortToast("Đang mở màn hình chỉnh sửa hành động…")

            val serviceIntent = Intent(this, FloatingActionEditorService::class.java)
            serviceIntent.putExtra("ACTION_ID", actionId)
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            
            "About to call startService()".logd()
            val componentName = startService(serviceIntent)
            "startService() returned: $componentName".logd()

            editorStarted = true
            shortToast("Đã mở trình chỉnh sửa floating")

            "Moving activity to background".logd()
            moveTaskToBack(true)
            
            "About to call finish()".logd()
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            "ERROR in startEditorService: ${e.message}".logd()
            shortToast("Lỗi khởi động editor: ${e.message}")
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Do not stop the floating editor service here. The overlay must remain visible
        // while the user configures click/swipe points.
        "ActionDetailActivity onDestroy".logd()
    }
}
