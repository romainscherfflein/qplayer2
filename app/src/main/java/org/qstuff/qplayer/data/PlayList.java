package org.qstuff.qplayer.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class PlayList implements Serializable {

    private ArrayList<Track>  trackList;
    private ArrayList<String> trackListNames;

    private String name;

    public PlayList() {
        trackList = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Track> getTrackList() {
        return trackList;
    }
    
    public ArrayList<String> getTrackListNames() {
        if (trackListNames != null)
            return trackListNames;
        trackListNames = new ArrayList<>();
        for (Track t : trackList) {
            trackListNames.add(t.getName());
        }
        return trackListNames;
    }
    
    public void setTrackList(ArrayList<Track> trackList) {
        this.trackList = trackList;
    }
    
    public void addTrack(Track track) {
        if (trackList == null)
            trackList = new ArrayList<>();
        trackList.add(track);
        if (trackListNames == null)
            trackListNames = new ArrayList<>();
        trackListNames.add(track.getName());
    }
    
    public void deleteTrack(Track track) {
        trackList.remove(track);
        trackListNames.remove(track.getName());
    }
    @Override
    public String toString() {
        return name;
    }
}
