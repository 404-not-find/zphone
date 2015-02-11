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
// Adapted from 
// http://jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/

package com.zycoo.android.zphone.ui.contacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.twotoasters.sectioncursoradapter.SectionCursorAdapter;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class SeparatedListAdapter extends BaseAdapter {
    private Context mContext;
    public final Map<String, Adapter> mSections = new LinkedHashMap<String, Adapter>();
    private final LayoutInflater mLayoutInflater;


    public SeparatedListAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addSection(String section, Adapter adapter) {
        synchronized (mSections) {
            mSections.put(section, adapter);
        }
    }

    public void clearSections() {
        synchronized (mSections) {
            mSections.clear();
        }
    }

    @Override
    public Object getItem(int position) {
        synchronized (mSections) {
            for (String section : this.mSections.keySet()) {
                final Adapter adapter = mSections.get(section);
                final int size = adapter.getCount() + 1;

                if (position == 0) {
                    return section;
                }
                if (position < size) {
                    return adapter.getItem(position - 1);
                }
                position -= size;
            }
            return null;
        }
    }

    public int sectionPosition(int position) {
        synchronized (mSections) {
            int sectionPosition = 0;
            for (String section : this.mSections.keySet()) {

                final Adapter adapter = mSections.get(section);
                final int size = adapter.getCount() + 1;
                position -= size;
                sectionPosition++;
                if (position < 0) {
                    break;
                }
            }
            return sectionPosition;
        }
    }

    public int getSectionsCount() {
        return mSections.size();
    }

    @Override
    public int getCount() {
        synchronized (mSections) {
            int total = 0;
            for (Adapter adapter : mSections.values()) {
                total += adapter.getCount() + 1;
            }
            return total;
        }
    }

    @Override
    public int getViewTypeCount() {
        synchronized (mSections) {
            int total = 1;
            for (Adapter adapter : mSections.values()) {
                total += adapter.getViewTypeCount();
            }
            return total;
        }
    }

    @Override
    public int getItemViewType(int position) {
        synchronized (mSections) {
            for (String section : mSections.keySet()) {
                final Adapter adapter = mSections.get(section);
                final int size = adapter.getCount() + 1;
                if (position == 0) {
                    return Item.SECTION;
                }
                if (position < size) {
                    return Item.ITEM;
                }
                position -= size;
            }
            return Item.ITEM;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return (getItemViewType(position) != Item.SECTION);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        synchronized (mSections) {
            SectionViewHolder sectionViewHolder;
            int sectionNum = 0;
            for (String section : mSections.keySet()) {
                final Adapter adapter = mSections.get(section);
                final int size = adapter.getCount() + 1;
                if (position == 0) {
                    if (null == convertView) {
                        convertView = mLayoutInflater.inflate(
                                R.layout.fragment_contact_item_section, parent, false);
                        sectionViewHolder = new SectionViewHolder(convertView);
                    } else {
                        sectionViewHolder = (SectionViewHolder) convertView.getTag();
                    }
                    sectionViewHolder.mTextView.setText(section);
                    return convertView;

                }
                if (position < size) {
                    return adapter.getView(position - 1, convertView, parent);
                }
                // otherwise jump into next section
                position -= size;
                sectionNum++;
            }
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class SectionViewHolder {
        TextView mTextView;

        public SectionViewHolder(View v) {
            v.setTag(this);
            mTextView = (TextView) v.findViewById(android.R.id.text1);
        }
    }

    public static class Item {
        public static final int ITEM = 0;
        public static final int SECTION = 1;
        public final int type;
        public final String text;
        public int sectionPosition;
        public int listPosition;

        public Item(int type, String text) {
            this.type = type;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
