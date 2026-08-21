package com.github.nestorm001.autoclicker

import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.nestorm001.autoclicker.bean.ActionItem
import com.github.nestorm001.autoclicker.bean.Click
import com.github.nestorm001.autoclicker.service.FloatingControlService

/**
 * Activity hiển thị danh sách các kịch bản
 */
class ActionListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActionListAdapter
    private lateinit var btnEditActions: Button
    private lateinit var btnExportSelected: Button
    private lateinit var btnRunSequence: Button
    
    private val actionList = mutableListOf<ActionItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Khởi tạo views
        initViews()
        
        // Tạo dữ liệu mẫu
        initSampleData()
        
        // Thiết lập RecyclerView
        setupRecyclerView()
        
        // Thiết lập click listeners cho các button trên cùng
        setupTopButtons()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewActions)
        btnEditActions = findViewById(R.id.btnEditActions)
        btnExportSelected = findViewById(R.id.btnExportSelected)
        btnRunSequence = findViewById(R.id.btnRunSequence)
    }
    
    private fun initSampleData() {
        // Tạo một số kịch bản mẫu
        for (i in 1..5) {
            val action = ActionItem(
                id = i,
                name = "Kịch bản $i",
                isActive = true
            )
            // Thêm một số event mẫu
            action.addEvent(Click(Point(100 * i, 200 * i)))
            actionList.add(action)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = ActionListAdapter(
            actions = actionList,
            onDetailClick = { action ->
                // Xử lý khi click button Chi tiết
                // Giống như nút start hiện tại nhưng không đóng ứng dụng
                handleDetailClick(action)
            },
            onExportClick = { action ->
                // Xử lý khi click button Xuất file
                shortToast(getString(R.string.coming_soon))
            },
            onRunClick = { action ->
                // Xử lý khi click button Chạy
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N
                    || Settings.canDrawOverlays(this)) {
                    val serviceIntent = Intent(this, FloatingControlService::class.java)
                    startService(serviceIntent)
                    shortToast("Đã khởi động kịch bản ${action.name}")
                } else {
                    shortToast(getString(R.string.need_permission))
                }
            },
            onActiveToggle = { action ->
                // Xử lý khi toggle active
                val status = if (action.isActive) "bật" else "tắt"
                shortToast("Kịch bản ${action.name} đã $status")
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun setupTopButtons() {
        // Button Điều chỉnh kịch bản
        btnEditActions.setOnClickListener {
            shortToast(getString(R.string.coming_soon))
        }
        
        // Button Xuất các kịch bản đã chọn ra file json
        btnExportSelected.setOnClickListener {
            shortToast(getString(R.string.coming_soon))
        }
        
        // Button Chạy 1 loạt kịch bản
        btnRunSequence.setOnClickListener {
            shortToast(getString(R.string.coming_soon))
        }
    }
    
    private fun handleDetailClick(action: ActionItem) {
        // Giống như nút start trong MainActivity nhưng không đóng ứng dụng
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N
            || Settings.canDrawOverlays(this)) {
            val serviceIntent = Intent(this, FloatingControlService::class.java)
            startService(serviceIntent)
            shortToast("Đã khởi động kịch bản ${action.name}")
            // Không gọi onBackPressed() để không đóng ứng dụng
        } else {
            shortToast("Bạn cần cấp quyền System Alert Window để khởi động kịch bản ${action.name}")
        }
    }
    
    override fun onBackPressed() {
        // Quay lại MainActivity
        finish()
        super.onBackPressed()
    }
}
