package com.afar.osaio.base;

import android.app.Activity;

import com.afar.osaio.smart.electrician.activity.GroupScheduleActivity;
import com.afar.osaio.smart.electrician.activity.GroupSettingActivity;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by victor on 2018/8/6
 * Email is victor.qiao.0604@gmail.com
 */
public class ActivityStack {
    private static final ActivityStack ACTIVITY_STACK = new ActivityStack();

    private List<Activity> activityList = new ArrayList<>();

    private void ActivityStack() {
    }

    public static ActivityStack instance() {
        return ACTIVITY_STACK;
    }


    public void add(Activity activity) {
        if (activity != null && !activityList.contains(activity)) activityList.add(activity);
    }

    public void remove(Activity activity) {
        if (activity != null && activityList.contains(activity)) {
            activityList.remove(activity);
            activity.finish();
        }
    }

    public void exit() {
        while (!activityList.isEmpty()) {
            remove(activityList.get(0));
        }
    }

    public void clearAll(Activity activity) {
        if (CollectionUtil.isNotEmpty(activityList)) {
            Iterator<Activity> iterator = activityList.iterator();
            while (iterator.hasNext()) {
                Activity removeActivity = iterator.next();
                if (activity != null && removeActivity != activity) {
                    removeActivity.finish();
                    iterator.remove();
                }
            }

            if (activity != null) {
                activityList.clear();
                activity.finish();
            }
        }

    }

    public void removeGroupSettingActivity() {
        for (Activity activity : activityList) {
            if (activity instanceof GroupSettingActivity) {
                activityList.remove(activity);
                activity.finish();
                return;
            }
        }
    }

    public void removeGroupScheduleActivity() {
        for (Activity activity : activityList) {
            if (activity instanceof GroupScheduleActivity) {
                activityList.remove(activity);
                activity.finish();
                return;
            }
        }
    }

    public Activity getCurrentActivity() {
        if (CollectionUtil.isNotEmpty(activityList)) {
            return null;
        }
        try {
            int lastIndex = activityList.size() - 1;
            return activityList.get(lastIndex);
        } catch (Exception e) {
        }
        return null;
    }
}
