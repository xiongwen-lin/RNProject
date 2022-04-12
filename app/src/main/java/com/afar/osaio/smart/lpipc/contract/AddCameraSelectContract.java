package com.afar.osaio.smart.lpipc.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.bean.SelectProduct;

import java.util.List;

public interface AddCameraSelectContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onLoadProductInfoResult(String result, List<SelectProduct> selectProducts);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void loadProductInfo();
    }
}
