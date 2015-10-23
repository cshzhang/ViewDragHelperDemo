package cn.hzh.mydrawerlayout;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by hzh on 2015/10/23.
 */
public class MyDrawerLayout extends ViewGroup
{

    private static final int MIN_DRAWER_MARGIN_RIGHT = 64;      //dp
    private static final int MIN_FLING_VELOCITY = 400;          //dp per sec
    //MenuView完全展开时，离容器的右边距：64dp
    private int mMinDrawerRightMargin;
    //控制LeftMenu显示宽度的比例值: 0.0f ~ 1.0f
    private float mMenuWidthRatio = 0.0f;

    private View mContentView;
    private View mMenuView;

    private ViewDragHelper mDrag;

    public MyDrawerLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        //在160dpi的屏幕上，density=1
        float density = getResources().getDisplayMetrics().density;
        //64dp对应的像素值
        mMinDrawerRightMargin = (int) (MIN_DRAWER_MARGIN_RIGHT *density + 0.5f);
        float minVel = MIN_FLING_VELOCITY * density;

        mDrag = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback()
        {
            @Override
            public boolean tryCaptureView(View child, int pointerId)
            {
                return child == mMenuView;
            }

            //将left锁定在-menuWidth ~ 0
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx)
            {
                int width = child.getWidth();
                left = left < -width ? -width : left;
                left = left > 0 ? 0 : left;
                return left;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel)
            {
                int width = releasedChild.getWidth();
                //offset : 0 ~ 1 展开时为1，隐藏时为0
                float offset = (width + releasedChild.getLeft()) * 1.0f / width;
                int finalLeft = xvel > 0 || xvel == 0 && offset > 0.5f ? 0 : -width;
                int finalTop = releasedChild.getTop();

                mDrag.settleCapturedViewAt(finalLeft, finalTop);
                invalidate();
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy)
            {
                int width = changedView.getWidth();
                //offset : 0 ~ 1 展开时为1，隐藏时为0
                float offset = (width + changedView.getLeft()) * 1.0f / width;
                mMenuWidthRatio = offset;

                changedView.setVisibility(offset == 0 ? INVISIBLE : VISIBLE);
                invalidate();
            }

            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId)
            {
                mDrag.captureChildView(mMenuView, pointerId);
            }

            @Override
            public int getViewHorizontalDragRange(View child)
            {
                return child == mMenuView ? child.getWidth() : 0;
            }
        });
        mDrag.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        mDrag.setMinVelocity(minVel);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return mDrag.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mDrag.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);

        //measure child
        View leftMenuView = getChildAt(1);
        MarginLayoutParams lp = (MarginLayoutParams) leftMenuView.getLayoutParams();
        int drawerWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                mMinDrawerRightMargin + lp.leftMargin+lp.rightMargin, lp.width);
        int drawerHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                lp.topMargin + lp.bottomMargin, lp.height);
        leftMenuView.measure(drawerWidthSpec, drawerHeightSpec);

        //measure child
        View contentView = getChildAt(0);
        lp = (MarginLayoutParams) contentView.getLayoutParams();
        int contentWidthSpec = MeasureSpec.makeMeasureSpec(width - lp.leftMargin - lp.rightMargin,
                MeasureSpec.EXACTLY);
        int contentHeightSpec = MeasureSpec.makeMeasureSpec(height - lp.topMargin - lp.bottomMargin,
                MeasureSpec.EXACTLY);
        contentView.measure(contentWidthSpec, contentHeightSpec);

        mMenuView = leftMenuView;
        mContentView = contentView;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        View menuView = mMenuView;
        View contentView = mContentView;

        //摆放contentView
        MarginLayoutParams lp = (MarginLayoutParams) contentView.getLayoutParams();
        contentView.layout(lp.leftMargin, lp.topMargin,
                lp.leftMargin + contentView.getMeasuredWidth(), lp.topMargin + contentView.getMeasuredHeight());

        //摆放menuView
        lp = (MarginLayoutParams) menuView.getLayoutParams();
        int menuWidth = menuView.getMeasuredWidth();
        //mMenuWidthRatio: 0~1; left: -menuWidth~0
        int left = -menuWidth + (int)(menuWidth * mMenuWidthRatio);
        menuView.layout(left, lp.topMargin,
                left+menuWidth, lp.topMargin + menuView.getMeasuredHeight());
    }

    @Override
    public void computeScroll()
    {
        if(mDrag.continueSettling(true))
        {
            invalidate();
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams()
    {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p)
    {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs)
    {
        return new MarginLayoutParams(getContext(), attrs);
    }

    public void openDrawer()
    {
        View menuView = mMenuView;
        mMenuWidthRatio = 1.0f;
        mDrag.smoothSlideViewTo(menuView, 0, menuView.getTop());
        invalidate();
    }

    public void closeDrawer()
    {
        View menuView = mMenuView;
        mMenuWidthRatio = 0.0f;
        mDrag.smoothSlideViewTo(menuView, -menuView.getWidth(), menuView.getTop());
        invalidate();
    }

    public void toggle()
    {
        if(mMenuWidthRatio == 0.0f)
        {
            openDrawer();
        }else if(mMenuWidthRatio == 1.0f)
        {
            closeDrawer();
        }
    }
}
