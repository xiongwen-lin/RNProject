package com.afar.osaio.protocol.bean;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public enum Week {
    Mon(0),
    Tues(1),
    Wed(2),
    Thur(3),
    Fri(4),
    Sat(5),
    Sun(6);

    private int num;

    public static Week getWeek(int type) {
        if (Mon.num == type) {
            return Mon;
        } else if (Tues.num == type) {
            return Tues;
        } else if (Wed.num == type) {
            return Wed;
        } else if (Thur.num == type) {
            return Thur;
        } else if (Fri.num == type) {
            return Fri;
        } else {
            return Sat.num == type ? Sat : Sun;
        }
    }

    Week(int num) {
        this.num = num;
    }

    public int getNum() {
        return this.num;
    }
}
