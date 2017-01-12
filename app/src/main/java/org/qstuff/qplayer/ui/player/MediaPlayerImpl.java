package org.qstuff.qplayer.ui.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.NonNull;

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
public class MediaPlayerImpl
	implements QPlayerWrapper,
               MediaPlayer.OnErrorListener,
               MediaPlayer.OnCompletionListener {

    
    private static QPlayerWrapper instance;
    private MediaPlayer           player;
    private QPlayerEventListener  qPlayerEventListener;
        

    MediaPlayerImpl() {
        
    }

    public static QPlayerWrapper getInstance() {

        if (instance == null) {
            instance = new MediaPlayerImpl();
        }
        return instance;
    }

    //
    // QPlayerWrapper
    //
    
    @Override
    public void play() {
        player.start();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void stop() {
        player.stop();
    }
    
    @Override
    public void create(@NonNull QPlayerEventListener qPlayerEventListener) {
        
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
        
        if (player != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            player.setPlaybackParams(player.getPlaybackParams().setSpeed(factor));
        } else {
            Timber.w("setSpeed(): not available for this API level %s",
                Build.VERSION.SDK_INT);
        }
    }

    @Override
    public void setPitch(float factor) {

        if (player != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            player.setPlaybackParams(player.getPlaybackParams().setPitch(factor));
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
            player.setDataSource(file.getAbsolutePath());
            player.prepare();
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
