package com.afar.osaio.account.model;

import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.nooie.common.bean.CConstant;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.file.ZipUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.aws.AwsFilePreSign;
import com.nooie.sdk.api.network.cloud.CloudService;
import com.nooie.sdk.base.SDKDataAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import rx.Observable;
import rx.functions.Func1;

/**
 * PlatformUpgradeModel
 *
 * @author Administrator
 * @date 2019/4/12
 */
public class PlatformUpgradeModel implements IPlatformUpgradeModel {

    private static final String LOG_PREFIX_TAG = "Android";

    public PlatformUpgradeModel() {}

    @Override
    public Observable<Boolean> reportNooieLog(final String uid, final String account) {
        return Observable.just(uid)
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String uid) {
                        String crashLogZipPath = "";
                        List<String> logPaths = getCrashLog();
                        if (CollectionUtil.isNotEmpty(logPaths)) {
                            crashLogZipPath = zipCrashLog(logPaths, uid);
                        }
                        return Observable.just(crashLogZipPath);
                    }
                })
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        return Observable.from(getCrashZipLog(uid));
                    }
                })
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String logZipPath) {
                        if (TextUtils.isEmpty(logZipPath)) {
                            return Observable.just(false);
                        }
                        //NooieLog.d("-->> PlatformUpgradeModel call logzippath=" + logZipPath);
                        try {
                            final File logZipFile = new File(logZipPath);
                            if (logZipFile.exists()) {
                                return CloudService.getService().getLogUploadPresignUrl(uid, account, logZipFile.getName(), (int)logZipFile.length())
                                        .flatMap(new Func1<BaseResponse<AwsFilePreSign>, Observable<Response>>() {
                                            @Override
                                            public Observable<Response> call(BaseResponse<AwsFilePreSign> response) {
                                                if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                                                    //NooieLog.d("-->> PlatformUpgradeModel call upload presignurl=" + response.getData().getUrl());
                                                    try {
                                                        String userNameParam = "username=" + account;
                                                        Response uploadResponse = CloudService.getService().upLoadFileToCloud(response.getData().getUrl(), "application/zip", userNameParam, logZipFile).execute();
                                                        return Observable.just(uploadResponse);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        NooieLog.d("-->> PlatformUpgradeModel call e=" + e.toString());
                                                    }
                                                }
                                                return Observable.just(null);
                                            }
                                        })
                                        .flatMap(new Func1<Response, Observable<Boolean>>() {
                                            @Override
                                            public Observable<Boolean> call(Response response) {
                                                if (response != null && response.isSuccessful()) {
                                                    NooieLog.d("-->> PlatformUpgradeModel call upload success logzipfile=" + logZipFile.getName());
                                                    FileUtil.deleteFile(logZipFile);
                                                }
                                                return Observable.just(true);
                                            }
                                        });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return Observable.just(false);
                    }
                });
    }

    @Override
    public List<String> getCrashLogForClearing(boolean isClear) {
        List<String> recentLogs = new ArrayList<>();
        try {
            List<File> logFiles = FileUtil.getLogFiles(NooieApplication.mCtx);
            String writingLogFileName = SDKDataAPI.sharedInstance().getConfigOptions() != null ? SDKDataAPI.sharedInstance().getConfigOptions().mLogPath : new String();
            for (File logFile : CollectionUtil.safeFor(logFiles)) {
                NooieLog.d("-->> PlatformUpgradeModel getCrashLog path=" + logFile.getAbsolutePath() + " modify time=" + logFile.lastModified() + " name=" + logFile.getName() + " write log name=" + writingLogFileName);
                boolean isWriteLogFile = !TextUtils.isEmpty(writingLogFileName) && writingLogFileName.contains(logFile.getName());
                boolean isEffectiveLogFile = !isClear && logFile.lastModified() > 0 && logFile.lastModified() > DateTimeUtil.getTodayStartTimeStamp();
                //boolean isEffectiveLogFile = !isClear && logFile.lastModified() > 0 && logFile.lastModified() > (System.currentTimeMillis() - 2 * 60 * 1000);
                if (isWriteLogFile || isEffectiveLogFile) {
                    continue;
                }
                recentLogs.add(logFile.getAbsolutePath());
            }
        } catch (Exception e) {
        }
        return recentLogs;
    }

    public List<String> getCrashLog() {
        List<String> recentLogs = new ArrayList<>();
        try {
            List<File> logFiles = FileUtil.getLogFiles(NooieApplication.mCtx);
            String writingLogFileName = SDKDataAPI.sharedInstance().getConfigOptions() != null ? SDKDataAPI.sharedInstance().getConfigOptions().mLogPath : new String();
            for (File logFile : CollectionUtil.safeFor(logFiles)) {
                NooieLog.d("-->> PlatformUpgradeModel getCrashLog path=" + logFile.getAbsolutePath() + " modify time=" + logFile.lastModified() + " name=" + logFile.getName() + " write log name=" + writingLogFileName);
                boolean isZipFile = logFile != null && !TextUtils.isEmpty(logFile.getName()) && logFile.getName().contains("zip");
                boolean isWriteLogFile = !TextUtils.isEmpty(writingLogFileName) && writingLogFileName.contains(logFile.getName());
                if (isZipFile || isWriteLogFile) {
                    continue;
                }
                recentLogs.add(logFile.getAbsolutePath());
            }
        } catch (Exception e) {
        }
        return recentLogs;
    }

    public List<String> getCrashZipLog(String uid) {
        List<String> zipLogs = new ArrayList<>();
        try {
            List<File> logFiles = FileUtil.getLogFiles(NooieApplication.mCtx);
            for (File logFile : CollectionUtil.safeFor(logFiles)) {
                //NooieLog.d("-->> PlatformUpgradeModel getCrashZipLog path=" + logFile.getAbsolutePath() + " modify time=" + logFile.lastModified());
                boolean isZipFile = logFile != null && !TextUtils.isEmpty(logFile.getName()) && logFile.getName().contains("zip") && logFile.getName().contains(uid);
                if (isZipFile) {
                    zipLogs.add(logFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
        }
        return zipLogs;
    }

    public String zipCrashLog(List<String> logFilePaths, String uid) {
        if (CollectionUtil.isEmpty(logFilePaths)) {
            return "";
        }
        StringBuilder crashLogZipPath = new StringBuilder();
        crashLogZipPath.append(LOG_PREFIX_TAG);
        crashLogZipPath.append(CConstant.UNDER_LINE);
        crashLogZipPath.append(uid);
        crashLogZipPath.append(CConstant.UNDER_LINE);
        crashLogZipPath.append(DateTimeUtil.getTimeString(System.currentTimeMillis(), DateTimeUtil.PATTERN_YMD_HMS_5));

        String srcFolder = FileUtil.getLogFolder(NooieApplication.mCtx) + File.separator + crashLogZipPath;
        String compressName = crashLogZipPath.toString();
        try {
            for (String logFilePath : CollectionUtil.safeFor(logFilePaths)) {
                FileUtil.moveFile(logFilePath, srcFolder);
            }
            ZipUtil.compressFile(srcFolder, compressName, FileUtil.getLogFolder(NooieApplication.mCtx));
            ZipUtil.compressFile(srcFolder, "", FileUtil.getLogFolder(NooieApplication.mCtx));
            FileUtil.deleteFolder(srcFolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return srcFolder + ".zip";
    }
}
