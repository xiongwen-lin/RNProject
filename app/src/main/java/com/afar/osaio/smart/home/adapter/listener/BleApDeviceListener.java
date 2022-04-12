package com.afar.osaio.smart.home.adapter.listener;

import com.nooie.sdk.db.entity.BleApDeviceEntity;

public interface BleApDeviceListener {

    void onItemClickListener(BleApDeviceEntity device);

    void onAccessClick(BleApDeviceEntity device);
}
