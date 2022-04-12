package com.afar.osaio.smart.player.delegate;

public class PlayState {

    public static final int PLAY_TYPE_LIVE = 1;
    public static final int PLAY_TYPE_CLOUD_PLAYBACK = 2;
    public static final int PLAY_TYPE_SD_PLAYBACK = 3;

    public static final int PLAY_STATE_INIT = 0;
    public static final int PLAY_STATE_START = 1;
    public static final int PLAY_STATE_FINISH = 2;

    public static final long CMD_TIMEOUT = 10 * 1000L;

    private int type;
    private int state;
    private long time;

    public PlayState() {
        type = PLAY_TYPE_LIVE;
        state = PLAY_STATE_INIT;
    }

    public PlayState(int type, int state, long time) {
        this.type = type;
        this.state = state;
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
