package com.codelang.messagehook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.butCheck).setOnClickListener {
            invokeMsg()
        }
    }


    private fun invokeMsg() {
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            Thread.sleep(5000)
            Log.i("BadTokenUtils", "isFound Destroy message : ${com.codelang.hook.BadTokenUtils.isOnDestroyMsgExit()}")
        }, 50)

        finish()
    }
}