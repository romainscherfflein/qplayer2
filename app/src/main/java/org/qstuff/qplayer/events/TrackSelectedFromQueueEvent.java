package org.qstuff.qplayer.events;

import org.qstuff.qplayer.data.Track;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class TrackSelectedFromQueueEvent {

    public Track track;

    public TrackSelectedFromQueueEvent(Track track) {
        this.track = track;
    }
}
