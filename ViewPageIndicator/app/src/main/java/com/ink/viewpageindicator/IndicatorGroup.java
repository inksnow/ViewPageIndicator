package com.ink.viewpageindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.Nullable;

/**
 * ProjectName:    test
 * Package:        com.ink.viewpageindicator
 * ClassName:      IndicatorGroup
 * Description:     java类作用描述
 * CreateDate:     2020-12-16 16:47
 * UpdateDate:     2020-12-16 16:47
 * UpdateRemark:   更新说明
 * Version:        1.0
 * 参考
 * https://github.com/yanyiqun001/bannerDot
 */
public class IndicatorGroup extends View {
    //第一阶段运动
    private int MOVE_STEP_ONE = 1;
    //第二阶段运动
    private int MOVE_STEP_TWO = 2;
    //向左滚动
    public static int DIRECTION_LEFT = 1;
    //向右滚动
    public static int DIRECTION_RIGHT = 2;

    //间隔距离（两圆之间的空白距离）
    private float distance = 100;
    //起始圆初始半径,未选中
    private float mDefaultRadius = 50;
    //起始圆初始半径，选中
    private float mSelectRadius = 60;

    //选中的画笔
    private Paint mSelectPaint;
    //选中颜色
    private int mSelectColor;
    //未选中的画笔
    private Paint mDefaultPaint;
    //未选中颜色
    private int mDefaultColor;


    //当前选中的路径
    private Path mSelectPath = new Path();
    //将要选中的路径
    private Path mDefaultPath = new Path();

    //圆点总个数
    private int count = 1;
    //当前选中的下标
    private int mSelectIndex = 0;

    //整体运动进度 也是原始进度
    private float mOriginProgress = 0;


    //进度前50%，起始圆由大到等于辅助圆
    private float mProgress1 = 0;
    //进度后50%
    private float mProgress2 = 0;
    //当前方向
    private int mDrection = 2;

    private Path mPath = new Path();
    private Path mPath2 = new Path();
    //起始圆变化半径（由大到小）
    private float mChangeRadius;
    //辅助圆变化半径（由小到大）
    private float mSupportChangeRadius;
    //起始圆圆心坐标（固定）
    float mCenterPointX;
    float mCenterPointY;
    //辅助圆圆心坐标（慢慢远离起始圆圆心，到达间隔距离）
    float mSupportCircleX;
    float mSupportCircleY;

    //起始圆变化半径（由大到小）
    private float mChangeRadius2;
    //辅助圆变化半径（由小到大）
    private float mSupportChangeRadius2;
    //起始圆圆心坐标（固定）
    float mCenterPointX2;
    float mCenterPointY2;
    //辅助圆圆心坐标（慢慢远离起始圆圆心，到达间隔距离）
    float mSupportCircleX2;
    float mSupportCircleY2;

    //插值器
    // AccelerateInterpolator 动画从开始到结束，变化率是一个加速的过程。
    //DecelerateInterpolator：动画从开始到结束，变化率是一个减速的过程。
    //CycleInterpolator：动画从开始到结束，变化率是循环给定次数的正弦曲线。
    //AccelerateDecelerateInterpolator：动画从开始到结束，变化率是先加速后减速的过程。
    Interpolator interpolator = new AccelerateDecelerateInterpolator();
    public IndicatorGroup(Context context) {
        this(context, (AttributeSet) null);
    }

