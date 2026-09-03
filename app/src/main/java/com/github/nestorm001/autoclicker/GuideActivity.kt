package com.github.nestorm001.autoclicker

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class GuideActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        
        title = "Hướng dẫn"
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        findViewById<Button>(R.id.btnOpenSettings).setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btnClose).setOnClickListener {
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Auto-close if permission granted
        if (checkAccessibilityPermission()) {
            shortToast("Đã cấp quyền thành công!")
            finish()
        }
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
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
