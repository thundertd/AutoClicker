package com.github.nestorm001.autoclicker.bean

import android.graphics.Point

/**
 * Lớp đại diện cho một target (điểm click hoặc swipe)
 */
sealed class Target {
    abstract val id: Int
    abstract var position: Point
    
    /**
     * Điểm nhấn (Click Point)
     */
    data class ClickPoint(
        override val id: Int,
        override var position: Point
    ) : Target()
    
    /**
     * Hành động vuốt (Swipe Action)
     */
    data class SwipeAction(
        override val id: Int,
        override var position: Point, // Điểm kết thúc
        var startPosition: Point      // Điểm bắt đầu
    ) : Target()
}
