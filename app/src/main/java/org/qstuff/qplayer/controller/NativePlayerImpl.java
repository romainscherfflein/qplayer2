package org.qstuff.qplayer.controller;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import timber.log.Timber;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class NativePlayerImpl
    implements QPlayerWrapper {

    private static NativePlayerImpl instance;

    private String  currentAudioUri;
    private boolean isPlayerReady;

    private QPlayerEventListener qPlayerEventListener;
    
    //
    // JNI native methods
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

    private native void    seekToNative(int position);
    private native int     getDurationNative();
    private native int     getCurrentPositionNative();
    
    private native void    setPitchNative(int rate);
    private native void    setPlaybackRateNative(int rate);
    private native int     getPlaybackRateNative();
    private native void    setLoopNative(int startPos, int endPos);
    private native void    setNoLoopNative();
	
    static {
    	System.loadLibrary("opensles_wrap");
    }


    private NativePlayerImpl() {
        
    }

    public static NativePlayerImpl getInstance() {

    	if (instance == null) {
			instance = new NativePlayerImpl();
		}
		return instance;
    }

    //
    // QPlayerWrapper
    //

    @Override
    public void play() {
        playNative();
    }

    @Override
    public void pause() {
        pauseNative();
    }

    @Override
    public void stop() {
        stopNative();
    }

    @Override
    public void create(@NonNull QPlayerEventListener qPlayerEventListener,
                       @Nullable Context ctx) {

        createEngine();

        this.qPlayerEventListener = qPlayerEventListener;
    }

    @Override
    public void destroy() {

        releaseAudioPlayer();
        releaseEngine();
    }

    @Override
    public boolean isPlaying() {
        return isPlayingNative();
    }

    @Override
    public boolean isPaused() {
        return !isPlayingNative();
    }

    @Override
    public void setSpeed(float factor) {
        // TODO:
    }

    @Override
    public void setPitch(float factor) {
        // TODO     
        // setPitchNative((int)factor);
    }

    @Override
    public void seekTo(int position) {
        seekToNative(position);
    }

    @Override
    public int getCurrentPosition() {
        return getCurrentPositionNative();
    }

    @Override
    public int getDuration() {
        return getDurationNative();
    }

    @Override
    public void loadTrackSync(@NonNull File file) {
        
        String uri = file.getAbsolutePath();
        
        currentAudioUri = uri;
        boolean ret = createAudioPlayer(uri);
        Timber.d("setDataSource(): success: %s", ret);
        isPlayerReady = ret;
    }

    //
    // MediaPlayer.OnErrorListener
    // MediaPlayer.OnCompletionListener
    //

    // TODO: implement native
    
    public boolean onError(MediaPlayer mp, int what, int extra) {
        qPlayerEventListener.onError();
        return false;
    }

    public void onCompletion(MediaPlayer mp) {
        qPlayerEventListener.onCompletion();
    }
        

    //
    //
    //

    public boolean isPlayerReady() {

        if (currentAudioUri == null)   return false;
        if (currentAudioUri.isEmpty()) return false;

        return isPlayerReady;
    }
}