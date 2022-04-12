package com.afar.osaio.smart.push.bean;

/**
 * JPushFeedbackMessageExtras
 *
 * @author Administrator
 * @date 2019/4/25
 */
public class PushBatteryMessageExtras extends PushMessageBaseExtras {

    private String name;
    private String uuid;
    private int battery_level;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getBattery_level() {
        return battery_level;
    }

    public void setBattery_level(int battery_level) {
        this.battery_level = battery_level;
    }
}
