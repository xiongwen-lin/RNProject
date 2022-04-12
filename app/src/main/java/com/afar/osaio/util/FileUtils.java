package com.afar.osaio.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;

import com.nooie.common.base.GlobalData;
import com.nooie.common.bean.CConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.afar.osaio.base.NooieApplication;
import com.bumptech.glide.Glide;
import com.nooie.common.utils.configure.PhoneUtil;
import com.nooie.common.utils.log.NooieLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by victor on 2018/6/27
 * Email is victor.qiao.0604@gmail.com
 */
public class FileUtils {
    public static final String MainDir = "Victure";
    public static final String VideoDir = "Video";
    public static final String VideoThumbsDir = "VideoThumb";
    public static final String SnapshotDir = "Snapshot";
    public static final String PreviewThumDir = "PreviewThumb";
    public static final String CacheDir = "Cache";
    public static final String PersonPortrait = "Portrait";
    public static final String RomDir = "Rom";
    public static final String LogDir = "log";
    public static final String DetectionThumbnail = "Thumbnail";

    public static final String NOOIE_PREVIEW_THUMBNAIL_PREFIX = "VICTURE_THUMBNAIL";
    public static final String NOOIE_SNAP_SHOT_THUMBNAIL_SUFFIX = "nooie";
    public static final String NOOIE_SNAP_SHOT_SUFFIX = "JPG";


    /**
     * 获得存储主目录名称
     *
     * @return
     */
    public static String getMainDir() {
        return MainDir;
    }

