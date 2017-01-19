package org.qstuff.qplayer.controller;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.protyposis.android.mediaplayer.FileSource;
import net.protyposis.android.mediaplayer.MediaPlayer;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 * 
 * Wraps the standard Android MediaPlayer into QPlayerWrapper
 */
public class MediaPlayerExtendedImpl
	implements QPlayerWrapper,
               MediaPlayer.OnErrorListener,
               MediaPlayer.OnCompletionListener {

    
    private static QPlayerWrapper instance;
    
    private MediaPlayer          player;
    private QPlayerEventListener qPlayerEventListener;
    private Context              ctx;
        

    private MediaPlayerExtendedImpl() {
        
    }

    public static QPlayerWrapper getInstance() {

        if (instance == null) {
            instance = new MediaPlayerExtendedImpl();
        }
        return instance;
    }

    //
    // QPlayerWrapper
    //
    
    @Override
    public void play() {
        Timber.d("play():");
        player.start();
    }

    @Override
    public void pause() {
        Timber.d("pause():");
        player.pause();
    }

    @Override
    public void stop() {
        Timber.d("stop():");
        player.stop();
    }
    
    @Override
    public void create(@NonNull QPlayerEventListener qPlayerEventListener, 
                       @NonNull Context ctx) {
        Timber.d("create():");
        this.ctx = ctx;
        
        if (player == null) {
            player = new MediaPlayer();
        }
        player.reset();
        
        this.qPlayerEventListener = qPlayerEventListener;
    }

    @Override
    public void destroy() {
        
        player.stop();
        player.release();
        player = null;
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public boolean isPaused() {
        return !player.isPlaying();
    }

    @Override
    public void setSpeed(float factor) {
        
        if (player != null) {
            player.setPlaybackSpeed(factor);
        } else {
            Timber.w("setSpeed(): not available for this API level %s",
                Build.VERSION.SDK_INT);
        }
    }

    @Override
    public void setPitch(float factor) {

        if (player != null) {
            player.setPlaybackSpeed(factor);
        } else {
            Timber.w("setPitch(): not available for this API level %s",
                Build.VERSION.SDK_INT);
        }
    }

    @Override
    public void seekTo(int position) {
        player.seekTo(position);
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public void loadTrackSync(@NonNull File file) {

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        
        try {
            player.setDataSource(new FileSource(null, file));
            player.prepare();
            qPlayerEventListener.onPrepared(true);
            
        } catch (IOException e) {
            Timber.e(e, "ERROR: player.setDataSource(): ");
        }
    }

    //
    // MediaPlayer.OnErrorListener
    // MediaPlayer.OnCompletionListener
    //

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        qPlayerEventListener.onError();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        qPlayerEventListener.onCompletion();
    }
}
