package cn.hzh.demo02.view;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by hzh on 2015/10/22.
 */
public class VDHLayout extends LinearLayout
{
    private ViewDragHelper mDragger;
    private View mDragView;
    private View mAutoBackView;
    private View mEdgeTrackerView;

    private Point mAutoBackOriginPos = new Point();

    public VDHLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        mDragger = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback()
        {
            @Override
            public boolean tryCaptureView(View child, int pointerId)
            {
                //mEdgeTrackerView禁止直接移动
                return child == mDragView || child == mAutoBackView;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx)
            {
                //边界控制，横向移动控制在viewgroup内部
                int paddingLeft = getPaddingLeft();
                int rightBound = getWidth() - getPaddingRight() - child.getWidth();
                //控制左边
                left = left < paddingLeft ? paddingLeft : left;
                //控制右边
                left = left > rightBound ?
                        rightBound : left;

                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy)
            {
                return top;
            }

            @Override
            public int getViewHorizontalDragRange(View child)
            {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(View child)
            {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }

            /**
             * 手指释放时的回调
             */
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel)
            {
                if(releasedChild == mAutoBackView)
                {
                    mDragger.settleCapturedViewAt(mAutoBackOriginPos.x, mAutoBackOriginPos.y);
                    invalidate();
                }
            }

            //边界拖动时的回调
            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId)
            {
                mDragger.captureChildView(mEdgeTrackerView, pointerId);
            }
        });

        mDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return mDragger.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mDragger.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        mDragView = getChildAt(0);
        mAutoBackView = getChildAt(1);
        mEdgeTrackerView = getChildAt(2);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);

        mAutoBackOriginPos.x = mAutoBackView.getLeft();
        mAutoBackOriginPos.y = mAutoBackView.getTop();
    }

    @Override
    public void computeScroll()
    {
        if(mDragger.continueSettling(true))
        {
            invalidate();
        }
    }
}
