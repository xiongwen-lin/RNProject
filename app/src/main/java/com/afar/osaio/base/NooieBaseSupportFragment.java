package com.afar.osaio.base;

import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.data.EventDictionary;
import com.nooie.eventtracking.EventTrackingApi;
import com.nooie.eventtracking.IAutoScreenTracker;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.receiver.UpdateShareDataReceiver;

import me.yokeyword.fragmentation.base.SupportFragment;

public class NooieBaseSupportFragment extends SupportFragment implements IAutoScreenTracker {

    protected String mUid;
    protected String mToken;
    protected String mUserAccount;

    public void initGlobalData() {
        mUid = GlobalData.getInstance().getUid();
        mToken = GlobalData.getInstance().getToken();
        mUserAccount = GlobalData.getInstance().getAccount();
    }

    public void unInitGlobalData() {
        mUid = "";
        mToken = "";
        mUserAccount = "";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGlobalData();
        registerUpdateShareDataReceiver();
        NooieLog.d("-->> debug NooieBaseSupportFragment onCreate: eventId=" + getEventId(EventDictionary.EVENT_TRACK_TYPE_START) + " trackType=" + getTrackType());
        EventTrackingApi.getInstance().trackScreenEvent(this, System.currentTimeMillis(), EventDictionary.EVENT_TRACK_TYPE_START);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterUpdateShareDataReceiver();
        NooieLog.d("-->> debug NooieBaseSupportFragment onDestroy: eventId=" + getEventId(EventDictionary.EVENT_TRACK_TYPE_END) + " trackType=" + getTrackType());
        EventTrackingApi.getInstance().trackScreenEvent(this, System.currentTimeMillis(), EventDictionary.EVENT_TRACK_TYPE_END);
    }

    @Override
    public String getEventId(int trackType) {
        return null;
    }

    /**
     * ??????????????????????????????
     * trackType???????????????
     * EventDictionary.EVENT_TRACK_TYPE_START?????????????????????????????????
     * EVENT_TRACK_TYPE_END?????????????????????????????????
     * EVENT_TRACK_TYPE_START_END???????????????????????????????????????
     * @return ???????????????????????????
     */
    @Override
    public int getTrackType() {
        return EventDictionary.EVENT_TRACK_TYPE_NONE;
    }

    /**
     * ???????????????????????????,??????????????????
     * @return
     */
    @Override
    public String getPageId() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String getExternal() {
        return null;
    }








    @Override
    public String getExternal2() {
        return null;
    }

    @Override
    public String getExternal3() {
        return  null;
    }

    /**
     * ????????????id
     * @return
     */
    @Override
    public String getUuid() {
        return null;
    }

    public boolean checkNull(Object ...args) {
        for (Object object : args) {
            if (object == null) {
                return true;
            }
        }

        return false;
    }

    public void showLoading(boolean isCancel) {
        if (checkActivityIsDestroy()) {
            return;
        }
        ((NooieBaseSupportActivity) _mActivity).showLoading(true);
    }

    public void hideLoading() {
        if (checkActivityIsDestroy()) {
            return;
        }
        ((NooieBaseSupportActivity) _mActivity).hideLoading();
    }

    private UpdateShareDataReceiver mUpdateShareDataReceiver;

    private void registerUpdateShareDataReceiver() {
        if (mUpdateShareDataReceiver == null) {
            mUpdateShareDataReceiver = new UpdateShareDataReceiver() {
                @Override
                public void onUpdateGlobalData(String action) {
                    if (SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGIN.equalsIgnoreCase(action)) {
                        initGlobalData();
                    } else if (SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGOUT.equalsIgnoreCase(action)) {
                        unInitGlobalData();
                    }
                    updateGlobalData(action);
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGIN);
            intentFilter.addAction(SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGOUT);
            NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mUpdateShareDataReceiver, intentFilter);
        }
    }

    private void unRegisterUpdateShareDataReceiver() {
        if (mUpdateShareDataReceiver != null) {
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mUpdateShareDataReceiver);
            mUpdateShareDataReceiver = null;
        }
    }

    public void updateGlobalData(String action) {
    }

}
