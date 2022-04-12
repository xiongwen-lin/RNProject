package com.afar.osaio.smart.device.model;

import com.afar.osaio.base.mvp.IBaseModel;

import java.io.File;

import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;

/**
 * ICloudModel
 *
 * @author Administrator
 * @date 2019/4/25
 */
public interface ICloudModel extends IBaseModel {

    Observable<String> getFeedbackPresignUrl(String userId, String username, String filename, int fileSize);

    Observable<String> getPhotoUploadPreSignUrl(String userId, String username, String filename, int fileSize);

    Observable<String> getPhotoDownloadPreSignUrl(String userId, String filename);

    void upLoadFileToCloud(String url, String contentType, String usenameParam, File uploadFile, Callback callback);

    Response upLoadFileToCloud(String url, String contentType, String usenameParam, File uploadFile) throws Exception;
}
