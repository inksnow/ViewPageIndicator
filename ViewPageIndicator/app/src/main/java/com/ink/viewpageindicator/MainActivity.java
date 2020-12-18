package com.ink.viewpageindicator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager1, viewPager2, viewPager3;
    private IndicatorGroup indicatorGroup1, indicatorGroup2,indicatorGroup3;
    private ViewAdapter viewAdapter1;
    private ViewAdapter2 viewAdapter2;
    private ViewAdapter3 viewAdapter3;
    private int mViewPagerIndex1;
    private int mViewPagerIndex2;
    private int mViewPagerIndex3;
    //自动滑动
    private boolean isAuto = true;

    ArrayList<View> views1 = new ArrayList<>();
    ArrayList<View> views2 = new ArrayList<>();
    ArrayList<View> views3 = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager1 = findViewById(R.id.ViewPager1);
        viewPager2 = findViewById(R.id.ViewPager2);
        viewPager3 = findViewById(R.id.ViewPager3);
        indicatorGroup1 = findViewById(R.id.IndicatorGroup1);
        indicatorGroup2 = findViewById(R.id.IndicatorGroup2);
        indicatorGroup3 = findViewById(R.id.IndicatorGroup3);

        //设置滑动速度，默认的切换太快了，
        // （滑动速度影响手动滑动，手动滑动到一半后，后面的会自动滑动到下一页或重置到当前页，这个速度影响这部分的自动滑动速度）
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(viewPager1.getContext(),
                    new AccelerateInterpolator());
            field.set(viewPager1, scroller);
            scroller.setmDuration(600);
        } catch (Exception e) {

        }

        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(viewPager2.getContext(),
                    new AccelerateInterpolator());
            field.set(viewPager2, scroller);
            scroller.setmDuration(600);
        } catch (Exception e) {

        }


        //无限循环
        initPagerData1();
        viewAdapter1 = new ViewAdapter(views1, viewPager1, this);
        viewPager1.setAdapter(viewAdapter1);
        viewPager1.setCurrentItem(1);
        mViewPagerIndex1 = 1;
        indicatorGroup1.setCount(views1.size() - 2);
        indicatorGroup1.setSelectIndex(mViewPagerIndex1 - 1);
        viewPager1.setOnPageChangeListener(pageChangeListener1);
        mHandler.removeMessages(111);
        mHandler.sendEmptyMessageDelayed(111, 2000);


        initPagerData2();
        //2张图时右滑会出现空白，滑动结束后才显示，因为2张图片时左右两边用的是同一个view，parent.removeView(v)后其中一个就没有显示了
        //解决办法，判断是否小于3张，是的话就复制扩充数据
        viewAdapter2 = new ViewAdapter2(views2, viewPager2, this);
        viewPager2.setAdapter(viewAdapter2);
        viewPager2.setCurrentItem(Integer.MAX_VALUE / 2 -(Integer.MAX_VALUE / 2 % views2.size()));
        indicatorGroup2.setCount(views2.size());
        indicatorGroup2.setSelectIndex(0);
        viewPager2.setOnPageChangeListener(pageChangeListener2);
        mHandler.removeMessages(333);
        mHandler.sendEmptyMessageDelayed(333, 2000);

        initPagerData3();
        viewAdapter3 = new ViewAdapter3(views3, viewPager3, this);
        viewPager3.setAdapter(viewAdapter3);
        viewPager3.setCurrentItem(0);
        indicatorGroup3.setCount(views3.size());
        indicatorGroup3.setSelectIndex(0);
        viewPager3.setOnPageChangeListener(pageChangeListener3);

    }




    ViewPager.OnPageChangeListener pageChangeListener1 = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            //position等于当前选中的，向右滑（下一张是右边的）
            //position小于当前选中的，向左滑（下一张是左边的）
            if (position == mViewPagerIndex1) {
                if (position == views1.size() - 2) {
                    if (positionOffset == 0.0f) {
                        indicatorGroup1.setProgress(positionOffset, 2, 0);
                    }
                } else {
                    indicatorGroup1.setProgress(positionOffset, 2, mViewPagerIndex1 - 1);
                }

            } else if (position < mViewPagerIndex1) {
                if (mViewPagerIndex1 - position == 1) {
                    if (position == 0) {
                        if (positionOffset == 0.0f) {
                            indicatorGroup1.setProgress(positionOffset, 2, views1.size() - 2);
                        }
                    } else {
                        indicatorGroup1.setProgress(1 - positionOffset, 1, mViewPagerIndex1 - 1);
                    }
                } else {
                    indicatorGroup1.setProgress(1 - positionOffset, 1, position);
                }

            } else {
                indicatorGroup1.setProgress(1 - positionOffset, 1, position);
            }
           // Log.e("progress", "position:" + position + "mViewPagerIndex1:" + mViewPagerIndex1 + "    positionOffset: " + positionOffset);


        }

        @Override
        public void onPageSelected(int position) {
            //Log.e("onPageSelected", position + "");
            //mViewPagerIndex1 = viewPager1.getCurrentItem();
            //延时动画时长毫秒检测是否需要切换，不然自动切换时最后一张到第一张没有动画效果
            //可能原因是viewPager1.setCurrentItem后onPageSelected立马回调，但是此时动画还没有完成，如果直接在viewPager1.setCurrentItem到第一张或最后一张，则没有动画
            //如果手动滑动的话，滑动到多一半会自动滑动完最后一部分，但是用时没有全部用时那么多，可能会造成切换延时
            //只手动滑动的话，在adapter的finishUpdate中检测最好。
            //只自动滑动的话可以这样设置延时，
            //如果手动加自动结合的话，可以把滑动时间设小一点。
//            isAuto = true;
//            mHandler.removeMessages(222);
//            mHandler.sendEmptyMessageDelayed(222,600);
//            mHandler.removeMessages(111);
//            mHandler.sendEmptyMessageDelayed(111, 3000);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //Log.e("onPageScrollState", state + "");
            //2自动滑动开始（手指滑动到一半再放开也会触发）
            //1手指滑动开始
            //0滑动结束
            //在手指滑到一半还有一半自动滑到下一页的情况下，此时getCurrentItem会立马变为下一页，但是动画还没有结束
            //或者setCurrentItem到下一页也会立马getCurrentItem变为下一页，但是动画还没结束
            //所以不要在state = 2时赋值mViewPagerIndex1


            if (state == 1) {
                mViewPagerIndex1 = viewPager1.getCurrentItem();
                mHandler.removeMessages(111);
                isAuto = false;

            } else if (state == 0) {
                mViewPagerIndex1 = viewPager1.getCurrentItem();
                //滑动结束
                isAuto = true;
                mHandler.sendEmptyMessage(222);
                mHandler.removeMessages(111);
                mHandler.sendEmptyMessageDelayed(111, 3000);

            }

        }
    };



    ViewPager.OnPageChangeListener pageChangeListener2= new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            //position等于当前选中的，向右滑（下一张是右边的）
            //position小于当前选中的，向左滑（下一张是左边的）
            if (position == mViewPagerIndex2) {
                if (position%views2.size() == views2.size()-1 ) {
                    //最后一个圆点，回到第一个圆点
                    if (positionOffset == 0.0f) {
                        indicatorGroup2.setProgress(positionOffset, 2, 0);
                    }
                } else {
                    indicatorGroup2.setProgress(positionOffset, 2, position%views2.size());
                }

            } else if (position < mViewPagerIndex2) {

                //第一个圆点左滑，跳到最后一个圆点
                    if (mViewPagerIndex2%views2.size() == 0) {
                        if (positionOffset == 0.0f) {
                            indicatorGroup2.setProgress(positionOffset, 2, views2.size()-1);
                        }
                    } else {
                        indicatorGroup2.setProgress(1 - positionOffset, 1, position%views2.size()+1);
                    }


            }else {
                indicatorGroup2.setProgress(positionOffset, 2, position%views2.size());
            }

