package org.qstuff.qplayer.util;

import timber.log.Timber;

/**
 * Created by Claus Chierici (chierici@karlmax-berlin.com) on 4/16/15
 * for Karlmax Berlin GmbH & Co. KG
 * <p/>
 * Copyright (C) 2014 Karlmax Berlin GmbH & Co. KG, All rights reserved.
 */
public class TimberCrashReportingTree extends Timber.HollowTree {

    @Override public void i(String message, Object... args) {
        // TODO e.g., Crashlytics.log(String.format(message, args));
    }

    @Override public void i(Throwable t, String message, Object... args) {
        i(message, args); // Just add to the log.
    }

    @Override public void e(String message, Object... args) {
        i("ERROR: " + message, args); // Just add to the log.
    }

    @Override public void e(Throwable t, String message, Object... args) {
        e(message, args);

        // TODO e.g., Crashlytics.logException(t);
    }
}
