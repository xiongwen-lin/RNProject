package com.afar.osaio.smart.electrician.eventbus;

public class HomeFamilyChangeEvent {

    private long homeId;

    public HomeFamilyChangeEvent(long homeId) {
        this.homeId = homeId;
    }

    public long getHomeId() {
        return homeId;
    }

    public void setHomeId(long homeId) {
        this.homeId = homeId;
    }
}

