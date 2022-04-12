package com.afar.osaio.smart.routerlocal;

import com.afar.osaio.smart.device.bean.ParentalControlDeviceInfo;
import com.afar.osaio.smart.device.bean.ParentalControlRuleInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UpdataRouterConnectDeviceInfo {

    private static List<ParentalControlDeviceInfo> parentalControlDeviceInfos;
    private static JSONArray ruleJson;

    /**
     * 父母控制  连接设备区分
     * @param info
     */
    public static List<ParentalControlDeviceInfo> sortParentalDevice(String info, JSONArray onlineMsgJson) {
        parentalControlDeviceInfos = new ArrayList<>();
        if (info == null || "".equals(info)) {
            return parentalControlDeviceInfos;
        }

        if (onlineMsgJson == null) {
            return parentalControlDeviceInfos;
        }

        try {
            JSONObject jsonObject = new JSONObject(info);
            ruleJson = jsonObject.getJSONArray("rule");
            if (ruleJson.length() <= 0) {
                return parentalControlDeviceInfos;
            }

            for (int j = 0; j < onlineMsgJson.length(); j++) {
                for (int i = 0; i < ruleJson.length(); i++) {
                    if (onlineMsgJson.getJSONObject(j).getString("mac").equals(ruleJson.getJSONObject(i).getString("mac"))) {
                        List<Integer> dayList = new ArrayList<>();
                        List<Integer> weekDays = new ArrayList<>();
                        List<String> timeList = splitTime(ruleJson.getJSONObject(i).getString("time"));
                        if (dayList.size() > 0) {
                            dayList.clear();
                        }
                        for (int k = 0; k < timeList.size() - 4; k++) {
                            dayList.add(Integer.parseInt(timeList.get(k)));
                        }

                        for (int h = 0; h < dayList.size(); h++) {
                            int ruleDay = dayList.get(h);
                            // 原日期采用国外方式,周天为一周第一天,也就是用了1代表  周1--6 用了 2--7代表,这边设置路由器需要转化下格式
                            // ruleDay 为路由器获取数据 按 1--7 排列周一到周天（所以要变为原先的日期排序）
                            if (dayList.get(h) == 7) {
                                ruleDay = 1;
                            } else {
                                ruleDay = ruleDay + 1;
                            }
                            weekDays.add(ruleDay);
                        }
                        ParentalControlDeviceInfo parentalControlRuleInfo = new ParentalControlDeviceInfo(
                                onlineMsgJson.getJSONObject(j).getString("mac"),
                                onlineMsgJson.getJSONObject(j).getString("name"),
                                timeList.get(timeList.size() - 4) + ":" + timeList.get(timeList.size() - 3),
                                timeList.get(timeList.size() - 2) + ":" + timeList.get(timeList.size() - 1),
                                weekDays, true, false);
                        parentalControlDeviceInfos.add(parentalControlRuleInfo);
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parentalControlDeviceInfos;
    }

    /**
     * 父母控制  时间规则
     * @param info
     * @param deviceMac
     * @return
     */
    public static List<ParentalControlRuleInfo> sortParentalRules(String info, String deviceMac) {
        List<ParentalControlRuleInfo> parentalControlRuleInfos = new ArrayList<>();
        if (info == null || "".equals(info)) {
            return parentalControlRuleInfos;
        }

        try {
            JSONObject jsonObject = new JSONObject(info);
            ruleJson = jsonObject.getJSONArray("rule");
            if (ruleJson.length() <= 0) {
                return parentalControlRuleInfos;
            }
            for (int i = 0; i < ruleJson.length(); i++) {
                List<String> dayList = new ArrayList<>();
                if (deviceMac.equals(ruleJson.getJSONObject(i).getString("mac"))) {
                    List<String> timeList = splitTime(ruleJson.getJSONObject(i).getString("time"));
                    if (dayList.size() > 0) {
                        dayList.clear();
                    }
                    for (int j = 0; j < timeList.size() - 4; j++) {
                        dayList.add(timeList.get(j));
                    }
                    ParentalControlRuleInfo parentalControlRuleInfo = new ParentalControlRuleInfo(
                            ruleJson.getJSONObject(i).getString("mac"), dayList,
                            timeList.get(timeList.size() - 4) , timeList.get(timeList.size() - 3),
                            timeList.get(timeList.size() - 2), timeList.get(timeList.size() - 1),
                            ruleJson.getJSONObject(i).getString("state"), ruleJson.getJSONObject(i).getString("desc"),
                            ruleJson.getJSONObject(i).getString("delRuleName"));
                    parentalControlRuleInfos.add(parentalControlRuleInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parentalControlRuleInfos;
    }

    private static List<String> splitTime(String timeInfo) {
        List<String> timeList = new ArrayList<>();
        String[] splitTime = timeInfo.split(",");
        String[] splitDay = splitTime[0].split(";");
        String[] splitStartTime = splitTime[1].split(":");
        String[] splitEndTime = splitTime[2].split(":");

        for (int i = 0; i < splitDay.length; i++) {
            if (splitDay[i] != null && !"".equals(splitDay[i])) {
                timeList.add(splitDay[i]);
            }
        }

        for (int i = 0; i < splitStartTime.length; i++) {
            timeList.add(splitStartTime[i]);
        }

        for (int i = 0; i < splitEndTime.length; i++) {
            timeList.add(splitEndTime[i]);
        }
        return timeList;
    }

    public static List<ParentalControlRuleInfo> getWifiRules(String info) throws JSONException {
        List<ParentalControlRuleInfo> parentalControlRuleInfos = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(info);
        JSONArray ruleJson = new JSONArray();
        ruleJson = jsonObject.getJSONArray("rule");
        if (ruleJson.length() > 0) {
            List<String> dayList = new ArrayList<>();
            for (int i = 0; i < ruleJson.length(); i++) {
                List<String> timeList = splitDay(ruleJson.getJSONObject(i).getString("week"));
                if (dayList.size() > 0) {
                    dayList.clear();
                }
                for (int j = 0; j < timeList.size(); j++) {
                    dayList.add(timeList.get(j));
                }
                ParentalControlRuleInfo parentalControlRuleInfo = new ParentalControlRuleInfo(dayList,
                        ruleJson.getJSONObject(i).getString("sHour"), ruleJson.getJSONObject(i).getString("sMinute"),
                        ruleJson.getJSONObject(i).getString("eHour"),ruleJson.getJSONObject(i).getString("eMinute"),
                        jsonObject.getString("enable"), ruleJson.getJSONObject(i).getString("desc"),
                        ruleJson.getJSONObject(i).getString("delRuleName"));
                parentalControlRuleInfos.add(parentalControlRuleInfo);
            }
        }
        return parentalControlRuleInfos;
    }

    private static List<String> splitDay(String timeInfo) {
        List<String> timeList = new ArrayList<>();
        String[] days = timeInfo.split(";");
        for (int i = 0; i < days.length; i++) {
            timeList.add(days[i]);
        }
        return timeList;
    }
}
