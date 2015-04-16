package org.qstuff.qplayer;

import android.app.Application;

import org.qstuff.qplayer.util.TimberCrashReportingTree;

import timber.log.Timber;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class QPlayerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();        Timber.d( "onCreate():");

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new TimberCrashReportingTree());
        }
    }
}
