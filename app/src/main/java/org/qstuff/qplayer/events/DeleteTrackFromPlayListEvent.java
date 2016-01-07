package org.qstuff.qplayer.events;

import org.qstuff.qplayer.data.PlayList;
import org.qstuff.qplayer.data.Track;

/**
 * Created by Claus Chierici (chierici@karlmax-berlin.com) on 4/16/15
 * for Karlmax Berlin GmbH & Co. KG
 * <p/>
 * Copyright (C) 2014 Karlmax Berlin GmbH & Co. KG, All rights reserved.
 */
public class DeleteTrackFromPlayListEvent {

    public PlayList playList;
    public Track track;

    public DeleteTrackFromPlayListEvent(PlayList playList, Track track) {
        this.playList = playList;
        this.track = track;
    }
}
