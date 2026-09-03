package com.github.nestorm001.autoclicker.bean

import android.graphics.Point

/**
 * Data class representing an action with multiple click points
 */
data class Action(
    val id: Long = 0,
    val name: String,
    val clickPoints: List<ClickPoint>,
    val repeatCount: Int = 1,
    val delayBetweenClicks: Long = 200, // milliseconds (deprecated, use delayBetweenLoops)
    val loopCount: Int = 1, // Number of times to repeat the entire sequence (0 = infinite)
    val delayBetweenLoops: Long = 1000 // Delay between each loop iteration in milliseconds
)

/**
 * Enum for target action type
 */
enum class TargetType {
    CLICK,
    SWIPE
}

/**
 * Data class representing a single click point or swipe action in an action
 */
data class ClickPoint(
    val id: Long = 0,
    val actionId: Long = 0,
    val sequence: Int,
    val type: TargetType = TargetType.CLICK,
    
    // For CLICK type
    val x: Int = 0,
    val y: Int = 0,
    val clickCount: Int = 1,
    val delayBefore: Long = 0, // Delay before performing this action (milliseconds)
    val holdDuration: Long = 100, // How long to hold the click (milliseconds)
    val delayAfter: Long = 0, // delay after this click in milliseconds (deprecated)
    
    // For SWIPE type
    val fromX: Int = 0,
    val fromY: Int = 0,
    val toX: Int = 0,
    val toY: Int = 0,
    val swipeDuration: Long = 300 // Duration of the swipe gesture (milliseconds)
)

/**
 * Data class representing a sequence of actions
 */
data class ActionSequence(
    val id: Long = 0,
    val name: String,
    val actions: List<SequenceItem>
)

/**
 * Data class representing an item in an action sequence
 */
data class SequenceItem(
    val actionId: Long,
    val order: Int,
    val delayAfter: Long = 0 // delay after this action in milliseconds
)
