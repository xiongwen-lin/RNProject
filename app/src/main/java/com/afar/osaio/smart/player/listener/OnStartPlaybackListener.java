package com.afar.osaio.smart.player.listener;

public interface OnStartPlaybackListener {

    void onPreStartPlayback(String deviceId, boolean isCloud, boolean isExist);
}
