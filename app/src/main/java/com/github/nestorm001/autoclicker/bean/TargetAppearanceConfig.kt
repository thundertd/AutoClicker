package com.github.nestorm001.autoclicker.bean

import android.graphics.Color

/**
 * Cấu hình hiển thị cho Target (Click Point và Swipe Action)
 */
data class TargetAppearanceConfig(
    // Click Point appearance
    var clickPointRadius: Int = 30,              // Bán kính vòng tròn (dp)
    var clickPointColor: Int = Color.RED,        // Màu vòng tròn
    var clickPointCenterColor: Int = Color.WHITE,// Màu tâm (dấu +)
    var clickPointNumberColor: Int = Color.GREEN,// Màu số thứ tự
    var clickPointActiveColor: Int = Color.YELLOW, // Màu khi đang chạy
    
    // Swipe Action appearance
    var swipeStartRadius: Int = 20,              // Bán kính điểm bắt đầu (dp)
    var swipeStartColor: Int = Color.GREEN,      // Màu điểm bắt đầu
    var swipeEndRadius: Int = 30,                // Bán kính điểm kết thúc (dp)
    var swipeEndColor: Int = Color.GREEN,        // Màu điểm kết thúc
    var swipeEndCenterColor: Int = Color.WHITE,  // Màu tâm điểm kết thúc
    var swipeNumberColor: Int = Color.parseColor("#FFA500"), // Màu số thứ tự (cam)
    var swipeArrowColor: Int = Color.RED,        // Màu mũi tên
    var swipeArrowWidth: Float = 4f,             // Độ dày mũi tên (dp)
    var swipeActiveColor: Int = Color.YELLOW,    // Màu khi đang chạy
    
    // Common appearance
    var inactiveAlpha: Float = 0.5f,             // Độ mờ khi không active (Play)
    var activeAlpha: Float = 1.0f                // Độ trong suốt khi active
) {
    companion object {
        /**
         * Cấu hình mặc định
         */
        fun default() = TargetAppearanceConfig()
    }
}
