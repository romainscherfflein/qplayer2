package org.qstuff.qplayer.events;

import org.qstuff.qplayer.data.Track;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class AddTrackListToQueueEvent {

    public ArrayList<Track> tracks;

    public AddTrackListToQueueEvent(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }
}
