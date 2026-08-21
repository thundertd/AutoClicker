package com.github.nestorm001.autoclicker.bean

/**
 * Cấu hình cho kịch bản chạy tự động
 */
data class ScriptSettings(
    var repeatCount: Int = 1,           // Số lần lặp lại (0 = vô hạn)
    var delayBetweenActions: Long = 200,  // Delay giữa các action (ms)
    var delayBetweenRepeats: Long = 1000  // Delay giữa các lần lặp (ms)
)
