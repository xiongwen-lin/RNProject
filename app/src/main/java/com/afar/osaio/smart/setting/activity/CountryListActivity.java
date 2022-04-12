package com.afar.osaio.smart.setting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.setting.adapter.CountryAdpater;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.bean.CountryViewBean;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.widget.contact.ContactItemInterface;
import com.nooie.common.widget.contact.CountryListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CountryListActivity extends BaseActivity implements TextWatcher, View.OnFocusChangeListener {

    private final static String TAG = "ContactListActivity";

    public final static String PHONE_CODE = "PHONE_CODE";
    public final static String COUNTRY_NAME = "COUNTRY_NAME";

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.lvCountrySearchCountry)
    CountryListView lvCountrySearchCountry;
    @BindView(R.id.countrySearchInputQuery)
    EditText searchBox;
    private String searchString;
    private Object searchLock = new Object();
    boolean inSearchMode = false;
    public static final String COUNTRYNUM = "country_num";

    List<ContactItemInterface> contactList;
    List<ContactItemInterface> filterList;
    private SearchListTask curSearchTask = null;

    public static void toCountryListActivity(Activity from, int requestCode) {
        Intent intent = new Intent(from, CountryListActivity.class);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_list);
        ButterKnife.bind(this);
        initView();
        searchBox.addTextChangedListener(this);
        searchBox.setOnFocusChangeListener(this);
        setupCountryListView();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
        tvTitle.setText(R.string.country_list_title);
    }

    private void setupCountryListView() {
        lvCountrySearchCountry.setThemeColorRes(R.color.theme_text_color);
        filterList = new ArrayList<ContactItemInterface>();
        contactList = CountryUtil.getSampleContactList(NooieApplication.mCtx);
        CountryAdpater adapter = new CountryAdpater(this, R.layout.country_list_item, CollectionUtil.safeFor(contactList));
        adapter.setThemeColorRes(R.color.theme_text_color);
        lvCountrySearchCountry.setFastScrollEnabled(true);
        lvCountrySearchCountry.setAdapter(adapter);

        // use this to process individual clicks
        // cannot use OnClickListener as the touch event is overrided by IndexScroller
        // use last touch X and Y if want to handle click for an individual item within the row
        lvCountrySearchCountry.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position,
                                    long id) {
                List<ContactItemInterface> searchList = inSearchMode ? filterList : contactList;
                CountryViewBean countryViewBean = searchList != null && position < searchList.size() ? (CountryViewBean) searchList.get(position) : null;
                if (countryViewBean != null) {
                    Intent intent = new Intent();
                    intent.putExtra(ConstantValue.INTENT_KEY_PHONE_CODE, countryViewBean.getNumber());
                    intent.putExtra(ConstantValue.INTENT_KEY_COUNTRY_NAME, countryViewBean.getCountryName());
                    intent.putExtra(ConstantValue.INTENT_KEY_COUNTRY_KEY, countryViewBean.getKey());
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish();
            }
        });
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (isDestroyed() || checkNull(searchBox)) {
            return;
        }
        searchString = searchBox.getText().toString().trim().toUpperCase();
        if (curSearchTask != null && curSearchTask.getStatus() != AsyncTask.Status.FINISHED) {
            try {
                curSearchTask.cancel(true);
            } catch (Exception e) {
                Log.i(TAG, "Fail to cancel running search task");
            }

        }
        curSearchTask = new SearchListTask();
        // putCurrentHome it in a task so that ui is not freeze
        curSearchTask.execute(searchString);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (isDestroyed()) {
            return;
        }
        NooieLog.d("-->> debug CountryListActivity onFocusChange: hasFocus=" + hasFocus);
        inSearchMode = hasFocus;
        if (!hasFocus) {
            hideInputMethod();
        }
        refreshListView(inSearchMode);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // do nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // do nothing
    }

    @Override
    public int getStatusBarMode() {
        return ConstantValue.STATUS_BAR_LIGHT_BLUE_MODE;
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                if (searchBox != null && searchBox.isFocused()) {
                    searchBox.clearFocus();
                    searchBox.setText("");
                    return;
                }
                finish();
                break;
            }
        }
    }

    private void refreshListView(boolean inSearchMode) {
        if (isDestroyed() || checkNull(lvCountrySearchCountry)) {
            return;
        }
        if (inSearchMode) {
            CountryAdpater adapter = new CountryAdpater(CountryListActivity.this, R.layout.country_list_item, CollectionUtil.safeFor(filterList));
            adapter.setThemeColorRes(R.color.theme_text_color);
            adapter.setInSearchMode(true);
            lvCountrySearchCountry.setInSearchMode(true);
            lvCountrySearchCountry.setAdapter(adapter);
        } else {
            CountryAdpater adapter = new CountryAdpater(CountryListActivity.this, R.layout.country_list_item, CollectionUtil.safeFor(contactList));
            adapter.setThemeColorRes(R.color.theme_text_color);
            adapter.setInSearchMode(false);
            lvCountrySearchCountry.setInSearchMode(false);
            lvCountrySearchCountry.setAdapter(adapter);
        }
    }

    private class SearchListTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (filterList != null) {
                filterList.clear();
            }
            String keyword = params[0];
            if (inSearchMode && !TextUtils.isEmpty(keyword) && CollectionUtil.isNotEmpty(contactList)) {
                // get all the mItems matching this
                for (ContactItemInterface item : contactList) {
                    CountryViewBean contact = (CountryViewBean) item;
                    //NooieLog.d("-->> CountryListActivity iterate country name=" + contact.getCountryName());
                    if (contact != null && !TextUtils.isEmpty(contact.getCountryName()) && !TextUtils.isEmpty(contact.getPinyin())) {
                        if ((contact.getCountryName().toUpperCase().indexOf(keyword) > -1) || (contact.isChinese() && contact.getPinyin().toUpperCase().indexOf(keyword) > -1)) {
                            filterList.add(item);
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            synchronized (searchLock) {
                refreshListView(inSearchMode);
            }
        }

    }
}
