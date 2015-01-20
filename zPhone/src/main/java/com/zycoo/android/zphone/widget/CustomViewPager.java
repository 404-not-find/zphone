package com.zycoo.android.zphone.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by tqcenglish on 15-1-20.
 */
public class CustomViewPager extends ViewPager {
    ViewPager mViewPager;
    Method populate;
    Method setChildrenDrawingOrderEnabledCompat;


    Field mPageTransformer;
    Field mDrawingOrder;
    Field DRAW_ORDER_REVERSE;
    Field DRAW_ORDER_FORWARD;
    Field DRAW_ORDER_DEFAULT;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViewPager = (ViewPager) this;
        getFields();
    }

    public CustomViewPager(Context context) {
        super(context);
        mViewPager = this;
        getFields();
    }

    private void getMethods() throws NoSuchMethodException {
        setChildrenDrawingOrderEnabledCompat = getClass().getSuperclass().getDeclaredMethod("setChildrenDrawingOrderEnabledCompat", boolean.class);
        setChildrenDrawingOrderEnabledCompat.setAccessible(true);
        populate = getClass().getSuperclass().getDeclaredMethod("populate");
        populate.setAccessible(true);
    }

    private void getFields() {
        Field[] fields = getClass().getSuperclass().getDeclaredFields();
        for (Field field : fields) {
            if ("mPageTransformer".equals(field.getName())) {
                mPageTransformer = field;
                field.setAccessible(true);

            } else if ("mDrawingOrder".equals(field.getName())) {
                mDrawingOrder = field;
                field.setAccessible(true);

            } else if ("DRAW_ORDER_REVERSE".equals(field.getName())) {
                DRAW_ORDER_REVERSE = field;
                field.setAccessible(true);

            } else if ("DRAW_ORDER_FORWARD".equals(field.getName())) {
                DRAW_ORDER_FORWARD = field;
                field.setAccessible(true);

            } else if ("DRAW_ORDER_DEFAULT".equals(field.getName())) {
                DRAW_ORDER_DEFAULT = field;
                field.setAccessible(true);
            }
        }
    }

    @Override
    public void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer) {
        // 对低于api 11的实现动画效果
        //super.setPageTransformer(reverseDrawingOrder, transformer);
        //        if (Build.VERSION.SDK_INT >= 11)
        try {
            getMethods();
            final boolean hasTransformer = transformer != null;
            final boolean needsPopulate = hasTransformer != (mPageTransformer.get(mViewPager) != null);
            mPageTransformer.set(mViewPager, transformer);
            setChildrenDrawingOrderEnabledCompat.invoke(mViewPager, hasTransformer);
            if (hasTransformer) {
                mDrawingOrder.setInt(mViewPager, reverseDrawingOrder ? DRAW_ORDER_REVERSE.getInt(mViewPager) : DRAW_ORDER_FORWARD.getInt(mViewPager));
            } else {
                mDrawingOrder.setInt(mViewPager, DRAW_ORDER_DEFAULT.getInt(mViewPager));
            }
            if (needsPopulate) {
                populate.invoke(mViewPager);
            }
        } catch (Exception e) {
            e.printStackTrace();
            super.setPageTransformer(reverseDrawingOrder, transformer);
        }
    }
}
