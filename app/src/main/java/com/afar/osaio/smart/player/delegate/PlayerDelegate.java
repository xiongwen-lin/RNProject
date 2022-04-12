package com.afar.osaio.smart.player.delegate;

public class PlayerDelegate implements IPlayerDelegate {

    private PlayState mPlayState;
    private long mLoadStorageTime;

    public PlayerDelegate() {
        mPlayState = new PlayState();
    }

    public PlayState getPlayState() {
        return mPlayState;
    }

    public void setPlayState(int type, int state, long time) {
        if (mPlayState == null) {
            mPlayState = new PlayState();
        }
        mPlayState.setType(type);
        mPlayState.setState(state);
        mPlayState.setTime(time);
    }

    public boolean isLiveStarting() {
        return mPlayState != null && mPlayState.getState() == PlayState.PLAY_TYPE_LIVE && mPlayState.getState() == PlayState.PLAY_STATE_START && (System.currentTimeMillis() - mPlayState.getTime() < PlayState.CMD_TIMEOUT);
    }

    public boolean isPlayStarting() {
        return mPlayState != null && mPlayState.getState() == PlayState.PLAY_STATE_START && (Math.abs(System.currentTimeMillis() - mPlayState.getTime()) < PlayState.CMD_TIMEOUT);
    }

    public void setLoadStorageTime(long loadStorageTime) {
        mLoadStorageTime = loadStorageTime;
    }

    public long getLoadStorageTime() {
        return mLoadStorageTime;
    }

    @Override
    public boolean isLoadStorageFinish() {
        return isLoadStorageFinish(mLoadStorageTime);
    }

    private boolean isLoadStorageFinish(long storageTime) {
        return System.currentTimeMillis() - storageTime > 0;
    }
}
