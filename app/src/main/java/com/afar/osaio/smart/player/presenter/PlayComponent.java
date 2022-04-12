package com.afar.osaio.smart.player.presenter;

import com.afar.osaio.smart.player.delegate.PlayerDelegate;
import com.nooie.sdk.media.NooieMediaPlayer;

public class PlayComponent extends BaseComponent {

    public NooieMediaPlayer player;
    public PlayerDelegate mPlayerDelegate;

    public void setPlayer(NooieMediaPlayer player) {
        this.player = player;
    }

    public void setPlayerDelegate(PlayerDelegate playerDelegate) {
        this.mPlayerDelegate = playerDelegate;
    }
}
