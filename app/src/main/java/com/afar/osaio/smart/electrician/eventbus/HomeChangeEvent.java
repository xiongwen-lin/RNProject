package com.afar.osaio.smart.electrician.eventbus;

public class HomeChangeEvent {

    private long homeId;

    public HomeChangeEvent(){

    }

    public HomeChangeEvent(long homeId){
        this.homeId = homeId;
    }
}
