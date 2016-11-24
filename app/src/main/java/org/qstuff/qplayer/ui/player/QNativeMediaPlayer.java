package org.qstuff.qplayer.ui.player;

import android.util.Log;

import timber.log.Timber;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class QNativeMediaPlayer {

	private static final String TAG = "QNativeMediaPlayer";

    private static QNativeMediaPlayer instance;
    private OnCompletionListener onCompletionListener;

    private String currentAudioUri;
    private boolean isPlayerReady;

    //
    // JNI methods
    //

    private native boolean createEngine();
    private native boolean releaseEngine();
    private native boolean createAudioPlayer(String uri);
    private native boolean releaseAudioPlayer();
    
    private native void    playNative();
    private native void    stopNative();
    private native void    pauseNative();

    private native boolean isPlayingNative();
    private native boolean isPausedNative();
    private native boolean isStoppedNative();

    private native void    seekTo(int position);
    private native int     getDuration();
    private native int     getCurrentPosition();
    private native void    setPitch(int rate);
    private native void    setPlaybackRate(int rate);
    private native int     getPlaybackRate();
    private native void    setLoop( int startPos, int endPos );
    private native void    setNoLoop();
	
    static {
    	System.loadLibrary("opensles_wrap");
    }


    private QNativeMediaPlayer() {
    	super();
    	Log.i(TAG, "CTOR()");
        createEngine();
    }

    public static QNativeMediaPlayer getInstance() {
    	Log.i(TAG, "getInstance():");

    	if (instance == null) {
			instance = new QNativeMediaPlayer();
		}
		return instance;
    }


    public void setOnCompletionListener( OnCompletionListener listener ) {
        onCompletionListener = listener;
    }

    //
    //
    //

    public boolean isPlayerReady() {
        Log.d(TAG, "isPlayerReady()");

        if (currentAudioUri == null)   return false;
        if (currentAudioUri.isEmpty()) return false;

        return isPlayerReady;
    }

    //
    // player functions
    //

    public void play() {
        playNative();
    }

    public void pause() {
        pauseNative();
    }

    public void stop() {
        stopNative();
    }

    public boolean isPlaying() {
        return isPlayingNative();
    }

    public boolean isPaused() {
        return isPausedNative();
    }

    public boolean isStopped() {
        return isStoppedNative();
    }

    public void setDataSource(String uri) {
        if (uri == null) return;

        currentAudioUri = uri;
        boolean ret = createAudioPlayer(uri);
        Timber.d("setDataSource(): success: %s", ret);
        isPlayerReady = ret;
    }
    
    public int getTrackDuration() {
        return getDuration();
    }
    
    public int getTrackCurrentPosition() {
        return getCurrentPosition();
    }
    
    public void seekToPosition(int position) {
        seekTo(position);
    }
}