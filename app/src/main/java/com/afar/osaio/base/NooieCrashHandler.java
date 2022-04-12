package com.afar.osaio.base;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.afar.osaio.BuildConfig;
import com.google.gson.Gson;
import com.afar.osaio.smart.db.dao.NooieLogService;
import com.nooie.sdk.db.entity.LogEntity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.FileUtils;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.file.ZipUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.aws.AwsFilePreSign;
import com.nooie.sdk.api.network.cloud.CloudService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class NooieCrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "NooieCrashHandler";
    private static final String FILE_NAME_PREFIX = "Crash_";
    private static final String FILE_NAME_SUFFIX = ".txt";
    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/" + FileUtils.MainDir + "/Crash/";
    private static final String CRASH_LN = "\n";
    private static final String CRASH_AT = "AT";
    //debug为true打印crash，false不打印
    private boolean debug = BuildConfig.DEBUG;
    private Context mContext;
    //系统默认的异常处理（默认情况下，系统会终止当前的异常程序）
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private String mUid;
    private String mAccount;

    private NooieCrashHandler() {}

    private static class NooieCrashHandlerHodler {
        private static final NooieCrashHandler INSTANCE = new NooieCrashHandler();
    }

    public static NooieCrashHandler getINSTANCE() {
        return NooieCrashHandlerHodler.INSTANCE;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        //获取系统默认的异常处理器
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        //将当前实例设为系统默认的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        //NooieLog.d(TAG + "-->> save path=" + Environment.getExternalStorageDirectory().getPath() + " current crash name=" + createCrashFileName() + " DEBUG=" + BuildConfig.DEBUG + " version=" + BuildConfig.VERSION_NAME + "_" + BuildConfig.VERSION_CODE);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            //dumpExceptionToSDCard(ex);
            //saveRecentCrashLog();

            //打印出当前调用栈信息
            //ex.printStackTrace();
            /*
            if (mDefaultCrashHandler != null) {
                //mDefaultCrashHandler.uncaughtException(thread, ex);
            } else {
                Process.killProcess(Process.myPid());
            }
            */
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dumpExceptionToSDCard(Throwable ex) throws Exception {
        logMsg("ex=" + ex.getStackTrace().length + " cause e=" + ex.toString());

        if (!debug && !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            logMsg("Close dump crash");
            return;
        }

        CrashInfo crashInfo = createCrashInfo(ex);
        Gson gson = new Gson();
        final String crashInfoJson = gson.toJson(crashInfo);
        logMsg("crashInfoJson=" + crashInfoJson);
        new Thread(new Runnable() {
            @Override
            public void run() {
                savedToLocal(crashInfoJson + ",");
            }
        }).start();
    }

    private void uploadExceptionToServer(Throwable ex) {}

    private void savedToLocal(String crashInfo) {
        File crashFile = new File(createCrashFileName());
        if (crashFile.exists()) {
        }
        try {
            FileOutputStream fOut = new FileOutputStream(crashFile, true);
            byte[] bytes = crashInfo.getBytes();
            fOut.write(bytes);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createCrashFileName() {
        File path = new File(PATH);
        if (!path.exists()) {
            path.mkdirs();
        }
        return PATH + FILE_NAME_PREFIX + new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())) + FILE_NAME_SUFFIX;
    }

    private CrashInfo createCrashInfo(Throwable ex) throws PackageManager.NameNotFoundException {
        CrashInfo crashInfo = new CrashInfo();
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        crashInfo.setAppVersion(packageInfo.versionName + "_" + Build.VERSION.CODENAME);
        crashInfo.setOsVersion(Build.VERSION.RELEASE + "_" +Build.VERSION.SDK_INT);
        crashInfo.setVendor(Build.MANUFACTURER);
        crashInfo.setModel(Build.MODEL);
        crashInfo.setCpuAbi(Build.CPU_ABI);
        crashInfo.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
        crashInfo.setOwner("Nooie");
        crashInfo.setStackTrace(createCrashStake(ex));

        return crashInfo;
    }

    private String createCrashStake(Throwable ex) {
        StringBuilder exBuilder = new StringBuilder();
        exBuilder.append(ex.getClass().getName() + ":" + ex.getLocalizedMessage());
        exBuilder.append(CRASH_LN);
        for(StackTraceElement element : ex.getStackTrace()) {
            exBuilder.append(CRASH_AT + element.toString());
            exBuilder.append(CRASH_LN);
        }
        logMsg("ex crash stake=" + exBuilder.toString());
        return exBuilder.toString();
    }

    private void saveRecentCrashLog() {
        Observable.just("")
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        List<String> recentCrashLogs = getRecentCrashLog();
                        for (String crashLogPath : CollectionUtil.safeFor(recentCrashLogs)) {
                            if (!TextUtils.isEmpty(mUid) && !TextUtils.isEmpty(mAccount)) {
                                long saveTime = System.currentTimeMillis();
                                String zipName = "nooie_" + DateTimeUtil.getTimeString(saveTime, DateTimeUtil.PATTERN_YMD_HMS_3) + "_" + saveTime;
                                //String zipName = "nooie_log_" + mUid + "_" + MD5Util.MD5Hash(crashLogPath);
                                //NooieLog.d("-->> NooieCrashHandler saveRecentCrashLog save in db zipname=" + zipName + " dir=" + crashLogPath + "savetime=" + DateTimeUtil.getTimeString(saveTime, DateTimeUtil.PATTERN_YMD_HMS_3) + " savetimestamp=" + saveTime);
                                NooieLogService.getInstance().addLog(mAccount, mUid, crashLogPath, zipName, saveTime, 0);
                            }
                        }
                        return Observable.just(ConstantValue.SUCCESS);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(String result) {
                    }
                });
    }

    public List<String> getRecentCrashLog() {
        List<String> recentLogs = new ArrayList<>();
        try {
            List<File> logFiles = FileUtils.getLogFiles();
            for (int i = 0; i < 2; i++) {
                String recentLog = "";
                long tmpModifyTime = 0;
                int removeIndex = -1;
                for (int j = 0; j < logFiles.size(); j++) {
                    File logFile = logFiles.get(j);
                    //NooieLog.d("-->> Nooie getRecentLog path= " + logFile.getAbsolutePath() + " modify time=" + DateTimeUtil.getTimeString(logFile.lastModified(), DateTimeUtil.PATTERN_YMD_HMS_3));
                    boolean isZipFile = logFile != null && !TextUtils.isEmpty(logFile.getName()) && logFile.getName().contains("zip");
                    if (isZipFile) {
                        //NooieLog.d("-->> NooieCrashHandler getRecentCrashLog path= " + logFile.getAbsolutePath() + " name=" + logFile.getName());
                        continue;
                    }
                    if (logFile != null && tmpModifyTime < logFile.lastModified()) {
                        //NooieLog.d("-->> NooieCrashHandler getRecentCrashLog path= " + logFile.getAbsolutePath() + " modify time=" + DateTimeUtil.getTimeString(logFile.lastModified(), DateTimeUtil.PATTERN_YMD_HMS_3));
                        tmpModifyTime = logFile.lastModified();
                        recentLog = logFile.getAbsolutePath();
                        removeIndex = j;
                    }
                }
                if (!TextUtils.isEmpty(recentLog)) {
                    NooieLog.d("-->> NooieCrashHandler getRecentCrashLog path= " + recentLog + " modify time=" + DateTimeUtil.getTimeString(tmpModifyTime, DateTimeUtil.PATTERN_YMD_HMS_3));
                    recentLogs.add(recentLog);
                }

                if (removeIndex > 0 && removeIndex < logFiles.size()) {
                    logFiles.remove(removeIndex);
                }
                recentLog = "";
                tmpModifyTime = 0;
                removeIndex = -1;
            }
            /*
            for (File logFile : CollectionUtil.safeFor(logFiles)) {
                NooieLog.d("-->> NooieCrashHandler getRecentCrashLog path= " + logFile.getAbsolutePath() + " modify time=" + logFile.lastModified());
                if (logFile != null && tmpModifyTime < logFile.lastModified()) {
                    tmpModifyTime = logFile.lastModified();
                    recentLog = logFile.getAbsolutePath();
                }
            }
            if (!TextUtils.isEmpty(recentLog)) {
                recentLogs.add(recentLog);
            }
            */
        } catch (Exception e) {
        }
        return recentLogs;
    }

    public void upLoadLogFiles() {
        if (!TextUtils.isEmpty(mAccount)) {
            Observable.just(mAccount)
                    .flatMap(new Func1<String, Observable<LogEntity>>() {
                        @Override
                        public Observable<LogEntity> call(String account) {
                            List<LogEntity> logFiles = NooieLogService.getInstance().getLogsByAccount(account);
                            if (CollectionUtil.isEmpty(logFiles)) {
                                Observable.error(new Throwable(""));
                            }
                            return Observable.from(logFiles);
                        }
                    })
                    .flatMap(new Func1<LogEntity, Observable<LogEntity>>() {
                        @Override
                        public Observable<LogEntity> call(LogEntity logEntity) {
                            ZipUtil.compressFile(logEntity.getPath(), logEntity.getZipName(), FileUtils.getLogFolder());
                            String zipPath = FileUtils.getLogFolder() + File.separator + logEntity.getZipName() + ".zip";
                            if (new File(zipPath).exists()) {
                                return Observable.just(logEntity);
                            }
                            return Observable.just(null);
                        }
                    })
                    .flatMap(new Func1<LogEntity, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(LogEntity logEntity) {
                            if (logEntity == null) {
                                return Observable.just(false);
                            }
                            final String zipName = logEntity.getZipName();
                            String fileName = zipName + ".zip";
                            final String filePath = FileUtils.getLogFolder() + File.separator + fileName;
                            final File file = new File(filePath);
                            if (file.exists()) {
                                //NooieLog.d("-->> NooieCrashHandler upLoadLogFiles log upload zipName=" + zipName + " zipFilePath=" + filePath);
                                return CloudService.getService().getLogUploadPresignUrl(mUid, mAccount, fileName, (int)file.length())
                                        .flatMap(new Func1<BaseResponse<AwsFilePreSign>, Observable<Response>>() {
                                            @Override
                                            public Observable<Response> call(BaseResponse<AwsFilePreSign> response) {
                                                if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                                                    //NooieLog.d("-->> NooieCrashHandler upLoadLogFiles call log upload presignurl=" + response.getData().getUrl());
                                                    try {
                                                        String userNameParam = "username=" + mAccount;
                                                        Response uploadResponse = CloudService.getService().upLoadFileToCloud(response.getData().getUrl(), "application/zip", userNameParam, file).execute();
                                                        return Observable.just(uploadResponse);
                                                    } catch (Exception e) {
                                                    }
                                                }
                                                return Observable.just(null);
                                            }
                                        })
                                        .flatMap(new Func1<Response, Observable<Boolean>>() {
                                            @Override
                                            public Observable<Boolean> call(Response response) {
                                                //NooieLog.d("-->> NooieCrashHandler upLoadLogFiles call log upload result code=" + (response != null ? response.code() : 0));
                                                if (response != null && response.isSuccessful()) {
                                                    NooieLogService.getInstance().updateLog(zipName, 1);
                                                    FileUtils.deleteFile(filePath);
                                                }
                                                return Observable.just(true);
                                            }
                                        });
                            }
                            return Observable.just(true);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(Boolean result) {
                        }
                    });
        }
    }

    public void setUidAndAccount(String uid, String account) {
        mUid = uid;
        mAccount = account;
    }

    public void clearUidAndAccount() {
        mUid = null;
        mAccount = null;
    }

    private void logMsg(String msg) {
        NooieLog.d("--> " + TAG + " " + msg);
    }

    private class CrashInfo {
        private String time;
        private String owner;
        private String appVersion;
        private String osVersion;
        private String vendor;
        private String model;
        private String cpuAbi;
        private String stackTrace;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public String getVendor() {
            return vendor;
        }

        public void setVendor(String vendor) {
            this.vendor = vendor;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getCpuAbi() {
            return cpuAbi;
        }

        public void setCpuAbi(String cpuAbi) {
            this.cpuAbi = cpuAbi;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }
    }
}
