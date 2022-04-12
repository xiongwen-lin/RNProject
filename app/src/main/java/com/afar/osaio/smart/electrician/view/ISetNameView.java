package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;

public interface ISetNameView extends IBaseView {

    void notifySetMemberNameResult(String result);

    void notifySetSelfNameResult(String result);

}
