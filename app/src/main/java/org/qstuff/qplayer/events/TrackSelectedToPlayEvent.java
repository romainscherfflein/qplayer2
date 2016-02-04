package org.qstuff.qplayer.events;

import org.qstuff.qplayer.data.PlayList;
import org.qstuff.qplayer.data.Track;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class TrackSelectedToPlayEvent {

    public Track track;
    public int queueIndex;

    public TrackSelectedToPlayEvent(int queueIndex, Track track) {
        this.queueIndex = queueIndex;
        this.track = track;
    }
}
