package com.afar.osaio.smart.electrician.manager;

import android.util.Log;

import androidx.annotation.NonNull;

import com.nooie.common.utils.collection.CollectionUtil;

import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;

import java.util.List;

/**
 * FamilyManager
 *
 * @author Administrator
 * @date 2019/3/6
 */
public class FamilyManager {

    public static final String TAG = FamilyManager.class.getSimpleName();

    private static volatile FamilyManager instance;

    public static final long DEFAULT_HOME_ID = 0;
    private HomeBean currentHomeBean;
    private FamilySpHelper mFamilySpHelper;

    private FamilyManager() {
        mFamilySpHelper = new FamilySpHelper();
    }

    public static FamilyManager getInstance() {
        if (null == instance) {
            synchronized (FamilyManager.class) {
                if (null == instance) {
                    instance = new FamilyManager();
                }
            }
        }
        return instance;
    }

    public void setCurrentHome(HomeBean homeBean) {
        if (null == homeBean) {
            return;
        }
        boolean isChange = false;

        if (null == currentHomeBean) {
            Log.i(TAG, "setCurrentHome  currentHome is null so push current teckin change event");
            isChange = true;
        } else {
            long currentHomeId = currentHomeBean.getHomeId();
            long targetHomeId = homeBean.getHomeId();
            Log.i(TAG, "setCurrentHome: currentHomeId=" + currentHomeId + " targetHomeId=" + targetHomeId);
            if (currentHomeId != targetHomeId) {
                isChange = true;
            }
        }
        //更新内存和sp
        currentHomeBean = homeBean;
        mFamilySpHelper.putCurrentHome(currentHomeBean);
        if (isChange) {
            //EventBus.getDefault().post(new EventCurrentHomeChange(currentHomeBean));
        }
    }


    public HomeBean getCurrentHome() {
        if (null == currentHomeBean) {
            setCurrentHome(mFamilySpHelper.getCurrentHome());
        }
        return currentHomeBean;
    }


    public long getCurrentHomeId() {
        HomeBean currentHome = getCurrentHome();
        if (null == currentHome) {
            return DEFAULT_HOME_ID;
        }
        return currentHome.getHomeId();
    }

    public void resetCurrentHome() {
        currentHomeBean = null;
    }


    public void getHomeList(@NonNull final ITuyaGetHomeListCallback callback) {
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> list) {
                callback.onSuccess(list);
            }

            @Override
            public void onError(String s, String s1) {
                callback.onError(s, s1);
            }
        });
    }


    public void createHome(String homeName, List<String> roomList, @NonNull final ITuyaHomeResultCallback callback) {
        NooieLog.e("---------> createHome  homeName " + homeName);
        TuyaHomeSdk.getHomeManagerInstance().createHome(homeName,
                0, 0, "", roomList, new ITuyaHomeResultCallback() {
                    @Override
                    public void onSuccess(HomeBean homeBean) {
                        //setCurrentHome(homeBean);
                        callback.onSuccess(homeBean);
                    }

                    @Override
                    public void onError(String s, String s1) {
                        callback.onError(s, s1);
                    }
                });
    }

    public void updateCurrentHome(final ITuyaHomeResultCallback callback) {
        NooieLog.e("---------> updateCurrentHome  homeId ");
        updateCurrentHomeById(getCurrentHomeId(), callback);
    }

    public void updateCurrentHomeById(long homeId, final ITuyaHomeResultCallback callback) {
        if (homeId == FamilyManager.DEFAULT_HOME_ID) {
            return;
        }
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                NooieLog.e("---------> updateCurrentHomeById  homeId " + homeBean.getHomeId());
                setCurrentHome(homeBean);
                if (callback != null) {
                    callback.onSuccess(homeBean);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (callback != null) {
                    callback.onError(code, msg);
                }
            }
        });
    }

    public void resetHome(final ITuyaHomeResultCallback callback) {
        getHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> list) {
                if (!CollectionUtil.isEmpty(list)) {
                    NooieLog.e("---------> resetHome  homeId " + list.get(0).getHomeId());
                    setCurrentHome(list.get(0));
                    updateCurrentHome(callback);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (callback != null) {
                    callback.onError(code, msg);
                }
            }
        });
    }
}
