package com.afar.osaio.smart.setting.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.afar.osaio.R;
import com.nooie.common.bean.CountryViewBean;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.widget.contact.ContactItemInterface;
import com.nooie.common.widget.contact.ContactListAdapter;

import java.util.List;

/**
 * CountryAdpater
 *
 * @author Administrator
 * @date 2019/4/2
 */
public class CountryAdpater extends ContactListAdapter {
    private static final String TAG = "CountryAdpater";

    public CountryAdpater(Context _context, int _resource, List<ContactItemInterface> _items) {
        super(_context, _resource, _items);
    }

    /**
     * override this for custom drawing
     * @param parentView
     * @param item
     * @param position
     */
    @Override
    public void populateDataForRow(View parentView, ContactItemInterface item, int position) {
        // default just draw the item only
        TextView fullNameView = (TextView) parentView.findViewById(R.id.nameView);
        fullNameView.setTextColor(getThemeColorResInt(getThemeColorRes()));

        if (item instanceof CountryViewBean) {
            CountryViewBean contactItem = (CountryViewBean) item;
            fullNameView.setText(contactItem.getCountryName());
            NooieLog.d("-->> CountryAdpater populateDataForRow countryItem" + fullNameView);
        }
    }
}
