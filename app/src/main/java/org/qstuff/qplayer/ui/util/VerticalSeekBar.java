package org.qstuff.qplayer.ui.util;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * Thanks to http://kersevanivan.org
 * 
 * @author claus chierici (cc@codeyard.de)
 */
public class VerticalSeekBar extends SeekBar {
	
	private static final String   TAG = "VerticalSeekBar";
	
	private OnSeekBarChangeListener seekbarListener;
	 
	public VerticalSeekBar(Context context) {
	    super(context);
	}
 
	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	}
 
	public VerticalSeekBar(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
 
	 protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	        super.onSizeChanged(h, w, oldh, oldw);
	 }
 
	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(heightMeasureSpec, widthMeasureSpec);
	    setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}
 
	@Override
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener mListener){
	    this.seekbarListener = mListener;
	}

	/**
	 * this does the rotation
	 */
	protected void onDraw(Canvas c) {
	    c.rotate(-90);
	    c.translate(-getHeight(), 0);
	    super.onDraw(c);
	}
 
	@Override
	public boolean onTouchEvent(MotionEvent event) {
 
		Log.d(TAG, "onTouchEvent(): ");
		
	    if (!isEnabled() || seekbarListener==null) {
	        return false;
	    }
 
	    switch (event.getAction()) {
 
	        case MotionEvent.ACTION_DOWN:
	            if(seekbarListener!=null)
	                seekbarListener.onStartTrackingTouch(this);
	            break;
 
	        case MotionEvent.ACTION_MOVE:
	        	int position = getMax() - (int) (getMax() * event.getY() / getHeight());
 
	        	if(position<0)
	        		position=0;
	        	if(position>getMax())
	        		position=getMax();
 
	            setProgress(position);
	            onSizeChanged(getWidth(), getHeight(), 0, 0);
	            if(seekbarListener!=null)
	            	seekbarListener.onProgressChanged(this, position, true);
	            break;
 
	        case MotionEvent.ACTION_UP:
	        	if(seekbarListener!=null)
	        		seekbarListener.onStopTrackingTouch(this);
	            break;
 
	        case MotionEvent.ACTION_CANCEL:
	            break;
 
	    }
 
	    return true;
	}
}
