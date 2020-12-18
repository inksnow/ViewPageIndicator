package com.ink.viewpageindicator;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;
import java.util.List;

//无限循环滚动 ViewPager

//参考
//https://blog.csdn.net/xia236326/article/details/84072247

class ViewAdapter extends PagerAdapter {
    private List<View> datas;

    private ViewPager viewPager;
    private Context context;

    public ViewAdapter(List<View> list, ViewPager viewPager, Context context) {
        datas=list;
        this.viewPager = viewPager;
        this.context = context;
    }


    @Override
    public int getCount() {
        return datas.size();
    }

    //删除指定位置的页面；适配器负责从view容器中删除view，然而它只保证在finishUpdate(ViewGroup)返回时才完成。
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // 把ImageView从ViewPager中移除掉
        viewPager.removeView(datas.get(position));
        //super.destroyItem(container, position, object);
    }

    //是否获取缓存
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    //实例化Item
    //在指定的位置创建页面；适配器负责添加view到这个容器中，然而它只保证在finishUpdate(ViewGroup)返回时才完成。
    @Override
    public Object instantiateItem(ViewGroup container, int position) {



      View  view = datas.get(position);
        viewPager.addView(view);
        return view;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    //无论是创建view添加到容器中  还是 销毁view 都是在此方法结束之后执行的
    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);

//        int position = viewPager.getCurrentItem();
//        Log.e("position",position+"");
//
//        if (position == 0) {
//            position = datas.size() - 2;
//            viewPager.setCurrentItem(position,false);
//        } else if (position == datas.size() - 1) {
//            position = 1;
//            viewPager.setCurrentItem(position,false);
//        }
    }



}