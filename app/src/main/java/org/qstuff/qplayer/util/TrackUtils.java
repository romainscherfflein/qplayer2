package org.qstuff.qplayer.util;

import org.qstuff.qplayer.data.Track;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by Claus Chierici (chierici@karlmax-berlin.com) on 2/4/16
 * for Karlmax Berlin GmbH & Co. KG
 * <p/>
 * Copyright (C) 2013, 2014, 2015 Karlmax Berlin GmbH & Co. KG,
 * All rights reserved.
 */
public class TrackUtils {
    
    public static boolean trackListContainsTrack(ArrayList<Track> trackList, Track track) {
        Timber.d("trackListContainsTrack(): size  " + trackList.size());
        Timber.d("trackListContainsTrack(): track " + track.getName());

        for (Track t:trackList) {
            Timber.d("track in list: " + t.getUri());
            
            if (t.getUri().compareTo(track.getUri()) == 0) {
                Timber.d("--> match:");
                return true;
            }
        }
        Timber.d("<-- NO match:");
        return false;
    }
}
