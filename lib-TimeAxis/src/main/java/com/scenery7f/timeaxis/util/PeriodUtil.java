package com.scenery7f.timeaxis.util;

import android.content.Context;
import androidx.core.content.ContextCompat;

import com.scenery7f.timeaxis.R;
import com.scenery7f.timeaxis.model.RecordType;

public class PeriodUtil {

    public static int getPeriodColor(Context context, RecordType recordType) {
        int color = ContextCompat.getColor(context, R.color.playback_mark_normal);
        switch (recordType) {
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
        return color;
    }

}
