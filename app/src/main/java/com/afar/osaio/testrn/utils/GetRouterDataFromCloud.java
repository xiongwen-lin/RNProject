package com.afar.osaio.testrn.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 设置/获取 路由器设备相关信息
 */
public class GetRouterDataFromCloud {
    private static final String TOPICUR = "topicurl";
    private SendHttpRequest.getRouterReturnInfo from;
    private String routerIp;

    public GetRouterDataFromCloud(SendHttpRequest.getRouterReturnInfo from) {
        this.from = from;
    }

    // deviceIp 看看怎么设置可以拿到
    private String getUrl() {
//        String url = "http://" + getRouterIp() + "/cgi-bin/cstecgi.cgi";
        String url = "";

        return url;
    }

    // 获取手机连接路由器ip地址
//    private String getRouterIp() {
//        WifiManager wifiManager = (WifiManager) MainApplication.mCtx.getSystemService(Context.WIFI_SERVICE);
//        DhcpInfo dhcp = wifiManager.getDhcpInfo();
//        int ip = dhcp.gateway;
//        return intToIp(ip);
//    }
//
//    private String intToIp(int i) {
//        return (i & 0xFF ) + "." +
//                ((i >> 8 ) & 0xFF) + "." +
//                ((i >> 16 ) & 0xFF) + "." +
//                ( i >> 24 & 0xFF) ;
//    }

    /**
     * 设备在线管理
     */

