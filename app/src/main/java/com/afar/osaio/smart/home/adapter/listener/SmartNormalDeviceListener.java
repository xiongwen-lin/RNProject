package com.afar.osaio.smart.home.adapter.listener;

import com.afar.osaio.smart.home.bean.SmartBaseDevice;

public interface SmartNormalDeviceListener {

    void onItemClick(SmartBaseDevice device);

    void onSwitchBtnClick(SmartBaseDevice device, boolean on);

    void onAddDeviceBtnClick();

    /**
     * 长按回调，返回true表示消费了该事件
     * @param device
     * @return
     */
    boolean onItemLongClick(SmartBaseDevice device);
}
