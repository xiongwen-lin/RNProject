package com.android.nordicbluetooth.observer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
 * 蓝牙开关监听
 * @Description:
 *
 ***********************************************************/
class BluetoothStateActionObserver(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner? = null,
    private val even: BluetoothStateEvent,
) {
    interface BluetoothStateEvent {
        fun bluetoothStateOnChange()
        fun bluetoothStateOffChange()
        fun bluetoothConnected()
        fun bluetoothDisConnected()
    }

    private val TAG = BluetoothStateActionObserver::class.java.simpleName
    private var visible = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val stateOnRunnable = Runnable {
        even.bluetoothStateOnChange()
    }
    private val stateOffRunnable = Runnable {
        even.bluetoothStateOffChange()
    }
    private val stateConnectedRunnable = Runnable {
        even.bluetoothConnected()
    }
    private val stateDisConnectedRunnable = Runnable {
        even.bluetoothDisConnected()
    }

    init {
        lifecycleOwner?.lifecycle?.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        Log.i(TAG, "ON_CREATE-注册蓝牙BroadcastReceiver")
                        register()
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        Log.i(TAG, "ON_DESTROY")
                        unObserve()

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

    private inner class BluetoothMonitorReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            when (action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                        BluetoothAdapter.STATE_TURNING_ON -> {
                            Log.i(TAG, "STATE_TURNING_ON 蓝牙正在打开")
                        }
                        BluetoothAdapter.STATE_ON -> {
                            Log.i(TAG, "STATE_ON 蓝牙已经打开")
                            mainHandler.removeCallbacks(stateOnRunnable)
                            mainHandler.post(stateOnRunnable)
                        }
                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            Log.i(TAG, "STATE_TURNING_OFF 蓝牙正在关闭")
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            Log.i(TAG, "STATE_OFF 蓝牙已经关闭")
                            mainHandler.removeCallbacks(stateOffRunnable)
                            mainHandler.post(stateOffRunnable)
                        }
                    }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    Log.i(TAG, "蓝牙设备已连接")
                    mainHandler.removeCallbacks(stateConnectedRunnable)
                    mainHandler.post(stateConnectedRunnable)
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Log.i(TAG, "系统回调蓝牙设备已断开--确保扫描和连接停止 disconnectBle")
                    SmartBleManager.core.stopScan()
                    SmartBleManager.core.disconnectBle()
                    mainHandler.removeCallbacks(stateDisConnectedRunnable)
                    mainHandler.post(stateDisConnectedRunnable)
                }
            }
        }
    }

    private lateinit var bluetoothBroadcastReceiver: BluetoothMonitorReceiver
    fun register() {
        // 初始化广播
        bluetoothBroadcastReceiver = BluetoothMonitorReceiver()
        val intentFilter = IntentFilter()
        // 监视蓝牙关闭和打开的状态
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        // 注册广播
        context.registerReceiver(bluetoothBroadcastReceiver, intentFilter)
    }

    private fun unObserve() {
        Log.i(TAG, "解除注册蓝牙BroadcastReceiver")

        context.unregisterReceiver(bluetoothBroadcastReceiver)
        mainHandler.removeCallbacksAndMessages(null)
    }

    fun unRegister() {
        unObserve()
    }
}