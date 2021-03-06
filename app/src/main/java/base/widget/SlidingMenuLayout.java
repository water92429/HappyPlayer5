package base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.zlm.hp.R;

import base.utils.ColorUtil;
import base.utils.LoggerUtil;

/**
 * @Description: SlidingMenu布局。因为该界面的view是一层一层的，所以这里使用FrameLayout布局
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/28 0:16
 * @Throws:
 */
public class SlidingMenuLayout extends FrameLayout {

    /**
     * 菜单布局
     */
    private FrameLayout mMenuFrameLayout;

    /**
     * 主界面布局
     */
    private LinearLayout mainLinearLayoutContainer;

    /**
     * 屏幕宽度
     */
    private int mScreensWidth;

    /**
     * 判断view是点击还是移动的距离
     */
    private int mTouchSlop;
    /**
     * 触摸最后一次的坐标
     */
    private float mLastX;
    private float mLastY;

    /**
     * 日志
     */
    private LoggerUtil logger;

    private ViewDragHelper mDragHelper;
    /**
     * 记录手势速度
     */
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;


    //
    private Context mContext;

    /**
     * 当前fragment
     */
    private Fragment mCurrentFragment;
    private FragmentManager mFragmentManager;
    private boolean isHandToClose = false;
    /////////////////////////////////////////////

    /**
     * 阴影画笔
     */
    private Paint mFadePaint;
    /**
     * 是否动画结束
     */
    private boolean isDragFinish = true;

    /**
     * 判断该menu是否正在被触摸移动
     */
    private boolean isTouchMove = false;

    /**
     * 记录menuX轴的位置，用于设置menuview的位置
     */
    private int mMenuCurLeftX = 0;

    /**
     * 是否允许拖动
     */
    private boolean isAllowDrag = true;

    public SlidingMenuLayout(@NonNull Context context) {
        super(context);
    }

