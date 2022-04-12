package com.afar.osaio.application.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.application.adapter.FAQAdapter;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.FAQBean;
import com.afar.osaio.bean.FAQMap;
import com.nooie.common.utils.configure.LanguageUtil;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.json.GsonHelper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * FAQActivity
 *
 * @author Administrator
 * @date 2019/9/9
 */
public class FAQActivity extends BaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.rcvFAQ)
    RecyclerView rcvFAQ;

    private FAQAdapter mAdapter;
    private List<FAQBean> mRootFAQs = new ArrayList<>();
    private boolean mIsRoot = true;

    public static void toFAQActivity(Context from) {
        Intent intent = new Intent(from, FAQActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        ButterKnife.bind(this);
        initFAQData();
        initView();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setVisibility(View.GONE);
        tvTitle.setText(R.string.help_faq);
    }

    @OnClick({R.id.ivLeft})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                if (mIsRoot) {
                    finish();
                } else {
                    mIsRoot = true;
                    tvTitle.setText(R.string.help_faq);
                    resetFAQList(mRootFAQs);
                }
                break;
        }
    }

    private void initFAQData() {
        Map<String,FAQBean> demo = new HashMap<>();
        demo.put("en", new FAQBean());
        //NooieLog.d("-->> FAQActivity initFAQData=" + GsonHelper.convertToJson(demo));
        Observable.just(getLanKey())
                .flatMap(new Func1<String, Observable<List<FAQBean>>>() {
                    @Override
                    public Observable<List<FAQBean>> call(String lan) {
                        String result = getFAQFromAssert();
                        //NooieLog.d("-->> FAQActivity initFAQData result=" + result);
                        FAQMap faqMap = GsonHelper.convertJson(result, FAQMap.class);
                        if (faqMap != null && faqMap.getData() != null && faqMap.getData().containsKey(lan)) {
                            return Observable.just(faqMap.getData().get(lan));
                        }
                        return Observable.just(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<FAQBean>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<FAQBean> faqBeans) {
                        setupFAQListView(faqBeans);
                    }
                });
    }

    private void setupFAQListView(List<FAQBean> faqBeans) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvFAQ.setLayoutManager(layoutManager);
        mAdapter = new FAQAdapter();
        mAdapter.setListener(new FAQAdapter.OnFAQItemClickListener() {
            @Override
            public void onItemClick(int position, FAQBean faqBean) {
                if (faqBean != null && CollectionUtil.isNotEmpty(faqBean.getChildren())) {
                    mIsRoot = false;
                    tvTitle.setText(faqBean.getTitle());
                    resetFAQList(faqBean.getChildren());
                }
            }
        });
        rcvFAQ.setAdapter(mAdapter);
        mIsRoot = true;
        if (CollectionUtil.isNotEmpty(faqBeans)) {
            mRootFAQs.addAll(faqBeans);
        }
        resetFAQList(faqBeans);
    }

    private void resetFAQList(List<FAQBean> faqBeans) {
        if (CollectionUtil.isNotEmpty(faqBeans)) {
            mAdapter.setData(faqBeans);
        }
    }

    private String getFAQFromAssert() {
        String result = "";
        try {
            InputStream inputStream = getResources().getAssets().open("html/faq.json");
            int length = inputStream.available();
            byte[] buffer = new byte[length];
            inputStream.read(buffer);
            result = new String(buffer);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getLanKey() {
        Locale locale = LanguageUtil.getLocal(NooieApplication.get());
        String language = locale.getLanguage();
        String key;
        if (language.contains("zh")) {
            key = "zh";
        } else if (language.contains("ru")) {
            key = "ru";
        } else if (language.contains("pl")) {
            key = "pl";
        } else if (language.contains("it")) {
            key = "it";
        } else if (language.contains("fr")) {
            key = "fr";
        } else if (language.contains("es")) {
            key = "es";
        } else if (language.contains("de")) {
            key = "de";
        } else if (language.contains("ja")) {
            key = "ja";
        } else {
            key = "en";
        }
        return key;
    }
}
