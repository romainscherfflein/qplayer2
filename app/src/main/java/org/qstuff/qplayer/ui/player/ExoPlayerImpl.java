package org.qstuff.qplayer.ui.player;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;

import java.io.File;

import timber.log.Timber;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class ExoPlayerImpl
    implements QPlayerWrapper {

    private static ExoPlayerImpl instance;

    private QPlayerEventListener  qPlayerEventListener;
    
    
    private ExoPlayerImpl() {
        
    }

    public static ExoPlayerImpl getInstance() {

    	if (instance == null) {
			instance = new ExoPlayerImpl();
		}
		return instance;
    }

    //
    // QPlayerWrapper
    //

    @Override
    public void play() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void create(@NonNull QPlayerEventListener qPlayerEventListener) {

        this.qPlayerEventListener = qPlayerEventListener;
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isPlaying() {
        
    }

    @Override
    public boolean isPaused() {
        
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
    }

    @Override
    public int getCurrentPosition() {
        
    }

    @Override
    public int getDuration() {
        
    }

    @Override
    public void loadTrackSync(@NonNull File file) {
        
    }

    //
    // MediaPlayer.OnErrorListener
    // MediaPlayer.OnCompletionListener
    //
    
    public boolean onError(MediaPlayer mp, int what, int extra) {
        qPlayerEventListener.onError();
        return false;
    }

    public void onCompletion(MediaPlayer mp) {
        qPlayerEventListener.onCompletion();
    }
       
}