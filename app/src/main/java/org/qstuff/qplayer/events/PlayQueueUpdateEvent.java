package org.qstuff.qplayer.events;

import org.qstuff.qplayer.data.Track;

import java.util.ArrayList;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class PlayQueueUpdateEvent {

    public ArrayList<Track> tracks;
    public boolean          forceSwap;
    public boolean          prepend;
    public boolean          append;
    public int              indexTrackImmediatePlay;



    public PlayQueueUpdateEvent(ArrayList<Track> tracks,
                                boolean forceSwap,
                                boolean prepend,
                                boolean append,
                                int indexTrackImmediatePlay) {
        this.tracks = tracks;
        this.forceSwap = forceSwap;
        this.prepend = prepend;
        this.append = append;
        this.indexTrackImmediatePlay = indexTrackImmediatePlay;
    }
}
