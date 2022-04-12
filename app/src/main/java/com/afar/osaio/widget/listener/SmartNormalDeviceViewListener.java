package com.afar.osaio.widget.listener;

import com.afar.osaio.smart.home.bean.SmartBaseDevice;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/11/30 9:52 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public interface SmartNormalDeviceViewListener {

    void onSwitchBtnClick(SmartBaseDevice device, boolean on);

}
