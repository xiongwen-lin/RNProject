package com.afar.osaio.smart.push.bean;

/**
 * JPushOrderMessageExtras
 *
 * @author Administrator
 * @date 2019/4/25
 */
public class PushOrderMessageExtras extends PushMessageBaseExtras {

    private String order;
    private int type;
    private String device;
    private long time;
    private String pack;

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }
}
