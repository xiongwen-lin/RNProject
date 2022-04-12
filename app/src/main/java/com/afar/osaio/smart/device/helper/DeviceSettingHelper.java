package com.afar.osaio.smart.device.helper;

import android.text.TextUtils;

import com.afar.osaio.bean.DetectionSchedule;
import com.nooie.common.bean.CConstant;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.data.DataHelper;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.entity.DeviceConfig;
import com.nooie.sdk.api.network.base.bean.entity.PIRPlanConfig;
import com.nooie.sdk.api.network.base.bean.entity.PresetPointConfigure;
import com.nooie.sdk.device.bean.hub.PirPlan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeviceSettingHelper {

    private static final int PIR_PLAN_DURATION_NUM = 48;
    private static final int PIR_PLAN_DURATION = 30;
    private static final int WEEK_DAY_LEN = 7;
    private static final int PIR_PLAN_MAX_SIZE = 3;

    public static PirPlan convertPirPlanFromConfig(DeviceConfig deviceConfig) {
        PirPlan pirPlan = new PirPlan();
        pirPlan.plan = new boolean[WEEK_DAY_LEN * PIR_PLAN_DURATION_NUM];

        if (deviceConfig != null && CollectionUtil.isNotEmpty(deviceConfig.getPIRPlanList())) {
            for (int i = 0; i < CollectionUtil.size(deviceConfig.getPIRPlanList()); i++) {
                boolean[] tmpPirPlan = convertPirPlanValue(deviceConfig.getPIRPlanList().get(i));
                logPirPlanForBoolean("DeviceConfig valid day id=" + deviceConfig.getPIRPlanList().get(i).getId() + " list  convert result " , tmpPirPlan);
                if (pirPlan.plan.length == tmpPirPlan.length) {
                    for (int j = 0; j < pirPlan.plan.length; j++) {
                        pirPlan.plan[j] = pirPlan.plan[j] || tmpPirPlan[j];
                    }
                }
            }
        }

        logPirPlanForBoolean("DeviceConfig valid day all list convert result " , pirPlan.plan);

        return pirPlan;
    }

    public static boolean[] convertPirPlanValue(PIRPlanConfig pirPlanConfig) {
        boolean[] pirPlan = new boolean[WEEK_DAY_LEN * PIR_PLAN_DURATION_NUM];

        if (pirPlanConfig == null || CollectionUtil.isEmpty(pirPlanConfig.getWeekArr())) {
            return pirPlan;
        }

        boolean[] pirPlanTimes = convertPirPlanTimes(pirPlanConfig.getStartTime(), pirPlanConfig.getEndTime());
        logPirPlanForBoolean("PirPlan valid day list convert schedule from " + pirPlanConfig.getStartTime() + " to " + pirPlanConfig.getEndTime(), pirPlanTimes);
        for (int i = 0; i < WEEK_DAY_LEN; i++) {
            boolean isDaySelected = CollectionUtil.isNotEmpty(pirPlanConfig.getWeekArr()) && i < CollectionUtil.size(pirPlanConfig.getWeekArr()) && pirPlanConfig.getWeekArr().get(i) == 1;
            if (!isDaySelected) {
                continue;
            }

            for (int j = PIR_PLAN_DURATION_NUM * i; j < PIR_PLAN_DURATION_NUM * (i + 1); j++) {
                pirPlan[j] = pirPlanTimes[j % PIR_PLAN_DURATION_NUM];
            }
        }

        return pirPlan;

    }

    public static boolean[] convertPirPlanTimes(int start, int end) {
        int startIndex = start / PIR_PLAN_DURATION;
        int endIndex = end / PIR_PLAN_DURATION;
        boolean[] pirPlanTimes = new boolean[PIR_PLAN_DURATION_NUM];
        if (startIndex > endIndex) {
            return pirPlanTimes;
        }

        if (startIndex == endIndex) {
            pirPlanTimes[startIndex] = true;
            return pirPlanTimes;
        }

        for (int i = 0; i < pirPlanTimes.length; i++) {
            pirPlanTimes[i] = (i >= startIndex && i < endIndex);
        }

        return pirPlanTimes;
    }

    public static List<Integer> convertPIRPlanWeekDayArr(boolean open, List<Integer> weekDayKeys) {
        List<Integer> weekDayArr = new ArrayList<>();
        for (int i = 0; i < WEEK_DAY_LEN; i++) {
            if (open) {
                weekDayArr.add(CollectionUtil.isNotEmpty(weekDayKeys) && weekDayKeys.contains(i) ? 1 : 0);
            } else {
                weekDayArr.add(0);
            }
        }
        return weekDayArr;
    }

    public static List<DetectionSchedule> convertDetectionSchedulesFromConfig(DeviceConfig deviceConfig) {
        List<DetectionSchedule> detectionSchedules = new ArrayList<>();
        if (deviceConfig == null || CollectionUtil.isEmpty(deviceConfig.getPIRPlanList())) {
            return detectionSchedules;
        }

        for (int i = 0; i < CollectionUtil.size(deviceConfig.getPIRPlanList()); i++) {
            PIRPlanConfig pirPlanConfig = deviceConfig.getPIRPlanList().get(i);
            if (pirPlanConfig != null) {
                DetectionSchedule schedule = new DetectionSchedule(pirPlanConfig.getStartTime(), pirPlanConfig.getEndTime(), false);
                List<Integer> weekDays = new ArrayList<>();
                if (CollectionUtil.isNotEmpty(pirPlanConfig.getWeekArr())) {
                    for (int j = 0; j < pirPlanConfig.getWeekArr().size(); j++) {
                        if (pirPlanConfig.getWeekArr().get(j) == 1) {
                            weekDays.add(DateTimeUtil.convertWeekDay(j));
                        }
                    }
                }
                schedule.setId(pirPlanConfig.getId());
                schedule.setWeekDays(weekDays);
                schedule.setOpen(CollectionUtil.isNotEmpty(weekDays));
                schedule.setEffective(true);
                detectionSchedules.add(schedule);
            }
        }

        return detectionSchedules;
    }

    public static DeviceConfig createDeviceConfig() {
        DeviceConfig deviceConfig = new DeviceConfig();
        List<PIRPlanConfig> pirPlanConfigs = new ArrayList<>();
        PIRPlanConfig pirPlanConfig = new PIRPlanConfig();
        pirPlanConfig.setId(1);
        pirPlanConfig.setStartTime(0);
        pirPlanConfig.setEndTime(1440);
        List<Integer> weekArr = new ArrayList<>();
        for (int i = 0; i < WEEK_DAY_LEN; i++) {
            weekArr.add(1);
        }
        pirPlanConfig.setWeekArr(weekArr);
        pirPlanConfigs.add(pirPlanConfig);
        deviceConfig.setPIRPlanList(pirPlanConfigs);
        return deviceConfig;
    }

    public static DeviceConfig createDeviceConfigForPresetPoint() {
        DeviceConfig deviceConfig = new DeviceConfig();
        return deviceConfig;
    }

    public static void logPirPlanForBoolean(String logTag, boolean[] list) {
        StringBuilder listSb =  new StringBuilder();
        if (list != null && list.length > 0) {
            for (int i = 0; i < list.length; i++) {
                listSb.append(list[i] ? "1" : "0");
            }
        }
        NooieLog.d("-->> DeviceSettingHelper logPirPlanForBoolean tag " + logTag + " {r list=" + listSb.toString());
    }

    public static final int PRESET_POINT_MAX_LEN = 3;
    public static final int PRESET_POINT_FIRST_POSITION = 1;
    public static final int PRESET_POINT_DEFAULT_POSITION = 0;
    //预设点排序id从1开始
    public static final int PRESET_POINT_ID_START_INDEX = 1;
    public static List<PresetPointConfigure> updatePresetPointConfigureList(List<PresetPointConfigure> presetPointConfigures, String name, int position) {
        if (CollectionUtil.isEmpty(presetPointConfigures)) {
            List<PresetPointConfigure> result = new ArrayList<>();
            PresetPointConfigure presetPointConfigure = new PresetPointConfigure();
            presetPointConfigure.setId(PRESET_POINT_ID_START_INDEX);
            presetPointConfigure.setName(name);
            presetPointConfigure.setPosition(position);
            result.add(presetPointConfigure);
            return result;
        }

        List<Integer> presetPointPositions = new ArrayList<>();
        for (PresetPointConfigure presetPointConfigure : CollectionUtil.safeFor(presetPointConfigures)) {
            if (presetPointConfigure != null) {
                presetPointPositions.add(presetPointConfigure.getPosition());
            }
        }
        if (presetPointPositions.contains(position)) {
            for (int i = 0; i < CollectionUtil.size(presetPointConfigures); i++) {
                if (presetPointConfigures.get(i) != null && presetPointConfigures.get(i).getPosition() == position) {
                    presetPointConfigures.get(i).setName(name);
                    break;
                }
            }
        } else if (CollectionUtil.size(presetPointConfigures) < PRESET_POINT_MAX_LEN) {
            PresetPointConfigure presetPointConfigure = new PresetPointConfigure();
            presetPointConfigure.setId(CollectionUtil.size(presetPointConfigures) + 1);
            presetPointConfigure.setName(name);
            presetPointConfigure.setPosition(position);
            presetPointConfigures.add(presetPointConfigure);
        }
        return presetPointConfigures;
    }

    public static List<PresetPointConfigure> filterPresetPointConfigureList(List<PresetPointConfigure> presetPointConfigures) {
        if (CollectionUtil.isEmpty(presetPointConfigures)) {
            return presetPointConfigures;
        }
        List<Integer> presetPointPositions = new ArrayList<>();
        Iterator<PresetPointConfigure> configureIterator = presetPointConfigures.iterator();
        while (configureIterator.hasNext()) {
            PresetPointConfigure presetPointConfigure = configureIterator.next();
            if (checkPresetPointValid(presetPointConfigure) && !presetPointPositions.contains(presetPointConfigure.getPosition())) {
                presetPointPositions.add(presetPointConfigure.getPosition());
            } else {
                configureIterator.remove();
            }
        }
        return presetPointConfigures;
    }

    public static boolean checkPresetPointValid(PresetPointConfigure presetPointConfigure) {
        return presetPointConfigure != null && checkPresetPointValid(presetPointConfigure.getPosition());
    }

    public static boolean checkPresetPointValid(int position) {
        return position >= 1 && position <= PRESET_POINT_MAX_LEN;
    }

    public static int getCorrectPresetPointPosition(int position, List<PresetPointConfigure> presetPointConfigures) {
        if (CollectionUtil.isEmpty(presetPointConfigures)) {
            return PRESET_POINT_FIRST_POSITION;
        }

        if (!checkPresetPointValid(position)) {
            position = PRESET_POINT_MAX_LEN;
        }
        List<Integer> presetPointPositions = new ArrayList<>();
        for (PresetPointConfigure presetPointConfigure : presetPointConfigures) {
            if (DeviceSettingHelper.checkPresetPointValid(presetPointConfigure)) {
                presetPointPositions.add(presetPointConfigure.getPosition());
            }
        }
        if (CollectionUtil.isEmpty(presetPointPositions) || (CollectionUtil.size(presetPointPositions) >= PRESET_POINT_MAX_LEN && presetPointPositions.contains(position))) {
            return position;
        }

        List<Integer> unAddPresetPointPositions = new ArrayList<>();
        for (int i = 1; i <= PRESET_POINT_MAX_LEN; i++) {
            if (!presetPointPositions.contains(i)) {
                unAddPresetPointPositions.add(i);
            }
        }

        return CollectionUtil.isEmpty(unAddPresetPointPositions) ? position : unAddPresetPointPositions.get(0);
    }

    public static int getPresetPointPositionByPath(String path) {
        if (TextUtils.isEmpty(path) || !path.contains(FileUtil.PRESET_POINT_PREFIX)) {
            return DeviceSettingHelper.PRESET_POINT_FIRST_POSITION;
        }
        try {
            NooieLog.d("-->> debug DeviceSettingHelper getPresetPointPositionByPath: 1 path =" + path);
            String positionStr = path.substring((path.indexOf(FileUtil.PRESET_POINT_PREFIX) + FileUtil.PRESET_POINT_PREFIX.length()), path.lastIndexOf(CConstant.UNDER_LINE));
            NooieLog.d("-->> debug DeviceSettingHelper getPresetPointPositionByPath: 2 path =" + path + " positionStr=" + positionStr + " position=" + DataHelper.toInt(positionStr));
            return DataHelper.toInt(positionStr);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return DeviceSettingHelper.PRESET_POINT_FIRST_POSITION;
    }

    public static int getTempPresetPointPositionByPath(String path) {
        if (TextUtils.isEmpty(path) || !path.contains(FileUtil.TEMP_PRESET_POINT_PREFIX)) {
            return DeviceSettingHelper.PRESET_POINT_FIRST_POSITION;
        }
        try {
            NooieLog.d("-->> debug DeviceSettingHelper getTempPresetPointPositionByPath: 1 path =" + path);
            String positionStr = path.substring((path.indexOf(FileUtil.TEMP_PRESET_POINT_PREFIX) + FileUtil.TEMP_PRESET_POINT_PREFIX.length()), path.lastIndexOf(CConstant.UNDER_LINE));
            NooieLog.d("-->> debug DeviceSettingHelper getTempPresetPointPositionByPath: 2 path =" + path + " positionStr=" + positionStr + " position=" + DataHelper.toInt(positionStr));
            return DataHelper.toInt(positionStr);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return DeviceSettingHelper.PRESET_POINT_FIRST_POSITION;
    }

    public static boolean checkPowerOnPresetPointChange(List<PresetPointConfigure> presetPointConfigures, List<PresetPointConfigure> lastPresetPointConfigures) {
        if (CollectionUtil.isEmpty(presetPointConfigures)) {
            return false;
        }
        if (CollectionUtil.isEmpty(lastPresetPointConfigures)) {
            return true;
        }
        boolean isPowerOnPresetPointChange = !(presetPointConfigures.get(0) != null && lastPresetPointConfigures.get(0) != null
                && presetPointConfigures.get(0).getPosition() == lastPresetPointConfigures.get(0).getPosition());
        return isPowerOnPresetPointChange;
    }

    public static List<PresetPointConfigure> sortPresetPointConfigureList(List<PresetPointConfigure> presetPointConfigures) {
        if (CollectionUtil.isEmpty(presetPointConfigures)) {
            return new ArrayList<>();
        }
        List<PresetPointConfigure> sorPresetPointList = new ArrayList<>();
        for (int i = 0; i < presetPointConfigures.size(); i++) {
            PresetPointConfigure presetPointConfigure = presetPointConfigures.get(i);
            if (presetPointConfigure != null) {
                presetPointConfigure.setId(i + 1);
                sorPresetPointList.add(presetPointConfigure);
            }
        }
        return  sorPresetPointList;
    }

    public static List<PresetPointConfigure> deletePresetPointConfigure(int position, List<PresetPointConfigure> presetPointConfigures) {
        if (CollectionUtil.isEmpty(presetPointConfigures)) {
            return new ArrayList<>();
        }
        Iterator<PresetPointConfigure> iterator = presetPointConfigures.iterator();
        while (iterator.hasNext()) {
            PresetPointConfigure configure = iterator.next();
            if (configure != null && configure.getPosition() == position) {
                iterator.remove();
            }
        }
        return  presetPointConfigures;
    }

}
