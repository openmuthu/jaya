package org.jaya.android;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by murthy on 22/04/17.
 */

public class JayaDocListView extends ListView {

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    private float mPreviousY = 0;
    private boolean mbOverscrollDown = false;

    private boolean mbIsScalingInProgress = false;

    public JayaDocListView(Context context) {
        super(context);
        mScaleDetector = new ScaleGestureDetector(getContext(), new JayaDocListView.ScaleListener());
    }

    public JayaDocListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleDetector = new ScaleGestureDetector(getContext(), new JayaDocListView.ScaleListener());
    }

    public JayaDocListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaleDetector = new ScaleGestureDetector(getContext(), new JayaDocListView.ScaleListener());
    }

    public boolean isScalingInProgress(){
        return mbIsScalingInProgress;
    }

    public boolean isOverscrollDown(){
        return mbOverscrollDown;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if( !isScalingInProgress() ) {
            if (mPreviousY - ev.getRawY() <= 0 && getFirstVisiblePosition() == 0) {
                mbOverscrollDown = true;
            } else {
                mbOverscrollDown = false;
            }
        }

        mPreviousY = ev.getRawY();

        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    public float getScaleFactor(){
        return mScaleFactor;
    }

    public int getVerticalScrollOffset() {
        return computeVerticalScrollOffset();
    }

    class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();

            refreshVisibleViews();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();

            refreshVisibleViews();
        }
    }

    private DataSetObserver mDataSetObserver = new AdapterDataSetObserver();
    private Adapter mAdapter;

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);

        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;

        mAdapter.registerDataSetObserver(mDataSetObserver);
    }

    void refreshVisibleViews() {
        if (mAdapter != null) {
            for (int i = getFirstVisiblePosition(); i <= getLastVisiblePosition(); i ++) {
                final int dataPosition = i - getHeaderViewsCount();
                final int childPosition = i - getFirstVisiblePosition();
                if (dataPosition >= 0 && dataPosition < mAdapter.getCount()
                        && getChildAt(childPosition) != null) {
                    //Log.v(TAG, "Refreshing view (data=" + dataPosition + ",child=" + childPosition + ")");
                    mAdapter.getView(dataPosition, getChildAt(childPosition), this);
                }
            }
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector){
            mbIsScalingInProgress = true;
            ListAdapter adapter = (ListAdapter)getAdapter();
            if( adapter != null && adapter instanceof IListAdapterWithScaleFactor )
                ((IListAdapterWithScaleFactor)adapter).onScaleBegin(detector);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 5.0f));

            ListAdapter adapter = (ListAdapter)getAdapter();
            if( adapter instanceof IListAdapterWithScaleFactor )
                ((IListAdapterWithScaleFactor)adapter).setScaleFactor(mScaleFactor);

            //setAdapter(adapter);
            JayaDocListView.this.invalidateViews();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 5.0f));
            ListAdapter adapter = (ListAdapter)getAdapter();
            if( adapter instanceof IListAdapterWithScaleFactor ) {
                ((IListAdapterWithScaleFactor) adapter).onScaleEnd(mScaleFactor);
            }
            mbIsScalingInProgress = false;
        }
    }

    interface IListAdapterWithScaleFactor {
        void onScaleBegin(ScaleGestureDetector detector);
        void setScaleFactor(float scaleFactor);
        void onScaleEnd(float scaleFactor);
    }
}
