/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.zycoo.android.zphone.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ZycooConfigurationEntry;

import java.util.HashMap;
import java.util.List;

/**
 * 对主题进行管理
 *
 * @author tqcenglish Oct 14, 2014 2:16:10 PM
 */
public class Theme {

    private static final String THIS_FILE = "Theme";
    private Context context;
    private Handler handler;
    private PackageManager pm;
    private Resources remoteRes = null;
    private PackageInfo pInfos = null;
    private Drawable oldBackground = null;

    public Theme(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public Theme(Context ctxt, String packageName) {
        pm = ctxt.getPackageManager();
        context = ctxt;
        ComponentName cn = ComponentName.unflattenFromString(packageName);

        try {
            pInfos = pm.getPackageInfo(cn.getPackageName(), 0);
            remoteRes = pm.getResourcesForApplication(cn.getPackageName());
        } catch (NameNotFoundException e) {
            Log.e(THIS_FILE, "Impossible to get resources from " + cn.toShortString());
            remoteRes = null;
            pInfos = null;
        }
    }

    public static Theme getCurrentTheme(Context ctxt) {
        String themeName = "selected_theme";
        if (!TextUtils.isEmpty(themeName)) {
            return new Theme(ctxt, themeName);
        }
        return null;
    }

    public static HashMap<String, String> getAvailableThemes(Context ctxt) {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("", ctxt.getResources().getString(R.string.app_name));

        PackageManager packageManager = ctxt.getPackageManager();
        //Intent it = new Intent(SipManager.ACTION_GET_DRAWABLES);
        //TODO 添加接受主题查询处理
        Intent it = new Intent("");
        List<ResolveInfo> availables = packageManager.queryBroadcastReceivers(it, 0);
        Log.d(THIS_FILE, "We found " + availables.size() + "themes");
        for (ResolveInfo resInfo : availables) {
            Log.d(THIS_FILE, "We have -- " + resInfo);
            ActivityInfo actInfos = resInfo.activityInfo;
            ComponentName cmp = new ComponentName(actInfos.packageName, actInfos.name);
            String label = (String) actInfos.loadLabel(packageManager);
            if (TextUtils.isEmpty(label)) {
                label = (String) resInfo.loadLabel(packageManager);
            }
            result.put(cmp.flattenToString(), label);
        }

        return result;
    }

    public Drawable getDrawableResource(String name) {
        if (remoteRes != null && pInfos != null) {
            int id = remoteRes.getIdentifier(name, "drawable", pInfos.packageName);
            return pm.getDrawable(pInfos.packageName, id, pInfos.applicationInfo);
        } else {
            Log.d(THIS_FILE, "No results yet !! ");
        }
        return null;
    }

    public Integer getDimension(String name) {
        if (remoteRes != null && pInfos != null) {
            int id = remoteRes.getIdentifier(name, "dimen", pInfos.packageName);
            if (id > 0) {
                return remoteRes.getDimensionPixelSize(id);
            }
        } else {
            Log.d(THIS_FILE, "No results yet !! ");
        }
        return null;
    }

    public Integer getColor(String name) {

        if (remoteRes != null && pInfos != null) {
            int id = remoteRes.getIdentifier(name, "color", pInfos.packageName);
            if (id > 0) {
                return remoteRes.getColor(id);
            }
        } else {
            Log.d(THIS_FILE, "No results yet !! ");
        }
        return null;
    }

    public void applyBackgroundDrawable(View button, String res) {
        Drawable d = getDrawableResource(res);
        if (d != null) {
            //UtilityWrapper.getInstance().setBackgroundDrawable(button, d);
        }
    }

    public void applyImageDrawable(ImageView subV, String res) {
        Drawable d = getDrawableResource(res);
        if (d != null) {
            subV.setImageDrawable(d);
        }
    }

    public void applyTextColor(TextView subV, String name) {
        Integer color = getColor(name);
        if (color != null) {
            subV.setTextColor(color);
        }
    }

    public void applyBackgroundStateListDrawable(View v, String prefix) {
        Drawable pressed = getDrawableResource(prefix + "_press");
        Drawable focused = getDrawableResource(prefix + "_focus");
        Drawable normal = getDrawableResource(prefix + "_normal");
        if (focused == null) {
            focused = pressed;
        }
        StateListDrawable std = null;
        if (pressed != null && focused != null && normal != null) {
            std = new StateListDrawable();
            std.addState(new int[]{
                    android.R.attr.state_pressed
            }, pressed);
            std.addState(new int[]{
                    android.R.attr.state_focused
            }, focused);
            std.addState(new int[]{}, normal);
        }

        if (std != null) {
            // UtilityWrapper.getInstance().setBackgroundDrawable(v, std);
        }
    }

    public void applyBackgroundStateListSelectableDrawable(View v, String prefix) {
        Drawable pressed = getDrawableResource(prefix + "_press");
        Drawable focused = getDrawableResource(prefix + "_focus");
        Drawable selected = getDrawableResource(prefix + "_selected");
        Drawable unselected = getDrawableResource(prefix + "_unselected");
        if (focused == null) {
            focused = pressed;
        }
        StateListDrawable std = null;
        if (pressed != null && focused != null && selected != null && unselected != null) {
            std = new StateListDrawable();
            std.addState(new int[]{
                    android.R.attr.state_pressed
            }, pressed);
            std.addState(new int[]{
                    android.R.attr.state_focused
            }, focused);
            std.addState(new int[]{
                    android.R.attr.state_selected
            }, selected);
            std.addState(new int[]{}, unselected);
        }

        if (std != null) {
            // UtilityWrapper.getInstance().setBackgroundDrawable(v, std);
        }
    }

    public void applyLayoutMargin(View v, String prefix) {
        ViewGroup.MarginLayoutParams lp = null;
        try {
            lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        } catch (ClassCastException e) {
            Log.e(THIS_FILE,
                    "Trying to apply layout params to invalid layout " + v.getLayoutParams());
        }
        Integer marginTop = getDimension(prefix + "_top");
        Integer marginBottom = getDimension(prefix + "_bottom");
        Integer marginRight = getDimension(prefix + "_right");
        Integer marginLeft = getDimension(prefix + "_left");
        if (marginTop != null) {
            lp.topMargin = marginTop;
        }
        if (marginBottom != null) {
            lp.bottomMargin = marginBottom;
        }
        if (marginRight != null) {
            lp.rightMargin = marginRight;
        }
        if (marginLeft != null) {
            lp.leftMargin = marginLeft;
        }
        v.setLayoutParams(lp);

    }

    public void applyLayoutSize(View v, String prefix) {
        LayoutParams lp = v.getLayoutParams();
        Integer width = getDimension(prefix + "_width");
        Integer height = getDimension(prefix + "_height");
        if (width != null) {
            lp.width = width;
        }
        if (height != null) {
            lp.height = height;
        }
        v.setLayoutParams(lp);
    }

    private static boolean needRepeatableFix() {
        // In ICS and upper the problem is fixed, so no need to apply by code
        //return (!Compatibility.isCompatible(14));
        //TODO 简单替换isCompatible
        return Build.VERSION.SDK_INT < 14;
    }

    /**
     * @param v The view to fix background of.
     * @see #fixRepeatableDrawable(Drawable)
     */
    public static void fixRepeatableBackground(View v) {
        if (!needRepeatableFix()) {
            return;
        }
        fixRepeatableDrawable(v.getBackground());
    }

    /**
     * Fix the repeatable background of a drawable. This support both bitmap and
     * layer drawables
     *
     * @param d the drawable to fix.
     */
    public static void fixRepeatableDrawable(Drawable d) {
        if (!needRepeatableFix()) {
            return;
        }
        if (d instanceof LayerDrawable) {
            LayerDrawable layer = (LayerDrawable) d;
            for (int i = 0; i < layer.getNumberOfLayers(); i++) {
                fixRepeatableDrawable(layer.getDrawable(i));
            }
        } else if (d instanceof BitmapDrawable) {
            fixRepeatableBitmapDrawable((BitmapDrawable) d);
        }

    }

    /**
     * Fix the repeatable background of a bitmap drawable. This only support a
     * BitmapDrawable
     *
     * @param d the BitmapDrawable to set repeatable.
     */
    public static void fixRepeatableBitmapDrawable(BitmapDrawable d) {
        if (!needRepeatableFix()) {
            return;
        }
        // I don't want to mutate because it's better to share the drawable fix for all that share this constant state
        //d.mutate();
        //Log.d(THIS_FILE, "Exisiting tile mode : " + d.getTileModeX() + ", "+ d.getTileModeY());
        d.setTileModeXY(d.getTileModeX(), d.getTileModeY());

    }

    /**
     * change ActionBar color just if an ActionBar is available
     *
     * @param newColor color id
     */
    public void changeActionBarColor(int newColor) {
        Drawable colorDrawable = new ColorDrawable(newColor);
        Drawable bottomDrawable = context.getResources().getDrawable(
                R.drawable.actionbar_bottom);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{
                colorDrawable, bottomDrawable
        });
        if (oldBackground == null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                ld.setCallback(drawableCallback);
            } else {
                ((FragmentActivity) context).getActionBar().setBackgroundDrawable(ld);
            }
        } else {
            TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                    oldBackground, ld
            });
            // workaround for broken ActionBarContainer drawable handling on
            // pre-API 17 builds
            // https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                td.setCallback(drawableCallback);
            } else {
                ((FragmentActivity) context).getActionBar().setBackgroundDrawable(td);
            }
            td.startTransition(200);
        }
        oldBackground = ld;
        // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
        ((FragmentActivity) context).getActionBar().setDisplayShowTitleEnabled(false);
        ((FragmentActivity) context).getActionBar().setDisplayShowTitleEnabled(true);
    }

    private Drawable.Callback drawableCallback = new Drawable.Callback() {

        @Override
        public void invalidateDrawable(Drawable who) {
            ((FragmentActivity) context).getActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            handler.postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            handler.removeCallbacks(what);
        }
    };
}
