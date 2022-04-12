package com.afar.osaio.smart.electrician.model;

import com.google.gson.Gson;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IGroupListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * GroupModel
 *
 * @author Administrator
 * @date 2019/3/29
 */
public class GroupModel implements IGroupModel {

    private ITuyaGroup mGroup;

    public GroupModel(long groupId) {
        mGroup = TuyaHomeSdk.newGroupInstance(groupId);
    }

    @Override
    public void sendCommand(String key, Object dps, IResultCallback callback) {
        NooieLog.e("---->> sendCommand params "+ dps.toString());
        Map<String, Object> dpsMap = new HashMap<>();
        dpsMap.put(key, dps);
        sendCommands(dpsMap, callback);
    }

    @Override
    public void sendCommands(Map<String, Object> dpsMap, IResultCallback callback) {
        NooieLog.e("---->> sendCommands params "+new Gson().toJson(dpsMap));
        mGroup.publishDps(new Gson().toJson(dpsMap), callback);
    }

    @Override
    public void registerListener(IGroupListener listener) {
        mGroup.registerGroupListener(listener);
    }

    @Override
    public void unRegisterListener() {
        if (mGroup != null){
            mGroup.unRegisterGroupListener();
        }
    }

    @Override
    public void release() {
//        unRegisterListener();
       if (mGroup != null){
           mGroup.onDestroy();
           mGroup = null;
       }
    }

}
