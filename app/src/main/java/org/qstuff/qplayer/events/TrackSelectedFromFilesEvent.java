package org.qstuff.qplayer.events;

import org.qstuff.qplayer.data.Track;

import java.io.File;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class TrackSelectedFromFilesEvent {

    public Track track;
    public boolean playNow;

    public TrackSelectedFromFilesEvent(Track track, boolean playNow) {
        this.track = track;
        this.playNow = playNow;
    }
}