    public IndicatorGroup(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.IndicatorGroup);
        this.distance = typedArray.getDimension(R.styleable.IndicatorGroup_distance, 100);
        this.mDefaultRadius = typedArray.getDimension(R.styleable.IndicatorGroup_defaultRadius, 20);
        this.mSelectRadius = typedArray.getDimension(R.styleable.IndicatorGroup_selectRadius, 25);
        this.mDefaultColor = typedArray.getColor(R.styleable.IndicatorGroup_defaultColor, 0XFF555555);
        this.mSelectColor = typedArray.getColor(R.styleable.IndicatorGroup_selectColor, 0XFFFF0000);
        typedArray.recycle();
        initPaint();
        moveToRight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //总高度,最大圆的直径
        int h = (int) Math.ceil(mSelectRadius * 2);
        int w = (int) Math.ceil(count * mSelectRadius * 2 + (count - 1) * distance);
        if (w < 0) {
            w = 0;
        }
       // Log.e("onMeasure", "h:   " + h + "    w ：" + w);
        setMeasuredDimension(w, h);

    }


    private void initPaint() {
        //选中的画笔
        mSelectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectPaint.setColor(mSelectColor);
        mSelectPaint.setStyle(Paint.Style.FILL);
        mSelectPaint.setAntiAlias(true);//抗锯齿
        mSelectPaint.setDither(true);//防抖动
        //未选中的画笔
        mDefaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDefaultPaint.setColor(mDefaultColor);
        mDefaultPaint.setStyle(Paint.Style.FILL);
        mDefaultPaint.setAntiAlias(true);//抗锯齿
        mDefaultPaint.setDither(true);//防抖动
    }


    public void reset() {
        initPaint();

        //整体运动进度 也是原始进度
        mOriginProgress = 0;
        //进度前50%，起始圆由大到等于辅助圆
        mProgress1 = 0;
        //进度后50%
        mProgress2 = 0;
        //当前方向，1左2右
        mDrection = 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.save();
        canvas.translate((float) this.getPaddingLeft(), (float) this.getPaddingTop());

        //先画出未选中的（不包括未选中将要选中的），即画n-2个未选中的圆
        for (int i = 0; i < count; i++) {
            if (mDrection == DIRECTION_RIGHT) {
                //向右，不画当前选中的及下一个的
                if (i != mSelectIndex && i != mSelectIndex + 1) {
                    canvas.drawCircle(this.getCenterPointAt(i), mSelectRadius, mDefaultRadius, mDefaultPaint);
                }
            } else {
                //向左，不画当前选中的及前一个的
                if (i != mSelectIndex && i != mSelectIndex - 1) {
                    canvas.drawCircle(this.getCenterPointAt(i), mSelectRadius, mDefaultRadius, mDefaultPaint);
                }
            }

        }



        canvas.drawCircle(this.mSupportCircleX2, this.mSupportCircleY2, this.mSupportChangeRadius2, this.mDefaultPaint);
        canvas.drawCircle(this.mCenterPointX2, this.mCenterPointY2, this.mChangeRadius2, this.mDefaultPaint);
        canvas.drawPath(this.mPath2, this.mDefaultPaint);

        canvas.drawCircle(this.mSupportCircleX, this.mSupportCircleY, this.mSupportChangeRadius, this.mSelectPaint);
        canvas.drawCircle(this.mCenterPointX, this.mCenterPointY, this.mChangeRadius, this.mSelectPaint);
        canvas.drawPath(this.mPath, this.mSelectPaint);

        canvas.restore();
    }


    public void setCount(int count){
        this.count = count;
    }

    public void setSelectIndex(int index){
        this.mSelectIndex = index;
        reset();
        moveToRight();
    }

    /**
    * 进度百分比，方向，当前选中的下标
    * */
    public void setProgress(float progress,int direction,int index) {
        //Log.e("progress","progress:" + progress);
        mOriginProgress=progress;
        mDrection = direction;
        mSelectIndex = index;
//        if(progress ==0.0f ){
//            mOriginProgress = 0;
//            //进度前50%，起始圆由大到等于辅助圆
//            mProgress1 = 0;
//            //进度后50%
//            mProgress2 = 0;
//            moveToRight();
//        }else
            if(progress<=0.5){
            mProgress1 = progress/0.5f;
            mProgress2=0;
        }else{
            mProgress2=(progress-0.5f)/0.5f;
            mProgress1=1;
        }
        if(mDrection==DIRECTION_RIGHT) {
            moveToRight();
        }else{
            moveToLeft();
        }
        invalidate();
    }



    private void moveToRight() {
        mPath.reset();
        mPath2.reset();
        float mRadiusProgress = interpolator.getInterpolation(mOriginProgress);

        //当前选中的变化

        //起始圆半径
        mChangeRadius = getValue(mSelectRadius, 0, mRadiusProgress);
        //起始圆圆心
        mCenterPointX = getValue(getCenterPointAt(mSelectIndex), getCenterPointAt(mSelectIndex+1) - mSelectRadius, MOVE_STEP_TWO);
        mCenterPointY = mSelectRadius;

        //起点与起始圆圆心间的角度，由45度变为0度
        double radian = Math.toRadians(getValue(45, 0, MOVE_STEP_ONE));
        //X轴距离圆心距离
        float mX = (float) (Math.sin(radian) * mChangeRadius);
        //Y轴距离圆心距离
        float mY = (float) (Math.cos(radian) * mChangeRadius);


        //辅助圆圆心
        mSupportCircleX = getValue(getCenterPointAt(mSelectIndex) + mSelectRadius, getCenterPointAt(mSelectIndex+1), MOVE_STEP_ONE);
        mSupportCircleY = mSelectRadius;

        //辅助圆半径
        mSupportChangeRadius = getValue(0, mSelectRadius, mRadiusProgress);
        //终点与辅助圆圆心间的角度，由0度变为45度
        double supportRadian = Math.toRadians(getValue(0, 45, MOVE_STEP_TWO));
        //X轴距离圆心距离
        float mSupportRadianX = (float) (Math.sin(supportRadian) * mSupportChangeRadius);
        //Y轴距离圆心距离
        float mSupportRadianY = (float) (Math.cos(supportRadian) * mSupportChangeRadius);
        //起点
        float mStartX = mCenterPointX + mX;
        float mStartY = mCenterPointY - mY;

        //终点
        float endPointX = mSupportCircleX - mSupportRadianX;
        float endPointY = mSelectRadius - mSupportRadianY;
        //控制点
        float controlPointX = getValueForAll(getCenterPointAt(mSelectIndex) + mSelectRadius, getCenterPointAt(mSelectIndex+1));
        float controlPointY = mSelectRadius;
        //移动至起点
        mPath.moveTo(mStartX, mStartY);
        //形成闭合区域
        mPath.quadTo(controlPointX, controlPointY, endPointX, endPointY);
        mPath.lineTo(endPointX, mSelectRadius + mSupportRadianY);
        mPath.quadTo(controlPointX, controlPointY, mStartX, mStartY + 2 * mY);
        mPath.lineTo(mStartX, mStartY);


        //将要选中的变化
        //index * mSelectRadius * 2 + mSelectRadius + index * distance

        //起始圆半径
        mChangeRadius2 = getValue(mDefaultRadius, 0, mRadiusProgress);
        //起始圆圆心
        mCenterPointX2 = getValue(getCenterPointAt(mSelectIndex+1), getCenterPointAt(mSelectIndex) + mDefaultRadius, MOVE_STEP_TWO);
        mCenterPointY2 = mSelectRadius;

        //起点与起始圆圆心间的角度，由45度变为0度
         radian = Math.toRadians(getValue(45, 0, MOVE_STEP_ONE));
        //X轴距离圆心距离
         mX = (float) (Math.sin(radian) * mChangeRadius2);
        //Y轴距离圆心距离
         mY = (float) (Math.cos(radian) * mChangeRadius2);


        //辅助圆圆心
        mSupportCircleX2 = getValue(getCenterPointAt(mSelectIndex+1) - mDefaultRadius, getCenterPointAt(mSelectIndex), MOVE_STEP_ONE);
        mSupportCircleY2 = mSelectRadius;

        //辅助圆半径
        mSupportChangeRadius2 = getValue(0, mDefaultRadius, mRadiusProgress);
        //终点与辅助圆圆心间的角度，由0度变为45度
         supportRadian = Math.toRadians(getValue(0, 45, MOVE_STEP_TWO));
        //X轴距离圆心距离
         mSupportRadianX = (float) (Math.sin(supportRadian) * mSupportChangeRadius2);
        //Y轴距离圆心距离
         mSupportRadianY = (float) (Math.cos(supportRadian) * mSupportChangeRadius2);
        //起点
         mStartX = mCenterPointX2 - mX;
         mStartY = mCenterPointY2 - mY;

        //终点
         endPointX = mSupportCircleX2 + mSupportRadianX;
         endPointY = mSelectRadius - mSupportRadianY;
        //控制点
         controlPointX = getValueForAll(getCenterPointAt(mSelectIndex+1)-mDefaultRadius, getCenterPointAt(mSelectIndex) );
         controlPointY = mSelectRadius;

        //移动至起点
        mPath2.moveTo(mStartX, mStartY);
        mPath2.quadTo(controlPointX, controlPointY, endPointX, endPointY);
        mPath2.lineTo(endPointX, mSelectRadius + mSupportRadianY);
        mPath2.quadTo(controlPointX, controlPointY, mStartX, mStartY + 2 * mY);
        mPath2.lineTo(mStartX, mStartY);

    }




    private void moveToLeft() {
        mPath.reset();
        mPath2.reset();
        float mRadiusProgress = interpolator.getInterpolation(mOriginProgress);

        //将要选中的变化

        //起始圆半径
        mChangeRadius2 = getValue(mDefaultRadius, 0, mRadiusProgress);
        //起始圆圆心
        mCenterPointX2 = getValue(getCenterPointAt(mSelectIndex-1), getCenterPointAt(mSelectIndex) - mDefaultRadius, MOVE_STEP_TWO);
        mCenterPointY2 = mSelectRadius;

        //起点与起始圆圆心间的角度，由45度变为0度
        double radian = Math.toRadians(getValue(45, 0, MOVE_STEP_ONE));
        //X轴距离圆心距离
        float mX = (float) (Math.sin(radian) * mChangeRadius2);
        //Y轴距离圆心距离
        float mY = (float) (Math.cos(radian) * mChangeRadius2);


        //辅助圆圆心
        mSupportCircleX2 = getValue(getCenterPointAt(mSelectIndex-1) + mDefaultRadius, getCenterPointAt(mSelectIndex), MOVE_STEP_ONE);
        mSupportCircleY2 = mSelectRadius;

        //辅助圆半径
        mSupportChangeRadius2 = getValue(0, mDefaultRadius, mRadiusProgress);
        //终点与辅助圆圆心间的角度，由0度变为45度
        double supportRadian = Math.toRadians(getValue(0, 45, MOVE_STEP_TWO));
        //X轴距离圆心距离
        float mSupportRadianX = (float) (Math.sin(supportRadian) * mSupportChangeRadius2);
        //Y轴距离圆心距离
        float mSupportRadianY = (float) (Math.cos(supportRadian) * mSupportChangeRadius2);
        //起点
        float mStartX = mCenterPointX2 + mX;
        float mStartY = mCenterPointY2 - mY;

        //终点
        float endPointX = mSupportCircleX2 - mSupportRadianX;
        float endPointY = mSelectRadius - mSupportRadianY;
        //控制点
        float controlPointX = getValueForAll(getCenterPointAt(mSelectIndex-1) + mDefaultRadius, getCenterPointAt(mSelectIndex));
        float controlPointY = mSelectRadius;
        //移动至起点
        mPath2.moveTo(mStartX, mStartY);
        //形成闭合区域
        mPath2.quadTo(controlPointX, controlPointY, endPointX, endPointY);
        mPath2.lineTo(endPointX, mSelectRadius + mSupportRadianY);
        mPath2.quadTo(controlPointX, controlPointY, mStartX, mStartY + 2 * mY);
        mPath2.lineTo(mStartX, mStartY);





        //当前选中的变化
        //起始圆半径
        mChangeRadius = getValue(mSelectRadius, 0, mRadiusProgress);
        //起始圆圆心
        mCenterPointX = getValue(getCenterPointAt(mSelectIndex), getCenterPointAt(mSelectIndex-1) + mSelectRadius, MOVE_STEP_TWO);
        mCenterPointY = mSelectRadius;

        //起点与起始圆圆心间的角度，由45度变为0度
        radian = Math.toRadians(getValue(45, 0, MOVE_STEP_ONE));
        //X轴距离圆心距离
        mX = (float) (Math.sin(radian) * mChangeRadius);
        //Y轴距离圆心距离
        mY = (float) (Math.cos(radian) * mChangeRadius);


        //辅助圆圆心
        mSupportCircleX = getValue(getCenterPointAt(mSelectIndex) - mSelectRadius, getCenterPointAt(mSelectIndex-1), MOVE_STEP_ONE);
        mSupportCircleY = mSelectRadius;

        //辅助圆半径
        mSupportChangeRadius = getValue(0, mSelectRadius, mRadiusProgress);
        //终点与辅助圆圆心间的角度，由0度变为45度
        supportRadian = Math.toRadians(getValue(0, 45, MOVE_STEP_TWO));
        //X轴距离圆心距离
        mSupportRadianX = (float) (Math.sin(supportRadian) * mSupportChangeRadius);
        //Y轴距离圆心距离
        mSupportRadianY = (float) (Math.cos(supportRadian) * mSupportChangeRadius);
        //起点
        mStartX = mCenterPointX - mX;
        mStartY = mCenterPointY - mY;

        //终点
        endPointX = mSupportCircleX + mSupportRadianX;
        endPointY = mSelectRadius - mSupportRadianY;
        //控制点
        controlPointX = getValueForAll(getCenterPointAt(mSelectIndex), getCenterPointAt(mSelectIndex-1) + mSelectRadius);
        controlPointY = mSelectRadius;

        //移动至起点
        mPath.moveTo(mStartX, mStartY);
        mPath.quadTo(controlPointX, controlPointY, endPointX, endPointY);
        mPath.lineTo(endPointX, mSelectRadius + mSupportRadianY);
        mPath.quadTo(controlPointX, controlPointY, mStartX, mStartY + 2 * mY);
        mPath.lineTo(mStartX, mStartY);


    }


    /**
     * 获取当前值(适用分阶段变化的值)
     * @param start 初始值
     * @param end  终值
     * @param step  第几活动阶段
     * @return
     */
    public float getValue(float start, float end, int step) {

        //当第一阶段的时候，mProgress2 = 0，第二阶段的时候，mProgress1 = 1
        //第一阶段的时候mProgress2 = 0 起始点圆心不变

        if(step==MOVE_STEP_ONE) {
            return start + (end - start) * mProgress1;
        }else{
            return start + (end - start) * mProgress2;
        }
    }
    /**
     * 获取当前值（适用全过程变化的值）
     * @param start 初始值
     * @param end  终值
     * @return
     */
    public float getValueForAll(float start, float end){
        return start + (end - start) * mOriginProgress;
    }

    /**
     * 通过进度获取当前值
     * @param start 初始值
     * @param end 终值
     * @param progress 当前进度
     * @return
     */
    public float getValue(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    private float getCenterPointAt(int index) {

        return index * mSelectRadius * 2 + mSelectRadius + index * distance;
    }


}
