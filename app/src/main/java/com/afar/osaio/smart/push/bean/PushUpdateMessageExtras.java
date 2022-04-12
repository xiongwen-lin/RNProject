package com.afar.osaio.smart.push.bean;

/**
 * JPushUpdateMessageExtras
 *
 * @author Administrator
 * @date 2019/4/25
 */
public class PushUpdateMessageExtras extends PushMessageBaseExtras {

    private String uuid;
    private String msg;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
