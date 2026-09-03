package com.github.nestorm001.autoclicker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Button for action list
        val btnActionList = findViewById<Button>(R.id.btnActionList)
        btnActionList.setOnClickListener {
            val intent = Intent(this@MainActivity, ActionListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