    // 获取设备列表
    public void getAccessDeviceCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getAccessDeviceCfg");
        runThreadTask(jsonObject);
    }

    // 允许/禁止上网
    public void setAccessDeviceCfg(String mac, String addEffect, String modelType) throws JSONException {
        JSONObject jsonObjectList = new JSONObject();

        JSONArray jsonArray = new JSONArray();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mac",mac);
        jsonArray.put(jsonObject);

        jsonObjectList.put("mac_array", jsonArray);
        jsonObjectList.put("addEffect",addEffect);
        jsonObjectList.put("modelType",modelType);
        jsonObjectList.put(TOPICUR,"setAccessDeviceCfg");
        runThreadTask(jsonObjectList);
    }

    // 修改设备名称
    public void setAccessDeviceCfg(String addEffect, String modelType, String idx, String name, String mac) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("addEffect",addEffect);
        jsonObject.put("modelType",modelType);
        jsonObject.put("idx",idx);
        jsonObject.put("name",name);
        jsonObject.put("mac",mac);
        jsonObject.put(TOPICUR,"setAccessDeviceCfg");
        runThreadTask(jsonObject);
    }


    /**
     * 设备基本信息
     */

    // 获取初始值
    public void getInitCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getInitCfg");
        runThreadTask(jsonObject);
    }

    // 获取系统当前信息
    public void getSysStatusCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getSysStatusCfg");
        runThreadTask(jsonObject);
    }

    // 获取lan口信息与网络信息
    public void getNetInfoCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getNetInfoCfg");
        runThreadTask(jsonObject);
    }


    /**
     * 无线网络设置
     */

    public void setOpenWifi(String wifiIdx, String wifiOff, String addEffect) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("wifiIdx",wifiIdx);
        jsonObject.put("wifiOff",wifiOff);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put(TOPICUR,"setWiFiBasicCfg");
        runThreadTask(jsonObject);
    }

    // 单独设置2.4G或5G网络
    public void set2_5GWiFiBasicCfg(String addEffect, String wifiIdx, String bw, String band, String channel, String hssid,
                                String ssid, String key, String wpaMode, String countryCode, String twt, String mumimo, String ofdma) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("addEffect",addEffect);
        jsonObject.put("wifiIdx",wifiIdx);
        jsonObject.put("bw",bw);
        jsonObject.put("band",band);
        jsonObject.put("channel",channel);
        jsonObject.put("hssid",hssid);
        jsonObject.put("ssid",ssid);
        jsonObject.put("key",key);
        jsonObject.put("wpaMode",wpaMode);
        jsonObject.put("countryCode",countryCode);
        jsonObject.put("twt",twt);
        jsonObject.put("mumimo",mumimo);
        jsonObject.put("ofdma",ofdma);
        jsonObject.put(TOPICUR,"setWiFiBasicCfg");
        runThreadTask(jsonObject);
    }

    // 不分区段设置
    public void setWiFiBasicCfg(String addEffect, String[] bw, String[] band, String[] channel, String hssid,
                                    String ssid, String key, String[] countryCode, String[] twt, String[] mumimo, String[] ofdma, String wpaMode) throws JSONException {
        JSONObject jsonObjectList = new JSONObject();
        for (int i = 0; i < 2; i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("addEffect",addEffect);
            jsonObject.put("bw",bw[i]);
            jsonObject.put("band",band[i]);
            jsonObject.put("channel",channel[i]);
            jsonObject.put("hssid",hssid);
            jsonObject.put("ssid",ssid);
            jsonObject.put("key",key);
            jsonObject.put("countryCode",countryCode[i]);
            jsonObject.put("twt",twt[i]);
            jsonObject.put("mumimo",mumimo[i]);
            jsonObject.put("ofdma",ofdma[i]);
            if ("2".equals(wpaMode)) {
                jsonObject.put("wpaMode",wpaMode);
            }
            if (i == 0) {
                jsonObjectList.put("wifi.1.ap.1", jsonObject);
            } else {
                jsonObjectList.put("wifi.2.ap.1", jsonObject);
            }
        }
        jsonObjectList.put(TOPICUR,"setWiFiBasicCfg");
        runThreadTask(jsonObjectList);
    }

    // 获取2.4G/5G访客网络配置
    public void getWiFiBasicCfg(String wifiIdx) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("wifiIdx",wifiIdx);
        jsonObject.put(TOPICUR,"getWiFiBasicCfg");
        runThreadTask(jsonObject);
    }

    // 系统模式
    public void getOpModeCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getOpModeCfg");
        runThreadTask(jsonObject);
    }

    // 获取自动上网配置
    public void discoverWan() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"discoverWan");
        runThreadTask(jsonObject);
    }

    // 设置快速设置标志
    public void setWizardCfg(String tz, String proto, String dnsMode, String merge, String wifiOff,
                             String hssid, String wpaMode, String ssid, String key, String wifiOff5g,
                             String hssid5g, String wpaMode_5g, String ssid5g, String key5g, String loginpass, String wizard, String staticIp,
                             String staticMask, String staticGw, String priDns, String secDns, String pppoeUser,
                             String pppoePass) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tz",tz);
        jsonObject.put("proto",proto);
        jsonObject.put("dnsMode",dnsMode);
        jsonObject.put("merge",merge);
        jsonObject.put("wifiOff",wifiOff);
        jsonObject.put("hssid",hssid);
        jsonObject.put("wpaMode",wpaMode);
        jsonObject.put("ssid",ssid);
        jsonObject.put("key",key);
        jsonObject.put("wifiOff5g",wifiOff5g);
        jsonObject.put("hssid5g",hssid5g);
        jsonObject.put("wpaMode5g",wpaMode_5g);
        jsonObject.put("ssid5g",ssid5g);
        jsonObject.put("key5g",key5g);
        jsonObject.put("loginpass",loginpass);
        jsonObject.put("wizard",wizard);
        if ("0".equals(proto)) {
            jsonObject.put("staticIp",staticIp);
            jsonObject.put("staticMask",staticMask);
            jsonObject.put("staticGw",staticGw);
            jsonObject.put("priDns",priDns);
            jsonObject.put("secDns",secDns);
        } else if ("3".equals(proto)) {
            jsonObject.put("pppoeUser",pppoeUser);
            jsonObject.put("pppoePass",pppoePass);
        }
        jsonObject.put(TOPICUR,"setWizardCfg");
        runThreadTask(jsonObject);
    }

    // 获取快速配置数据
    public void getWizardCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getWizardCfg");
        runThreadTask(jsonObject);
    }

    // 路由器时间控制
    // 获取路由器时间控制
    public void getWiFiScheduleCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getWiFiScheduleCfg");
        runThreadTask(jsonObject);
    }

    // 设置总开关
    public void setWiFiScheduleCfg(String enable, String addEffect) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("enable",enable);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put(TOPICUR,"setWiFiScheduleCfg");
        runThreadTask(jsonObject);
    }

    // 设置路由器时间控制
    public void setWiFiScheduleCfg(String desc, String enable, String addEffect, String week, String sHour, String sMinute,
                                   String eHour, String eMinute) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("desc",desc);
        jsonObject.put("enable",enable);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put("week",week);
        jsonObject.put("sHour",sHour);
        jsonObject.put("sMinute",sMinute);
        jsonObject.put("eHour",eHour);
        jsonObject.put("eMinute",eMinute);
        jsonObject.put(TOPICUR,"setWiFiScheduleCfg");
        runThreadTask(jsonObject);
    }

    // 删除路由器时间控制
    public void delWiFiScheduleCfg(List<String> rules) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < rules.size(); i++) {
            String numString = rules.get(i).substring(rules.get(i).length()-1, rules.get(i).length());
            jsonObject.put("" + rules.get(i),numString);
        }
        jsonObject.put(TOPICUR,"delWiFiScheduleCfg");
        runThreadTask(jsonObject);
    }


    /**
     * 访客网络
     */

    // 设置2.4G/5G访客网络
    public void setWiFiGuestCfg2_5(String wifiOff, String hssid, String ssid, String key, String wifiIdx, String accessEnabled) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("wifiOff",wifiOff);
        jsonObject.put("hssid",hssid);
        jsonObject.put("ssid",ssid);
        jsonObject.put("key",key);
        jsonObject.put("wifiIdx",wifiIdx);
        jsonObject.put("accessEnabled",accessEnabled);
        jsonObject.put(TOPICUR,"setWiFiGuestCfg");
        runThreadTask(jsonObject);
    }

    // 不分区段设置
    public void setWiFiGuestCfg(String wifiOff, String hssid, String ssid, String key, String[] accessEnabled) throws JSONException {
        JSONObject jsonObjectList = new JSONObject();
        for (int i = 0; i < 2; i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("wifiOff",wifiOff);
            jsonObject.put("hssid",hssid);
            jsonObject.put("ssid",ssid);
            jsonObject.put("key",key);
            jsonObject.put("accessEnabled",accessEnabled[i]);
            if (i == 0) {
                jsonObjectList.put("wifi.1.ap.1", jsonObject);
            } else {
                jsonObjectList.put("wifi.2.ap.1", jsonObject);
            }
        }
        jsonObjectList.put(TOPICUR,"setWiFiGuestCfg");
        runThreadTask(jsonObjectList);
    }

    // 获取2.4G/5G访客网络配置
    public void getWiFiGuestCfg(String wifiIdx) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("wifiIdx",wifiIdx);
        jsonObject.put(TOPICUR,"getWiFiGuestCfg");
        runThreadTask(jsonObject);
    }

    //  访客网络基础配置
    public void setWiFiEasyGuestCfg(String accessEnabled2g, String ssid, String key, String wifiOff,
                                    String hssid, String merge, String ssid5g, String key5g, String wifiOff5g,
                                    String hssid5g, String accessEnabled5g) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accessEnabled2g",accessEnabled2g);
        jsonObject.put("ssid",ssid);
        jsonObject.put("key",key);
        jsonObject.put("wifiOff",wifiOff);
        jsonObject.put("hssid",hssid);
        jsonObject.put("merge",merge);
        jsonObject.put("ssid5g",ssid5g);
        jsonObject.put("key5g",key5g);
        jsonObject.put("wifiOff5g",wifiOff5g);
        jsonObject.put("hssid5g",hssid5g);
        jsonObject.put("accessEnabled5g",accessEnabled5g);
        jsonObject.put(TOPICUR,"setWiFiEasyGuestCfg");
        runThreadTask(jsonObject);
    }

    public void getWiFiEasyGuestCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getWiFiEasyGuestCfg");
        runThreadTask(jsonObject);
    }


    /**
     * WAN设置
     */

    // 设置wan 连接方式：动态IP（DHCP）
    public void setWanCfgDHCP(String hostName, String dhcpMtu, String proto, String dnsMode, String priDns, String secDns,
                            String ttlWay, String clone, String cloneMac) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hostName",hostName);
        jsonObject.put("dhcpMtu",dhcpMtu);
        jsonObject.put("proto",proto);
        jsonObject.put("dnsMode",dnsMode);
        jsonObject.put("priDns",priDns);
        jsonObject.put("secDns",secDns);
        jsonObject.put("ttlWay",ttlWay);
        jsonObject.put("clone",clone);
        jsonObject.put("cloneMac",cloneMac);
        jsonObject.put(TOPICUR,"setWanCfg");
        runThreadTask(jsonObject);
    }

    // 设置wan 连接方式： 静态IP
    public void setWanCfgStatic(String staticIp, String staticMask, String staticGw, String staticMtu, String proto, String dnsMode,
                            String priDns, String secDns, String ttlWay, String lcpEchoEnable, String clone, String cloneMac) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("staticIp",staticIp);
        jsonObject.put("staticMask",staticMask);
        jsonObject.put("staticGw",staticGw);
        jsonObject.put("staticMtu",staticMtu);
        jsonObject.put("proto",proto);
        jsonObject.put("dnsMode",dnsMode);
        jsonObject.put("priDns",priDns);
        jsonObject.put("secDns",secDns);
        jsonObject.put("ttlWay",ttlWay);
        jsonObject.put("lcpEchoEnable",lcpEchoEnable);
        jsonObject.put("clone",clone);
        jsonObject.put("cloneMac",cloneMac);
        jsonObject.put(TOPICUR,"setWanCfg");
        runThreadTask(jsonObject);
    }

    // 设置wan 连接方式： 宽带上网
    public void setWanCfgPPPOE(String pppoeUser, String pppoePass, String pppoeMtu, String proto, String dnsMode, String ttlWay,
                            String clone, String cloneMac) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pppoeUser",pppoeUser);
        jsonObject.put("pppoePass",pppoePass);
        jsonObject.put("pppoeMtu",pppoeMtu);
        jsonObject.put("proto",proto);
        jsonObject.put("dnsMode",dnsMode);
        jsonObject.put("ttlWay",ttlWay);
        jsonObject.put("clone",clone);
        jsonObject.put("cloneMac",cloneMac);
        jsonObject.put(TOPICUR,"setWanCfg");
        runThreadTask(jsonObject);
    }

    // 获取wan
    public void getWanCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getWanCfg");
        runThreadTask(jsonObject);
    }


    /**
     * LAN设置
     */

    // 设置wan
    public void setLanCfg(String lanIp, String lanNetmask, String dhcpServer, String dhcpLease, String dhcpStart, String dhcpEnd) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lanIp",lanIp);
        jsonObject.put("lanNetmask",lanNetmask);
        jsonObject.put("dhcpServer",dhcpServer);
        jsonObject.put("dhcpLease",dhcpLease);
        jsonObject.put("dhcpStart",dhcpStart);
        jsonObject.put("dhcpEnd",dhcpEnd);
        jsonObject.put(TOPICUR,"setLanCfg");
        runThreadTask(jsonObject);
    }

    // 获取wan
    public void getLanCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getLanCfg");
        runThreadTask(jsonObject);
    }


    /**
     * 家长控制
     */

    // 开启/关闭家长控制
    public void setParentalRules(String enable, String addEffect) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("enable",enable);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put(TOPICUR,"setParentalRules");
        runThreadTask(jsonObject);
    }

    // 家长控制添加规则
    public void setParentalRules(String mac, String desc, String week, String sTime, String eTime, String state, String addEffect) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mac",mac);
        jsonObject.put("desc",desc);
        jsonObject.put("week",week);
        jsonObject.put("sTime",sTime);
        jsonObject.put("eTime",eTime);
        jsonObject.put("state",state);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put(TOPICUR,"setParentalRules");
        runThreadTask(jsonObject);
    }

    // 添加规则时获取客户端
    public void getOnlineMsg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getOnlineMsg");
        runThreadTask(jsonObject);
    }

    // 删除家长控制
    public void delParentalRules(List<String> rules) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < rules.size(); i++) {
            String numString = rules.get(i).substring(rules.get(i).length()-1, rules.get(i).length());
            jsonObject.put("" + rules.get(i),numString);
        }
        jsonObject.put(TOPICUR,"delParentalRules");
        runThreadTask(jsonObject);
    }

    // 获取家长控制
    public void getParentalRules() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getParentalRules");
        runThreadTask(jsonObject);
    }


    /**
     * 黑名单管理
     */

    // 开启/关闭 mac过滤功能
    public void setMacFilterRules(String enable, String addEffect) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("enable",enable);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put(TOPICUR,"setMacFilterRules");
        runThreadTask(jsonObject);
    }

    // 获取黑名单列表
    public void getMacFilterRules() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getMacFilterRules");
        runThreadTask(jsonObject);
    }

    // 添加黑名单规则
    public void setMacFilterRules(String mac, String desc, String addEffect) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mac",mac);
        jsonObject.put("desc",desc);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put(TOPICUR,"setMacFilterRules");
        runThreadTask(jsonObject);
    }

    // 修改规则
    public void setMacFilterRules(String mac, String addEffect,String desc, String idx) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mac",mac);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put("desc",desc);
        jsonObject.put("idx",idx);
        jsonObject.put(TOPICUR,"setMacFilterRules");
        runThreadTask(jsonObject);
    }

    // 删除规则
    public void delMacFilterRules(String delRule) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        String numString = delRule.substring(delRule.length()-1, delRule.length());
        jsonObject.put(delRule,numString);
        jsonObject.put(TOPICUR,"delMacFilterRules");
        runThreadTask(jsonObject);
    }


    /**
     * UPNP设置
     */

    // 开启/关闭upnp
    public void setUPnPCfg(String enable) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("enable",enable);
        jsonObject.put(TOPICUR,"setUPnPCfg");
        runThreadTask(jsonObject);
    }

    // 获取upnp状态
    public void getUPnPCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getUPnPCfg");
        runThreadTask(jsonObject);
    }


    /**
     * QOS设置
     */

    // 开启/关闭QOS
    public void setSmartQosCfg(String qos_enable, String addEffect) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("qos_enable",qos_enable);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put(TOPICUR,"setSmartQosCfg");
        runThreadTask(jsonObject);
    }

    // 设置统一的上下行带宽
    public void setUniteBandwidth(String qos_enable, String qos_up_bw, String qos_down_bw, String addEffect) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("qos_enable",qos_enable);
        jsonObject.put("qos_up_bw",qos_up_bw);
        jsonObject.put("qos_down_bw",qos_down_bw);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put(TOPICUR,"setSmartQosCfg");
        runThreadTask(jsonObject);
    }

    // 添加QOS规则
    public void setAddQos(String ip, String maxUpload, String maxDownload) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ip",ip);
        jsonObject.put("maxUpload",maxUpload);
        jsonObject.put("maxDownload",maxDownload);
        jsonObject.put(TOPICUR,"setSmartQosCfg");
        runThreadTask(jsonObject);
    }

    // 获取QOS配置
    public void getSmartQosCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getSmartQosCfg");
        runThreadTask(jsonObject);
    }

    // 删除QOS
    public void delSmartQosCfg(List<String> rules) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < rules.size(); i++) {
            String numString = rules.get(i).substring(rules.get(i).length()-1, rules.get(i).length());
            jsonObject.put("" + rules.get(i),numString);
        }
        jsonObject.put(TOPICUR,"delSmartQosCfg");
        runThreadTask(jsonObject);
    }


    /**
     * 端口转发
     */

    // 开启/关闭端口转发
    public void setPortForwardRules(String enable, String addEffect) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("enable",enable);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put(TOPICUR,"setPortForwardRules");
        runThreadTask(jsonObject);
    }

    // 添加/修改端口转发
    public void setPortForwardRules(String ip, String proto, String iPort, String ePort, String desc, String addEffect) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ip",ip);
        jsonObject.put("proto",proto);
        jsonObject.put("iPort",iPort);
        jsonObject.put("ePort",ePort);
        jsonObject.put("desc",desc);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put(TOPICUR,"setPortForwardRules");
        runThreadTask(jsonObject);
    }

    // 修改规则
    public void setPortForwardRules(String ip, String proto, String iPort, String ePort, String desc, String idx, String addEffect) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ip",ip);
        jsonObject.put("proto",proto);
        jsonObject.put("iPort",iPort);
        jsonObject.put("ePort",ePort);
        jsonObject.put("desc",desc);
        jsonObject.put("idx",idx);
        jsonObject.put("addEffect",addEffect);
        jsonObject.put(TOPICUR,"setPortForwardRules");
        runThreadTask(jsonObject);
    }

    // 获取端口转发规则
    public void getPortForwardRules() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getPortForwardRules");
        runThreadTask(jsonObject);
    }

    // 删除端口转发规则
    public void delPortForwardRules(List<String> rules) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < rules.size(); i++) {
            String numString = rules.get(i).substring(rules.get(i).length()-1, rules.get(i).length());
            jsonObject.put("" + rules.get(i),numString);
        }
        jsonObject.put(TOPICUR,"delPortForwardRules");
        runThreadTask(jsonObject);
    }


    /**
     * led灯控制
     */

    // 设置LED灯状态
    public void setLedCfg(String enable) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("enable",enable);
        jsonObject.put(TOPICUR,"setLedCfg");
        runThreadTask(jsonObject);
    }


    /**
     * 语言设置
     */

    // 语言设置
    public void setLanguageCfg(String lang, String langAutoFlag) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lang",lang);
        jsonObject.put("langAutoFlag",langAutoFlag);
        jsonObject.put(TOPICUR,"setLanguageCfg");
        runThreadTask(jsonObject);
    }


    /**
     * 时区设置
     */

    // 设置使用本机时间
    public void NTPSyncWithHost(String host_time) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("host_time",host_time);
        jsonObject.put(TOPICUR,"NTPSyncWithHost");
        runThreadTask(jsonObject);
    }

    //设置ntp时间
    public void setNtpCfg(String tz, String enable, String server) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tz",tz);
        jsonObject.put("enable",enable);
        jsonObject.put("server",server);
        jsonObject.put(TOPICUR,"setNtpCfg");
        runThreadTask(jsonObject);
    }

    //获取ntp时间
    public void getNtpCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getNtpCfg");
        runThreadTask(jsonObject);
    }


    /**
     * 定时重启
     */

    //设置ntp时间
    public void setScheduleCfg(String mode, String hour, String minute,String week) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mode",mode);
        jsonObject.put("hour",hour);
        jsonObject.put("minute",minute);
        jsonObject.put("week",week);
        jsonObject.put(TOPICUR,"setScheduleCfg");
        runThreadTask(jsonObject);
    }

    //设置倒计时
    public void setScheduleCfg(String mode, String recHour) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mode",mode);
        jsonObject.put("recHour",recHour);
        jsonObject.put(TOPICUR,"setScheduleCfg");
        runThreadTask(jsonObject);
    }

    //获取定时重启配置
    public void getScheduleCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getScheduleCfg");
        runThreadTask(jsonObject);
    }


    /**
     * 系统日志
     */

    // 开启/关闭系统日志
    public void setSyslogCfg(String enable) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("enable",enable);
        jsonObject.put(TOPICUR,"setSyslogCfg");
        runThreadTask(jsonObject);
    }

    // 开启之后，查看/刷新日志
    public void showSyslog() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"showSyslog");
        runThreadTask(jsonObject);
    }

    // 清除日志
    public void clearSyslog() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"clearSyslog");
        runThreadTask(jsonObject);
    }

    // 获取系统日志状态
    public void getSyslogCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getSyslogCfg");
        runThreadTask(jsonObject);
    }


    /**
     * 系统复位
     * "topicurl":"LoadDefSettings"
     */

    // 系统复位（名称是根据意思起的，而传参需要用LoadDefSettings）
    public void setSysResetCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"LoadDefSettings");
        runThreadTask(jsonObject);
    }


    /**
     * 系统重启
     */

    // 系统重启
    public void rebootSystem() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"RebootSystem");
        runThreadTask(jsonObject);
    }


    /**
     * 本地升级
     */

    // 设置ntp时间 "topicurl":"FirmwareUpgrade"
    public void setNtpCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"FirmwareUpgrade");
        runThreadTask(jsonObject);
    }

    // 上传固件
    public void upLoadFirmware(String upgradeStatus, String wtime) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("upgradeStatus",upgradeStatus);
        jsonObject.put("wtime",wtime);
        //jsonObject.put(TOPICUR,"FirmwareUpgrade");
        runThreadTask(jsonObject);
    }

    // 升级固件
    public void setUpgradeFW(String resetFlags, String FileName, String ContentLength) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("resetFlags",resetFlags);
        jsonObject.put("FileName",FileName);
        jsonObject.put("ContentLength",ContentLength);
        jsonObject.put(TOPICUR,"setUpgradeFW");
        runThreadTask(jsonObject);
    }

    public void setUpgradeFWTest() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"setUpgradeFW");
        runThreadTask(jsonObject);
    }


    /**
     * 修改密码
     */

    // 修改密码
    public void setPasswordCfg(String admuser, String admpass, String origPass) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("admuser",admuser);
        jsonObject.put("admpass",admpass);
        jsonObject.put("origPass",origPass);
        jsonObject.put(TOPICUR,"setPasswordCfg");
        runThreadTask(jsonObject);
    }

    // 获取密码
    public void getPasswordCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getPasswordCfg");
        runThreadTask(jsonObject);
    }

    public void getCheckPasswordResult(String password) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("password",password);
        jsonObject.put(TOPICUR,"getCheckPasswordResult");
        runThreadTask(jsonObject);
    }

    // 获取路由器uuid
    public void getRouterUUid() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getUuidInfo");
        runThreadTask(jsonObject);
    }


    /**
     * 固件OTA升级
     */

    // 版本检测 "topicurl":"CloudSrvVersionCheck"
    public void cloudSrvVersionCheck() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"CloudSrvVersionCheck");
        runThreadTask(jsonObject);
    }

    // 获取检测版本状态 "topicurl":"getCloudSrvCheckStatus"
    public void getCloudSrvCheckStatus() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getCloudSrvCheckStatus");
        runThreadTask(jsonObject);
    }

    // 升级
    public void setUpgradeFW() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"setUpgradeFW");
        runThreadTask(jsonObject);
    }

    // OTA升级
    public void setVictureOTA(String version, String upgrade) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("vApp_newVersion",version);
        jsonObject.put("vApp_upgrade",upgrade);
        jsonObject.put(TOPICUR,"setVictureOTA");
        runThreadTask(jsonObject);
    }


    /**
     * 远程管理
     */

    // 设置远程管理功能
    public void setRemoteCfg(String port, String enable) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("port",port);
        jsonObject.put("enable",enable);
        jsonObject.put(TOPICUR,"setRemoteCfg");
        runThreadTask(jsonObject);
    }

    // 获取远程管理状态
    public void getRemoteCfg() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOPICUR,"getRemoteCfg");
        runThreadTask(jsonObject);
    }

    public void setRouterBind(String user_uuid, String time_zone, String region) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user_uuid",user_uuid);
        jsonObject.put("time_zone",time_zone);
        jsonObject.put("region",region);
        jsonObject.put(TOPICUR,"setVictureBindCfg");
        runThreadTask(jsonObject);
    }

    public void setRouterUnBind(String unbind) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("unbind",unbind);
        jsonObject.put(TOPICUR,"setVictureUnbind");
        runThreadTask(jsonObject);
    }



    private void runThreadTask(JSONObject jsonObject) {
        String url = getUrl();
        SendHttpRequest sendHttpRequest = new SendHttpRequest();
        sendHttpRequest.setRouterReturnInfoListener(from);
        Thread thread=new Thread(){
            public void run(){
                try {
                    sendHttpRequest.sendPost(url, jsonObject);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        };
        thread.start();
    }
}











