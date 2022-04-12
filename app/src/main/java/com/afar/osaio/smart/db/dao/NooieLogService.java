package com.afar.osaio.smart.db.dao;

import android.text.TextUtils;

import com.nooie.sdk.db.base.core.DbManager;
import com.nooie.sdk.db.entity.DaoSession;
import com.nooie.sdk.db.entity.LogEntity;
import com.nooie.sdk.db.entity.LogEntityDao;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;

import java.util.List;

/**
 * UserRegionService
 *
 * @author Administrator
 * @date 2019/6/12
 */
public class NooieLogService {

    private static class SingleTon {
        private static final NooieLogService INSTANCE = new NooieLogService();
    }

    public static NooieLogService getInstance() {
        return SingleTon.INSTANCE;
    }

    private LogEntityDao mLogDao;

    private NooieLogService() {
        DaoSession daoSession = DbManager.getInstance().getDaoSession();
        mLogDao = daoSession.getLogEntityDao();
    }

    public void addLog(String account, String uuid, String path, String zipName, long saveTime, int upload) {
        if (getLogByPath(path) != null) return;

        LogEntity logEntity = new LogEntity();
        logEntity.setAccount(account);
        logEntity.setUuid(uuid);
        logEntity.setPath(path);
        logEntity.setZipName(zipName);
        logEntity.setSaveTime(saveTime);
        logEntity.setUpload(upload);
        mLogDao.insertOrReplace(logEntity);
    }

    public LogEntity getLogByAccount(String account) {
        try {
            return mLogDao.queryBuilder()
                    .where(LogEntityDao.Properties.Account.eq(account))
                    .build()
                    .unique();
        } catch (Exception e) {
        }
        return null;
    }

    public LogEntity getLogByPath(String path) {
        try {
            return mLogDao.queryBuilder()
                    .where(LogEntityDao.Properties.Path.eq(path))
                    .build()
                    .unique();
        } catch (Exception e) {
        }
        return null;
    }

    public LogEntity getLogByZipName(String zipName) {
        try {
            return mLogDao.queryBuilder()
                    .where(LogEntityDao.Properties.ZipName.eq(zipName))
                    .build()
                    .unique();
        } catch (Exception e) {
        }
        return null;
    }

    public List<LogEntity> getLogsByAccount(String account) {
        //log();
        try {
            return mLogDao.queryBuilder()
                    .where(LogEntityDao.Properties.Account.eq(account), LogEntityDao.Properties.Upload.eq(0))
                    .build()
                    .list();
        } catch (Exception e) {
        }
        return null;
    }

    public void deleteLogByAccount(String account) {
        try {
            LogEntity logEntity = getLogByAccount(account);
            if (logEntity != null) {
                mLogDao.delete(logEntity);
            }
        } catch (Exception e) {
        }
    }

    public void updateLog(String zipName, int upload) {
        try {
            LogEntity logEntity = getLogByZipName(zipName);
            if (checkLogAvailable(logEntity)) {
                logEntity.setUpload(upload);
                mLogDao.update(logEntity);
            }
        } catch (Exception e) {
        }
    }

    public boolean checkLogAvailable(LogEntity logEntity) {
        return logEntity != null && !TextUtils.isEmpty(logEntity.getAccount()) && !TextUtils.isEmpty(logEntity.getUuid()) && !TextUtils.isEmpty(logEntity.getPath()) && !TextUtils.isEmpty(logEntity.getZipName());
    }

    public void log() {
        try {
            List<LogEntity> logEntities = mLogDao.queryBuilder().build().forCurrentThread().list();
            for (LogEntity logEntity : CollectionUtil.safeFor(logEntities)) {
                NooieLog.d("-->> NooieLogService log account=" + logEntity.getAccount() + " path=" + logEntity.getPath() + " zipName=" + logEntity.getZipName() + " upload=" + logEntity.getUpload());
            }
        } catch (Exception e) {
        }
    }
}
