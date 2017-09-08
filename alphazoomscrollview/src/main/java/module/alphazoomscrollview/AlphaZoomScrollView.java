package module.alphazoomscrollview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.Scroller;

public class AlphaZoomScrollView extends ScrollView {

    private Context mContext;

    private static final String TAG_HEADER = "header";        //头布局Tag


    private float mSensitive = 1.5f;         //放大的敏感系数
    private int mZoomTime = 500;             //头部缩放时间，单位 毫秒
    private boolean mIsParallax = true;      //是否让头部具有视差动画
    private boolean mIsZoomEnable = true;    //是否允许头部放大

    private Scroller mScroller;              //辅助缩放的对象
    private boolean mIsActionDown = false;   //第一次接收的事件是否是Down事件
    private boolean mIsZooming = false;      //是否正在被缩放
    private ViewGroup.LayoutParams mHeaderParams;//头部的参数
    private int mHeaderHeight;               //头部的原始高度
    private View mHeaderView;                //头布局

    private float mLastEventY;               //Move事件最后一次发生时的Y坐标
    private float mDownX;                    //Down事件的X坐标
    private float mDownY;                    //Down事件的Y坐标
    private int mTouchSlop;

    private int mColorRed = 147;
    private int mColorGreen = 176;
    private int mColorBlue = 170;


    private OnPullZoomListener mOnPullZoomListener; //下拉放大的监听

    private AlphaListener mAlphaListener;


    public interface OnPullZoomListener {

        void onPullZoom(int originHeight, int currentHeight);

        void onZoomFinish();

    }

    public interface AlphaListener {

        void toolbarAlphaChange(int alpha);
    }


    public void setOnPullZoomListener(OnPullZoomListener pullZoomListener) {
        this.mOnPullZoomListener = pullZoomListener;
    }


    public void setAlphaListener(AlphaListener alphaListener) {
        mAlphaListener = alphaListener;
    }


    public AlphaZoomScrollView(Context context) {
        this(context, null);
    }

    public AlphaZoomScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlphaZoomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PullZoomAlphaView);
        mSensitive = a.getFloat(R.styleable.PullZoomAlphaView_pzav_sensitive, mSensitive);
        mIsParallax = a.getBoolean(R.styleable.PullZoomAlphaView_pzav_isParallax, mIsParallax);
        mIsZoomEnable = a.getBoolean(R.styleable.PullZoomAlphaView_pzav_isZoomEnable, mIsZoomEnable);
        mZoomTime = a.getInt(R.styleable.PullZoomAlphaView_pzav_zoomTime, mZoomTime);

        a.recycle();

        mScroller = new Scroller(getContext());

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        findTagViews(getChildAt(0));
        if (mHeaderView == null) {
            throw new IllegalStateException("header 不允许为空,请在Xml布局中设置Tag，或者使用属性设置");
        }
        mHeaderParams = mHeaderView.getLayoutParams();
        mHeaderHeight = mHeaderParams.height;
        smoothScrollTo(0, 0);//如果是滚动到最顶部，默认最顶部是ListView的顶部
    }

    /**
     * 递归遍历所有的View，查询Tag
     */
    private void findTagViews(View v) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View childView = vg.getChildAt(i);
                String tag = (String) childView.getTag();
                if (tag != null) {
                    if (TAG_HEADER.equals(tag) && mHeaderView == null) mHeaderView = childView;
                }
                if (childView instanceof ViewGroup) {
                    findTagViews(childView);
                }
            }
        } else {
            String tag = (String) v.getTag();
            if (tag != null) {
                if (TAG_HEADER.equals(tag) && mHeaderView == null) mHeaderView = v;
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {

        super.onScrollChanged(l, t, oldl, oldt);

        int scrollY = getScrollY();
        int headHeight = getResources().getDimensionPixelOffset(R.dimen.header_height);
        if (scrollY <= 0) {
            //get argb by ColorPix
            mAlphaListener.toolbarAlphaChange(Color.argb(0, mColorRed, mColorGreen, mColorBlue));
        } else if (scrollY > 0 && scrollY <= headHeight) {
            float alpha = (float) scrollY / headHeight * 255;
            mAlphaListener.toolbarAlphaChange(Color.argb((int) alpha, mColorRed, mColorGreen, mColorBlue));
        } else {
            mAlphaListener.toolbarAlphaChange(Color.argb(255, mColorRed, mColorGreen, mColorBlue));
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = e.getX();
                mDownY = e.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveY = e.getY();
                if (Math.abs(moveY - mDownY) > mTouchSlop) {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mIsZoomEnable) return super.onTouchEvent(ev);

        float currentX = ev.getX();
        float currentY = ev.getY();
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = currentX;
                mDownY = mLastEventY = currentY;
                mScroller.abortAnimation();
                mIsActionDown = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsActionDown) {
                    mDownX = currentX;
                    mDownY = mLastEventY = currentY;
                    mScroller.abortAnimation();
                    mIsActionDown = true;
                }
                float shiftX = Math.abs(currentX - mDownX);
                float shiftY = Math.abs(currentY - mDownY);
                float dy = currentY - mLastEventY;
                mLastEventY = currentY;
                if (isTop()) {
                    if (shiftY > shiftX && shiftY > mTouchSlop) {
                        int height = (int) (mHeaderParams.height + dy / mSensitive + 0.5);

                        if (height <= mHeaderHeight) {
                            height = mHeaderHeight;
                            mIsZooming = false;
                        } else {
                            mIsZooming = true;
                        }
                        mHeaderParams.height = height;
                        mHeaderView.setLayoutParams(mHeaderParams);
                        if (mOnPullZoomListener != null)
                            mOnPullZoomListener.onPullZoom(mHeaderHeight, mHeaderParams.height);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsActionDown = false;
                if (mIsZooming) {
                    mScroller.startScroll(0, mHeaderParams.height, 0, -(mHeaderParams.height - mHeaderHeight), mZoomTime);
                    mIsZooming = false;
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                break;
        }
        return mIsZooming || super.onTouchEvent(ev);
    }

    private boolean isStartScroll = false;

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            isStartScroll = true;
            mHeaderParams.height = mScroller.getCurrY();
            mHeaderView.setLayoutParams(mHeaderParams);
            if (mOnPullZoomListener != null)
                mOnPullZoomListener.onPullZoom(mHeaderHeight, mHeaderParams.height);
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            if (mOnPullZoomListener != null && isStartScroll) {
                isStartScroll = false;
                mOnPullZoomListener.onZoomFinish();
            }
        }
    }

    private boolean isTop() {
        return getScrollY() <= 0;
    }

    public void setSensitive(float sensitive) {
        this.mSensitive = sensitive;
    }

    public void setIsParallax(boolean isParallax) {
        this.mIsParallax = isParallax;
    }

    public void setIsZoomEnable(boolean isZoomEnable) {
        this.mIsZoomEnable = isZoomEnable;
    }

    public void setZoomTime(int zoomTime) {
        this.mZoomTime = zoomTime;
    }

    public void setColorRed(int colorRed) {
        this.mColorRed = colorRed;
    }

    public void setColorGreen(int colorGreen) {
        this.mColorGreen = colorGreen;
    }

    public void setColorBlue(int colorBlue) {
        this.mColorBlue = colorBlue;
    }
}
