package com.afar.osaio.smart.player.activity;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.nooie.sdk.media.NooieMediaPlayer;

/**
 * NooieBasePlayerActivity
 *
 * @author Administrator
 * @date 2019/4/26
 */
public class NooieBasePlayerActivity extends BasePlayerActivity {

    public NooieMediaPlayer player;

    @Override
    protected void init() {
        super.init();
        setupRecentDays();
    }

    @Override
    protected NooieMediaPlayer nooiePlayer() {
        return player;
    }

    public void setPlayerMargin(int left, int right) {
        if (player != null) {
            ConstraintLayout.LayoutParams playerParams = (ConstraintLayout.LayoutParams) player.getLayoutParams();
            playerParams.setMarginStart(left);
            playerParams.setMarginEnd(right);
            player.setLayoutParams(playerParams);
        }
    }

    public void setupRecentDays() {
        if (mSDCardDataList != null) {
            mSDCardDataList.clear();
            mSDCardDataList.addAll(getUtcRecent92Days());
        }

        if (mCloudDataList != null) {
            mCloudDataList.clear();
            mCloudDataList.addAll(getUtcRecent7Days());
        }
    }
}