//            Log.e("progress", "position:" + position+ "mViewPagerIndex2:" + mViewPagerIndex2);
//
//            Log.e("progress", "position:" + position%views2.size() + "mViewPagerIndex2:" + mViewPagerIndex2%views2.size() + "    positionOffset: " + positionOffset);


        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //Log.e("onPageScrollState", state + "");
            //2自动滑动开始（手指滑动到一半再放开也会触发）
            //1手指滑动开始
            //0滑动结束
            //在手指滑到一半还有一半自动滑到下一页的情况下，此时getCurrentItem会立马变为下一页，但是动画还没有结束
            //或者setCurrentItem到下一页也会立马getCurrentItem变为下一页，但是动画还没结束
            //所以不要在state = 2时赋值mViewPagerIndex1


            if (state == 1) {
                mViewPagerIndex2 = viewPager2.getCurrentItem();
                mHandler.removeMessages(333);

            } else if (state == 0) {
                mViewPagerIndex2 = viewPager2.getCurrentItem();
                //滑动结束
                mHandler.removeMessages(333);
                mHandler.sendEmptyMessageDelayed(333, 3000);

            }

        }
    };




    ViewPager.OnPageChangeListener pageChangeListener3= new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            //position等于当前选中的，向右滑（下一张是右边的）
            //position小于当前选中的，向左滑（下一张是左边的）
            if (position == mViewPagerIndex3) {
                indicatorGroup3.setProgress(positionOffset, 2, position);

//                if (position == views2.size()-1 ) {
//                    //最后一个圆点，回到第一个圆点
//                    if (positionOffset == 0.0f) {
//                        indicatorGroup2.setProgress(positionOffset, 2, 0);
//                    }
//                } else {
//                    indicatorGroup2.setProgress(positionOffset, 2, position%views2.size());
//                }

            } else if (position < mViewPagerIndex3) {
                indicatorGroup3.setProgress(1 - positionOffset, 1, position+1);
                //第一个圆点左滑，跳到最后一个圆点
//                if (mViewPagerIndex2%views2.size() == 0) {
//                    if (positionOffset == 0.0f) {
//                        indicatorGroup2.setProgress(positionOffset, 2, views2.size()-1);
//                    }
//                } else {
//                    indicatorGroup2.setProgress(1 - positionOffset, 1, position%views2.size()+1);
//                }

            }else {
                indicatorGroup3.setProgress(positionOffset, 2, position);
            }

