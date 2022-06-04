package com.haojiangbo.widget;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewOutlineProvider;

public class VideoSurface extends SurfaceView {

    public Surface mSurface;

    private View groupView;

    public VideoSurface(Context context) {
        super(context);
        init();
    }

    public VideoSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public VideoSurface(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        getHolder().setFormat(PixelFormat.RGBA_8888);
        mSurface = getHolder().getSurface();
    }

    public void setSurfaceviewCorner(final float radius) {
        this.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                Rect rect = new Rect();
                view.getGlobalVisibleRect(rect);
                int leftMargin = 0;
                int topMargin = 0;
                Rect selfRect = new Rect(leftMargin, topMargin, rect.right - rect.left - leftMargin, rect.bottom - rect.top - topMargin);
                outline.setRoundRect(selfRect, radius);
            }
        });
        this.setClipToOutline(true);
    }



    public View getGroupView() {
        return groupView;
    }

    public void setGroupView(View groupView) {
        this.groupView = groupView;
    }
}
