package com.afar.osaio.smart.routerlocal.internet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;

import com.nooie.common.utils.log.NooieLog;

import java.io.IOException;

import static com.afar.osaio.base.BaseApplication.mCtx;

public class InternetConnectionStatus {

    public InternetConnectionStatus() {

    }

    /**
     * 检测网络是否连接（不能判断是否可以上外网）
     *
     * @return
     */
    public static boolean isNetworkAvailable() {
        // 得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    /*ConnectivityManager connManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }
        return false;*/

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailableTest() {
        ConnectivityManager connectivity = (ConnectivityManager) mCtx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isStartWifi() {
        try {
            WifiManager wm = (WifiManager) mCtx.getSystemService(Context.WIFI_SERVICE);
            return (wm!=null && WifiManager.WIFI_STATE_ENABLED == wm.getWifiState());
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 判断当前网络是否可用(6.0以上版本)
     * 实时,快速
     * @return
     */
    public static boolean isNetSystemUsable() {
        boolean isNetUsable = false;
        ConnectivityManager manager = (ConnectivityManager)
                mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                NetworkCapabilities networkCapabilities =
                        manager.getNetworkCapabilities(manager.getActiveNetwork());
                isNetUsable = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                if (!isNetUsable) {
                    for (int i = 0; i < 3; i++) {
                        //WiFi初始连接到的瞬间，WiFi是不可用的，给过一段时间后才可用，故做循环延时判断
                        //Thread.sleep(1000);
                        NetworkCapabilities networkCapability =
                                manager.getNetworkCapabilities(manager.getActiveNetwork());
                        isNetUsable = networkCapability.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                        if (isNetUsable)
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return isNetUsable;
    }


    /**
     * @author suncat
     * @category 判断是否可以上外网（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * @return
     * @param handler
     */
    public static void ping(Handler handler) {
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                String result = null;
                try {
                    String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网地址 ping -c 3 -w 100
                    Process p = Runtime.getRuntime().exec("ping -c 3 " + ip);// ping网址3次
                    // 读取ping的内容，可以不加
            /*InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            NooieLog.d("result content : " + stringBuffer.toString());*/
                    // ping的状态
                    int status = p.waitFor();
                    if (status == 0) {
                        result = "success";
                    } else {
                        result = "failed";
                    }
                } catch (IOException e) {
                    result = "IOException";
                } catch (InterruptedException e) {
                    result = "InterruptedException";
                } finally {
                    NooieLog.d("result = " + result);
                    Message msg = new Message();
                    if ("success".equals(result)) {
                        msg.what = 111;
                    } else {
                        msg.what = 000;
                    }
                    handler.sendMessage(msg);
                }
            }
        };
        thread.start();
    }
}
