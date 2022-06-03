package com.haojiangbo.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;

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

    public View getGroupView() {
        return groupView;
    }

    public void setGroupView(View groupView) {
        this.groupView = groupView;
    }
}
