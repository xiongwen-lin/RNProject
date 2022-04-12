package com.afar.osaio.smart.player.presenter;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.afar.osaio.smart.device.helper.NooieCloudHelper;
import com.afar.osaio.smart.player.contract.PlayContract;
import com.afar.osaio.smart.player.delegate.PlayState;
import com.afar.osaio.smart.player.listener.OnStartPlaybackListener;
import com.nooie.common.base.GlobalData;
import com.nooie.common.bean.UrlBean;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.IPv4IntTransformer;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.core.NetConfigure;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.device.bean.RecordFragment;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;
import com.scenery7f.timeaxis.model.PeriodTime;
import com.scenery7f.timeaxis.view.UtcTimerShaft;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.CloudFileBean;
import com.afar.osaio.smart.device.bean.CloudRecordInfo;
import com.scenery7f.timeaxis.model.RecordType;
import com.afar.osaio.smart.device.helper.RecordTimeHelper;
import com.afar.osaio.smart.player.adapter.PlaybackDetectionAdapter;
import com.afar.osaio.util.ConstantValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PlaybackComponent extends PlayComponent implements UtcTimerShaft.OnTimeShaftListener, OnLoadMoreListener {

    private PlayContract.View mPlayerView;

    UtcTimerShaft timerShaftPortrait;
    UtcTimerShaft timerShaftLand;
    RecyclerView rcvPlaybackDetection;
    SwipeToLoadLayout swtllPlaybackDetection;

    private String mDeviceId;
    private boolean mIsOwner = true;
    private boolean mIsSubDevice = false;
    private boolean mIsLpDevice = false;
    private int mConnectionMode = ConstantValue.CONNECTION_MODE_QC;
    private int mModelType;
    private int mPlaybackType;
    private long mTodayStartTime;
    private long mCurrentSeekTime;
    private long mDirectTime;
    private String mModel;
    private boolean mIsInitCurrentTime = true;
    private int mPlaybackSourceType = ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL;
    private boolean mIsPlayerStarting = false;
    private long mPlayerStartVideoTime = 0;
    private List<RecordFragment> mRecordFragments;
    private String mFileType;
    private String mPicType;
    private String mFilePrefix;
    private int mExpireDate;
    private List<CloudRecordInfo> mAlarmRecords = new ArrayList<>();
    private int mPickAlarmSeek = -1;
    private boolean mIsAsc = false;

    private PlaybackDetectionAdapter mPlaybackDetectionAdapter;

    public void initData(String deviceId, boolean isOwner, boolean isSubDevice, boolean isLpDevice, int connectionMode, int modelType, int playbackType, int playbackSourceType, long directTime, String model, UtcTimerShaft timerShaftPortrait, UtcTimerShaft timerShaftLand, RecyclerView rcvPlaybackDetection, SwipeToLoadLayout swtllPlaybackDetection) {
        setDeviceId(deviceId);
        setIsOwner(isOwner);
        setIsSubDevice(isSubDevice);
        setIsLpDevice(isLpDevice);
        setConnectionMode(connectionMode);
        setModelType(modelType);
        setPlaybackType(playbackType);
        setPlaybackSourceType(playbackSourceType);
        setDirectTime(directTime);
        setModel(model);
        this.timerShaftPortrait = timerShaftPortrait;
        this.timerShaftLand = timerShaftLand;
        this.rcvPlaybackDetection = rcvPlaybackDetection;
        this.swtllPlaybackDetection = swtllPlaybackDetection;
        setupTimeLineView();
        setupPlaybackDetection();
    }

    public void setView(PlayContract.View view) {
        this.mPlayerView = view;
    }

    public void tryStartVideo(int modelType, int start) {
        //该时间点有内容，直接播放，无录影，直接提示用户，不跳转到其他时间点
        if (!checkSeekTimeExist(start * 1000L, true)) {
            return;
        }
        startVideo(modelType, start);
    }

    public void tryStartCloudVideo(int modelType, final int start, final List<RecordFragment> list, final String fileType, final int bindType, final long baseTime, final int expire, String filePrefix) {
        //该时间点有内容，直接播放，无录影，直接提示用户，不跳转到其他时间点
        if (!checkSeekTimeExist(start * 1000L, true)) {
            return;
        }
        startCloudVideo(modelType, start, list, fileType, bindType, baseTime, expire, filePrefix);
    }

    public void startCloudVideo(int modelType, final int start, final List<RecordFragment> list, final String fileType, final int bindType, final long baseTime, final int expire, String filePrefix) {
        String webUrl = GlobalData.getInstance().getS3Url();
        String uid = GlobalData.getInstance().getUid();
        String token = GlobalData.getInstance().getToken();

        if (player == null || TextUtils.isEmpty(webUrl) || TextUtils.isEmpty(uid) || TextUtils.isEmpty(token)) {
            return;
        }

        try {
            UrlBean urlBean = IPv4IntTransformer.convertIpAndPort(webUrl);
            NooieLog.d("-->> PlaybackComponent startCloudVideo host url=" + urlBean.getUrl() + " ip=" + urlBean.getIp() + " port=" + urlBean.getPort() + " isSubDevice=" + mIsSubDevice + " isLpDevice=" + mIsLpDevice);
            /*
            mPlayerStartVideoTime = System.currentTimeMillis();
            mIsPlayerStarting = true;
            if (mIsSubDevice) {
                player.startMhCloudPlayback(mDeviceId, modelType, list, uid, fileType, urlBean.getIp(), urlBean.getPort(), bindType, baseTime, prefs.getGapTime(), expire, urlBean.isEncryption() ? 1 : 0, ApiConstant.APP_ID, token, ApiConstant.API_SECRET, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->>> PlaybackComponent startCloudVideo onResult");
                        mIsPlayerStarting = false;
                        if (code == Constant.OK) {
                            seekToSelectTime(start * 1000L);
                        } else {
                            //ToastUtil.showToast(NooiePlaybackActivity.this, getResources().getString(R.string.get_fail));
                        }
                    }
                });
            } else {
                player.startNooieCloudPlayback(mDeviceId, modelType, list, uid, fileType, urlBean.getIp(), urlBean.getPort(), bindType, baseTime, prefs.getGapTime(), expire, urlBean.isEncryption() ? 1 : 0, ApiConstant.APP_ID, token, ApiConstant.API_SECRET, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->>> PlaybackComponent startCloudVideo onResult");
                        mIsPlayerStarting = false;
                        if (code == Constant.OK) {
                            seekToSelectTime(start * 1000L);
                        } else {
                            //ToastUtil.showToast(NooiePlaybackActivity.this, getResources().getString(R.string.get_fail));
                        }
                    }
                });
            }
            */
            if (mIsLpDevice) {
                startLpCloudPlayback(mDeviceId, mConnectionMode, mIsSubDevice, uid, token, GlobalData.getInstance().getGapTime(), urlBean, modelType, start, list, fileType, bindType, baseTime, expire, filePrefix);
            } else {
                startCloudPlayback(mDeviceId, mConnectionMode, uid, token, GlobalData.getInstance().getGapTime(), urlBean, modelType, start, list, fileType, bindType, baseTime, expire, filePrefix);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startLpCloudPlayback(String deviceId, int connectionMode, boolean isSubDevice, String uid, String token, int gapTime, UrlBean urlBean, int modelType, final int start, final List<RecordFragment> list, final String fileType, final int bindType, final long baseTime, final int expire, String filePrefix) {
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
        } else {
            prepareStartPlayback();
            updatePlayState(PlayState.PLAY_TYPE_CLOUD_PLAYBACK, PlayState.PLAY_STATE_START);
            int playbackSeekTime = getPlaybackSeekTime((start * 1000L), mTodayStartTime, ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD);
            modelType = NooieDeviceHelper.convertNooieModelForCloudPlayback(IpcType.getIpcType(mModel), mModel);
            if (isSubDevice) {
                player.startMhCloudPlayback(mDeviceId, modelType, list, uid, fileType, urlBean.getIp(), urlBean.getPort(), bindType, baseTime, gapTime, expire, urlBean.isEncryption() ? 1 : 0, NetConfigure.getInstance().getAppId(), token, NetConfigure.getInstance().getAppSecret(), filePrefix, playbackSeekTime, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->>> PlaybackComponent startLpCloudPlayback LpCloud onResult code=" + code);
                        onStartPlayback(code, start, ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD);
                    }
                });
            } else {
                player.startMhCloudPlayback(mDeviceId, modelType, list, uid, fileType, urlBean.getIp(), urlBean.getPort(), bindType, baseTime, gapTime, expire, urlBean.isEncryption() ? 1 : 0, NetConfigure.getInstance().getAppId(), token, NetConfigure.getInstance().getAppSecret(), filePrefix, playbackSeekTime, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->>> PlaybackComponent startLpCloudPlayback LpCloud onResult code=" + code);
                        onStartPlayback(code, start, ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD);
                    }
                });
            }
        }
    }

    private void prepareStartPlayback() {
        mPlayerStartVideoTime = System.currentTimeMillis();
        mIsPlayerStarting = true;
    }

    private ArrayMap<String, Object> mPlaybackEvent = null;

    public void createPlaybackEvent(int start, int playbackType) {
        mPlaybackEvent = new ArrayMap<>();
        try {
            mPlaybackEvent.put(EventDictionary.EXTERNAL_KEY_PLAYER_TYPE, playbackType);
            mPlaybackEvent.put(EventDictionary.EXTERNAL_KEY_PLAYER_TIME, start);
            mPlaybackEvent.put(EventDictionary.EXTERNAL_KEY_START_TIME, (System.currentTimeMillis() / 1000L));
            mPlaybackEvent.put(EventDictionary.EXTERNAL_KEY_END_TIME, 0L);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    public ArrayMap<String, Object> getPlaybackEvent() {
        return mPlaybackEvent;
    }

    public void clearPlaybackEvent() {
        if (mPlaybackEvent != null) {
            mPlaybackEvent.clear();
            mPlaybackEvent = null;
        }
    }

    private void onStartPlayback(int code, int start, int playbackType) {
        mIsPlayerStarting = false;
        if (code == Constant.OK) {
            createPlaybackEvent(start, playbackType);
//            Util.delayTask(500, new Util.OnDelayTaskFinishListener() {
//                @Override
//                public void onFinish() {
//                    seekToSelectTime(start * 1000L);
//                }
//            });
        }
        updatePlayState(playbackType == ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD ? PlayState.PLAY_TYPE_CLOUD_PLAYBACK : PlayState.PLAY_TYPE_SD_PLAYBACK, PlayState.PLAY_STATE_FINISH);
    }

    private void startCloudPlayback(String deviceId, int connectionMode, String uid, String token, int gapTime, UrlBean urlBean, int modelType, final int start, final List<RecordFragment> list, final String fileType, final int bindType, final long baseTime, final int expire, String filePrefix) {
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
        } else {
            prepareStartPlayback();
            updatePlayState(PlayState.PLAY_TYPE_CLOUD_PLAYBACK, PlayState.PLAY_STATE_START);
            int playbackSeekTime = getPlaybackSeekTime((start * 1000L), mTodayStartTime, ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD);
            player.startNooieCloudPlayback(deviceId, modelType, list, uid, fileType, urlBean.getIp(), urlBean.getPort(), bindType, baseTime, gapTime, expire, urlBean.isEncryption() ? 1 : 0, NetConfigure.getInstance().getAppId(), token, NetConfigure.getInstance().getAppSecret(), filePrefix, playbackSeekTime, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    NooieLog.d("-->>> PlaybackComponent startCloudPlayback onResult code=" + code);
                    onStartPlayback(code, start, ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD);
                }
            });
        }
    }

    private void startVideo(int modelType, final int start) {
        if (player == null) {
            return;
        }

        NooieLog.d("-->>> PlaybackComponent startSdVideo deviceId=" + mDeviceId + " modeType=" + modelType + " isSubDevice=" + mIsSubDevice);
        /*
        mPlayerStartVideoTime = System.currentTimeMillis();
        mIsPlayerStarting = true;
        if (mIsSubDevice) {
            player.startMhSDPlayback(mDeviceId, modeType, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    NooieLog.d("-->>> PlaybackComponent startVideo onResult code=" + code);
                    mIsPlayerStarting = false;
                    if (code == Constant.OK) {
                        seekToSelectTime(start * 1000L);
                    } else {
                    }
                }
            });
        } else {
            player.startNooieSDPlayback(mDeviceId, modeType, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    NooieLog.d("-->>> PlaybackComponent startVideo onResult code=" + code);
                    mIsPlayerStarting = false;
                    if (code == Constant.OK) {
                        seekToSelectTime(start * 1000L);
                    } else {
                        //ToastUtil.showToast(NooiePlaybackActivity.this, getResources().getString(R.string.get_fail));
                    }
                }
            });
        }
        */
        if (mIsLpDevice) {
            startLpSDPlayback(mDeviceId, mConnectionMode, mIsSubDevice, modelType, start);
        } else {
            startSDPlayback(mDeviceId, mConnectionMode, modelType, start);
        }
    }

    private void startLpSDPlayback(String deviceId, int connectionMode, boolean isSubDevice, int modelType, int start) {
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            prepareStartPlayback();
            updatePlayState(PlayState.PLAY_TYPE_SD_PLAYBACK, PlayState.PLAY_STATE_START);
            int playbackSeekTime = getPlaybackSeekTime((start * 1000L), mTodayStartTime, ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
            if (isSubDevice) {
                if (modelType == Constant.MODEL_TYPE_MH_EC810_CAM) {
                    player.startAPP2PPlayback(DeviceCmdApi.getInstance().getApDeviceId(mDeviceId, ""), 0, playbackSeekTime, modelType, new OnActionResultListener() {
                        @Override
                        public void onResult(int code) {
                            NooieLog.d("-->>> PlaybackComponent startLpSDPlayback LpSD onResult code=" + code);
                            onStartPlayback(code, start, ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
                        }
                    });
                } else {
                    player.startAPPlayback(DeviceCmdApi.getInstance().getApDeviceId(mDeviceId, ""), 0, playbackSeekTime, modelType, new OnActionResultListener() {
                        @Override
                        public void onResult(int code) {
                            NooieLog.d("-->>> PlaybackComponent startLpSDPlayback LpSD onResult code=" + code);
                            onStartPlayback(code, start, ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
                        }
                    });
                }
            } else {
                if (modelType == Constant.MODEL_TYPE_MH_EC810_CAM) {
                    player.startAPP2PPlayback(DeviceCmdApi.getInstance().getApDeviceId(mDeviceId, ""), 0, playbackSeekTime, modelType, new OnActionResultListener() {
                        @Override
                        public void onResult(int code) {
                            NooieLog.d("-->>> PlaybackComponent startLpSDPlayback LpSD onResult code=" + code);
                            onStartPlayback(code, start, ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
                        }
                    });
                } else {
                    player.startAPPlayback(DeviceCmdApi.getInstance().getApDeviceId(mDeviceId, ""), 0, playbackSeekTime, modelType, new OnActionResultListener() {
                        @Override
                        public void onResult(int code) {
                            NooieLog.d("-->>> PlaybackComponent startLpSDPlayback LpSD onResult code=" + code);
                            onStartPlayback(code, start, ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
                        }
                    });
                }
            }
        } else {
            prepareStartPlayback();
            updatePlayState(PlayState.PLAY_TYPE_SD_PLAYBACK, PlayState.PLAY_STATE_START);
            int playbackSeekTime = getPlaybackSeekTime((start * 1000L), mTodayStartTime, ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
            if (isSubDevice) {
                player.startMhSDPlayback(mDeviceId, modelType, playbackSeekTime, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->>> PlaybackComponent startLpSDPlayback onResult code=" + code);
                        onStartPlayback(code, start, ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
                    }
                });
            } else {
                player.startMhSDPlayback(mDeviceId, modelType, playbackSeekTime, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->>> PlaybackComponent startLpSDPlayback onResult code=" + code);
                        onStartPlayback(code, start, ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
                    }
                });
            }
        }
    }

    private void startSDPlayback(String deviceId, int connectionMode, int modelType, int start) {
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            prepareStartPlayback();
            updatePlayState(PlayState.PLAY_TYPE_SD_PLAYBACK, PlayState.PLAY_STATE_START);
            int playbackSeekTime = getPlaybackSeekTime((start * 1000L), mTodayStartTime, ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
            player.startAPPlayback(DeviceCmdApi.getInstance().getApDeviceId(mDeviceId, ""), 0, playbackSeekTime, modelType, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    NooieLog.d("-->>> PlaybackComponent startSDPlayback onResult code=" + code);
                    onStartPlayback(code, start, ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
                }
            });
        } else {
            prepareStartPlayback();
            updatePlayState(PlayState.PLAY_TYPE_SD_PLAYBACK, PlayState.PLAY_STATE_START);
            int playbackSeekTime = getPlaybackSeekTime((start * 1000L), mTodayStartTime, ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
            player.startNooieSDPlayback(mDeviceId, 0, modelType, playbackSeekTime, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    NooieLog.d("-->>> PlaybackComponent startSDPlayback onResult code=" + code);
                    onStartPlayback(code, start, ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
                }
            });
        }
    }

    private void trySeekVideo(int modelType, long time, List<RecordFragment> recordFragment, String fileType, int expireDate, String filePrefix, int bindType) {
        if (player == null) {
            return;
        }

        if (player.isPlayingng()) {
            seekToSelectTime(time);
        } else {
            if (mPlaybackType == ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD) {
                tryStartCloudVideo(modelType, (int) (time / 1000L), recordFragment, fileType, bindType, mTodayStartTime / 1000L, expireDate, filePrefix);
            } else {
                tryStartVideo(modelType, (int) (time / 1000L));
            }
        }
    }

    public void seekToSelectTime(long time) {
        if (player == null) {
            return;
        }

        //该时间点有内容，直接播放，无录影，直接提示用户，不跳转到其他时间点
        if (!checkSeekTimeExist(time, true)) {
            return;
        }

        /*
        if (!timerShaftPortrait.isInRecordList(time)) {
            //ToastUtil.showToast(this, R.string.no_record);
            showNoRecording(time);
            return;
        }
        */

        NooieLog.d("NOOIE-JNI-demux-cloud seekToSelectTime seektime=" + DateTimeUtil.getUtcTimeString(time, DateTimeUtil.PATTERN_HMS) + " mTodayStartTime=" + mTodayStartTime + " time=" + time);
        int seekTime = (int) (time / 1000L);
        if (mPlaybackType == ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD) {
            seekTime = ((int) (time / 1000L) - (int) (mTodayStartTime / 1000L)) > 0 ? ((int) (time / 1000L) - (int) (mTodayStartTime / 1000L)) : 0;
        }
        //NooieLog.d("NOOIE-JNI-demux-cloud seekToSelectTime seek=" + DateTimeUtil.formatDayTimeByMinute((int) (time / 1000L / 60L)));
        NooieLog.d("NOOIE-JNI-demux-cloud seekToSelectTime seek basetime=" + (mTodayStartTime / 1000L) + " len=" + seekTime + " total=" + ((mTodayStartTime / 1000L) + seekTime));
        player.seek(seekTime);
        /*
        if (mPlaybackType == ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD) {
            time = (time - mTodayStartTime) > 0 ? (time - mTodayStartTime) : 0;
        }
        NooieLog.d("NOOIE-JNI-demux-cloud seekToSelectTime seek=" + DateTimeUtil.formatDayTimeByMinute((int) (time / 1000 / 60)));
        NooieLog.d("NOOIE-JNI-demux-cloud seekToSelectTime seek basetime=" + (mTodayStartTime / 1000L) + " len=" + (time / 1000) + " total=" + ((mTodayStartTime / 1000L) + (time / 1000)));
        player.seek((int) (time / 1000));

         */
    }

    public int getPlaybackSeekTime(long seekTime, long todayStartTime, int playbackType) {
        NooieLog.d("NOOIE-JNI-demux-cloud seekToSelectTime seektime=" + DateTimeUtil.getUtcTimeString(seekTime, DateTimeUtil.PATTERN_HMS) + " mTodayStartTime=" + todayStartTime + " time=" + seekTime);
        int playbackSeekTime = (int) (seekTime / 1000L);
        if (playbackType == ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD) {
            playbackSeekTime = ((int) (seekTime / 1000L) - (int) (todayStartTime / 1000L)) > 0 ? ((int) (seekTime / 1000L) - (int) (todayStartTime / 1000L)) : 0;
        }
        //NooieLog.d("NOOIE-JNI-demux-cloud getPlaybackSeekTime seek=" + DateTimeUtil.formatDayTimeByMinute((int) (seekTime / 1000L / 60L)));
        NooieLog.d("NOOIE-JNI-demux-cloud getPlaybackSeekTime seek basetime=" + (todayStartTime / 1000L) + " len=" + playbackSeekTime + " total=" + ((todayStartTime / 1000L) + playbackSeekTime));
        return playbackSeekTime;
        /*
        NooieLog.d("NOOIE-JNI-demux-cloud seekToSelectTime seektime=" + DateTimeUtil.getUtcTimeString(seekTime, DateTimeUtil.PATTERN_HMS));
        if (playbackType == ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD) {
            seekTime = (seekTime - todayStartTime) > 0 ? (seekTime - todayStartTime) : 0;
        }
        NooieLog.d("NOOIE-JNI-demux-cloud getPlaybackSeekTime seek=" + DateTimeUtil.formatDayTimeByMinute((int) (seekTime / 1000 / 60)));
        NooieLog.d("NOOIE-JNI-demux-cloud getPlaybackSeekTime seek basetime=" + (mTodayStartTime / 1000L) + " len=" + (seekTime / 1000) + " total=" + ((mTodayStartTime / 1000L) + (seekTime / 1000L)));
        return (int)(seekTime / 1000L);

         */
    }

    private long pickStartVideoTime(long timeStamp, List<CloudRecordInfo> recordInfoList) {

        if (!mIsInitCurrentTime) {
            return pickTimeCloseToCurrent(mCurrentSeekTime, recordInfoList);
        } else if (mPlaybackSourceType == ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_DIRECT) {
            mIsInitCurrentTime = false;
            return timeStamp;
        }

        //从Live跳到回放，默认从第一个时间点开始播
        mIsInitCurrentTime = false;
        long startVideoTime = CollectionUtil.isEmpty(recordInfoList) ? timeStamp : (mIsAsc ? (recordInfoList.get(0).getStartTime() + getDetectionTimeLen(recordInfoList.get(0)) * 1000L) : (recordInfoList.get(recordInfoList.size() - 1).getStartTime() + getDetectionTimeLen(recordInfoList.get(recordInfoList.size() - 1)) * 1000L));
        //NooieLog.d("-->> NooiePlaybackActivity pickStartVideoTime timestamp=" + timeStamp + " first start=" + (mIsAsc ? recordInfoList.get(0).getStartTime() : recordInfoList.get(recordInfoList.size() - 1).getStartTime()) + " today stamp=" + mTodayStartTime);
        return startVideoTime;
    }

    /**
     * 云卡切换，从同一个开始播放的时间点播放 -> 没有，就找附近可播的时间点
     *
     * @param currentSeekTime
     * @param recordInfoList
     * @return
     */
    private long pickTimeCloseToCurrent(long currentSeekTime, List<CloudRecordInfo> recordInfoList) {
        if (timerShaftPortrait != null && timerShaftPortrait.isInRecordList(currentSeekTime)) {
            return currentSeekTime;
        }

        long resultTime = currentSeekTime;
        long seekTimeGap = 0;
        if (CollectionUtil.isNotEmpty(recordInfoList)) {
            for (int i = 0; i < recordInfoList.size(); i++) {
                CloudRecordInfo recordInfo = recordInfoList.get(i);
                long timeGap = Math.abs(recordInfo.getStartTime() - currentSeekTime);
                if (i == 0) {
                    seekTimeGap = Math.abs(recordInfo.getStartTime() - currentSeekTime);
                    resultTime = recordInfo.getStartTime() + getDetectionTimeLen(recordInfo) * 1000L;
                } else if (timeGap < seekTimeGap) {
                    seekTimeGap = timeGap;
                    resultTime = recordInfo.getStartTime() + getDetectionTimeLen(recordInfo) * 1000L;
                }
            }
        }

        return resultTime;
    }

    private void setupTimeLineView() {
        timerShaftPortrait.setOnTimeShaftListener(this);
        timerShaftLand.setOnTimeShaftListener(this);
        resetTimeLine();
    }

    private void resetTimeLine() {
        if (timerShaftPortrait == null || timerShaftLand == null) {
            return;
        }
        timerShaftPortrait.setIsNormalRecord(false);
        timerShaftPortrait.setRecordList(new ArrayList<PeriodTime>());
        timerShaftPortrait.setDate(mTodayStartTime);
        timerShaftLand.setIsNormalRecord(false);
        timerShaftLand.setRecordList(new ArrayList<PeriodTime>());
        timerShaftLand.setDate(mTodayStartTime);
    }

    /**
     * 同步刻度指针
     */
    public void syncTimeShaft() {
        if (timerShaftPortrait == null || timerShaftLand == null) {
            return;
        }
        Calendar calendar = DateTimeUtil.getUtcCalendar();
        calendar.setTimeInMillis(mCurrentSeekTime);
        timerShaftPortrait.moveScroll(calendar);
        timerShaftLand.moveScroll(calendar);
    }

    private boolean checkSeekTimeExist(long seekTime, boolean showToast) {
        NooieLog.d("NOOIE-JNI-demux-cloud checkSeekTimeExist seektime=" + DateTimeUtil.getUtcTimeString(seekTime, DateTimeUtil.PATTERN_HMS));
        if (timerShaftPortrait != null && !timerShaftPortrait.isInRecordList(seekTime)) {
            if (showToast && mPlayerView != null) {
                mPlayerView.showNoRecording(false, seekTime);
            }
            return false;
        }
        return true;
    }

    @Override
    public void timeChangeOver(String timeStr, long time, boolean manuallyScroll) {
        if (isDestroyed() || timerShaftPortrait == null || timerShaftLand == null || player == null) {
            return;
        }

        NooieLog.d("-->> PlaybackComponent detection seek right scroll time=" + DateTimeUtil.getUtcTimeString(time, DateTimeUtil.PATTERN_HMS) + " timeStr=" + timeStr + " timeStamp=" + time);
        resetPlaybackDetections();
        mCurrentSeekTime = time;
        Calendar calendar = DateTimeUtil.getUtcCalendar();
        calendar.setTimeInMillis(time);
        timerShaftPortrait.moveScroll(calendar);
        timerShaftLand.moveScroll(calendar);
        pickAlarmJumpSeek();
//        selectNearlyDetections(mAlarmRecords, mPickAlarmSeek, SELECT_NEARLY_RECORD_COUNT, mCurrentSeekTime, mPicType, mExpireDate, mFilePrefix);
//        updatePlaybackDetection();
        //startLoadDetections(ConstantValue.CLOUD_RECORD_REQUEST_NORMAL, 1, mTodayStartTime, mCurrentSeekTime, mPicType, mExpireDate, mFilePrefix);
        firstLoadDetectionsData();
        displayAlarmJump();

        if (manuallyScroll) {
            int bindType = mIsOwner ? ApiConstant.BIND_TYPE_OWNER : ApiConstant.BIND_TYPE_SHARE;
            trySeekVideo(mModelType, time, mRecordFragments, mFileType, mExpireDate, mFilePrefix, bindType);
        }
    }

    @Override
    public void timeChangeAction() {
    }

    @Override
    public void moveStart() {
    }

    @Override
    public void moveStop() {
    }

    public void showRecordList(List<CloudRecordInfo> recordInfoList, boolean isPause, OnStartPlaybackListener listener) {
        if (isDestroyed() || timerShaftPortrait == null || timerShaftLand == null) {
            return;
        }
        if (recordInfoList.size() == 0) {
            if (mPlayerView != null) {
                mPlayerView.showNoRecording(true, 0);
            }
//            showPlaybackDetections(new ArrayList<CloudFileBean>());
            resetPlaybackDetections();
            timerShaftPortrait.setIsNormalRecord(true);
            timerShaftPortrait.setRecordList(new ArrayList<PeriodTime>());
            timerShaftLand.setIsNormalRecord(true);
            timerShaftLand.setRecordList(new ArrayList<PeriodTime>());
            mCurrentSeekTime = pickStartVideoTime(mDirectTime, recordInfoList);
            syncTimeShaft();
            if (listener != null) {
                listener.onPreStartPlayback(mDeviceId, false, false);
            }
        } else {
//            showPlaybackDetections(new ArrayList<CloudFileBean>());
            resetPlaybackDetections();
            ArrayList<PeriodTime> times = new ArrayList<>();
            for (int i = 0; i < recordInfoList.size(); i++) {
                times.add(RecordTimeHelper.toUtcPeriodTime(NooieApplication.mCtx, recordInfoList.get(i)));
            }
            timerShaftPortrait.setIsNormalRecord(true);
            timerShaftPortrait.setRecordList(times);
            timerShaftLand.setIsNormalRecord(true);
            timerShaftLand.setRecordList(times);
            mCurrentSeekTime = pickStartVideoTime(mDirectTime, recordInfoList);
            syncTimeShaft();
            pickAlarmJumpSeek();
            firstLoadDetectionsData();

            if (!isPause) {
                tryStartVideo(mModelType, (int) (mCurrentSeekTime / 1000L));
            }
            if (listener != null) {
                listener.onPreStartPlayback(mDeviceId, false, checkSeekTimeExist(mCurrentSeekTime, false));
            }
        }
    }

    public void showCloudRecordList(List<CloudRecordInfo> recordInfoList, List<RecordFragment> recordFragments, String fileType, int expireDate, String picType, String filePrefix, boolean isPause) {
        if (isDestroyed() || timerShaftPortrait == null || timerShaftLand == null) {
            return;
        }
        mFileType = fileType;
        mPicType = picType;
        mFilePrefix = filePrefix;
        mExpireDate = expireDate;
        if (recordInfoList.size() == 0 || recordFragments.size() == 0) {
            if (mPlayerView != null) {
                mPlayerView.showNoRecording(true, 0);
            }
//            showPlaybackDetections(new ArrayList<CloudFileBean>());
            resetPlaybackDetections();
            timerShaftPortrait.setIsNormalRecord(false);
            timerShaftPortrait.setRecordList(new ArrayList<PeriodTime>());
            timerShaftLand.setIsNormalRecord(false);
            timerShaftLand.setRecordList(new ArrayList<PeriodTime>());
            mCurrentSeekTime = pickStartVideoTime(mDirectTime, recordInfoList);
            syncTimeShaft();
        } else {
            resetPlaybackDetections();
            clearAlarmRecords();
            ArrayList<PeriodTime> times = new ArrayList<>();
            for (int i = 0; i < recordInfoList.size(); i++) {
                times.add(RecordTimeHelper.toUtcPeriodTime(NooieApplication.mCtx, recordInfoList.get(i)));
                addAlarmRecord(recordInfoList.get(i));
            }
            //showPlaybackDetections(getCloudFileBeans(mAlarmRecords));
            timerShaftPortrait.setIsNormalRecord(false);
            timerShaftPortrait.setRecordList(times);
            timerShaftLand.setIsNormalRecord(false);
            timerShaftLand.setRecordList(times);
            mCurrentSeekTime = pickStartVideoTime(mDirectTime, recordInfoList);
            syncTimeShaft();
            pickAlarmJumpSeek();
//            selectNearlyDetections(mAlarmRecords, mPickAlarmSeek, SELECT_NEARLY_RECORD_COUNT, mCurrentSeekTime, mPicType, mExpireDate, mFilePrefix);
//            updatePlaybackDetection();
            firstLoadDetectionsData();
//            resetPageAlarmRecords();
//            resetSelectNearlyRecords();
            displayAlarmJump();

            if (mRecordFragments == null) {
                mRecordFragments = new ArrayList<>();
            }
            mRecordFragments.clear();
            mRecordFragments.addAll(CollectionUtil.safeFor(recordFragments));
            if (!isPause) {
                int bindType = mIsOwner ? ApiConstant.BIND_TYPE_OWNER : ApiConstant.BIND_TYPE_SHARE;
                tryStartCloudVideo(mModelType, (int) (mCurrentSeekTime / 1000L), recordFragments, fileType, bindType, mTodayStartTime / 1000L, expireDate, filePrefix);
            }
        }
    }

    public void clearRecordList(boolean isCloud) {
        if (isCloud) {
            resetPlaybackDetections();
            timerShaftPortrait.setIsNormalRecord(false);
            timerShaftPortrait.setRecordList(new ArrayList<PeriodTime>());
            timerShaftLand.setIsNormalRecord(false);
            timerShaftLand.setRecordList(new ArrayList<PeriodTime>());
        } else {
            resetPlaybackDetections();
            timerShaftPortrait.setIsNormalRecord(true);
            timerShaftPortrait.setRecordList(new ArrayList<PeriodTime>());
            timerShaftLand.setIsNormalRecord(true);
            timerShaftLand.setRecordList(new ArrayList<PeriodTime>());
        }
    }

    private void addAlarmRecord(CloudRecordInfo cloudRecordInfo) {
        if (cloudRecordInfo != null && (cloudRecordInfo.getRecordType() == RecordType.MOTION_RECORD || cloudRecordInfo.getRecordType() == RecordType.SOUND_RECORD || cloudRecordInfo.getRecordType() == RecordType.PIR_RECORD)) {
            mAlarmRecords.add(cloudRecordInfo);
        }
    }

    private void clearAlarmRecords() {
        mPickAlarmSeek = 0;
        if (mAlarmRecords != null) {
            mAlarmRecords.clear();
        }
    }

    private int mAlarmSeek = 0;
    public void jumpToAlarm(boolean next) {
        if (isDestroyed() || (timerShaftPortrait == null && timerShaftLand == null)) {
            return;
        }
        if (CollectionUtil.isNotEmpty(mAlarmRecords) && mAlarmSeek < mAlarmRecords.size()) {
            if (next) {
                if (mPickAlarmSeek != -1) {
                    mAlarmSeek = mPickAlarmSeek == 0 ? mAlarmRecords.size() - 1 : mPickAlarmSeek - 1;
                    mPickAlarmSeek = -1;
                }
                mAlarmSeek = mAlarmSeek == mAlarmRecords.size() - 1 ? 0 : mAlarmSeek + 1;
            } else {
                if (mPickAlarmSeek != -1) {
                    mAlarmSeek = mPickAlarmSeek;
                    mPickAlarmSeek = -1;
                }
                mAlarmSeek = mAlarmSeek == 0 ? mAlarmRecords.size() - 1 : mAlarmSeek - 1;
            }
            NooieLog.d("-->> NooiePlaybackActivity jumpToAlarm seek to index=" + mAlarmSeek + " time=" + DateTimeUtil.getUtcTimeString(mAlarmRecords.get(mAlarmSeek).getStartTime(), DateTimeUtil.PATTERN_HMS));
            long time = mAlarmRecords.get(mAlarmSeek).getStartTime() + getDetectionTimeLen(mAlarmRecords.get(mAlarmSeek)) * 1000L;
            mCurrentSeekTime = time;
            Calendar calendar = DateTimeUtil.getUtcCalendar();
            calendar.setTimeInMillis(time);
            timerShaftPortrait.moveScroll(calendar);
            timerShaftLand.moveScroll(calendar);
            int bindType = mIsOwner ? ApiConstant.BIND_TYPE_OWNER : ApiConstant.BIND_TYPE_SHARE;
            trySeekVideo(mModelType, time, mRecordFragments, mFileType, mExpireDate, mFilePrefix, bindType);
        }
    }

    private void pickAlarmJumpSeek() {
        if (CollectionUtil.isEmpty(mAlarmRecords)) {
            mPickAlarmSeek = 0;
            return;
        }
        for (int i = 0; i < mAlarmRecords.size(); i++) {
            if (mAlarmRecords.get(i).getStartTime() >= mCurrentSeekTime) {
                NooieLog.d("-->> PlaybackComponent detection pickAlarmJumpSeek currentSeekTime=" + mCurrentSeekTime + " i=" + i + " iStartTime=" + mAlarmRecords.get(i).getStartTime());
                mPickAlarmSeek = i;
                break;
            } else if (i == mAlarmRecords.size() - 1) {
                mPickAlarmSeek = 0;
            }
        }
    }

    private void displayAlarmJump() {
        /*
        if (mPlaybackDetectionAdapter != null && mPickAlarmSeek > 0 && mPickAlarmSeek < mPlaybackDetectionAdapter.getItemCount()) {
            rcvPlaybackDetection.scrollToPosition(mPickAlarmSeek);
        }
        */
        if (mPlaybackDetectionAdapter != null && mPlaybackDetectionAdapter.getItemCount() > 0) {
            rcvPlaybackDetection.scrollToPosition(0);
        }
    }

    public static final int SELECT_NEARLY_RECORD_COUNT = 5;

//    public void resetSelectNearlyRecords() {
//        startLoadDetections(ConstantValue.CLOUD_RECORD_REQUEST_NORMAL, 1, mTodayStartTime, mCurrentSeekTime, mPicType, mExpireDate, mFilePrefix);
//    }

    private void startLoadDetections(int requestType, int page, long todayStartTime, long currentSeekTime, String fileType, int expiration, String filePrefix) {
        //if (mPlaybackType == ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD && mPlayerView != null) {
        if (mPlayerView !=    null) {
            int bindType = mIsOwner ? ApiConstant.BIND_TYPE_OWNER : ApiConstant.BIND_TYPE_SHARE;
            mPlayerView.onLoadDetections(ConstantValue.SUCCESS, requestType, page, todayStartTime, currentSeekTime, fileType, expiration, filePrefix, bindType);
        }
    }

    public void setDetectionsData(int requestType, List<CloudFileBean> cloudFileBeans) {
        if (requestType == ConstantValue.CLOUD_RECORD_REQUEST_NORMAL) {
            if (mSelectNearlyAlarmRecords == null) {
                mSelectNearlyAlarmRecords = new ArrayList<>();
            }
            mSelectNearlyAlarmRecords.clear();
            mSelectNearlyAlarmRecords.addAll(CollectionUtil.safeFor(cloudFileBeans));
        } else if (requestType == ConstantValue.CLOUD_RECORD_REQUEST_MORE) {
            if (mPageAlarmRecords == null) {
                mPageAlarmRecords = new ArrayList<>();
            }
            mPageAlarmRecords.addAll(CollectionUtil.safeFor(cloudFileBeans));
        }
        updatePlaybackDetection();
    }

    private void setupPlaybackDetection() {
        if (swtllPlaybackDetection != null) {
            swtllPlaybackDetection.setOnLoadMoreListener(this);
        }
        mPlaybackDetectionAdapter = new PlaybackDetectionAdapter();
        if (rcvPlaybackDetection != null) {
            rcvPlaybackDetection.setLayoutManager(new LinearLayoutManager(NooieApplication.mCtx));
            rcvPlaybackDetection.setAdapter(mPlaybackDetectionAdapter);
        }
        mPlaybackDetectionAdapter.setListener(new PlaybackDetectionAdapter.PlaybackDetectionListener() {
            @Override
            public void onItemClickListener(CloudFileBean cloudFileBean) {
                if (timerShaftPortrait != null && timerShaftLand != null && player != null && cloudFileBean != null) {
                    //long time = cloudFileBean.getBaseTime() + cloudFileBean.getStartTime() * 1000L;
                    long time = cloudFileBean.getBaseTime() + (cloudFileBean.getStartTime() + getDetectionTimeLen(cloudFileBean)) * 1000L;
                    NooieLog.d("-->> PlaybackComponent detection seek left click time=" + DateTimeUtil.getUtcTimeString(time, DateTimeUtil.PATTERN_HMS) + " timeStamp=" + time + " baseTime=" + cloudFileBean.getBaseTime() + " s=" + cloudFileBean.getStartTime());
                    mCurrentSeekTime = time;
                    Calendar calendar = DateTimeUtil.getUtcCalendar();
                    calendar.setTimeInMillis(time);
                    timerShaftPortrait.moveScroll(calendar);
                    timerShaftLand.moveScroll(calendar);
                    int bindType = mIsOwner ? ApiConstant.BIND_TYPE_OWNER : ApiConstant.BIND_TYPE_SHARE;
                    trySeekVideo(mModelType, time, mRecordFragments, mFileType, mExpireDate, mFilePrefix, bindType);
                }
            }
        });
        setDetectionDisplayType(ConstantValue.PLAY_DISPLAY_TYPE_DETAIL);
    }

    private static final int DEFAULT_DETECTION_TIME_LEN = 0;
    private int getDetectionTimeLen(CloudRecordInfo cloudRecordInfo) {
        int detectionTimeLen = DEFAULT_DETECTION_TIME_LEN;
        if (cloudRecordInfo == null || cloudRecordInfo.getCloudFileBean() == null) {
            return detectionTimeLen;
        }
        detectionTimeLen = getDetectionTimeLen(cloudRecordInfo.getCloudFileBean());
        return detectionTimeLen;
    }

    private int getDetectionTimeLen(CloudFileBean cloudFileBean) {
        int detectionTimeLen = DEFAULT_DETECTION_TIME_LEN;
        if (cloudFileBean == null) {
            return detectionTimeLen;
        }
        if (NooieCloudHelper.isDetectionAvailable(cloudFileBean.getMotionDetectionTime())) {
            detectionTimeLen = cloudFileBean.getMotionDetectionTime() == 0 ? DEFAULT_DETECTION_TIME_LEN : cloudFileBean.getMotionDetectionTime();
        } else if (NooieCloudHelper.isDetectionAvailable(cloudFileBean.getSoundDetectionTime())) {
            detectionTimeLen = cloudFileBean.getSoundDetectionTime() == 0 ? DEFAULT_DETECTION_TIME_LEN : cloudFileBean.getSoundDetectionTime();
        } else if (NooieCloudHelper.isDetectionAvailable(cloudFileBean.getPirDetectionTime())) {
            detectionTimeLen = cloudFileBean.getPirDetectionTime() == 0 ? DEFAULT_DETECTION_TIME_LEN : cloudFileBean.getPirDetectionTime();
        }
        boolean isOverDayTimeLen = ((cloudFileBean.getStartTime() + detectionTimeLen) * 1000L - cloudFileBean.getBaseTime()) >= DateTimeUtil.DAY_SECOND_COUNT * 1000L;
        if (isOverDayTimeLen) {
            detectionTimeLen = (((cloudFileBean.getBaseTime() / 1000L) + DateTimeUtil.DAY_SECOND_COUNT) - cloudFileBean.getStartTime()) < DEFAULT_DETECTION_TIME_LEN ? 0 : DEFAULT_DETECTION_TIME_LEN;
        }
        return detectionTimeLen;
    }

//    public void showPlaybackDetections(List<CloudFileBean> cloudFileBeans) {
//        if (mPlaybackDetectionAdapter != null) {
//            mPlaybackDetectionAdapter.setDataList(cloudFileBeans);
//        }
//    }

    public void refreshPlaybackDetections() {
        if (mPlaybackDetectionAdapter != null) {
            mPlaybackDetectionAdapter.notifyDataSetChanged();
        }
    }

    public void resetPlaybackDetections() {
        if (mSelectNearlyAlarmRecords == null) {
            mSelectNearlyAlarmRecords = new ArrayList<>();
        }
        mSelectNearlyAlarmRecords.clear();
        if (mPageAlarmRecords == null) {
            mPageAlarmRecords = new ArrayList<>();
        }
        mPageAlarmRecords.clear();
        if (mPlaybackDetectionAdapter != null) {
            mPlaybackDetectionAdapter.setDataList(null);
        }
    }

    public List<CloudFileBean> getCloudFileBeans(List<CloudRecordInfo> cloudRecordInfos) {
        List<CloudFileBean> cloudFileBeans = new ArrayList<>();
        for (CloudRecordInfo cloudRecordInfo : CollectionUtil.safeFor(cloudRecordInfos)) {
            if (cloudRecordInfo != null && cloudRecordInfo.getCloudFileBean() != null) {
                cloudFileBeans.add(cloudRecordInfo.getCloudFileBean());
            }
        }
        return cloudFileBeans;
    }

    private List<CloudFileBean> mSelectNearlyAlarmRecords = new ArrayList<>();
    private static final int ALARM_PAGE_COUNT = 10;
    private List<CloudFileBean> mPageAlarmRecords = new ArrayList<>();

//    public void resetPageAlarmRecords() {
//        startLoadDetections(ConstantValue.CLOUD_RECORD_REQUEST_MORE, 0, mTodayStartTime, mCurrentSeekTime, mPicType, mExpireDate, mFilePrefix);
//    }

    public void firstLoadDetectionsData() {
        startLoadDetections(ConstantValue.CLOUD_RECORD_REQUEST_NORMAL, 1, mTodayStartTime, mCurrentSeekTime, mPicType, mExpireDate, mFilePrefix);
        startLoadDetections(ConstantValue.CLOUD_RECORD_REQUEST_MORE, 0, mTodayStartTime, mCurrentSeekTime, mPicType, mExpireDate, mFilePrefix);
    }

    @Override
    public void onLoadMore() {
        startLoadDetections(ConstantValue.CLOUD_RECORD_REQUEST_MORE, 1, mTodayStartTime, mCurrentSeekTime, mPicType, mExpireDate, mFilePrefix);
        stopLoadMore();
    }

    public void stopLoadMore() {
        if (swtllPlaybackDetection != null && swtllPlaybackDetection.isLoadingMore()) {
            swtllPlaybackDetection.setLoadingMore(false);
        }
    }

    public void setLoadMoreEnabled(boolean enable) {
        if (swtllPlaybackDetection != null) {
            swtllPlaybackDetection.setLoadMoreEnabled(enable);
        }
    }

    public boolean isDestroyed() {
        return false;
    }

    public void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
    }

    public void setIsOwner(boolean isOwner) {
        mIsOwner = isOwner;
    }

    public void setIsSubDevice(boolean isSubDevice) {
        mIsSubDevice = isSubDevice;
    }

    public void setIsLpDevice(boolean isLpDevice) {
        mIsLpDevice = isLpDevice;
    }

    public void setConnectionMode(int connectionMode) {
        mConnectionMode = connectionMode;
    }

    public void setModelType(int modelType) {
        mModelType = modelType;
    }

    public void setPlaybackType(int playbackType) {
        mPlaybackType = playbackType;
    }

    public void setPlaybackSourceType(int playbackSourceType) {
        mPlaybackSourceType = playbackSourceType;
    }

    public void setIsInitCurrentTime(boolean isInitCurrentTime) {
        mIsInitCurrentTime = isInitCurrentTime;
    }

    public long getTodayStartTime() {
        return mTodayStartTime;
    }

    public void setTodayStartTime(long todayStartTime) {
        this.mTodayStartTime = todayStartTime;
    }

    public long getCurrentSeekTime() {
        return mCurrentSeekTime;
    }

    public void setCurrentSeekTime(long currentSeekTime) {
        this.mCurrentSeekTime = currentSeekTime;
    }

    public long getDirectTime() {
        return mDirectTime;
    }

    public void setDirectTime(long directTime) {
        this.mDirectTime = directTime;
    }

    public void setModel(String model) {
        this.mModel = model;
    }

    public void setCloudRecordInfo(List<RecordFragment> recordFragments, String fileType, int expireDate, String picType) {
        if (mRecordFragments == null) {
            mRecordFragments = new ArrayList<>();
        }
        mRecordFragments.clear();
        mRecordFragments.addAll(CollectionUtil.safeFor(recordFragments));
        mFileType = fileType;
        mPicType = picType;
        mExpireDate = expireDate;
    }

    public void setupPlayback(int playbackType, int playbackSourceType, long directTime) {
        mPlaybackType = playbackType;
        mPlaybackSourceType = playbackSourceType;
        mDirectTime = directTime;
        mIsInitCurrentTime = true;
        mTodayStartTime = mDirectTime == 0 ? DateTimeUtil.getUtcTodayStartTimeStamp() : DateTimeUtil.getUtcDayStartTimeStamp(mDirectTime);
        mCurrentSeekTime = mTodayStartTime;
        clearAlarmRecords();
        resetTimeLine();
        resetPlaybackDetections();
    }

    public void resetPlayback(String deviceId, boolean isOwner, boolean isSubDevice, boolean isLpDevice, int connectionMode, int modelType, int playbackType, int playbackSourceType, long directTime, String model) {
        setDeviceId(deviceId);
        setIsOwner(isOwner);
        setIsSubDevice(isSubDevice);
        setIsLpDevice(isLpDevice);
        setConnectionMode(connectionMode);
        setModelType(modelType);
        setModel(model);
        setPlaybackType(playbackType);
        setupPlayback(playbackType, playbackSourceType, directTime);
    }

    public void setDetectionDisplayType(int displayType) {
        setLoadMoreEnabled(displayType == ConstantValue.PLAY_DISPLAY_TYPE_DETAIL ? true : false);
        if (mPlaybackDetectionAdapter != null) {
            mPlaybackDetectionAdapter.setDisplayType(displayType);
            updatePlaybackDetection();
        }
    }

    public void updatePlaybackDetection() {
        if (mPlaybackDetectionAdapter == null) {
            return;
        }
        if (mPlaybackDetectionAdapter.getDisplayType() == ConstantValue.PLAY_DISPLAY_TYPE_DETAIL) {
            mPlaybackDetectionAdapter.setDataList(mPageAlarmRecords);
        } else {
            mPlaybackDetectionAdapter.setDataList(mSelectNearlyAlarmRecords);
        }
    }

    private void updatePlayState(int type, int state) {
        if (mPlayerDelegate != null) {
            mPlayerDelegate.setPlayState(type, state, System.currentTimeMillis());
        }
    }

    public void release() {
        if (mPlaybackDetectionAdapter != null) {
            mPlaybackDetectionAdapter.release();
            mPlaybackDetectionAdapter = null;
        }

        if (mRecordFragments != null) {
            mRecordFragments.clear();
            mRecordFragments = null;
        }

        if (mAlarmRecords != null) {
            mAlarmRecords.clear();
            mAlarmRecords = null;
        }
    }

}
