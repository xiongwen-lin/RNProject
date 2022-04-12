package com.afar.osaio.smart.home.adapter.listener;

import com.afar.osaio.bean.ApDeviceInfo;

public interface DvDeviceListListener {

    void onItemClick(ApDeviceInfo deviceInfo);

    void onChangeSleep(ApDeviceInfo deviceInfo, boolean isOpen);
}
