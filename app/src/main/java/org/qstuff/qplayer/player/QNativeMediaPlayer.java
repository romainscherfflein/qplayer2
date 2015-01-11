package org.qstuff.qplayer.player;

import android.util.Log;

/**
 * MediaPlayer using the native OpenSL ES Library
 * 
 * @author claus chierici (cc@codeyard.de)
 */
public class QNativeMediaPlayer {

	private static final String TAG = "QNativeMediaPlayer";
	
	public native boolean createEngine();
    public native boolean releaseEngine();
    public native boolean createAudioPlayer(String uri);
    public native boolean releaseAudioPlayer();
    
    public native void    play();
    public native void    stop();
    public native void    pause();
    
    public native boolean isPlaying();
    public native boolean isPaused();
    public native boolean isStopped();
    
    public native void    seekTo(int position);
    public native int     getDuration();
    public native int     getCurrentPosition();
    public native void    setPitch(int rate);
    public native void    setPlaybackRate(int rate);
    public native int     getPlaybackRate();
    public native void    setLoop( int startPos, int endPos );
    public native void    setNoLoop();
	
    private static QNativeMediaPlayer instance;
    
    static {
    	System.loadLibrary("opensles_wrap");
    }
    
    
    private QNativeMediaPlayer() {
    	super();
    	Log.i(TAG, "CTOR()");
    }
    
    
    public static QNativeMediaPlayer getInstance() {
    	Log.i(TAG, "getInstance():");

    	if (instance == null) {
			instance = new QNativeMediaPlayer();
		}
		return instance;
    }
    
    
//    public interface OnCompletionListener {
//        public void OnCompletion();
//    }
//  
    
    public OnCompletionListener mCompletionListener;
    
    public void setOnCompletionListener( OnCompletionListener listener )
    {
        mCompletionListener = listener;
    }
    
//
//    private void OnCompletion()
//    {
//        mCompletionListener.OnCompletion();
//
//        int position = getCurrentPosition();
//        int duration = getDuration();
//        
//        if( position != duration )
//        {
//
//        }
//        else
//        {
//            
//        }
//    }
}
