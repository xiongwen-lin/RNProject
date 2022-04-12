package com.scenery7f.timeaxis.model;

public enum RecordType {

    PLAN_RECORD(1),
    ALERT_RECORD(2),
    MOTION_RECORD(3),
    SOUND_RECORD(4),
    PIR_RECORD(5),
    ALERT_JUST_MARK(-1);

    private int num;

    private RecordType(int num) {
        this.num = num;
    }

    public int getValue() {
        return this.num;
    }

    public static RecordType getType(int num) {
        if (PLAN_RECORD.num == num) {
            return PLAN_RECORD;
        } else if (ALERT_RECORD.num == num) {
            return ALERT_RECORD;
        } else if (MOTION_RECORD.num == num) {
            return MOTION_RECORD;
        } else if (SOUND_RECORD.num == num) {
            return SOUND_RECORD;
        } else if (PIR_RECORD.num == num) {
            return PIR_RECORD;
        } else {
            return ALERT_JUST_MARK.num == num ? ALERT_JUST_MARK : null;
        }
    }

}
