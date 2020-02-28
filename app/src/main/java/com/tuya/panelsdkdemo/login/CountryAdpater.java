package com.tuya.panelsdkdemo.login;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.tuya.panelsdkdemo.R;
import com.tuya.panelsdkdemo.base.widget.contact.ContactItemInterface;
import com.tuya.panelsdkdemo.base.widget.contact.ContactListAdapter;
import com.tuya.smart.android.common.utils.L;

import java.util.List;


public class CountryAdpater extends ContactListAdapter {
    private static final String TAG = "CountryAdpater";

    public CountryAdpater(Context _context, int _resource,
                          List<ContactItemInterface> _items) {
        super(_context, _resource, _items);
    }

    // override this for custom drawing
    @Override
    public void populateDataForRow(View parentView, ContactItemInterface item, int position) {
        // default just draw the item only
        TextView fullNameView = (TextView) parentView.findViewById(R.id.nameView);

        if (item instanceof CountryViewBean) {
            CountryViewBean contactItem = (CountryViewBean) item;
            fullNameView.setText(contactItem.getCountryName());
            L.d(TAG, "countryItem" + fullNameView);
        }

    }

}
