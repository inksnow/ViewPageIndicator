package com.ink.viewpageindicator;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;



//2张图时右滑会出现空白，滑动结束后才显示，因为2张图片时左右两边用的是同一个view，parent.removeView(v)后其中一个就没有显示了
//解决办法，判断是否小于3张，是的话就复制扩充数据


class ViewAdapter2 extends PagerAdapter {
    private List<View> datas;

    private ViewPager viewPager;
    private Context context;

    public ViewAdapter2(List<View> list, ViewPager viewPager, Context context) {
        datas=list;
        this.viewPager = viewPager;
        this.context = context;

        //2张图时右滑会出现空白，滑动结束后才显示，因为2张图片时左右两边用的是同一个view，parent.removeView(v)后其中一个就没有显示了
        //解决办法，判断是否小于3张，是的话就复制扩充数据
        //view 不能克隆，只能在创建数据是如果是小于三个的数据是多创建数据
//        if(datas.size()==1){
//            datas.add(1,datas.get(0));
//            datas.add(2,datas.get(0));
//        }else if(datas.size()==2){
//            datas.add(2,datas.get(0));
//            datas.add(3,datas.get(1));
//        }

    }


    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = datas.get(position % datas.size());
        ViewGroup parent = (ViewGroup) v.getParent();
        if (parent != null) {
            parent.removeView(v);
        }
        container.addView(v);
        return v;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        Log.e(this.getClass().getName(),"destroyItem。position="+position);
//        position = position % datas.size();
//        container.removeView(datas.get(position));
//        Log.e(this.getClass().getName(),"destroyItem.view_size="+container.getChildCount());
    }




}