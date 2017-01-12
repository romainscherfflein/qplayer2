package org.qstuff.qplayer.ui.player;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public interface QPlayerWrapper {
    
    void play();
    void pause();
    void stop();
    
    void create(@NonNull QPlayerEventListener qPlayerEventListener, @Nullable Context ctx);
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
