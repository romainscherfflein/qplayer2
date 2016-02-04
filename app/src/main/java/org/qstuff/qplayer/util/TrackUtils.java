package org.qstuff.qplayer.util;

import org.qstuff.qplayer.data.Track;

import java.util.ArrayList;

/**
 * Created by Claus Chierici (chierici@karlmax-berlin.com) on 2/4/16
 * for Karlmax Berlin GmbH & Co. KG
 * <p/>
 * Copyright (C) 2013, 2014, 2015 Karlmax Berlin GmbH & Co. KG,
 * All rights reserved.
 */
public class TrackUtils {
    
    public static boolean trackListContainsTrack(ArrayList<Track> trackList, Track track) {
        
        for (Track t:trackList) {
            if (t.getUri().equals(track.getUri()))
                return true;
        }
        return false;
    }
}
