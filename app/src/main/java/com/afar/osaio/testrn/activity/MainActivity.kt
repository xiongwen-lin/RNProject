package com.afar.osaio.testrn.activity

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.afar.osaio.R
import com.apemans.base.middleservice.YRMiddleServiceManager
import com.apemans.logger.YRLog
import com.afar.osaio.testrn.dialog.YRCustomProgressDialog
import com.afar.osaio.testrn.utils.GetRouterDataFromCloud
import com.afar.osaio.testrn.utils.SendHttpRequest
import com.apemans.yrpannelkit.yrpannelkit.activity.YRReactActivity
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity(), SendHttpRequest.getRouterReturnInfo {
    var responseInfo = ""
    lateinit var dialog: Dialog
    private lateinit var YRCustomProgressDialog: YRCustomProgressDialog

    private var routerWifiMac = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rn_activity_main)
        initView()
    }

    private fun initView() {
//        YRCustomProgressDialog = YRCustomProgressDialog(this)
//        dialog = YRCustomProgressDialog.createLoadingDialog(this, "正在加载中...")!!
        findViewById<Button>(R.id.btn).setOnClickListener {
            loadPannel()
        }

        findViewById<Button>(R.id.setBtn).setOnClickListener {
            onClickEvent()
        }
    }

    private fun onClickEvent() {
        if (!requestPermissions()) {
            return
        }
        clickEvent()
    }

    override fun onResume() {
        super.onResume()
//        getRouterInitCfg()
    }

    override fun onPause() {
        super.onPause()
//        dialog.dismiss()
    }

    private fun getRouterInitCfg() {
        // 本地通信
        val routerDataFromCloud = GetRouterDataFromCloud(this)
        try {
            routerDataFromCloud.getInitCfg()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun getDeviceList(){
        YRMiddleServiceManager.requestAsync("yrcx://yrxrouter/devicelist", null) {
            if ((it.data as HashMap<String, Any>)["result"] == true) {
                // 有查询到设备
                findViewById<View>(R.id.setBtn).visibility = View.VISIBLE
            } else {
                findViewById<View>(R.id.setBtn).visibility = View.GONE
            }
        }
    }

    private fun clickEvent() {
        if (!requestPermissions()) {
            return
        }
        if ("" == routerWifiMac) {
            Toast.makeText(this, "wifi已断开，请重新连接路由器wifi", Toast.LENGTH_LONG).show()
        } else {
            loadPannel()
        }
    }

    private fun loadPannel() {
        if (!requestPermissions()) {
            return
        }
        var hashMap = HashMap<String, Any>()
        hashMap["context"] = this
        hashMap["url"] = "http://192.168.8.181:9960/android.zip"
        hashMap["uiid"] = "syjdjskj"
        hashMap["version"] = "1.0.14"
        YRLog.d { "MainActivity xxxxxxxxxxxxxxxxxxxxx: YRMiddleServiceManager.requestAsync" }
        YRMiddleServiceManager.requestAsync("yrcx://yrpannelkit/loadpannel", hashMap) {

        }
    }

    private fun downLoadProgress (hashMap : HashMap<String, Any>) {
        dialog.setCancelable(true)//允许返回
        dialog.show()//显示
        YRMiddleServiceManager.listening("yrcx://yrpannelkit/loadpannel", lifecycle, hashMap) {
            if (null != it.data) {
                Log.d("MainActivity", "MainActivity xxxxxxx=======loadpannel: ${it.data}")
                YRCustomProgressDialog.setProgress(it.data as Int)
                if (it.data == 100) {
                    dialog.dismiss()
                }
            }
        }
    }

    private fun requestPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            val REQUEST_CODE_CONTACT = 101
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            //验证是否许可权限
            for (str in permissions) {
                if (this!!.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this!!.requestPermissions(permissions, REQUEST_CODE_CONTACT)
                    return false
                }
            }
        }
        return true
    }

    /**
     * 没有连接路由器wifi,显示设备未连接
     */
    private fun updataRouterStatus() {
        routerWifiMac = ""
    }

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1) {
                updataRouterStatus()
            }
            getDeviceList()
        }
    }

    override fun routerReturnInfo(info: String?, topicurlString: String?) {
        val message = Message()
        if ("error" == info && "getInitCfg" == topicurlString) {
            message.what = 1
        } else if ("error" != info && "getInitCfg" == topicurlString) {
            routerWifiMac = JSONObject(info).getString("mac")
            message.what = 2
        }
        handler.sendMessage(message)
    }
}