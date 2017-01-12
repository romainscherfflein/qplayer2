package org.qstuff.qplayer.ui.player;

import android.content.Context;
import android.media.MediaPlayer;
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
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.io.File;

import timber.log.Timber;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class ExoPlayerImpl
    implements QPlayerWrapper,
    ExoPlayer.EventListener,
    AudioRendererEventListener {


    private static ExoPlayerImpl instance;
    
    private SimpleExoPlayer      exoPlayer;
    private QPlayerEventListener qPlayerEventListener;
    
    
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
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void stop() {
        exoPlayer.stop();
    }

    @Override
    public void create(@NonNull  QPlayerEventListener qPlayerEventListener, 
                       @Nullable Context ctx) {

        this.qPlayerEventListener = qPlayerEventListener;
        
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
        exoPlayer = ExoPlayerFactory.newSimpleInstance(ctx, trackSelector, loadControl);
        
        exoPlayer.addListener(this);
        exoPlayer.setAudioDebugListener(this);
    }

    @Override
    public void destroy() {

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
        
    }

    //
    // ExoPlayer.EventListener
    //
    
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        qPlayerEventListener.onError();
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    //
    // AudioRendererEventListener
    //
    
    @Override
    public void onAudioEnabled(DecoderCounters counters) {

    }

    @Override
    public void onAudioSessionId(int audioSessionId) {

    }

    @Override
    public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
        
    }

    @Override
    public void onAudioInputFormatChanged(Format format) {

    }

    @Override
    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

    }

    @Override
    public void onAudioDisabled(DecoderCounters counters) {

    }
}