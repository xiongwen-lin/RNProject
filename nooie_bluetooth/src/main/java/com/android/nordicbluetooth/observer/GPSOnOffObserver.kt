package com.android.nordicbluetooth.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.android.nordicbluetooth.SmartBleManager

/***********************************************************
 * @Author : caro
 * @Date   : 2/22/21
 * @Func:
 * Gps开关监听
 * @Description:
 * @param bindLifecycleOwner 若在Dialog中使用时，请设置为false ，并主动注册和解绑Observer
 *
 ***********************************************************/
class GPSOnOffObserver(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner? = null,
    private val even: GpsStateEvent,
) {
    interface GpsStateEvent {
        fun gspStateOnChange()
        fun gspStateOffChange()
    }

    private val TAG = GPSOnOffObserver::class.java.simpleName
    private val GPS_ACTION = "android.location.PROVIDERS_CHANGED"
    private var visible = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var gpsBroadcastReceiver: GpsBroadcastReceiver

    private val gpsOnRunnable = Runnable {
        even.gspStateOnChange()
    }
    private val gpsOffRunnable = Runnable {
        even.gspStateOffChange()
    }

    init {
        lifecycleOwner?.lifecycle?.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        Log.i(TAG, "ON_CREATE--注册GPSOnOffObserver")
                        register()
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        Log.i(TAG, "ON_DESTROY 解除注册GPSOnOffObserver")
                        unRegister()

                    }
                    Lifecycle.Event.ON_START -> {
                        Log.i(TAG, "ON_START")
                        visible = true
                    }
                    Lifecycle.Event.ON_STOP -> {
                        Log.i(TAG, "ON_STOP")
                        visible = false
                    }

                    else -> {
                        //noop
                    }
                }
            }
        })

    }


    private inner class GpsBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            if (action == GPS_ACTION) {
                val isOpenGps = SmartBleManager.isLocationServiceEnabled(context)
                if (isOpenGps) {
                    mainHandler.removeCallbacks(gpsOnRunnable)
                    mainHandler.post(gpsOnRunnable)
                } else {
                    mainHandler.removeCallbacks(gpsOffRunnable)
                    mainHandler.post(gpsOffRunnable)
                }
            }
        }
    }

    fun register() {
        gpsBroadcastReceiver = GpsBroadcastReceiver()
        val intentFilter = IntentFilter()
        // 监视GSP关闭和打开的状态
        intentFilter.addAction(GPS_ACTION)
        // 注册广播
        context.registerReceiver(gpsBroadcastReceiver, intentFilter)
    }

    fun unRegister() {
        context.unregisterReceiver(gpsBroadcastReceiver)
        mainHandler.removeCallbacksAndMessages(null)
    }
}