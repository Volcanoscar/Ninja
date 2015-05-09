package io.github.mthli.Ninja.View;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import io.github.mthli.Ninja.R;
import io.github.mthli.Ninja.Unit.ViewUnit;

public class SwitcherPanel extends ViewGroup {
    private View switcherView;
    private View mainView;
    private RelativeLayout omnibox;
    private LinearLayout progressWrapper;
    private Drawable shadowDrawable;

    private float dimen72dp = 0f;
    private float dimen54dp = 0f;
    private float dimen48dp = 0f;

    /* slideRange: px */
    private float slideRange = 0f;
    private float slideOffset = 1f;
    private float interceptX = 0f;
    private float interceptY = 0f;

    /* coverHeight: px */
    private float coverHeight = 0f;
    public float getCoverHeight() {
        return coverHeight;
    }
    public void setCoverHeight(float coverHeight) {
        this.coverHeight = coverHeight;
    }

    /* shadowHeight: dp */
    public static final int SHADOW_HEIGHT_DEFAULT = 2;
    private int shadowHeight = SHADOW_HEIGHT_DEFAULT;
    public int getShadowHeight() {
        return shadowHeight;
    }
    public void setShadowHeight(int shadowHeight) {
        this.shadowHeight = shadowHeight;
    }

    /* parallaxOffset: dp */
    public static final int PARALLAX_OFFSET_DEFAULT = 64;
    private int parallaxOffset = PARALLAX_OFFSET_DEFAULT;
    public int getParallaxOffset() {
        return parallaxOffset;
    }
    public void setParallaxOffset(int parallaxOffset) {
        this.parallaxOffset = parallaxOffset;
    }

    /* flingVelocity: dp/s */
    public static final int FLING_VELOCITY_DEFAULT = 256;
    private int flingVelocity = FLING_VELOCITY_DEFAULT;
    public int getFlingVelocity() {
        return flingVelocity;
    }
    public void setFlingVelocity(int flingVelocity) {
        this.flingVelocity = flingVelocity;
        if (dragHelper != null) {
            dragHelper.setMinVelocity(flingVelocity * ViewUnit.getDensity(getContext()));
        }
    }

    public enum Status {
        EXPANDED,
        COLLAPSED,
        FLING
    }
    private static final Status STATUS_DEFAULT = Status.EXPANDED;
    private Status status = STATUS_DEFAULT;
    public Status getStatus() {
        return status;
    }

