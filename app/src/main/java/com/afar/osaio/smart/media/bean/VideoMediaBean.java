package com.afar.osaio.smart.media.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class VideoMediaBean extends BaseMediaBean implements Parcelable {

    private static final long MAX_GIF_SIZE = 1024 * 1024L;
    private static final long MAX_IMAGE_SIZE = 1024 * 1024L;

    private boolean mIsSelected;
    private String mThumbnailPath;
    private String mCompressPath;
    private int mHeight;
    private int mWidth;
    private VIDEO_TYPE mVideoType;
    private String mMimeType;

    public enum VIDEO_TYPE {
        MP4
    }

    @Override
    public TYPE getType() {
        return TYPE.VIDEO;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean selected) {
        mIsSelected = selected;
    }

    public String getmThumbnailPath() {
        return mThumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.mThumbnailPath = thumbnailPath;
    }

    public String getCompressPath() {
        return mCompressPath;
    }

    public void setCompressPath(String compressPath) {
        this.mCompressPath = compressPath;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    /**
     * get mime type displayed in database.
     *
     * @return "image/gif" or "image/jpeg".
     */
    public String getMimeType() {
        if (getVideoType() == VIDEO_TYPE.MP4) {
            return "video/mp4";
        }
        return "video/mp4";
    }

    public VIDEO_TYPE getVideoType() {
        return mVideoType;
    }

    private VIDEO_TYPE getImageTypeByMime(String mimeType) {
        if (!TextUtils.isEmpty(mimeType)) {
            if ("video/mp4".equals(mimeType)) {
                return VIDEO_TYPE.MP4;
            }
        }
        return VIDEO_TYPE.MP4;
    }

    public void setVideoType(VIDEO_TYPE videoType) {
        mVideoType = videoType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(this.mIsSelected ? (byte) 1 : (byte) 0);
        dest.writeString(this.mThumbnailPath);
        dest.writeString(this.mCompressPath);
        dest.writeInt(this.mHeight);
        dest.writeInt(this.mWidth);
        dest.writeInt(this.mVideoType == null ? -1 : this.mVideoType.ordinal());
        dest.writeString(this.mMimeType);
    }

    public VideoMediaBean() {
    }

    protected VideoMediaBean(Parcel in) {
        super(in);
        this.mIsSelected = in.readByte() != 0;
        this.mThumbnailPath = in.readString();
        this.mCompressPath = in.readString();
        this.mHeight = in.readInt();
        this.mWidth = in.readInt();
        int tmpMImageType = in.readInt();
        this.mVideoType = tmpMImageType == -1 ? null : VIDEO_TYPE.values()[tmpMImageType];
        this.mMimeType = in.readString();
    }

    public static final Creator<VideoMediaBean> CREATOR = new Creator<VideoMediaBean>() {
        @Override
        public VideoMediaBean createFromParcel(Parcel source) {
            return new VideoMediaBean(source);
        }

        @Override
        public VideoMediaBean[] newArray(int size) {
            return new VideoMediaBean[size];
        }
    };
}
