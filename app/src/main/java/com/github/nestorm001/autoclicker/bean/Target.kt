package com.github.nestorm001.autoclicker.bean

import android.graphics.Point

/**
 * Đơn vị thời gian cho delay
 */
enum class TimeUnit {
    MILLISECOND,  // Mili giây
    SECOND,       // Giây
    MINUTE,       // Phút
    HOUR          // Giờ
}

/**
 * Lớp đại diện cho một target (điểm click hoặc swipe)
 */
sealed class Target {
    abstract val id: Int
    abstract var position: Point
    abstract var delay: Long                    // Thời gian chờ trước khi thực hiện
    abstract var delayUnit: TimeUnit           // Đơn vị thời gian
    abstract var repeatCount: Int              // Số lần lặp lại
    abstract var scheduledTime: Long?          // Hẹn giờ (timestamp millis), null = không hẹn
    abstract var imageComparisonPath: String?  // Đường dẫn ảnh so sánh
    
    /**
     * Điểm nhấn (Click Point)
     */
    data class ClickPoint(
        override val id: Int,
        override var position: Point,
        override var delay: Long = 0L,
        override var delayUnit: TimeUnit = TimeUnit.MILLISECOND,
        override var repeatCount: Int = 1,
        override var scheduledTime: Long? = null,
        override var imageComparisonPath: String? = null
    ) : Target()
    
    /**
     * Hành động vuốt (Swipe Action)
     */
    data class SwipeAction(
        override val id: Int,
        override var position: Point,           // Điểm kết thúc
        var startPosition: Point,               // Điểm bắt đầu
        override var delay: Long = 0L,
        override var delayUnit: TimeUnit = TimeUnit.MILLISECOND,
        override var repeatCount: Int = 1,
        override var scheduledTime: Long? = null,
        override var imageComparisonPath: String? = null,
        var swipeDuration: Long = 200L          // Thời gian vuốt (milliseconds)
    ) : Target()
    
    /**
     * Chuyển đổi delay sang milliseconds
     */
    fun getDelayInMillis(): Long {
        return when (delayUnit) {
            TimeUnit.MILLISECOND -> delay
            TimeUnit.SECOND -> delay * 1000
            TimeUnit.MINUTE -> delay * 60 * 1000
            TimeUnit.HOUR -> delay * 60 * 60 * 1000
        }
    }
}