    public static String getLocalRootSavePathDir(String dir) {
        String path = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || !Environment.isExternalStorageRemovable()) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + dir;
        } else {
            path = NooieApplication.mCtx.getFilesDir().getAbsolutePath() + File.separator + MainDir
                    + File.separator + dir;
        }
        return path;
    }

    /**
     * 获取指定用户名下的录像目录
     *
     * @param account
     * @return
     */
    public static File getRecordDir(String account) {
        String videoDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || !Environment.isExternalStorageRemovable()) {
            videoDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + VideoDir;
        } else {
            videoDir = NooieApplication.mCtx.getFilesDir().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + VideoDir;
        }

        File file = null;
        file = new File(videoDir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获取preview thumbnail的路径，包含文件名
     * .../Nooie/PreviewThumb/
     *
     * @return
     */
    public static String getPreviewThubFolder() {
        String thumbDir = getPrivateLocalRootSavePathDir(PreviewThumDir);
        /*
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            thumbDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + PreviewThumDir;
        } else {
            thumbDir = NooieApplication.mCtx.getFilesDir().getAbsolutePath() + File.separator + MainDir
                    + File.separator + PreviewThumDir;
        }
        */
        File file = new File(thumbDir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public static String getPreviewThubSavePath(String deviceId) {
        return new File(getPreviewThubFolder(), String.format("%s_%s_%s.%s", NOOIE_PREVIEW_THUMBNAIL_PREFIX, deviceId, String.valueOf(System.currentTimeMillis()), NOOIE_SNAP_SHOT_SUFFIX)).getAbsolutePath();
    }

    public static String mapPreviewThubPath(String path) {
        return path.replace(NOOIE_SNAP_SHOT_SUFFIX, NOOIE_SNAP_SHOT_THUMBNAIL_SUFFIX);
    }

    public static void renamePreviewThub(String path, String newPath) {
        new File(path).renameTo(new File(newPath));
    }

    /**
     * 获得录像缩略图的目录
     *
     * @param account
     * @return
     */
    public static File getRecordThumbsDir(String account) {
        String videoThumbsDir = null;
        if (Environment.getExternalStorageState().equals((Environment.MEDIA_MOUNTED))
                || !Environment.isExternalStorageRemovable()) {
            videoThumbsDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + VideoThumbsDir;
        } else {
            videoThumbsDir = NooieApplication.mCtx.getFilesDir().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + VideoThumbsDir;
        }

        File file = null;
        file = new File(videoThumbsDir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }


    /**
     * 获取指定用户名下的抓拍目录
     *
     * @param account
     * @return
     */
    public static File getSnapshotDir(String account) {
        String snapshotDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            snapshotDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + SnapshotDir;
        } else {
            snapshotDir = NooieApplication.mCtx.getFilesDir().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + SnapshotDir;
        }
        File file = new File(snapshotDir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获取指定用户名下的录像目录
     *
     * @param account
     * @return
     */
    public static File getRecordVideoDir(String account) {
        String snapshotDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            snapshotDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + VideoDir;
        } else {
            snapshotDir = NooieApplication.mCtx.getFilesDir().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + VideoDir;
        }
        File file = new File(snapshotDir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获取指定用户名下的缓存目录
     *
     * @param account
     * @return
     */
    public static File getCacheDir(String account) {
        String cacheDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + CacheDir;
        } else {
            cacheDir = NooieApplication.mCtx.getCacheDir().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + CacheDir;
        }
        File file = new File(cacheDir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获得头像的存储路径
     *
     * @param account
     * @return
     */
    public static File getPersonPortrait(String account) {
        File file = null;
        String portrait = Environment.getExternalStorageDirectory().getPath() + File.separator + MainDir
                + File.separator + account + File.separator + PersonPortrait;
        file = new File(portrait);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获取日志缓存的路径，包含文件名
     * .../Nooie/log/
     *
     * @return
     */
    public static String getLogFolder() {
        String thumbDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            thumbDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + LogDir;
        } else {
            thumbDir = NooieApplication.mCtx.getFilesDir().getAbsolutePath() + File.separator + MainDir
                    + File.separator + LogDir;
        }
        File file = new File(thumbDir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * 获取所有log文件
     *
     * @return
     * @throws Exception
     */
    public static List<File> getLogFiles() {
        List<File> logFiles = new ArrayList<>();
        try {
            File file = new File(getLogFolder());
            File[] fileList = file.listFiles();
            if (fileList != null && fileList.length > 0) {
                for (int i = 0; i < fileList.length; i++) {
                    // 如果下面还有文件
                    if (fileList[i] != null && fileList[i].isFile()) {
                        logFiles.add(fileList[i]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logFiles;
    }

    private static final String DOT_NOOIE = ".Victure";

    public static void createNooieConfigFile(String uuidWithLine) {
        String rootPath = getLocalRootSavePathDir("");
        String configFilePath = rootPath + DOT_NOOIE;
        File rootDir = new File(rootPath);
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        File configFile = new File(configFilePath);
        NooieLog.d("-->> FileUtils createNooieConfigFile congifFilePath=" + configFilePath);
        String content = "device-id:" + (TextUtils.isEmpty(uuidWithLine) ? PhoneUtil.getUUIDWithLine() : uuidWithLine);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(configFile);
            byte[] outputBytes = content.getBytes();
            fileOutputStream.write(outputBytes);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNooieCongfigContent(String uuidWithLine) {
        String configFilePath = getLocalRootSavePathDir("") + DOT_NOOIE;
        File configFile = new File(configFilePath);

        String content = "";
        try {
//            if (!configFile.exists()) {
//                createNooieConfigFile(uuidWithLine);
//            }
            createNooieConfigFile(uuidWithLine);

            configFile = new File(configFilePath);
            FileInputStream fileInputStream = new FileInputStream(configFile);
            byte[] inputBytes = new byte[(int) configFile.length()];
            int fileLength = fileInputStream.read(inputBytes);
            content = new String(inputBytes);
            //NooieLog.d("-->> FileUtils getNooieCongfigContent file length=" + fileLength + " content=" + content);
        } catch (Exception e) {
        }

        return content;
    }

    public static String getPhoneUuid(String uuidWithLine) {
        String phoneUuid = "";
        try {
            String configContent = getNooieCongfigContent(uuidWithLine);
            configContent = !TextUtils.isEmpty(configContent) ? configContent.replaceAll("-", "") : "";
            if (!TextUtils.isEmpty(configContent)) {
                String[] configMap = configContent.split(":");
                if (configMap != null && configMap.length == 2) {
                    String uuid = configMap[1];
                    if (!TextUtils.isEmpty(uuid)) {
                        phoneUuid = uuid;
                    }
                    NooieLog.d("-->> FileUtils getPhoneUuid uuid=" + uuid);
                }
            }
        } catch (Exception e) {
        }
        return phoneUuid;
    }

    /**
     * Bitmap 转换base64
     */
    public static String bitmapToBase64(Bitmap src) {
        String result = "";
        if (src == null)
            throw new IllegalArgumentException("argument bitmap is null!!! ");

        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            src.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            baos.flush();
            baos.close();
            result = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            return result;
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            try {
                baos.close();
                baos = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;
    }

    public static boolean isPortraitDirExist(String account) {
        File file = null;
        String portrait = Environment.getExternalStorageDirectory().getPath() + File.separator + MainDir
                + File.separator + account + File.separator + PersonPortrait;
        file = new File(portrait);
        return file.exists() && file.isDirectory();
    }

    /**
     * 获得以用户名命名的头像的存储路径
     *
     * @param userName
     * @return
     */
    public static String getAccountNamePortrait(String userName) {
        String name = TextUtils.isEmpty(userName) ? "" : userName;
        File portraitDir = getPersonPortrait(name);
        String namePP = name + ".jpg";
        StringBuffer buffer = new StringBuffer();
        buffer.append(portraitDir.getAbsolutePath()).append('/').append(namePP);
        return buffer.toString();
    }

    public static String getTmpAccountNamePortrait(String userName) {
        String name = TextUtils.isEmpty(userName) ? "" : userName;
        File portraitDir = getPersonPortrait(name);
        String namePP = "tmp_" + name + ".jpg";
        StringBuffer buffer = new StringBuffer();
        buffer.append(portraitDir.getAbsolutePath()).append('/').append(namePP);
        return buffer.toString();
    }

    public static String getPortraitPhotoPath(String username, String useid) {
        File portraitDir = getPersonPortrait(username);
        StringBuffer buffer = new StringBuffer();
        buffer.append(portraitDir.getAbsolutePath()).append('/').append(getPortraitPhotoName(useid));
        return buffer.toString();
    }

    public static String getPortraitPhotoName(String useid) {
        StringBuilder sb = new StringBuilder();
        sb.append(useid);
        sb.append(".jpg");
        return sb.toString();
    }

    /**
     * 保存图片到指定路径(bitmap)
     *
     * @param bitmap
     * @param path
     */
    public static void savePic(Bitmap bitmap, String path) {
        try {
            FileOutputStream out = null;
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件
     *
     * @param filePath
     * @return
     */
    public static boolean deleteFile(String filePath) {
        return deleteFile(new File(filePath));
    }

    /**
     * 删除文件
     *
     * @param file
     * @return
     */
    public static boolean deleteFile(File file) {
        try {
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean copyFile(String srcFilePath, String targetPath) {
        try {
            int byteread = 0;
            File targetFile = new File(targetPath);
            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }

            File srcFile = new File(srcFilePath);
            if (srcFile.exists()) {
                InputStream inStream = new FileInputStream(srcFilePath);
                String targetFilePath = targetPath + File.separator + srcFile.getName();
                FileOutputStream fs = new FileOutputStream(targetFilePath);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.flush();
                fs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void moveFile(String srcFilePath, String targetPath) {
        File srcFile = new File(srcFilePath);
        if (srcFile.exists()) {
            if (copyFile(srcFilePath, targetPath)) {
                deleteFile(srcFilePath);
            }
        }
    }

    /**
     * 根据folder 和Device获取file
     *
     * @param file
     * @param device
     * @return
     */
    public static String getSavedScreenShotPath(File file, BindDevice device) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String devName = device.getName();
        String channel = String.format("ch%1$02d", device.getUuid());
        String time = format.format(new Date());
        String fileName = devName + '_' + channel + '_' + time + ".png";
        final String path = file.getAbsolutePath() + File.separatorChar + fileName;
        try {
            new File(path).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    /**
     * nooie smart get snapshot path
     *
     * @param deviceId
     * @param suffix
     * @return
     */
    public static String getNooieSavedScreenShotPath(String deviceId, String suffix) {
        File saveSnapshotDir = getSnapshotDir(GlobalData.getInstance().getAccount());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String time = format.format(new Date());
        String fileName = deviceId + '_' + time + "." + suffix;
        final String path = saveSnapshotDir.getAbsolutePath() + File.separatorChar + fileName;
        try {
            new File(path).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public static String getNooieSavedRecordPath(String deviceId, String suffix) {
        File saveRecordDir = getRecordDir(GlobalData.getInstance().getAccount());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String time = format.format(new Date());
        String fileName = deviceId + '_' + time + "." + suffix;
        final String path = saveRecordDir.getAbsolutePath() + File.separatorChar + fileName;
        try {
            new File(path).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    /**
     * 获取视频Thumbnail
     *
     * @param file
     * @param device
     * @return
     */
    public static String getVideoThumbPath(File file, BindDevice device) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String devId = device.getUuid();
        String channel = String.format("ch%1$02d", device.getUuid());
        String fileName = devId + '_' + channel + ".png";
        final String path = file.getAbsolutePath() + File.separatorChar + fileName;
        try {
            new File(path).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    /**
     * 获得录像文件存储路径
     *
     * @param account
     * @param deviceAlias
     * @param channelNum
     * @return
     */
    public static String getRecordFilePath(String account, String deviceAlias, int channelNum, String suffix) {
        File file = FileUtils.getRecordDir(account);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String channel = String.format("ch%1$02d", channelNum);
        String time = format.format(new Date());

        String fileName = deviceAlias + '_' + channel + '_' + time + suffix;
        return (file.getAbsolutePath() + File.separatorChar + fileName);
    }

    /**
     * 获得录像文件存储路径
     *
     * @param account
     * @param deviceAlias
     * @param channelNum
     * @return
     */
    public static String getRecordFilePathPortraitBell(String account, String deviceAlias, int channelNum, String suffix) {
        File file = FileUtils.getRecordDir(account);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String channel = String.format("ch%1$02d", channelNum);
        String time = format.format(new Date());

        String fileName = deviceAlias + '_' + channel + '_' + time + '_' + "bell" + suffix;
        return (file.getAbsolutePath() + File.separatorChar + fileName);
    }

    /**
     * 获得录像文件存储路径(鱼眼)
     *
     * @param account
     * @param deviceAlias
     * @param channelNum
     * @return
     */
    public static String getRecordFilePathPortraitFisher(String account, String deviceAlias, int channelNum, String configue, String suffix) {
        File file = FileUtils.getRecordDir(account);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String channel = String.format("ch%1$02d", channelNum);
        String time = format.format(new Date());

        String fileName = deviceAlias + '_' + channel + '_' + time + '_' + "fish_" + configue + suffix;
        return (file.getAbsolutePath() + File.separatorChar + fileName);
    }

    /**
     * 获得对应录像文件缩略图的存储路径
     *
     * @param account
     * @param recordFilePath
     * @return
     */
    public static String getRecordFileThumbPath(String account, String recordFilePath, String suffix) {
        File recordFile = FileUtils.getRecordDir(account);
        File recordThumbFile = FileUtils.getRecordThumbsDir(account);

        String recordThumbPath = recordFilePath.replace(recordFile.getAbsolutePath(), recordThumbFile.getAbsolutePath())
                .replace(suffix, ConstantValue.Suffix.PNG);

        return recordThumbPath;
    }

    /**
     * 获取私人根目录
     * @param dir
     * @return
     */
    public static String getPrivateLocalRootSavePathDir(String dir) {
        String path = NooieApplication.mCtx.getExternalFilesDir("").getAbsolutePath() + File.separator + MainDir + File.separator + dir;
        NooieLog.d("-->> FileUtils getPrivateLocalRootSavePathDir filedir=" + NooieApplication.mCtx.getFilesDir() + " externalfiledir=" + NooieApplication.mCtx.getExternalFilesDir(null));
        return path;
    }

    public static File getPersonPortraitInPrivate(String account) {
        StringBuilder portraitSubPathSb = new StringBuilder();
        portraitSubPathSb.append(account);
        portraitSubPathSb.append(File.separator);
        portraitSubPathSb.append(PersonPortrait);
        File file = null;
        String portrait = getPrivateLocalRootSavePathDir(portraitSubPathSb.toString());
        file = new File(portrait);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    public static String getPortraitPhotoPathInPrivate(String account, String useid) {
        File portraitDir = getPersonPortraitInPrivate(account);
        StringBuffer buffer = new StringBuffer();
        buffer.append(portraitDir.getAbsolutePath()).append(File.separator).append(getPortraitPhotoName(useid));
        return buffer.toString();
    }

    /**
     * Victure/account/Thumbnail
     * @param account
     * @return
     */
    public static File getDetectionThumbnailRootPathInPrivate(String account) {
        StringBuilder subPathSb = new StringBuilder();
        subPathSb.append(account);
        subPathSb.append(File.separator);
        subPathSb.append(DetectionThumbnail);
        File file = null;
        String portrait = getPrivateLocalRootSavePathDir(subPathSb.toString());
        file = new File(portrait);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * Victure/account/Thumbnail/deviceId/startTime.jpeg
     * @param account
     * @param deviceId
     * @return
     */
    public static File getDetectionThumbnailPathInPrivate(String account, String deviceId) {
        StringBuilder subPathSb = new StringBuilder();
        subPathSb.append(account);
        subPathSb.append(File.separator);
        subPathSb.append(DetectionThumbnail);
        subPathSb.append(File.separator);
        subPathSb.append(deviceId);
        File file = null;
        String portrait = getPrivateLocalRootSavePathDir(subPathSb.toString());
        file = new File(portrait);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    public static String getDetectionThumbnailFilename(long startTime) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(startTime).append(CConstant.PERIOD).append(CConstant.MEDIA_TYPE_JPEG);
        return buffer.toString();
    }

    public static String getDetectionThumbnailFilePath(String account, String deviceId, long startTime) {
        StringBuilder subPathSb = new StringBuilder();
        subPathSb.append(getDetectionThumbnailPathInPrivate(account, deviceId));
        subPathSb.append(File.separator);
        subPathSb.append(getDetectionThumbnailFilename(startTime));
        return subPathSb.toString();
    }

    public static String getFilePathByUri(Context context, Uri uri) {
        String path = null;
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
            return path;
        }
        // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        return path;
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));
                    path = getDataColumn(context, contentUri, null, null);
                    return path;
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                    return path;
                }
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isSdCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static File getDiskCacheDir(Context context, String name) {
        if (isSdCardAvailable()) {
            // /SDCard/Android/data/包名/cache/，当应用被卸载后，此目录会被删除
            File cacheFile = context.getExternalCacheDir();
            if (cacheFile != null) {
                return new File(cacheFile, name);
            }
        }
        // /data/data/包名/cache/，当应用被卸载后，此目录会被删除
        return new File(context.getCacheDir(), name);
    }

    /**
     * 获取缓存大小
     *
     * @param context
     * @return
     * @throws Exception
     */
    public static String getTotalCacheSize(Context context) throws Exception {
        long cacheSize = getFolderSize(context.getCacheDir());
        if (isSdCardAvailable()) {
            cacheSize += getFolderSize(context.getExternalCacheDir());
            cacheSize += getFolderSize(new File(getPreviewThubFolder()));
            cacheSize += getFolderSize(new File(getLogFolder()));
        }
        return getFormatSize(cacheSize);
    }

    /**
     * 清除缓存
     * 必須在子綫程執行
     *
     * @param context
     */
    public static void clearAllCache(Context context) {
        try {
            // FUCK: Must put it in subThread thread
            Glide.get(NooieApplication.mCtx).clearDiskCache();

            deleteDir(context.getCacheDir());
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                deleteDir(context.getExternalCacheDir());
                deleteDir(new File(getPreviewThubFolder()));
                deleteDir(new File(getLogFolder()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * FUCK: Must put it in Main thread
     */
    public static void clearGlideMemoryCache() {
        Glide.get(NooieApplication.mCtx).clearMemory();
    }

    public static boolean delelteFolder(String path) {
        try {
            File deleteFile = new File(path);
            if (deleteFile.exists() && deleteFile.isDirectory()) {
                return deleteDir(deleteFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    // 获取文件大小
    //Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
    //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 格式化单位
     *
     * @param size-> Byte
     * @return
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return "0K";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "K";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "M";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }
}