    public SlidingMenuLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlidingMenuLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SlidingMenuLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        logger = LoggerUtil.getZhangLogger(context);
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        //
        mFadePaint = new Paint();
        // 去锯齿
        mFadePaint.setAntiAlias(true);

    }

    /**
     * 初始化菜单布局
     */
    public void initView(LinearLayout mainLinearLayoutContainer) {
        this.mainLinearLayoutContainer = mainLinearLayoutContainer;

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreensWidth = display.getWidth();

        //
        mMenuFrameLayout = new FrameLayout(mContext);
        mMenuFrameLayout.setBackgroundColor(Color.BLACK);
        mMenuFrameLayout.setId(R.id.menu_container);

        //
        LayoutParams menuLayout = new LayoutParams(mScreensWidth, LayoutParams.MATCH_PARENT);
        menuLayout.leftMargin = mScreensWidth;
        addView(mMenuFrameLayout, menuLayout);

        //
        mMenuCurLeftX = menuLayout.leftMargin;

        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        //置顶部
        this.bringChildToFront(mMenuFrameLayout);

        //设置防止事件穿透明
        mMenuFrameLayout.setClickable(true);
        this.mainLinearLayoutContainer.setClickable(true);

    }

    /**
     * 显示菜单view
     *
     * @param fragmentManager
     * @param fragment
     */
    public void showMenuView(FragmentManager fragmentManager, Fragment fragment) {

        mFragmentManager = fragmentManager;
        mCurrentFragment = fragment;
        mFragmentManager.beginTransaction().add(mMenuFrameLayout.getId(), mCurrentFragment).commit();

        //
        isHandToClose = false;
        mDragHelper.smoothSlideViewTo(mMenuFrameLayout, 0, 0);
        ViewCompat.postInvalidateOnAnimation(this);

    }

    /**
     * 隐藏菜单界面
     */
    public void hideMenuView(FragmentManager supportFragmentManager) {
        isAllowDrag = true;
        isHandToClose = true;
        mDragHelper.smoothSlideViewTo(mMenuFrameLayout, getWidth(), 0);
        ViewCompat.postInvalidateOnAnimation(this);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isAllowDrag) return super.onInterceptTouchEvent(event);
        try {
            int actionId = event.getAction();
            switch (actionId) {
                case MotionEvent.ACTION_DOWN:
                    mLastX = event.getX();
                    mLastY = event.getY();
                    //
                    mDragHelper.processTouchEvent(event);
                    break;
                case MotionEvent.ACTION_MOVE:


                    int[] location = new int[2];
                    mMenuFrameLayout.getLocationOnScreen(location);
                    int mDragViewLeftX = location[0];
                    int mDragViewRightX = mDragViewLeftX + mMenuFrameLayout.getWidth();

//                logger.e("mDragViewLeftX=" + mDragViewLeftX + "  mDragViewRightX=" + mDragViewRightX);
//                logger.e("event.getRawX() = " + event.getRawX());
                    //按下焦点在手动view里面或者菜单界面已打开
                    if ((mDragViewLeftX <= event.getRawX() && event.getRawX() <= mDragViewRightX)) {
                        float curX = event.getX();
                        int deltaX = (int) (mLastX - curX);
                        float curY = event.getY();
                        int deltaY = (int) (mLastY - curY);
                        //左右移动事件
                        if (Math.abs(deltaX) > mTouchSlop && Math.abs(deltaY) < mTouchSlop) {

                            isTouchMove = true;
                            return true;
                        }

                    }


                    //logger.e("isDrag = " + isDrag);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:

                    isTouchMove = false;
                    mLastX = 0;
                    mDragHelper.cancel();


                    if (mMenuFrameLayout.getLeft() < getWidth() / 2) {

                        isHandToClose = false;
                        //在左半边
                        mDragHelper.smoothSlideViewTo(mMenuFrameLayout, 0, 0);
                        ViewCompat.postInvalidateOnAnimation(SlidingMenuLayout.this);
                    } else {

                        //在右半边
                        isHandToClose = true;
                        mDragHelper.smoothSlideViewTo(mMenuFrameLayout, getWidth(), 0);
                        ViewCompat.postInvalidateOnAnimation(SlidingMenuLayout.this);
                    }

                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
        return false;
    }

    /**
     * @param event
     */
    private void obtainVelocityTracker(MotionEvent event) {

        if (mVelocityTracker == null) {

            mVelocityTracker = VelocityTracker.obtain();

        }

        mVelocityTracker.addMovement(event);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isAllowDrag) return super.onTouchEvent(event);
        try {
            obtainVelocityTracker(event);
            mDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
        return true;
    }

    @Override
    public void computeScroll() {

        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
            isDragFinish = false;
        } else {
            //结束
            isDragFinish = true;

            if (isHandToClose) {
                isHandToClose = false;

                if (mCurrentFragment != null) {

                    if (mFragmentManager != null) {

                        //logger.e("回收currentFragment");
                        FragmentTransaction transaction = mFragmentManager.beginTransaction();
                        transaction.remove(mCurrentFragment);
                        transaction.commit();
                    }

                    mCurrentFragment = null;
                }


            }

        }

    }

    public boolean isMenuViewShow() {
        return mMenuFrameLayout.getLeft() < getWidth() / 2;
    }

    /**
     * 释放
     */
    private void releaseVelocityTracker() {

        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;

        }

    }

    /**
     * 添加状态栏视图
     *
     * @param statusBarParentView
     */
    public void addStatusBarView(ViewGroup statusBarParentView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View statusBarView = new View(mContext.getApplicationContext());
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(mContext.getApplicationContext()));
            statusBarView.setBackgroundColor(getStatusColor());
            statusBarParentView.addView(statusBarView, 0, lp);

        }
    }

    /**
     * @Description: 获取状态栏高度
     * @Param: context
     * @Return:
     * @Author: zhangliangming
     * @Date: 2017/7/15 19:30
     * @Throws:
     */
    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取状态栏颜色
     *
     * @return
     */
    private int getStatusColor() {

        return ColorUtil.parserColor(ContextCompat.getColor(mContext.getApplicationContext(), R.color.colorPrimary));

    }

    /**
     * 绘画阴影
     */
    private void drawFade() {

        float percent = mMenuFrameLayout.getLeft() * 1.0f / getWidth();
        int alpha = 200 - (int) (200 * percent);
        mFadePaint.setColor(Color.argb(Math.max(alpha, 0), 0, 0, 0));

        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        //拖动未结束或者正在拖动
        if (!isDragFinish || isTouchMove)
            canvas.drawRect(0, 0, mMenuFrameLayout.getLeft(), getHeight(), mFadePaint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mMenuFrameLayout != null) {
            mMenuFrameLayout.layout(mMenuCurLeftX, 0, mMenuCurLeftX + mMenuFrameLayout.getWidth(), mMenuFrameLayout.getHeight());
        }
    }

    /////////////////////////////////

    public interface FragmentListener {
        void openFragment(Fragment fragment);

        void closeFragment();
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        //此方法是自动生成的，何时开始检测触摸事件
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //如果当前触摸的child是mMainView时开始检测
            return mMenuFrameLayout == child;
        }

        //处理水平和垂直滑动，返回top和left，如果为0，不能滑动，dy、dx表示相对上一次的增量
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0; //不允许垂直滑动
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left < 0) {
                //不允许左越界
                left = 0;
            } else if (left > getWidth()) {
                left = getWidth();
            }
            return left;
        }

        /**
         * 当child位置改变时执行
         *
         * @param changedView 位置改变的子View
         * @param left        child最新的left位置
         * @param top         child最新的top位置
         * @param dx          相较于上一次水平移动的距离
         * @param dy          相较于上一次垂直移动的距离
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (changedView == mMenuFrameLayout) {

                //1.计算view移动的百分比0~1
                float percent = left * 1f / getWidth();

                //缩放
                mainLinearLayoutContainer.setScaleX(0.9f + 0.1f * percent);
                mainLinearLayoutContainer.setScaleY(0.9f + 0.1f * percent);

                drawFade();
                //
                mMenuCurLeftX = left;
                //因为view的位置发生了改变，需要重新布局，如果不进行此操作，存在刷新时，view的位置被还原的问题.之前老是因为view中动态添加数据后，导致还原view位置的问题
                requestLayout();
            }
        }


        //拖动结束后调用,类似于ACTION_UP事件

        /**
         * 手指抬起的时候执行该方法
         *
         * @param releasedChild 当前抬起的View
         * @param xvel          x方向移动的速度：正值：向右移动  负值：向左移动
         * @param yvel          y方向移动的速度：正值：向下移动  负值：向上移动
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (releasedChild == mMenuFrameLayout) {

                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                int xVelocity = (int) velocityTracker.getXVelocity();


                if (Math.abs(xVelocity) > mMinimumVelocity && xvel > 0) {


                    isHandToClose = true;
                    mDragHelper.smoothSlideViewTo(releasedChild, getWidth(), 0);
                    ViewCompat.postInvalidateOnAnimation(SlidingMenuLayout.this);


                } else {
                    if (releasedChild.getLeft() < getWidth() / 2) {


                        isHandToClose = false;
                        //在左半边
                        mDragHelper.smoothSlideViewTo(releasedChild, 0, 0);
                        ViewCompat.postInvalidateOnAnimation(SlidingMenuLayout.this);
                    } else {
                        //在右半边

                        isHandToClose = true;
                        mDragHelper.smoothSlideViewTo(releasedChild, getWidth(), 0);
                        ViewCompat.postInvalidateOnAnimation(SlidingMenuLayout.this);
                    }

                }

                releaseVelocityTracker();
                mLastX = 0;
                isTouchMove = false;
                mDragHelper.cancel();

            }
        }
    }

    ///////////////////////////

    public void setAllowDrag(boolean allowDrag) {
        isAllowDrag = allowDrag;
    }
}
