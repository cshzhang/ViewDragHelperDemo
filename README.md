# ViewDragHelperDemo
ViewDragHelper---自定义ViewGroup神器
# 概述
自定义ViewGroup的时候，子view和用户进行交互是常有的事，即用户拖动某个子view(eg:侧滑菜单)。针对具体需求重写onInterceptTouchEvent和onTouchEvent不是件容易的事。

好在官方在V4包中提供了一个辅助类ViewDragHelper，在我们自定义ViewGroup的时候帮助我们处理子view拖动等事件。简单看一下官方解释：
```java
ViewDragHelper is a utility class for writing custom ViewGroups. It offers a number of useful operations and state tracking for allowing a user to drag and reposition views within their parent ViewGroup.
```

本档案库中包含3个module，分别是Demo1,Demo2,MyDrawerLayout；前两个用来演示ViewDragHelper的简单用法，最后一个利用ViewDragHelper实现DrawerLayout。

# ViewDragHelper初探之Demo1
Demo1简单演示一下ViewDragHelper用法:
* ViewDragHelper实例的创建
* 触摸相关方法的调用
* ViewDragHelper.Callback内部类的编写

自定义ViewGroup：
```java
public class VDHLayout extends LinearLayout
{
    private ViewDragHelper mDragger;

    public VDHLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);

	//创建ViewDragHelper实例
        mDragger = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback()
        {
            @Override
            public boolean tryCaptureView(View child, int pointerId)
            {
                return true;
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

        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return  mDragger.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mDragger.processTouchEvent(event);
        return true;
    }
}
```
分析一下，使用ViewDragHelper有3个步骤：

* 1.创建实例
```java
mDragger = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback()
		{
			//...
		});
```
需要三个参数，第一个就是ViewGroup；第二个sesitivity,主要用于设置touchslop，1.0f是常用值；第三个是Callback，在用户回调过程中会调用相关方法。

* 2.将MotionEvent交给ViewDragHelper
```java
@Override
public boolean onInterceptTouchEvent(MotionEvent ev)
{
	return  mDragger.shouldInterceptTouchEvent(ev);
}

@Override
public boolean onTouchEvent(MotionEvent event)
{
	mDragger.processTouchEvent(event);
	return true;
}
```
OK,没什么好说的；通过mDragger.shouldInterceptTouchEvent(ev)来决定我们是否应该拦截当前事件；mDragger.processTouchEvent(event);来处理事件。

* 3.ViewDragHelper.Callback中相关方法
```java
    @Override
    public boolean tryCaptureView(View child, int pointerId)
    {
	return true;
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
```
ViewDragHelper中拦截和处理事件时，会调用Callback中的很多方法决定一些事：比如哪些子view可以移动，移动的范围是什么，子view的边界控制等。

下面说明一下这三个方法：
```java
public boolean tryCaptureView(View child, int pointerId)
```
返回true，则表示可以捕获该view。第一个参数就是捕获的view。

```java
public int clampViewPositionHorizontal(View child, int left, int dx);

public int clampViewPositionVertical(View child, int top, int dy);
```
这两个方法用来锁定子view移动后的位置。上面方法中对水平移动范围进行的控制，即只能在ViewGroup中移动。而垂直移动范围没有限制，直接return top;

* OK,下面贴一下布局文件：
```java
<?xml version="1.0" encoding="utf-8"?>
<cn.hzh.vdh.view.VDHLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    android:paddingBottom="@dimen/activity_vertical_margin">

    <TextView
        android:text="Hello World!"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_margin="10dp"
        android:background="#44ff0000"
        android:layout_gravity="center"
        android:gravity="center"/>

    <TextView
        android:text="Hello World!"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_margin="10dp"
        android:background="#44ff0000"
        android:layout_gravity="center"
        android:gravity="center"/>

    <TextView
        android:text="Hello World!"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_margin="10dp"
        android:background="#44ff0000"
        android:layout_gravity="center"
        android:gravity="center"/>

</cn.hzh.vdh.view.VDHLayout>
```
* Demo1效果图：
<img src="xxx.gif" width="320px"/>

# ViewDragHelper初探之Demo2
对ViewDragHelper有了直观认识之后，我们继续学习ViewDragHelper其他功能。
* edge检测(eg: DrawerLayout是边界触发拉出菜单)
* 回调Drag release(手指抬起的时候，菜单自动展开/收缩)
* 移动到指定位置(eg:点击button，可以自动展开/收缩菜单)

我们在Demo1的基础上添加上面三个操作；

首先看一下效果图：
<img src="xxx.gif" width="320px"/>
* 第一个子view和Demo1一样
* 第二个子view，除了移动之外，松手就回到原本的位置
* 第三个子view，在边界移动时才操作该子view

由于代码比较长，下面分段贴上相应的代码：
```java
    @Override
    public boolean tryCaptureView(View child, int pointerId)
    {
	//mEdgeTrackerView禁止直接移动
	return child == mDragView || child == mAutoBackView;
    }
```
这里我们对第三个子view禁止直接移动；即，当用户touch第三个子view上时，tryCaptureView()方法return false; 那么这个方法return false的直接后果就是不会执行dragTo()方法，该方法是ViewDragHelper内部的私有方法，实现对capturedView进行移动。

```java
/**
* 手指释放时的回调以及相关方法
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

    @Override
    public void computeScroll()
    {
        if(mDragger.continueSettling(true))
        {
            invalidate();
        }
    }
```
这里调用了ViewDragHelper中的settleCapturedViewAt()方法，让CapturedView移动到指定位置。内部使用scroller对象实现移动的。所以需要调用invalidate(),并配合computeScroll()方法实现移动。

```java
    //边界拖动时的回调
    @Override
    public void onEdgeDragStarted(int edgeFlags, int pointerId)
    {
	mDragger.captureChildView(mEdgeTrackerView, pointerId);
    }

    //enable EdgeTracking
    mDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
```
Callback.onEdgeDragStarted()方法在用户touch屏幕edge的时候触发，在里面我们调用ViewDragHelper的captureChildView()方法设置第三个子view为capturedView；当然还需要手动enable EdgeTracking。

* 下面贴一下其他方法
```java
@Override
protected void onFinishInflate()
{
    super.onFinishInflate();

    mDragView = getChildAt(0);
    mAutoBackView = getChildAt(1);
    mEdgeTrackerView = getChildAt(2);
}

@Override
//初始化，第二个子view的初始位置
protected void onLayout(boolean changed, int l, int t, int r, int b)
{
    super.onLayout(changed, l, t, r, b);

    mAutoBackOriginPos.x = mAutoBackView.getLeft();
    mAutoBackOriginPos.y = mAutoBackView.getTop();
}
```
最后，还有一点需要说明。如果把三个子view的clickable设置为true，即让子view能够消耗事件。你会发现原先能拖拽的子view现在已经无法移动。

解释一下这种情况：
* 如果子view不能消耗事件，那么事件(从DOWN到UP)最终都进ViewGroup的onTouchEvent()方法，
里面调用mDragger.processTouchEvent(event);这样ViewDragHelper的processTouchEvent()方法能正常通过dragTo()方法移动子view
* 如果子view消耗了事件，那么就得根据mDragger.shouldInterceptTouchEvent(ev)函数的返回值，查看源码你发现你需要重写如下两个方法才能让mDragger.shouldInterceptTouchEvent返回true
```java
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
```
上面方法默认返回0,如果返回0，mDragger.shouldInterceptTouchEvent()就返回false。只有getViewHorizontalDragRange和getViewVerticalDragRange返回大于0，能可以对事件进行捕获。

OK,至此Demo2分析完毕。