package com.afar.osaio.smart.electrician.bean;

import android.content.res.Resources;
import android.text.TextUtils;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.util.Base64Util;
import com.afar.osaio.smart.electrician.util.HexUtil;
import com.afar.osaio.smart.electrician.util.StringUtil;
import com.afar.osaio.util.ConstantValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.collection.CollectionUtil;
import com.tuya.smart.sdk.bean.Timer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ScheduleHelper {

    public final  String TIME_SPLIT = ":";

    private volatile static ScheduleHelper instance;

    private ScheduleHelper() {
    }

    public static ScheduleHelper getInstance() {
        if (instance == null) {
            synchronized (ScheduleHelper.class) {
                if (instance == null) {
                    instance = new ScheduleHelper();
                }
            }
        }
        return instance;
    }

    public Map<String, Object> convertDps(String dps) {
        if (!TextUtils.isEmpty(dps)) {
            Gson gson = new Gson();
            Map<String, Object> dpsMap = gson.fromJson(dps, new TypeToken<Map<String, Object>>() {
            }.getType());
            return dpsMap;
        }
        return null;
    }

    public String getLooper(List<Integer> weekList){
        StringBuilder looper = new StringBuilder("0000000");
        if (weekList == null || weekList.size() < 1){
            return looper.toString();
        }else {
            for (int i = 0; i < weekList.size(); i++) {
                switch (weekList.get(i)) {
                    case Calendar.MONDAY:
                        looper.replace(1,2,"1");
                        break;
                    case Calendar.TUESDAY:
                        looper.replace(2,3,"1");
                        break;
                    case Calendar.WEDNESDAY:
                        looper.replace(3,4,"1");
                        break;
                    case Calendar.THURSDAY:
                        looper.replace(4,5,"1");
                        break;
                    case Calendar.FRIDAY:
                        looper.replace(5,6,"1");
                        break;
                    case Calendar.SATURDAY:
                        looper.replace(6,7,"1");
                        break;
                    case Calendar.SUNDAY:
                        looper.replace(0,1,"1");
                        break;
                    default:
                        break;
                }
            }
        }
        return looper.toString();
    }

    public static List<MixScheduleBean> convertMixScheduleBean(List<Timer> timerList) {
        List<MixScheduleBean> mixScheduleBeanList = new ArrayList<>();
        if (CollectionUtil.isEmpty(timerList)){
            return mixScheduleBeanList;
        }
        for (Timer timerBeam : CollectionUtil.safeFor(timerList)) {
            MixScheduleBean mixScheduleBean = new MixScheduleBean();
            mixScheduleBean.setTimerBean(timerBeam);
            mixScheduleBeanList.add(mixScheduleBean);
        }
        return mixScheduleBeanList;
    }

    public static List<MixScheduleBean> convertMixScheduleBean(List<Schedule> scheduleList, List<Timer> timerList) {
        List<MixScheduleBean> mixScheduleBeanList = new ArrayList<>();
        if (CollectionUtil.isEmpty(scheduleList) && CollectionUtil.isEmpty(timerList)){
            return mixScheduleBeanList;
        }
        for (Timer timerBeam : CollectionUtil.safeFor(timerList)) {
            MixScheduleBean mixScheduleBean = new MixScheduleBean();
            mixScheduleBean.setTimerBean(timerBeam);
            mixScheduleBeanList.add(mixScheduleBean);
        }
        for (Schedule scheduleBean : CollectionUtil.safeFor(scheduleList)) {
            MixScheduleBean mixScheduleBean = new MixScheduleBean();
            mixScheduleBean.setScheduleBean(scheduleBean);
            mixScheduleBeanList.add(mixScheduleBean);
        }
        return mixScheduleBeanList;
    }

    public static List<PowerStripScheduleBean> convertPowerStripScheduleBean(List<Timer> timerList) {
        List<PowerStripScheduleBean> powerStripScheduleList = new ArrayList<>();
        if (CollectionUtil.isEmpty(timerList)){
            return powerStripScheduleList;
        }
        for (Timer timerBeam : CollectionUtil.safeFor(timerList)) {
            PowerStripScheduleBean powerStripScheduleBean = new PowerStripScheduleBean(timerBeam);
            powerStripScheduleList.add(powerStripScheduleBean);
        }
        return powerStripScheduleList;
    }

    //
    public String getBeginTimeDes(String scheduleTime, int startIndex, int endIndex){
        int onH = Integer.parseInt(scheduleTime.substring(startIndex, endIndex), 16) / ConstantValue.HOUR_MINUTE;
        int onM = Integer.parseInt(scheduleTime.substring(startIndex, endIndex), 16) % ConstantValue.HOUR_MINUTE;
        return String.format(ConstantValue.TIME_FORMAT_SHORT, onH) + TIME_SPLIT + String.format(ConstantValue.TIME_FORMAT_SHORT, onM);
    }

    public String getEndTimeDes(String scheduleTime,int startIndex, int endIndex){
        String off = scheduleTime.substring(startIndex, endIndex);
        int offH = Integer.parseInt(off, 16) / ConstantValue.HOUR_MINUTE;
        int offM = Integer.parseInt(off, 16) % ConstantValue.HOUR_MINUTE;
        return String.format(ConstantValue.TIME_FORMAT_SHORT, offH) + TIME_SPLIT + String.format(ConstantValue.TIME_FORMAT_SHORT, offM);
    }

    public String getLooperDes(String looper){
        Resources resources = NooieApplication.get().getResources();
        if (looper.equals("0000000")){
            return resources.getString(R.string.once);
        }
        if (looper.equals("1111111")){
            return resources.getString(R.string.every_day);
        }
        List<String> strList = StringUtil.getStrList(looper, 1);
        StringBuffer sbValue = new StringBuffer();
        for (int i = 1; i < strList.size(); i++) {
            if (strList.get(i).equals("1") && (i == 1)){
                if (sbValue.length() > 0) {
                    sbValue.append(" ");
                }
                sbValue.append( resources.getString(R.string.mon));
                continue;
            }
            if (strList.get(i).equals("1") && (i == 2)){
                if (sbValue.length() > 0) {
                    sbValue.append(" ");
                }
                sbValue.append( resources.getString(R.string.tues));
                continue;
            }
            if (strList.get(i).equals("1") && (i == 3)){
                if (sbValue.length() > 0) {
                    sbValue.append(" ");
                }
                sbValue.append( resources.getString(R.string.wed));
                continue;
            }
            if (strList.get(i).equals("1") && (i == 4)){
                if (sbValue.length() > 0) {
                    sbValue.append(" ");
                }
                sbValue.append( resources.getString(R.string.thurs));
                continue;
            }
            if (strList.get(i).equals("1") && (i == 5)){
                if (sbValue.length() > 0) {
                    sbValue.append(" ");
                }
                sbValue.append( resources.getString(R.string.fri));
                continue;
            }
            if (strList.get(i).equals("1") && (i == 6)){
                if (sbValue.length() > 0) {
                    sbValue.append(" ");
                }
                sbValue.append( resources.getString(R.string.sat));
                continue;
            }
        }
        if (strList.get(0).equals("1")){
            if (sbValue.length() > 0) {
                sbValue.append(" ");
            }
            sbValue.append( resources.getString(R.string.sun));
        }
        return sbValue.toString();
    }


    //根据星期hex获取日期描述
    public String getLoopWeekDes(String  hexStr){
        StringBuffer sbValue = new StringBuffer();
        int decimalStr = Integer.parseInt(hexStr,16);

        String weekHexStr =  addZeroForNum(Integer.toBinaryString(decimalStr),8);

        Resources resources = NooieApplication.get().getResources();

        if (weekHexStr.equals("01111111")){
            return resources.getString(R.string.every_day);
        }

        List<String> strList = StringUtil.getStrList(weekHexStr, 1);

        if (strList.get(6).equals("1")){
            if (sbValue.length() > 0) {
                sbValue.append(" ");
            }
            sbValue.append(resources.getString(R.string.mon));
        }

        if (strList.get(5).equals("1")){
            if (sbValue.length() > 0) {
                sbValue.append(" ");
            }
            sbValue.append(resources.getString(R.string.tues));
        }

        if (strList.get(4).equals("1")){
            if (sbValue.length() > 0) {
                sbValue.append(" ");
            }
            sbValue.append(resources.getString(R.string.wed));
        }

        if (strList.get(3).equals("1")){
            if (sbValue.length() > 0) {
                sbValue.append(" ");
            }
            sbValue.append(resources.getString(R.string.thurs));
        }

        if (strList.get(2).equals("1")){
            if (sbValue.length() > 0) {
                sbValue.append(" ");
            }
            sbValue.append(resources.getString(R.string.fri));
        }

        if (strList.get(1).equals("1")){
            if (sbValue.length() > 0) {
                sbValue.append(" ");
            }
            sbValue.append(resources.getString(R.string.sat));
        }

        if (strList.get(7).equals("1")){
            if (sbValue.length() > 0) {
                sbValue.append(" ");
            }
            sbValue.append(resources.getString(R.string.sun));
        }

        if (strList.get(7).equals("0") && (sbValue.length() == 0)){
            sbValue.append(resources.getString(R.string.once));
        }

        return sbValue.toString();

    }




    //判断是否显示循环定时的标志 当时时间段定时时不显示循环定时的标志
    public boolean isShowCycleTime(String cycleTime){
        int onInt = Integer.parseInt(cycleTime.substring(4, 8), 16) ;
        int offInt = Integer.parseInt(cycleTime.substring(8, 12), 16);
        int turnOn =  Integer.parseInt(cycleTime.substring(12, 16), 16);
        int turnOff = Integer.parseInt(cycleTime.substring(16, 20), 16);

        if ((turnOn == Math.abs(onInt - offInt)) && (turnOff==turnOn)){
            return  false;
        }
        return true;
    }

    public int getCurrentFromTime(int hour, int min){
        return hour * ConstantValue.HOUR_MINUTE + min;
    }

    public int getCurrentToTime(int hour,int min){
        return hour * ConstantValue.HOUR_MINUTE + min;
    }

    public int getCurrentToTime(boolean isOverOneDay,int hour,int min){
        if (isOverOneDay){
            return (hour + 24) * ConstantValue.HOUR_MINUTE + min;
        }else {
            return hour * ConstantValue.HOUR_MINUTE + min;
        }
    }


    /**
     * 根据定时HexStr获取开始时间 十进制
     * @param scheduleHex
     * @return
     */
    public int getFromTimeInt(String scheduleHex){
        String on = scheduleHex.substring(4, 8);
        return (Integer.parseInt(on, 16) / ConstantValue.HOUR_MINUTE) * 60 + Integer.parseInt(on, 16) % ConstantValue.HOUR_MINUTE;
    }

    /**
     * 根据定时HexStr获取结束时间 十进制
     * @param scheduleHex
     * @return
     */
    public int getToTimeInt(String scheduleHex){
        String off = scheduleHex.substring(8, 12);
        return (Integer.parseInt(off, 16) / ConstantValue.HOUR_MINUTE)*60+Integer.parseInt(off, 16) % ConstantValue.HOUR_MINUTE;
    }

    /**
     * 根据定时HexStr获取定时的Hour
     * @param scheduleHex
     * @param startIndex
     * @param endIndex
     * @return
     */
    public int getTimeHourInt(String scheduleHex,int startIndex, int endIndex){
        return Integer.parseInt(scheduleHex.substring(startIndex, endIndex), 16) / ConstantValue.HOUR_MINUTE;
    }


    /**
     * 根据定时HexStr获取定时的Minute
     * @param scheduleHex
     * @param startIndex
     * @param endIndex
     * @return
     */
    public int getTimeMinuteInt(String scheduleHex,int startIndex, int endIndex){
        return Integer.parseInt(scheduleHex.substring(startIndex, endIndex), 16) % ConstantValue.HOUR_MINUTE;
    }

    /**
     * 位数不够时左边补零
     * @param str
     * @param strLength
     * @return
     */
    public String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);// 左补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    /**
     *  判断时间是否有冲突
     * @param from
     * @param to
     * @param begin
     * @param end
     * @return
     */
    public boolean haveIntersection(int from, int to, int begin, int end){
        if (to < from){
            to = to + 24*60;
        }
        if (end < begin){
            end = end + 24*60;
        }
        return (begin > to || end < from) ? false : true;
    }

    /**
     * 得到十进制星期数
     * @param weekList
     * @return
     */
    public int getDecimalWeek(List<Integer> weekList){
        int week = 0;

        if (weekList == null || weekList.size() == 0){
            return week;
        }
        for (int i = 0; i < weekList.size(); i++) {
            switch (weekList.get(i)) {
                case Calendar.MONDAY:
                    week += 2;
                    break;
                case Calendar.TUESDAY:
                    week += 4;
                    break;
                case Calendar.WEDNESDAY:
                    week += 8;
                    break;
                case Calendar.THURSDAY:
                    week += 16;
                    break;
                case Calendar.FRIDAY:
                    week += 32;
                    break;
                case Calendar.SATURDAY:
                    week += 64;
                    break;
                case Calendar.SUNDAY:
                    week += 1;
                    break;
                default:
                    break;
            }
        }
        return week;
    }

    //获取单次定时对应时间的二进制字符串
    public String getOnceStr(){
        Calendar cal = Calendar.getInstance();
        int i = cal.get(Calendar.DAY_OF_WEEK);
        switch (i) {
            case 1://星期日
                return "00000001";
            case 2://星期一
                return "00000010";
            case 3://星期二
                return "00000100";
            case 4://星期三
                return "00001000";
            case 5://星期四
                return "00010000";
            case 6://星期五
                return "00100000";
            case 7://星期六
                return "01000000";
            default://单次
                return "00000000";
        }
    }

    /**
     * 单次定时转换为对应星期的List
     * @return
     */
    public List<String> getOnceWeekList(){
        return StringUtil.getStrList(getOnceStr(), 1);
    }

    public List<String> getTodayWeek(){
        return StringUtil.getStrList(getTodayWeeks(),1);
    }

    /**
     * 根据星期的十六进制得到相应的二进制字符串的List
     * @param hexStr
     * @return
     */
    public List<String> getWeekList(String hexStr){
       return StringUtil.getStrList( addZeroForNum(   decimal2Binary(hexStr2Decimal(hexStr)),8),1);
    }

    /***
     * 把十进制数转为相应的二进制
     * @param binary
     * @return
     */
    public String decimal2Binary (int binary ){
        return  Integer.toBinaryString(binary);
    }

    /**
     *  将十六进制字符串转换为十进制整数 例如"7F" --> 127
     * @param hexStr
     * @return
     */
    public int hexStr2Decimal(String hexStr){
        return Integer.parseInt(hexStr, 16);
    }

    public String getTodayWeeks(){
        String [] weekHexStr = {"00000001","00000010","00000100","00001000","00010000","00100000","01000000"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekHexStr[w];
    }

    public String getCycleTimerTurnOffDes(int loopOn, int loopOff){
        return convertTimeToText(loopOn)+"-ON, "+convertTimeToText(loopOff)+"-OFF";
    }

    private String convertTimeToText(int time) {
        StringBuilder timeText = new StringBuilder();
        if (time == 0){
            timeText.append("0");
        } else if (time / (60*60) > 0){//超过1小时
            timeText.append(time / (60 * 60));
            timeText.append("h");
            if (time % (60*60) > 0){
                timeText.append(time % (60*60) / 60);
                timeText.append("m");
            }
        }else if (time / 60 >0){
            timeText.append(time / 60);
            timeText.append("m");
        }
        return timeText.toString();
    }

    public String base64Encode(String hexStr){
        if (TextUtils.isEmpty(hexStr))
            return "";
        return Base64Util.byteArrayToBase64(HexUtil.decode(hexStr));
    }

    public String base64Decode(String base64Str){
        if (TextUtils.isEmpty(base64Str))
            return "";
        return HexUtil.encodeToString( Base64Util.base64ToByteArray(base64Str));
    }

}
