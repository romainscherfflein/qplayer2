package org.qstuff.qplayer.ui.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.SeekBar;

import org.qstuff.qplayer.R;

import timber.log.Timber;

/**
 * Thanks to http://kersevanivan.org
 * 
 * @author claus chierici (cc@codeyard.de)
 */
public class VerticalSeekBar extends SeekBar {
	
	private static final String   TAG = "VerticalSeekBar";

	private Rect  rect;
	private Paint paint ;
	private int seekbarWidth;
	
	
	private OnSeekBarChangeListener seekbarListener;
	 
	public VerticalSeekBar(Context context) {
	    super(context);
	}
 
	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);

        Log.d(TAG, "VerticalSeekBar(1): ");
	}
 
	public VerticalSeekBar(Context context, AttributeSet attrs) {
	    super(context, attrs);

        Log.d(TAG, "VerticalSeekBar(2): ");
        rect = new Rect();
        paint = new Paint();
        // seekbarWidth = 30;
        Resources r = getResources();
        seekbarWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            r.getDimension(R.dimen.pitchbar_width),
            r.getDisplayMetrics());
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
	protected void onDraw(Canvas canvas) {

        canvas.rotate(-90);
        canvas.translate(-getHeight(), 0);
        int offset = getThumbOffset();
        int progress = getProgress();
		
	    rect.set(offset,
            	 (getWidth() / 2) - (seekbarWidth / 2),
            	 getHeight(),
            	 (getWidth() / 2) + (seekbarWidth / 2));
        
        paint.setColor(getResources().getColor(R.color.black));

        canvas.drawRect(rect, paint);
        
        float diff = (float)getHeight() / 1000;
        
        if (progress > 500) {
	    	
            rect.set(getHeight() / 2,
                	 (getWidth() / 2) - (seekbarWidth / 2),
                     (int)(getHeight() / 2  + (diff) * (getProgress() - 500)), 
                	 getWidth() / 2   + (seekbarWidth / 2));
            
            paint.setColor(getResources().getColor(R.color.q_orange));
            canvas.drawRect(rect, paint);
        }

        if (progress < 500) {
	
            rect.set((int)(getHeight() / 2 - ((diff) * (500 - progress))),
                	 (getWidth() / 2) - (seekbarWidth / 2),
                	 getHeight() / 2,
                	 getWidth() / 2   + (seekbarWidth / 2));
            
            paint.setColor(getResources().getColor(R.color.q_orange));
            canvas.drawRect(rect, paint);
        }

        super.onDraw(canvas);
	}
 
	@Override
	public boolean onTouchEvent(MotionEvent event) {
 		
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
 
	        	if(position < 0)
	        		position = 0;
	        	if(position > getMax())
	        		position = getMax();
 
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
