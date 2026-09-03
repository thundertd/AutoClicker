package com.github.nestorm001.autoclicker.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.github.nestorm001.autoclicker.bean.Action
import com.github.nestorm001.autoclicker.bean.ActionSequence
import com.github.nestorm001.autoclicker.bean.ClickPoint
import com.github.nestorm001.autoclicker.bean.TargetType
import com.github.nestorm001.autoclicker.bean.SequenceItem
import com.github.nestorm001.autoclicker.logd
import org.json.JSONArray
import org.json.JSONObject

class ActionDatabaseHelper(context: Context) : 
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "autoclicker.db"
        private const val DATABASE_VERSION = 2 // Increased for new schema

        // Actions table
        private const val TABLE_ACTIONS = "actions"
        private const val COLUMN_ACTION_ID = "id"
        private const val COLUMN_ACTION_NAME = "name"
        private const val COLUMN_ACTION_REPEAT_COUNT = "repeat_count"
        private const val COLUMN_ACTION_DELAY = "delay_between_clicks"
        private const val COLUMN_ACTION_LOOP_COUNT = "loop_count"
        private const val COLUMN_ACTION_DELAY_BETWEEN_LOOPS = "delay_between_loops"

        // Click points table
        private const val TABLE_CLICK_POINTS = "click_points"
        private const val COLUMN_POINT_ID = "id"
        private const val COLUMN_POINT_ACTION_ID = "action_id"
        private const val COLUMN_POINT_SEQUENCE = "sequence"
        private const val COLUMN_POINT_TYPE = "type" // "CLICK" or "SWIPE"
        private const val COLUMN_POINT_X = "x"
        private const val COLUMN_POINT_Y = "y"
        private const val COLUMN_POINT_CLICK_COUNT = "click_count"
        private const val COLUMN_POINT_DELAY_BEFORE = "delay_before"
        private const val COLUMN_POINT_HOLD_DURATION = "hold_duration"
        private const val COLUMN_POINT_DELAY_AFTER = "delay_after"
        private const val COLUMN_POINT_FROM_X = "from_x"
        private const val COLUMN_POINT_FROM_Y = "from_y"
        private const val COLUMN_POINT_TO_X = "to_x"
        private const val COLUMN_POINT_TO_Y = "to_y"
        private const val COLUMN_POINT_SWIPE_DURATION = "swipe_duration"

        // Action sequences table
        private const val TABLE_ACTION_SEQUENCES = "action_sequences"
        private const val COLUMN_SEQUENCE_ID = "id"
        private const val COLUMN_SEQUENCE_NAME = "name"

        // Sequence items table
        private const val TABLE_SEQUENCE_ITEMS = "sequence_items"
        private const val COLUMN_SEQUENCE_ITEM_ID = "id"
        private const val COLUMN_SEQUENCE_ITEM_SEQUENCE_ID = "sequence_id"
        private const val COLUMN_SEQUENCE_ITEM_ACTION_ID = "action_id"
        private const val COLUMN_SEQUENCE_ITEM_ORDER = "item_order"
        private const val COLUMN_SEQUENCE_ITEM_DELAY_AFTER = "delay_after"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create actions table
        val createActionsTable = """
            CREATE TABLE $TABLE_ACTIONS (
                $COLUMN_ACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ACTION_NAME TEXT NOT NULL,
                $COLUMN_ACTION_REPEAT_COUNT INTEGER DEFAULT 1,
                $COLUMN_ACTION_DELAY INTEGER DEFAULT 200,
                $COLUMN_ACTION_LOOP_COUNT INTEGER DEFAULT 1,
                $COLUMN_ACTION_DELAY_BETWEEN_LOOPS INTEGER DEFAULT 1000
            )
        """.trimIndent()
        db.execSQL(createActionsTable)

        // Create click points table
        val createClickPointsTable = """
            CREATE TABLE $TABLE_CLICK_POINTS (
                $COLUMN_POINT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_POINT_ACTION_ID INTEGER NOT NULL,
                $COLUMN_POINT_SEQUENCE INTEGER NOT NULL,
                $COLUMN_POINT_TYPE TEXT DEFAULT 'CLICK',
                $COLUMN_POINT_X INTEGER DEFAULT 0,
                $COLUMN_POINT_Y INTEGER DEFAULT 0,
                $COLUMN_POINT_CLICK_COUNT INTEGER DEFAULT 1,
                $COLUMN_POINT_DELAY_BEFORE INTEGER DEFAULT 0,
                $COLUMN_POINT_HOLD_DURATION INTEGER DEFAULT 100,
                $COLUMN_POINT_DELAY_AFTER INTEGER DEFAULT 0,
                $COLUMN_POINT_FROM_X INTEGER DEFAULT 0,
                $COLUMN_POINT_FROM_Y INTEGER DEFAULT 0,
                $COLUMN_POINT_TO_X INTEGER DEFAULT 0,
                $COLUMN_POINT_TO_Y INTEGER DEFAULT 0,
                $COLUMN_POINT_SWIPE_DURATION INTEGER DEFAULT 300,
                FOREIGN KEY($COLUMN_POINT_ACTION_ID) REFERENCES $TABLE_ACTIONS($COLUMN_ACTION_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(createClickPointsTable)

        // Create action sequences table
        val createSequencesTable = """
            CREATE TABLE $TABLE_ACTION_SEQUENCES (
                $COLUMN_SEQUENCE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SEQUENCE_NAME TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createSequencesTable)

        // Create sequence items table
        val createSequenceItemsTable = """
            CREATE TABLE $TABLE_SEQUENCE_ITEMS (
                $COLUMN_SEQUENCE_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SEQUENCE_ITEM_SEQUENCE_ID INTEGER NOT NULL,
                $COLUMN_SEQUENCE_ITEM_ACTION_ID INTEGER NOT NULL,
                $COLUMN_SEQUENCE_ITEM_ORDER INTEGER NOT NULL,
                $COLUMN_SEQUENCE_ITEM_DELAY_AFTER INTEGER DEFAULT 0,
                FOREIGN KEY($COLUMN_SEQUENCE_ITEM_SEQUENCE_ID) REFERENCES $TABLE_ACTION_SEQUENCES($COLUMN_SEQUENCE_ID) ON DELETE CASCADE,
                FOREIGN KEY($COLUMN_SEQUENCE_ITEM_ACTION_ID) REFERENCES $TABLE_ACTIONS($COLUMN_ACTION_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(createSequenceItemsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SEQUENCE_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACTION_SEQUENCES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLICK_POINTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACTIONS")
        onCreate(db)
    }

    // Insert a new action
    fun insertAction(action: Action): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ACTION_NAME, action.name)
            put(COLUMN_ACTION_REPEAT_COUNT, action.repeatCount)
            put(COLUMN_ACTION_DELAY, action.delayBetweenClicks)
            put(COLUMN_ACTION_LOOP_COUNT, action.loopCount)
            put(COLUMN_ACTION_DELAY_BETWEEN_LOOPS, action.delayBetweenLoops)
        }
        val actionId = db.insert(TABLE_ACTIONS, null, values)
        
        // Insert click points
        action.clickPoints.forEach { point ->
            insertClickPoint(point.copy(actionId = actionId))
        }
        
        return actionId
    }

    // Insert a click point
    private fun insertClickPoint(point: ClickPoint): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_POINT_ACTION_ID, point.actionId)
            put(COLUMN_POINT_SEQUENCE, point.sequence)
            put(COLUMN_POINT_TYPE, point.type.name)
            put(COLUMN_POINT_X, point.x)
            put(COLUMN_POINT_Y, point.y)
            put(COLUMN_POINT_CLICK_COUNT, point.clickCount)
            put(COLUMN_POINT_DELAY_BEFORE, point.delayBefore)
            put(COLUMN_POINT_HOLD_DURATION, point.holdDuration)
            put(COLUMN_POINT_DELAY_AFTER, point.delayAfter)
            put(COLUMN_POINT_FROM_X, point.fromX)
            put(COLUMN_POINT_FROM_Y, point.fromY)
            put(COLUMN_POINT_TO_X, point.toX)
            put(COLUMN_POINT_TO_Y, point.toY)
            put(COLUMN_POINT_SWIPE_DURATION, point.swipeDuration)
        }
        return db.insert(TABLE_CLICK_POINTS, null, values)
    }

    // Get all actions
    fun getAllActions(): List<Action> {
        val actions = mutableListOf<Action>()
        
        try {
            val db = readableDatabase
            val cursor = db.query(TABLE_ACTIONS, null, null, null, null, null, "$COLUMN_ACTION_ID DESC")
            
            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ACTION_ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTION_NAME))
                    val repeatCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ACTION_REPEAT_COUNT))
                    val delay = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ACTION_DELAY))
                    
                    val loopCount = try {
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ACTION_LOOP_COUNT))
                    } catch (e: Exception) { 1 }
                    
                    val delayBetweenLoops = try {
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ACTION_DELAY_BETWEEN_LOOPS))
                    } catch (e: Exception) { 1000L }
                    
                    val clickPoints = getClickPointsForAction(id)
                    
                    actions.add(Action(id, name, clickPoints, repeatCount, delay, loopCount, delayBetweenLoops))
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Skip this action if error
                }
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return actions
    }

    // Get action by ID
    fun getActionById(actionId: Long): Action? {
        var action: Action? = null
        
        try {
            "Database: getActionById($actionId)".logd()
            val db = readableDatabase
            val cursor = db.query(
                TABLE_ACTIONS,
                null,
                "$COLUMN_ACTION_ID = ?",
                arrayOf(actionId.toString()),
                null, null, null
            )
            
            "Database: query returned cursor".logd()
            if (cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTION_NAME))
                val repeatCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ACTION_REPEAT_COUNT))
                val delay = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ACTION_DELAY))
                
                val loopCount = try {
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ACTION_LOOP_COUNT))
                } catch (e: Exception) { 1 }
                
                val delayBetweenLoops = try {
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ACTION_DELAY_BETWEEN_LOOPS))
                } catch (e: Exception) { 1000L }
                
                "Database: loaded action name=$name, getting click points...".logd()
                val clickPoints = getClickPointsForAction(actionId)
                "Database: got ${clickPoints.size} click points".logd()
                
                action = Action(actionId, name, clickPoints, repeatCount, delay, loopCount, delayBetweenLoops)
                "Database: created Action object successfully".logd()
            } else {
                "Database: cursor.moveToFirst() returned false for id=$actionId".logd()
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
            "Database: ERROR in getActionById($actionId): ${e.message}".logd()
        }
        
        "Database: getActionById returning ${if (action != null) "Action(${action.name})" else "null"}".logd()
        return action
    }

    // Get click points for an action
    private fun getClickPointsForAction(actionId: Long): List<ClickPoint> {
        val points = mutableListOf<ClickPoint>()
        val db = readableDatabase
        
        try {
            "Database: getClickPointsForAction($actionId)".logd()
            val cursor = db.query(
                TABLE_CLICK_POINTS,
                null,
                "$COLUMN_POINT_ACTION_ID = ?",
                arrayOf(actionId.toString()),
                null, null,
                "$COLUMN_POINT_SEQUENCE ASC"
            )
            
            "Database: click points query returned cursor".logd()
            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_POINT_ID))
                    val sequence = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINT_SEQUENCE))
                    
                    // Try to get new columns, fallback to defaults if not exist
                    val typeStr = try {
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POINT_TYPE))
                    } catch (e: Exception) {
                        "CLICK"
                    }
                    val type = try { 
                        com.github.nestorm001.autoclicker.bean.TargetType.valueOf(typeStr) 
                    } catch (e: Exception) { 
                        com.github.nestorm001.autoclicker.bean.TargetType.CLICK 
                    }
                    
                    val x = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINT_X))
                    val y = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINT_Y))
                    val clickCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINT_CLICK_COUNT))
                    
                    val delayBefore = try {
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_POINT_DELAY_BEFORE))
                    } catch (e: Exception) { 0L }
                    
                    val holdDuration = try {
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_POINT_HOLD_DURATION))
                    } catch (e: Exception) { 100L }
                    
                    val delayAfter = try {
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_POINT_DELAY_AFTER))
                    } catch (e: Exception) { 0L }
                    
                    val fromX = try {
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINT_FROM_X))
                    } catch (e: Exception) { 0 }
                    
                    val fromY = try {
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINT_FROM_Y))
                    } catch (e: Exception) { 0 }
                    
                    val toX = try {
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINT_TO_X))
                    } catch (e: Exception) { 0 }
                    
                    val toY = try {
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINT_TO_Y))
                    } catch (e: Exception) { 0 }
                    
                    val swipeDuration = try {
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_POINT_SWIPE_DURATION))
                    } catch (e: Exception) { 300L }
                    
                    val point = ClickPoint(id, actionId, sequence, type, x, y, clickCount, delayBefore, holdDuration, delayAfter, fromX, fromY, toX, toY, swipeDuration)
                    points.add(point)
                    "Database: loaded ClickPoint seq=$sequence type=$type coords=($x,$y)".logd()
                } catch (e: Exception) {
                    e.printStackTrace()
                    "Database: ERROR loading ClickPoint: ${e.message}".logd()
                    // Skip this point if error
                }
            }
            cursor.close()
            "Database: successfully loaded ${points.size} click points".logd()
        } catch (e: Exception) {
            e.printStackTrace()
            "Database: ERROR in getClickPointsForAction($actionId): ${e.message}".logd()
        }
        
        return points
    }

    // Update an action
    fun updateAction(action: Action): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ACTION_NAME, action.name)
            put(COLUMN_ACTION_REPEAT_COUNT, action.repeatCount)
            put(COLUMN_ACTION_DELAY, action.delayBetweenClicks)
            put(COLUMN_ACTION_LOOP_COUNT, action.loopCount)
            put(COLUMN_ACTION_DELAY_BETWEEN_LOOPS, action.delayBetweenLoops)
        }
        
        // Delete old click points and insert new ones
        db.delete(TABLE_CLICK_POINTS, "$COLUMN_POINT_ACTION_ID = ?", arrayOf(action.id.toString()))
        action.clickPoints.forEach { point ->
            insertClickPoint(point.copy(actionId = action.id))
        }
        
        return db.update(TABLE_ACTIONS, values, "$COLUMN_ACTION_ID = ?", arrayOf(action.id.toString()))
    }

    // Delete an action
    fun deleteAction(actionId: Long): Int {
        val db = writableDatabase
        // Click points will be deleted automatically due to CASCADE
        return db.delete(TABLE_ACTIONS, "$COLUMN_ACTION_ID = ?", arrayOf(actionId.toString()))
    }

    // Export actions to JSON
    fun exportActionsToJson(actionIds: List<Long>): String {
        val jsonArray = JSONArray()
        
        actionIds.forEach { actionId ->
            getActionById(actionId)?.let { action ->
                val jsonObject = JSONObject().apply {
                    put("id", action.id)
                    put("name", action.name)
                    put("repeatCount", action.repeatCount)
                    put("delayBetweenClicks", action.delayBetweenClicks)
                    
                    val pointsArray = JSONArray()
                    action.clickPoints.forEach { point ->
                        val pointObject = JSONObject().apply {
                            put("sequence", point.sequence)
                            put("x", point.x)
                            put("y", point.y)
                            put("clickCount", point.clickCount)
                            put("delayAfter", point.delayAfter)
                        }
                        pointsArray.put(pointObject)
                    }
                    put("clickPoints", pointsArray)
                }
                jsonArray.put(jsonObject)
            }
        }
        
        return jsonArray.toString(2)
    }

    // Import actions from JSON
    fun importActionsFromJson(jsonString: String): List<Long> {
        val importedIds = mutableListOf<Long>()
        val jsonArray = JSONArray(jsonString)
        
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val name = jsonObject.getString("name")
            val repeatCount = jsonObject.getInt("repeatCount")
            val delayBetweenClicks = jsonObject.getLong("delayBetweenClicks")
            
            val clickPoints = mutableListOf<ClickPoint>()
            val pointsArray = jsonObject.getJSONArray("clickPoints")
            for (j in 0 until pointsArray.length()) {
                val pointObject = pointsArray.getJSONObject(j)
                clickPoints.add(ClickPoint(
                    sequence = pointObject.getInt("sequence"),
                    x = pointObject.getInt("x"),
                    y = pointObject.getInt("y"),
                    clickCount = pointObject.getInt("clickCount"),
                    delayAfter = pointObject.getLong("delayAfter")
                ))
            }
            
            val action = Action(
                name = name,
                clickPoints = clickPoints,
                repeatCount = repeatCount,
                delayBetweenClicks = delayBetweenClicks
            )
            
            val actionId = insertAction(action)
            importedIds.add(actionId)
        }
        
        return importedIds
    }
}
