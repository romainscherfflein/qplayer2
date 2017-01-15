package org.qstuff.qplayer.controller;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;


import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class ExoPlayerImpl
    implements QPlayerWrapper,
    ExoPlayer.EventListener,
    AudioRendererEventListener,
    ExtractorMediaSource.EventListener {


    private static ExoPlayerImpl instance;
    private SimpleExoPlayer      exoPlayer;
    private QPlayerEventListener qPlayerEventListener;

    private Handler mainHandler;
    
    private Context ctx;
    
    
    private ExoPlayerImpl() {
        mainHandler = new Handler(); 
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
        Timber.d("play():");
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        Timber.d("pause():");
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void stop() {
        Timber.d("stop():");
        exoPlayer.stop();
    }

    @Override
    public void create(@NonNull  QPlayerEventListener qPlayerEventListener, 
                       @Nullable Context ctx) {
        Timber.d("create():");

        this.qPlayerEventListener = qPlayerEventListener;
        if (this.ctx == null) {
            this.ctx = ctx;
        }
        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
            new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
            new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the player
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this.ctx, trackSelector, loadControl);
        
        exoPlayer.addListener(this);
        exoPlayer.setAudioDebugListener(this);
    }

    @Override
    public void destroy() {
        ctx = null;
        mainHandler = null;
        exoPlayer.release();
        exoPlayer = null;
    }

    @Override
    public boolean isPlaying() {
        return exoPlayer.getPlayWhenReady();
    }

    @Override
    public boolean isPaused() {
        return exoPlayer.getPlayWhenReady();
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
        exoPlayer.seekTo(position);
    }

    @Override
    public int getCurrentPosition() {
        return (int) exoPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return (int) exoPlayer.getDuration();
    }

    @Override
    public void loadTrackSync(@NonNull File file) {
        Uri uri = Uri.parse(file.getAbsolutePath());
        String userAgent = Util.getUserAgent(this.ctx, "qplayer");
        MediaSource mediaSource = new ExtractorMediaSource(
            uri,
            new DefaultDataSourceFactory(this.ctx, userAgent),
            new DefaultExtractorsFactory(),
            mainHandler,
            this);
        
        exoPlayer.prepare(mediaSource);
        pause();
    }

    //
    // ExoPlayer.EventListener
    //
    
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        Timber.d("ExoPlayer.EventListener.onTimelineChanged()");
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Timber.d("ExoPlayer.EventListener.onTracksChanged()");
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Timber.d("ExoPlayer.EventListener.onLoadingChanged()");
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Timber.d("ExoPlayer.EventListener.onPlayerStateChanged()");
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Timber.d("ExoPlayer.EventListener.onPlayerError()");

        qPlayerEventListener.onError();
    }

    @Override
    public void onPositionDiscontinuity() {
        Timber.d("ExoPlayer.EventListener.onPositionDiscontinuity()");

    }

    //
    // AudioRendererEventListener
    //
    
    @Override
    public void onAudioEnabled(DecoderCounters counters) {
        Timber.d("AudioRendererEventListener.onAudioEnabled()");

    }

    @Override
    public void onAudioSessionId(int audioSessionId) {
        Timber.d("AudioRendererEventListener.onAudioSessionId()");

    }

    @Override
    public void onAudioDecoderInitialized(String decoderName, 
                                          long initializedTimestampMs, 
                                          long initializationDurationMs) {
        Timber.d("AudioRendererEventListener.onAudioDecoderInitialized()");
        
    }

    @Override
    public void onAudioInputFormatChanged(Format format) {
        Timber.d("AudioRendererEventListener.onAudioInputFormatChanged()");

    }

    @Override
    public void onAudioTrackUnderrun(int bufferSize, 
                                     long bufferSizeMs, 
                                     long elapsedSinceLastFeedMs) {
        Timber.d("AudioRendererEventListener.onAudioTrackUnderrun()");

    }

    @Override
    public void onAudioDisabled(DecoderCounters counters) {
        Timber.d("AudioRendererEventListener.onAudioDisabled()");

    }

    //
    // ExtractorMediaSource.EventListener
    //
    
    @Override
    public void onLoadError(IOException error) {
        Timber.e(error, "ExtractorMediaSource.onLoadError()");
    }
}