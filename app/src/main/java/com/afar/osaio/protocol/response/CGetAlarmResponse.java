package com.afar.osaio.protocol.response;

import androidx.annotation.NonNull;

public class CGetAlarmResponse extends CResponseImpl {
    private int alarmState;
    private int talkState;
    private byte[] value;

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public boolean parse(@NonNull byte[] data) {
        value = data;
        boolean result = super.parse(data);
        if (result) {
            alarmState = (int)data[3];
            talkState = (int)data[4];
        } else {
            return result;
        }
        return true;
    }

    public int getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(int alarmState) {
        this.alarmState = alarmState;
    }

    public int getTalkState() {
        return talkState;
    }

    public void setTalkState(int talkState) {
        this.talkState = talkState;
    }
}
