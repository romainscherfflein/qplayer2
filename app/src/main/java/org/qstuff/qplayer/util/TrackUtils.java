package org.qstuff.qplayer.util;

import org.qstuff.qplayer.data.Track;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Created by Claus Chierici (chierici@karlmax-berlin.com) on 2/4/16
 * for Karlmax Berlin GmbH & Co. KG
 * <p/>
 * Copyright (C) 2013, 2014, 2015 Karlmax Berlin GmbH & Co. KG,
 * All rights reserved.
 */
public class TrackUtils {
    
    public static int trackListContainsTrack(ArrayList<Track> trackList, Track track) {
    
        int index = 0;
        
        for (Track t:trackList) {
            if (t.getUri().compareTo(track.getUri()) == 0) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Calculate the current track position from seekbar progress and total track duration
     *
     * @param progress the seekbars progress value
     * @param totalDuration total track duration in millis
     * @return current track position in millis
     */
    public static int progressToTimer(int progress, int totalDuration) {
        totalDuration = totalDuration / 1000;
        return ((int) ((((double)progress) / 100) * totalDuration)) * 1000;
    }

    /**
     * Calculate the percentage value of currentDuration from totalDuration
     *
     * @param currentDuration current track position in millis
     * @param totalDuration total track duration in millis
     * @return percentage
     */
    public static int getProgressPercentage(long currentDuration, long totalDuration){
        
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
        Double percentage =(((double)currentSeconds)/totalSeconds)*100;
        return percentage.intValue();
    }

    public static String milisecondsToTimeFormattedString(int millis) {
        
        return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }


}
