package com.afar.osaio.bean;

import com.nooie.sdk.api.network.base.bean.entity.FeedbackProduct;
import com.nooie.sdk.api.network.base.bean.entity.FeedbackType;

import java.util.List;

/**
 * NooieFeedbackOption
 *
 * @author Administrator
 * @date 2019/4/24
 */
public class NooieFeedbackOption {

    private List<FeedbackType> feedbackTypes;
    private List<FeedbackProduct> feedbackProducts;

    public List<FeedbackType> getFeedbackTypes() {
        return feedbackTypes;
    }

    public void setFeedbackTypes(List<FeedbackType> feedbackTypes) {
        this.feedbackTypes = feedbackTypes;
    }

    public List<FeedbackProduct> getFeedbackProducts() {
        return feedbackProducts;
    }

    public void setFeedbackProducts(List<FeedbackProduct> feedbackProducts) {
        this.feedbackProducts = feedbackProducts;
    }
}
