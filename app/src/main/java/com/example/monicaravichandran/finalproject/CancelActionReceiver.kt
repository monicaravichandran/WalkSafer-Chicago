package com.example.monicaravichandran.finalproject

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.R.string.cancel
import android.app.NotificationManager



/**
 * Created by monicaravichandran on 11/28/18.
 */
class CancelActionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println("RECEIVED")
        if(intent?.getAction().equals("CANCEL")){
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            println("RECEIVED")
            notificationManager.cancel(101)
        }
    }
}