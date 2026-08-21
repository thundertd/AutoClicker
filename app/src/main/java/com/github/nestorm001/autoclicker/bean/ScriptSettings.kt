package com.github.nestorm001.autoclicker.bean

/**
 * Chế độ chạy script
 */
enum class RunMode {
    REPEAT_COUNT,      // Chạy lặp lại bao nhiêu lần
    DURATION,          // Chạy liên tục trong bao lâu
    INFINITE           // Chạy vô hạn (mặc định)
}

/**
 * Cấu hình cho kịch bản chạy tự động
 */
data class ScriptSettings(
    var runMode: RunMode = RunMode.INFINITE,  // Chế độ chạy
    var repeatCount: Int = 1,                 // Số lần lặp lại (khi runMode = REPEAT_COUNT)
    var durationHours: Int = 0,               // Số giờ chạy (khi runMode = DURATION)
    var durationMinutes: Int = 0,             // Số phút chạy
    var durationSeconds: Int = 0,             // Số giây chạy
    var scheduledTime: Long? = null,          // Hẹn giờ chạy (timestamp millis), null = chạy ngay
    var delayBetweenRepeats: Long = 0,        // Thời gian chờ cho từng lần chạy kịch bản
    var delayUnit: TimeUnit = TimeUnit.MILLISECOND, // Đơn vị thời gian cho delay
    var antiDetection: Boolean = false,       // Chống phát hiện (random thêm 1-200ms)
    
    // Legacy fields - deprecated but kept for backward compatibility
    @Deprecated("Use runMode and other fields instead")
    var delayBetweenActions: Long = 200       // Delay giữa các action (ms)
) {
    /**
     * Lấy thời gian delay giữa các lần lặp theo milliseconds
     */
    fun getDelayBetweenRepeatsInMillis(): Long {
        return when (delayUnit) {
            TimeUnit.MILLISECOND -> delayBetweenRepeats
            TimeUnit.SECOND -> delayBetweenRepeats * 1000
            TimeUnit.MINUTE -> delayBetweenRepeats * 60 * 1000
            TimeUnit.HOUR -> delayBetweenRepeats * 60 * 60 * 1000
        }
    }
    
    /**
     * Lấy tổng thời gian chạy theo milliseconds (cho DURATION mode)
     */
    fun getDurationInMillis(): Long {
        return (durationHours * 3600L + durationMinutes * 60L + durationSeconds) * 1000
    }
    
    /**
     * Tạo delay ngẫu nhiên khi bật chống phát hiện
     */
    fun getRandomizedDelay(baseDelay: Long): Long {
        return if (antiDetection) {
            baseDelay + (1..200).random()
        } else {
            baseDelay
        }
    }
}
