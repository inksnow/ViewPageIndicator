package com.ink.viewpageindicator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;


class ViewAdapter3 extends PagerAdapter {
    private List<View> datas;

    private ViewPager viewPager;
    private Context context;

    public ViewAdapter3(List<View> list, ViewPager viewPager, Context context) {
        datas=list;
        this.viewPager = viewPager;
        this.context = context;
    }


    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view=datas.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(datas.get(position));
    }


}