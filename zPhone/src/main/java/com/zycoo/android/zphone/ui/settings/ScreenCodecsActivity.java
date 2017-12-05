/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.zycoo.android.zphone.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ui.me.ListViewItem;
import com.zycoo.android.zphone.ui.me.ListViewItemSwitchIconWithTextCodecs;
import com.zycoo.android.zphone.ui.me.ListViewItemSwitchIconWithTextViewHolder;
import com.zycoo.android.zphone.ui.me.ListViewItemGrey;
import com.zycoo.android.zphone.utils.Utils;

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.SipStack;
import org.doubango.tinyWRAP.tdav_codec_id_t;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ScreenCodecsActivity extends BaseScreen implements OnItemClickListener {
    private ListViewItem[] items;
    private ListView mListView;
    private BaseAdapter adapter;
    private int mCodecs;
    private final INgnConfigurationService mConfigurationService = Engine.getInstance().getConfigurationService();
    private Logger mLogger = LoggerFactory.getLogger(ScreenCodecsActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCodecs = mConfigurationService.getInt(NgnConfigurationEntry.MEDIA_CODECS, NgnConfigurationEntry.DEFAULT_MEDIA_CODECS);
        setContentView(R.layout.settings_listview);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mListView = (ListView) findViewById(android.R.id.list);
        initData();
        adapter = new ScreenSettingsAdapter(this, items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);

    }
    public void initData() {
        List<ListViewItem> arrayItems = new ArrayList<ListViewItem>();
        arrayItems.add(new ListViewItemGrey(20));
        arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.pcma, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_pcma), tdav_codec_id_t.tdav_codec_id_pcma.swigValue()));
        arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.pcmu, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_pcmu), tdav_codec_id_t.tdav_codec_id_pcmu.swigValue()));
        if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_gsm)) {
            arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.gsm, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_gsm), tdav_codec_id_t.tdav_codec_id_gsm.swigValue()));
        }

        if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_g722)) {
            arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.g_722, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_g722), tdav_codec_id_t.tdav_codec_id_g722.swigValue()));
        }

        if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_g729ab)) {
            arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.g_729, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_g729ab), tdav_codec_id_t.tdav_codec_id_g729ab.swigValue()));
        }

        if (Utils.isPro(this)) {
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_amr_nb_oa)) {
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.amr_nb_oa, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_amr_nb_oa), tdav_codec_id_t.tdav_codec_id_amr_nb_oa.swigValue()));
            }

            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_amr_nb_be)) {
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.amr_nb_be, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_amr_nb_be), tdav_codec_id_t.tdav_codec_id_amr_nb_be.swigValue()));
            }
        }


        if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_speex_nb)) {
            arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.speex_nb, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_speex_nb), tdav_codec_id_t.tdav_codec_id_speex_nb.swigValue()));
        }
        if (Utils.isPro(this)) {
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_speex_wb)) {
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.speex_wb, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_speex_wb), tdav_codec_id_t.tdav_codec_id_speex_wb.swigValue()));
            }
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_speex_uwb)) {
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.speex_uwb, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_speex_uwb), tdav_codec_id_t.tdav_codec_id_speex_uwb.swigValue()));
            }
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_ilbc))
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.i_lbc, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_ilbc), tdav_codec_id_t.tdav_codec_id_ilbc.swigValue()));
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_opus))
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.opus, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_opus), tdav_codec_id_t.tdav_codec_id_opus.swigValue()));


            arrayItems.add(new ListViewItemGrey(40));
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_vp8)) {
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.vp8, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_vp8), tdav_codec_id_t.tdav_codec_id_vp8.swigValue()));
            }
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_mp4ves_es)) {
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.mp4v_es, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_mp4ves_es), tdav_codec_id_t.tdav_codec_id_mp4ves_es.swigValue()));
            }
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_theora)) {
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.theora, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_theora), tdav_codec_id_t.tdav_codec_id_theora.swigValue()));
            }
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_h264_bp)) {
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.h264_bp, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_h264_bp), tdav_codec_id_t.tdav_codec_id_h264_bp.swigValue()));
            }
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_h264_mp)) {
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.h264_mp, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_h264_mp), tdav_codec_id_t.tdav_codec_id_h264_mp.swigValue()));
            }
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_h263)) {
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.h_263, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_h263), tdav_codec_id_t.tdav_codec_id_h263.swigValue()));
            }
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_h263p)) {
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.h_263_plus, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_h263p), tdav_codec_id_t.tdav_codec_id_h263p.swigValue()));
            }
            if (SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_h263pp)) {
                arrayItems.add(new ListViewItemSwitchIconWithTextCodecs(R.string.h_263_plus_plus, true, codeIsCheck(tdav_codec_id_t.tdav_codec_id_h263pp), tdav_codec_id_t.tdav_codec_id_h263pp.swigValue()));
            }
        }
        items = arrayItems.toArray(new ListViewItem[arrayItems.size()]);
    }

    public boolean codeIsCheck(tdav_codec_id_t code) {
        return (code.swigValue() & mCodecs) == code.swigValue();
    }

    protected void onPause() {
        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof ListViewItemSwitchIconWithTextCodecs) {
                ListViewItemSwitchIconWithTextCodecs item = (ListViewItemSwitchIconWithTextCodecs) items[i];
                if (item.isChecked()) {
                    mCodecs |= item.getCodecsId();
                } else {
                    mCodecs &= ~item.getCodecsId();
                }
            }
        }
        if (super.mComputeConfiguration) {
            mConfigurationService.putInt(NgnConfigurationEntry.MEDIA_CODECS, mCodecs);
            SipStack.setCodecs_2(mCodecs);
            // commit
            if (!mConfigurationService.commit()) {
                mLogger.debug("Failed to commit() configuration");
            }
            super.mComputeConfiguration = false;
        }
        super.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListViewItemSwitchIconWithTextViewHolder switchIconWithTextViewHolder = (ListViewItemSwitchIconWithTextViewHolder) view.getTag(ListViewItemSwitchIconWithTextViewHolder.switch_icon_with_text_id);
        if (null != switchIconWithTextViewHolder) {
            switchIconWithTextViewHolder.getSwitchButton().toggle();
            switchIconWithTextViewHolder.setChecked(!switchIconWithTextViewHolder.getSwitchButton().isChecked());

            final ListViewItemSwitchIconWithTextCodecs item = (ListViewItemSwitchIconWithTextCodecs) items[position];
            if (item != null) {
                if ((mCodecs & item.getCodecsId()) == item.getCodecsId()) {
                    mCodecs &= ~item.getCodecsId();
                } else {
                    mCodecs |= item.getCodecsId();
                }
                mComputeConfiguration = true;
            }

        }
    }
}
