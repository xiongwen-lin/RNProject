package com.afar.osaio.smart.event;

/**
 * TabSelectedEvent
 *
 * @author Administrator
 * @date 2019/11/4
 */
public class TabSwitchEvent {
    public int position;
    public boolean listener;

    public TabSwitchEvent(int position, boolean listener) {
        this.position = position;
        this.listener = listener;
    }
}
