package com.afar.osaio.smart.event;

/**
 * TabSelectedEvent
 *
 * @author Administrator
 * @date 2019/11/4
 */
public class SelectPortraitEvent {
    public String photoPath;

    public SelectPortraitEvent(String photoPath) {
        this.photoPath = photoPath;
    }
}
