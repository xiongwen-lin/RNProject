package com.afar.osaio.smart.media.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class ImageMediaBean extends BaseMediaBean implements Parcelable {

    private static final long MAX_GIF_SIZE = 1024 * 1024L;
    private static final long MAX_IMAGE_SIZE = 1024 * 1024L;

    private boolean mIsSelected;
    private String mThumbnailPath;
    private String mCompressPath;
    private int mHeight;
    private int mWidth;
    private IMAGE_TYPE mImageType;
    private String mMimeType;

    public enum IMAGE_TYPE {
        PNG, JPG, GIF
    }

    @Override
    public TYPE getType() {
        return TYPE.IMAGE;
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
        if (getImageType() == IMAGE_TYPE.GIF) {
            return "image/gif";
        } else if (getImageType() == IMAGE_TYPE.JPG) {
            return "image/jpeg";
        }
        return "image/jpeg";
    }

    public IMAGE_TYPE getImageType() {
        return mImageType;
    }

    private IMAGE_TYPE getImageTypeByMime(String mimeType) {
        if (!TextUtils.isEmpty(mimeType)) {
            if ("image/gif".equals(mimeType)) {
                return IMAGE_TYPE.GIF;
            } else if ("image/png".equals(mimeType)) {
                return IMAGE_TYPE.PNG;
            } else {
                return IMAGE_TYPE.JPG;
            }
        }
        return IMAGE_TYPE.PNG;
    }

    public void setImageType(IMAGE_TYPE imageType) {
        mImageType = imageType;
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
        dest.writeInt(this.mImageType == null ? -1 : this.mImageType.ordinal());
        dest.writeString(this.mMimeType);
    }

    public ImageMediaBean() {
    }

    protected ImageMediaBean(Parcel in) {
        super(in);
        this.mIsSelected = in.readByte() != 0;
        this.mThumbnailPath = in.readString();
        this.mCompressPath = in.readString();
        this.mHeight = in.readInt();
        this.mWidth = in.readInt();
        int tmpMImageType = in.readInt();
        this.mImageType = tmpMImageType == -1 ? null : IMAGE_TYPE.values()[tmpMImageType];
        this.mMimeType = in.readString();
    }

    public static final Creator<ImageMediaBean> CREATOR = new Creator<ImageMediaBean>() {
        @Override
        public ImageMediaBean createFromParcel(Parcel source) {
            return new ImageMediaBean(source);
        }

        @Override
        public ImageMediaBean[] newArray(int size) {
            return new ImageMediaBean[size];
        }
    };
}
