package com.reige.swipedeleteitem;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by REIGE
 * Date :2017/4/9.
 */

public class SwipeDeleteItem extends FrameLayout {

    private ViewDragHelper viewDragHelper;
    private View contentView;
    private View deleteView;
    private int contentHeight;
    private int contentWidth;
    private int deleteWidth;

    enum State {
        close, open
    }

    //默认状态是关闭
    private State state = State.close;

    public SwipeDeleteItem(Context context) {
        this(context, null);
    }

    public SwipeDeleteItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeDeleteItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        viewDragHelper = ViewDragHelper.create(this, 1.0f, callback);
    }

    /**
     * 此控件的结束标签读取完毕
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        contentView = getChildAt(0);
        deleteView = getChildAt(1);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        contentView.layout(0, 0, contentWidth, contentHeight);
        Log.e("", "content" + contentWidth);

        deleteView.layout(contentWidth, 0, contentWidth + deleteWidth, deleteWidth);
    }

    /**
     * onMeasure 已经执行完毕 可以直接获取孩子宽高
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        contentHeight = contentView.getMeasuredHeight();
        contentWidth = contentView.getMeasuredWidth();
        deleteWidth = deleteView.getMeasuredWidth();
    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return super.onInterceptHoverEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean value = viewDragHelper.shouldInterceptTouchEvent(event);
        Log.e("item","是否已经打开一个item"+SwipeDeleteManager.getInstance().haveOpened(this));


        if (SwipeDeleteManager.getInstance().haveOpened(this)) {
            //如果打开的是当前的item 不做关闭处理 拦截事件
            Log.e("item","已经打开一个item");
            value = true;
        }else if(SwipeDeleteManager.getInstance().haveOpened()){
            //如果打开的不是当前的item 关闭 拦截事件
            SwipeDeleteManager.getInstance().close();
            value = true;
        }

        return value;
    }

    float downX, downY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //如果已经有item打开 不允许父控件拦截事件
        if(SwipeDeleteManager.getInstance().haveOpened(this)){
            //如果触摸的是打开的item 让viewDragHelper处理触摸事件
            //请求父控件不要拦截事件
            requestDisallowInterceptTouchEvent(true);
        }else if (SwipeDeleteManager.getInstance().haveOpened()) {
            //如果触摸的不是当前打开的item 直接消耗事件
            requestDisallowInterceptTouchEvent(true);
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                float dx = Math.abs(moveX - downX);
                float dy = Math.abs(moveY - downY);
                if (dx > dy) {
                    //如果 x距离大于y 则不允许父控件拦截事件
                    requestDisallowInterceptTouchEvent(true);
                }
                downX = moveX;
                downY = moveY;
                break;
        }

        viewDragHelper.processTouchEvent(event);
        return true;
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == contentView || child == deleteView;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (changedView == contentView) {
                deleteView.layout(deleteView.getLeft() + dx, deleteView.getTop() + dy,
                        deleteView.getRight() + dx, deleteView.getBottom() + dy);
            } else if (changedView == deleteView) {
                contentView.layout(contentView.getLeft() + dx, contentView.getTop() + dy,
                        contentView.getRight() + dx, contentView.getBottom() + dy);
            }
            if (contentView.getLeft() == 0 && state != State.close) {
                state = State.close;
                //item 已经关闭 让manager清空
                SwipeDeleteManager.getInstance().clear();
            } else if (contentView.getLeft() == -deleteWidth && state != State.open) {
                state = State.open;
                //item 已经打开 让manager纪录一下
                Log.e("asdf","打开");
                SwipeDeleteManager.getInstance().setSwipeDeleteItem(SwipeDeleteItem.this);
            }

        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return deleteWidth;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            //限定滑动的范围
            if (child == contentView) {
                if (left > 0) left = 0;
                if (left < -deleteWidth) left = -deleteWidth;
            } else if (child == deleteView) {
                if (left > contentWidth) left = contentWidth;
                if (left < contentWidth - deleteWidth) left = contentWidth - deleteWidth;
            }
            return left;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            //当速度达到一定的值的时候 直接打开或者关闭
            if (xvel < -2000) {
                open();
                return;
            }
            if (xvel > 2000) {
                close();
                return;
            }
            if (contentView.getLeft() > -deleteWidth / 2) {
                close();
            } else {
                open();
            }
        }
    };

    public void open() {
        viewDragHelper.smoothSlideViewTo(contentView, -deleteWidth, contentView.getTop());
        ViewCompat.postInvalidateOnAnimation(SwipeDeleteItem.this);
    }

    public void close() {
        viewDragHelper.smoothSlideViewTo(contentView, 0, contentView.getTop());
        ViewCompat.postInvalidateOnAnimation(SwipeDeleteItem.this);
    }

    /**
     * 重写此方法刷新动画
     */
    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
}
