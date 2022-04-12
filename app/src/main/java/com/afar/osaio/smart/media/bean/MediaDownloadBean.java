package com.afar.osaio.smart.media.bean;

public class MediaDownloadBean {

    private String downloadUrl;
    private String storagePath;
    private String storageFileName;
    private String thumbnailFileName;
    private NEMediaType mediaType;
    private String key;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getStorageFileName() {
        return storageFileName;
    }

    public void setStorageFileName(String storageFileName) {
        this.storageFileName = storageFileName;
    }

    public String getThumbnailFileName() {
        return thumbnailFileName;
    }

    public void setThumbnailFileName(String thumbnailFileName) {
        this.thumbnailFileName = thumbnailFileName;
    }

    public NEMediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(NEMediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
