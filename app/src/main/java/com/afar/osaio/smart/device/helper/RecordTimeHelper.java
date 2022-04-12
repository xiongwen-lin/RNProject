package com.afar.osaio.smart.device.helper;

import android.content.Context;
import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.afar.osaio.smart.device.bean.CloudRecordInfo;
import com.nooie.common.utils.time.DateTimeUtil;
import com.scenery7f.timeaxis.model.PeriodTime;

import java.util.Calendar;

/**
 * Created by victor on 2018/7/18
 * Email is victor.qiao.0604@gmail.com
 */
public class RecordTimeHelper {
    public static PeriodTime toPeriodTime(Context context, CloudRecordInfo info) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(info.getStartTime());

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(info.getStartTime() + info.getTimeLen());

        int color = ContextCompat.getColor(context, R.color.playback_mark_normal);
        switch (info.getRecordType()) {
            case PLAN_RECORD:
                color = ContextCompat.getColor(context, R.color.playback_mark_plan);
                break;
            case ALERT_RECORD:
                color = ContextCompat.getColor(context, R.color.playback_mark_alarm);
                break;
            case MOTION_RECORD:
                color = ContextCompat.getColor(context, R.color.playback_mark_motion);
                break;
            case SOUND_RECORD:
                color = ContextCompat.getColor(context, R.color.playback_mark_sound);
                break;
            case PIR_RECORD:
                color = ContextCompat.getColor(context, R.color.playback_mark_pir);
                break;
            case ALERT_JUST_MARK:
                color = ContextCompat.getColor(context, R.color.playback_mark_normal);
                break;
        }
        return new PeriodTime(start, end, color, info.getRecordType());
    }

    public static PeriodTime toUtcPeriodTime(Context context, CloudRecordInfo info) {
        Calendar start = DateTimeUtil.getUtcCalendar();
        start.setTimeInMillis(info.getStartTime());

        Calendar end = DateTimeUtil.getUtcCalendar();
        end.setTimeInMillis(info.getStartTime() + info.getTimeLen());

        int color = ContextCompat.getColor(context, R.color.playback_mark_normal);
        switch (info.getRecordType()) {
            case PLAN_RECORD:
                color = ContextCompat.getColor(context, R.color.playback_mark_plan);
                break;
            case ALERT_RECORD:
                color = ContextCompat.getColor(context, R.color.playback_mark_alarm);
                break;
            case MOTION_RECORD:
                color = ContextCompat.getColor(context, R.color.playback_mark_motion);
                break;
            case SOUND_RECORD:
                color = ContextCompat.getColor(context, R.color.playback_mark_sound);
                break;
            case PIR_RECORD:
                color = ContextCompat.getColor(context, R.color.playback_mark_pir);
                break;
            case ALERT_JUST_MARK:
                color = ContextCompat.getColor(context, R.color.playback_mark_normal);
                break;
        }
        return new PeriodTime(start, end, color, info.getRecordType(), info.getRecordTypes());
    }
}
