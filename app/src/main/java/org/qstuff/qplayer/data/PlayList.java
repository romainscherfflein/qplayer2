package org.qstuff.qplayer.data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Claus Chierici (chierici@karlmax-berlin.com) on 5/7/15
 * for Karlmax Berlin GmbH & Co. KG
 * <p/>
 * Copyright (C) 2014 Karlmax Berlin GmbH & Co. KG, All rights reserved.
 */
public class PlayList implements Serializable {

    private List<Track> trackList;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Track> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
    }
}
