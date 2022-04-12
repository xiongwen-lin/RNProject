package com.android.nordicbluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import com.android.nordicbluetooth.core.BleCore


/***********************************************************
 * @Author : caro
 * @Date   : 2/21/21
 * @Func:
 * 对BleCore做一层包装,以便单例化BleCore，避免每次都传入Context 构造没必要的参数
 *
 * @Description:
 *
 *
 ***********************************************************/
object SmartBleManager {
    private val TAG = SmartBleManager::class.java.simpleName
    lateinit var core: BleCore

    /**
     * 初始化BleCore
     */
    fun initCore(context: Context): BleCore {
        core = BleCore(context)
        return core
    }

    /**
     * 判断是否支持蓝牙
     */
    fun isSupportBluetooth(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e("Bluetooth", "该设备不支持蓝牙")
            return false
        }
        return true

        //val pm = context.packageManager
        //return pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        //return pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    /**
     * 判断蓝牙是否已经打开
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    fun bluetoothIsEnable(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter.isEnabled
    }

    /**
     * 请求打开蓝牙
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    fun requestEnableBluetooth(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter.enable()
    }

    /**
     * 手机是否开启位置服务，如果没有开启那么将不能使用定位功能
     */
    fun isLocationServiceEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return gps
        /*
         return when {
             Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                 try {
                     Settings.Secure.getInt(context.contentResolver,
                         Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF
                 } catch (e: Settings.SettingNotFoundException) {
                     false
                 }
             }
             Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                 locationManager.isLocationEnabled
             }
             else -> {
                 Settings.Secure.getString(context.contentResolver,
                     Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
                     .isNotEmpty()
             }
         }*/
    }

    //跳转到GPS开关设置界面
    fun openGpsSwitch(fragment: Fragment, requestCode: Int) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        fragment.startActivityForResult(intent, requestCode)
    }

    //跳转到GPS开关设置界面
    fun openGpsSwitch(activity: Activity, requestCode: Int) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * 跳转到GPS开关设置界面
     * Compat 模式
     */
    fun openGpsSwitch(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            intent.action = Settings.ACTION_SETTINGS
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}