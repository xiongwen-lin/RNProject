package com.afar.osaio.smart.electrician.eventbus;

public class ListStyleSwitchEvent {
    private int sortType;

    public ListStyleSwitchEvent(int sortType) {
        this.sortType = sortType;
    }

    public int getSortType() {
        return sortType;
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
    }
}
