package com.afar.osaio.smart.event;

/**
 * TabSelectedEvent
 *
 * @author Administrator
 * @date 2019/11/4
 */
public class ViewPagerSwitchEvent {

    public static final int VIEWPAGER_SWITCH_OF_PREVIEW_VIDEO = 1;

    public int type;
    public int position;
    public boolean listener;

    public ViewPagerSwitchEvent(int type, int position, boolean listener) {
        this.type = type;
        this.position = position;
        this.listener = listener;
    }
}