//            Log.e("progress", "position:" + position+ "mViewPagerIndex2:" + mViewPagerIndex2);
//
//            Log.e("progress", "position:" + position%views2.size() + "mViewPagerIndex2:" + mViewPagerIndex2%views2.size() + "    positionOffset: " + positionOffset);


        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == 1) {
                mViewPagerIndex3 = viewPager3.getCurrentItem();
            } else if (state == 0) {
                mViewPagerIndex3 = viewPager3.getCurrentItem();
            }

        }
    };



    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 111:
                    //Log.e("viewPager1", viewPager1.getCurrentItem() + "");
                    viewPager1.setCurrentItem(viewPager1.getCurrentItem() + 1, true);
                    break;
                case 222:
                    int positions = viewPager1.getCurrentItem();
                    //Log.e("position", positions + "");

                    if (positions == 0) {
                        positions = views1.size() - 2;
                        viewPager1.setCurrentItem(positions, false);
                    } else if (positions == views1.size() - 1) {
                        positions = 1;
                        viewPager1.setCurrentItem(positions, false);
                    }

                    break;
                case 333:
                    viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1, true);
                    break;
            }
        }
    };


    private void initPagerData1() {
        TextView textView = new TextView(this);
        textView.setBackgroundColor(0XFF722ed1);
        textView.setText("5");
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        views1.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0XFFfa541c);
        textView.setText("1");
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        views1.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0XFF13c2c2);
        textView.setText("2");
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        views1.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0XFF1890ff);
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        textView.setText("3");
        views1.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0xffeb2f96);
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        textView.setText("4");
        views1.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0XFF722ed1);
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        textView.setText("5");
        views1.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0XFFfa541c);
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        textView.setText("1");
        views1.add(textView);


    }

    private void initPagerData2() {


        TextView textView = new TextView(this);
        textView.setBackgroundColor(0XFFfa541c);
        textView.setText("1");
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        views2.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0XFF13c2c2);
        textView.setText("2");
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        views2.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0XFF1890ff);
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        textView.setText("3");
        views2.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0xffeb2f96);
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        textView.setText("4");
        views2.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0XFF722ed1);
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        textView.setText("5");
        views2.add(textView);
    }

    private void initPagerData3() {


        TextView textView = new TextView(this);
        textView.setBackgroundColor(0XFFfa541c);
        textView.setText("1");
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        views3.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0XFF13c2c2);
        textView.setText("2");
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        views3.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0XFF1890ff);
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        textView.setText("3");
        views3.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0xffeb2f96);
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        textView.setText("4");
        views3.add(textView);

        textView = new TextView(this);
        textView.setBackgroundColor(0XFF722ed1);
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0XFFFFFFFF);
        textView.setText("5");
        views3.add(textView);
    }
    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
