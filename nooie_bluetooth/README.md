# 使用参考
https://github.com/NordicSemiconductor/Android-BLE-Library
https://github.com/NordicSemiconductor/Android-nRF-Toolbox

#备注
1： ConnectionStateLiveData中
    init {
        value = ConnectionState.Disconnected(reason = ConnectionObserver.REASON_UNKNOWN)
    }
    导致BleManager初始化时，会先回调设备断开连接，故不集成
    api 'no.nordicsemi.android:ble-livedata:2.2.4'
clone 代码自己修改

#一分钟读懂低功耗蓝牙(BLE)MTU交换数据包
https://www.zhihu.com/column/p/28141658