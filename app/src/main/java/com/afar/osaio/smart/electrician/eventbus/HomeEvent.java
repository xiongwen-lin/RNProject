package com.afar.osaio.smart.electrician.eventbus;

public class HomeEvent {

    private String eventType;

    public HomeEvent(String eventType){
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
