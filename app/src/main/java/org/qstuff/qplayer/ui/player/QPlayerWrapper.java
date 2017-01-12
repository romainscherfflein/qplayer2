package org.qstuff.qplayer.ui.player;

import android.support.annotation.NonNull;

import java.io.File;

public interface QPlayerWrapper {
    
    void play();
    void pause();
    void stop();
    
    void create(@NonNull QPlayerEventListener qPlayerEventListener);
    void destroy();
    
    boolean isPlaying();
    boolean isPaused();

    void setSpeed(float factor);
    void setPitch(float factor);

    void seekTo(int position);
    int  getCurrentPosition();
    int  getDuration();

    void loadTrackSync(@NonNull File file);
    
}