    public interface StatusListener {
        void onExpanded();
        void onCollapsed();
        void onFling();
    }
    private StatusListener statusListener;
    public void setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    private ViewDragHelper dragHelper;
    private class DragHelperCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mainView;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            int hideTop = computeTopPosition(0f);
            int showTop = computeTopPosition(1f);
            return Math.min(Math.max(top, showTop), hideTop);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return (int) slideRange;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            fling(top);
            invalidate();
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (dragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE) {
                slideOffset = computeSlideOffset(mainView.getTop());
                applyParallaxForCurrentSlideOffset();

                if (slideOffset == 1f && status != Status.EXPANDED) {
                    status = Status.EXPANDED;
                    switcherView.setVisibility(INVISIBLE);
                    dispatchOnExpanded();
                } else if (slideOffset == 0f && status != Status.COLLAPSED) {
                    status = Status.COLLAPSED;
                    dispatchOnCollapsed();
                }
            }
        }

        @Override
        public void onViewReleased(View view, float x, float y) {
            int target;
            float direction = -y;
            if (direction > 0) {
                target = computeTopPosition(1f);
            } else if (direction < 0) {
                target = computeTopPosition(0f);
            } else {
                target = computeTopPosition(0f);
            }

            dragHelper.settleCapturedViewAt(view.getLeft(), target);
            invalidate();
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        private static final int[] ATTRS = new int[] {
                android.R.attr.layout_weight
        };

        public LayoutParams() {
            super(MATCH_PARENT, MATCH_PARENT);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray typedArray = c.obtainStyledAttributes(attrs, ATTRS);
            typedArray.recycle();
        }
    }

    public SwitcherPanel(Context context) {
        this(context, null);
    }

    public SwitcherPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("getResource().getDrawable(int) is deprecated.")
    public SwitcherPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shadowDrawable = getResources().getDrawable(R.drawable.shadow, null);
        } else {
            shadowDrawable = getResources().getDrawable(R.drawable.shadow);
        }
        this.dragHelper = ViewDragHelper.create(this, 0.5f, new DragHelperCallback());
        setFlingVelocity(FLING_VELOCITY_DEFAULT);
        setWillNotDraw(false);

        dimen72dp = ViewUnit.dp2px(context, getResources().getDimension(R.dimen.layout_width_72dp));
        dimen54dp = ViewUnit.dp2px(context, getResources().getDimension(R.dimen.layout_height_54dp));
        dimen48dp = ViewUnit.dp2px(context, getResources().getDimension(R.dimen.layout_height_48dp));
        if (context instanceof Activity) {
            int windowHeight = ViewUnit.getWindowHeight((Activity) context);
            int statusBarHeight = ViewUnit.getStatusBarHeight(context);
            int navigationBarHeight = ViewUnit.getNavigationBarHeight(context);
            coverHeight = windowHeight + navigationBarHeight - statusBarHeight - dimen54dp - dimen48dp;
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof MarginLayoutParams ? new LayoutParams((MarginLayoutParams) layoutParams) : new LayoutParams(layoutParams);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams && super.checkLayoutParams(layoutParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Width must have an exact value or MATCH_PARENT.");
        } else if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Height must have an exact value or MATCH_PARENT.");
        } else if (getChildCount() != 2) {
            throw new IllegalStateException("SwitcherPanel layout must have exactly 2 children!");
        }

        switcherView = getChildAt(0);
        mainView = getChildAt(1);
        omnibox = (RelativeLayout) mainView.findViewById(R.id.main_omnibox);
        progressWrapper = (LinearLayout) mainView.findViewById(R.id.main_progress_wrapper);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int layoutWidth = widthSize - getPaddingLeft() - getPaddingRight();
        int layoutHeight = heightSize - getPaddingTop() - getPaddingBottom();

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();

            int width = layoutWidth;
            int height = layoutHeight;
            if (child == switcherView) {
                width = width - layoutParams.leftMargin - layoutParams.rightMargin;
            } else if (child == mainView) {
                height = height - layoutParams.topMargin;
            }

            int childWidthSpec;
            if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                childWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
            } else if (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                childWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            } else {
                childWidthSpec = MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY);
            }

            int childHeightSpec;
            if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
            } else if (layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            } else {
                childHeightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
            }

            child.measure(childWidthSpec, childHeightSpec);
            if (child == mainView) {
                slideRange = mainView.getMeasuredHeight() - coverHeight;
            }
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean change, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();

            int top = paddingTop;
            if (child == mainView) {
                top = computeTopPosition(slideOffset);
            }
            int height = child.getMeasuredHeight();
            int bottom = top + height;
            int left = paddingLeft + layoutParams.leftMargin;
            int right = left + child.getMeasuredWidth();
            child.layout(left, top, right, bottom);
        }
        applyParallaxForCurrentSlideOffset();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int left = mainView.getLeft();
        int top = mainView.getTop() + omnibox.getHeight();
        if (progressWrapper.getVisibility() == VISIBLE) {
            top = top + progressWrapper.getHeight();
        }
        int right = mainView.getRight();
        int bottom = top + ((int) (shadowHeight * ViewUnit.getDensity(getContext())));
        shadowDrawable.setBounds(left, top, right, bottom);
        shadowDrawable.draw(canvas);
    }

    private int computeTopPosition(float slideOffset) {
        int slidePixelOffset = (int) (slideOffset * slideRange);
        return (int) (getMeasuredHeight() - getPaddingBottom() - coverHeight - slidePixelOffset);
    }

    private float computeSlideOffset(int topPosition) {
        return (computeTopPosition(0f) - topPosition) / slideRange;
    }

    @Override
    public void computeScroll() {
        if (dragHelper != null && dragHelper.continueSettling(true)) {
            if (!isEnabled()) {
                dragHelper.abort();
                return;
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        if (!isEnabled() || action == MotionEvent.ACTION_CANCEL) {
            return super.onInterceptTouchEvent(motionEvent);
        }

        if (action == MotionEvent.ACTION_DOWN) {
            interceptX = motionEvent.getRawX();
            interceptY = motionEvent.getRawY();
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (shouldCollapsed()) {
                float deltaY = motionEvent.getRawY() - interceptY;
                if (deltaY >= ViewUnit.getDensity(getContext()) * 64) {
                    collapsed();
                    return true;
                }
            }
        }

        if (shouldExpanded(motionEvent)) {
            expanded();
            return true;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    private boolean shouldCollapsed() {
        int[] location = new int[2];
        omnibox.getLocationOnScreen(location);

        int left = location[0];
        int right = left + omnibox.getWidth();
        int top = location[1];
        int bottom = top + omnibox.getHeight();
        return status == Status.EXPANDED
                && left <= interceptX
                && interceptX <= right
                && top <= interceptY
                && interceptY <= bottom;
    }

    private boolean shouldExpanded(@NonNull MotionEvent motionEvent) {
        int[] location = new int[2];
        mainView.getLocationOnScreen(location);

        int left = location[0];
        int right = left + mainView.getWidth();
        int top = location[1];
        int bottom = top + mainView.getHeight();
        return status == Status.COLLAPSED
                && left <= motionEvent.getRawX()
                && motionEvent.getRawX() <= right
                && top <= motionEvent.getRawY()
                && motionEvent.getRawY() <= bottom;
    }

    public void expanded() {
        smoothSlideTo(1f);
        status = Status.EXPANDED;
    }

    public void collapsed() {
        switcherView.setVisibility(VISIBLE);
        smoothSlideTo(0f);
        status = Status.COLLAPSED;
    }

    private void fling(int top) {
        status = Status.FLING;
        slideOffset = computeSlideOffset(top);

        applyParallaxForCurrentSlideOffset();
        dispatchOnFling();

        LayoutParams layoutParams = (LayoutParams) switcherView.getLayoutParams();
        int defaultHeight = (int) (getHeight() - getPaddingBottom() - getPaddingTop() - coverHeight);
        if (slideOffset < 0) {
            layoutParams.height = top - getPaddingBottom();
            switcherView.requestLayout();
        } else if (layoutParams.height != defaultHeight) {
            layoutParams.height = defaultHeight;
            switcherView.requestLayout();
        }
    }

    private void dispatchOnExpanded() {
        if (statusListener != null) {
            statusListener.onExpanded();
        }
    }

    private void dispatchOnCollapsed() {
        if (statusListener != null) {
            statusListener.onCollapsed();
        }
    }

    private void dispatchOnFling() {
        if (statusListener != null) {
            statusListener.onFling();
        }
    }

    private boolean smoothSlideTo(float slideOffset) {
        if (!isEnabled()) {
            return false;
        }

        int top = computeTopPosition(slideOffset);
        if (dragHelper.smoothSlideViewTo(mainView, mainView.getLeft(), top)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    private void applyParallaxForCurrentSlideOffset() {
        if (parallaxOffset > 0) {
            float offset = parallaxOffset * ViewUnit.getDensity(getContext());
            switcherView.setTranslationY(-(offset * Math.max(slideOffset, 0)));
        }
    }
}
