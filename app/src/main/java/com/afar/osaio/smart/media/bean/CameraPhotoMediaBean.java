package com.afar.osaio.smart.media.bean;

import android.os.Parcel;
import android.text.TextUtils;

public class CameraPhotoMediaBean extends BaseCameraMediaBean {

    private String mThumbnailPath;
    private IMAGE_TYPE mImageType;
    private String mMimeType;

    public enum IMAGE_TYPE {
        PNG, JPG, GIF
    }

    @Override
    public TYPE getType() {
        return TYPE.IMAGE;
    }

    public String getThumbnailPath() {
        return mThumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.mThumbnailPath = thumbnailPath;
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
        dest.writeString(this.mThumbnailPath);
        dest.writeInt(this.mImageType == null ? -1 : this.mImageType.ordinal());
        dest.writeString(this.mMimeType);
    }

    public CameraPhotoMediaBean() {
        super();
    }

    protected CameraPhotoMediaBean(Parcel in) {
        super(in);
        this.mThumbnailPath = in.readString();
        int tmpMImageType = in.readInt();
        this.mImageType = tmpMImageType == -1 ? null : IMAGE_TYPE.values()[tmpMImageType];
        this.mMimeType = in.readString();
    }

    public static final Creator<CameraPhotoMediaBean> CREATOR = new Creator<CameraPhotoMediaBean>() {
        @Override
        public CameraPhotoMediaBean createFromParcel(Parcel source) {
            return new CameraPhotoMediaBean(source);
        }

        @Override
        public CameraPhotoMediaBean[] newArray(int size) {
            return new CameraPhotoMediaBean[size];
        }
    };
}
