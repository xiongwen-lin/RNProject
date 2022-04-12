package com.afar.osaio.smart.electrician.eventbus;

public class HomeLoadingEvent {

    //用與刷新
    private String eventType;

    public HomeLoadingEvent(String eventType){
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

}
