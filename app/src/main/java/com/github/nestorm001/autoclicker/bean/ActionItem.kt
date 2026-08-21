package com.github.nestorm001.autoclicker.bean

import android.graphics.Point

/**
 * Data class đại diện cho một kịch bản trong danh sách
 */
data class ActionItem(
    val id: Int,
    val name: String,
    var isActive: Boolean = true,
    val events: MutableList<Event> = mutableListOf()
) {
    // Thêm event vào kịch bản
    fun addEvent(event: Event) {
        events.add(event)
    }
    
    // Chuyển đổi trạng thái active
    fun toggleActive() {
        isActive = !isActive
    }
}
