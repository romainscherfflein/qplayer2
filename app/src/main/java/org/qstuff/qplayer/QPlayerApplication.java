package org.qstuff.qplayer;

import android.app.Application;
import android.content.Context;

import com.squareup.otto.Bus;

import org.qstuff.qplayer.ui.content.CurrentBrowserFragment;
import org.qstuff.qplayer.ui.content.FilesystemBrowserFragment;
import org.qstuff.qplayer.ui.content.PlaylistBrowserFragment;
import org.qstuff.qplayer.ui.player.PlayerFragment;
import org.qstuff.qplayer.util.TimberCrashReportingTree;

import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import timber.log.Timber;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class QPlayerApplication extends Application {

    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(new MyModule(this));

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new TimberCrashReportingTree());
        }
    }

    public ObjectGraph objectGraph() {
        return objectGraph;
    }

    public void inject(Object object) {
        objectGraph.inject(object);
    }

    @Module(injects = {
            Bus.class,
            QPlayerMainActivity.class,
            PlayerFragment.class,
            FilesystemBrowserFragment.class,
            AbstractBaseFragment.class,
            CurrentBrowserFragment.class,
            PlaylistBrowserFragment.class
    })

    static class MyModule {
        private final Context appContext;

        MyModule(Context appContext) {
            this.appContext = appContext;
        }

        @Provides
        @Singleton
        Bus provideBus() {
            return new Bus();
        }
    }
}
