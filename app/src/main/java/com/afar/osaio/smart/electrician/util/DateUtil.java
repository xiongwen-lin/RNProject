package com.afar.osaio.smart.electrician.util;


import com.afar.osaio.util.ConstantValue;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static String getWeek() {

        Calendar cal = Calendar.getInstance();
        int i = cal.get(Calendar.DAY_OF_WEEK);

        switch (i) {
            case 1:
                return ConstantValue.SCHEDULE_SUNDAY;
            case 2:
                return ConstantValue.SCHEDULE_MONDAY;
            case 3:
                return ConstantValue.SCHEDULE_TUESDAY;
            case 4:
                return ConstantValue.SCHEDULE_WEDNESDAY;
            case 5:
                return ConstantValue.SCHEDULE_THURSDAY;
            case 6:
            return ConstantValue.SCHEDULE_FRIDAY;
            case 7:
                return ConstantValue.SCHEDULE_SATERDAY;
            default:
                return ConstantValue.SCHEDULE_ONCE;
        }
    }


    public static int getHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.MINUTE);
    }

    public static int getSecond() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.SECOND);
    }

    public static int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static int getMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    public static int getDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }


}
