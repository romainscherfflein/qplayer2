package org.qstuff.qplayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.squareup.otto.Bus;

import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.ui.AbstractBaseDialogFragment;
import org.qstuff.qplayer.ui.content.AddTrackToPlayListDialogFragment;
import org.qstuff.qplayer.ui.content.ChoosePlayListDialogFragment;
import org.qstuff.qplayer.ui.content.CurrentBrowserFragment;
import org.qstuff.qplayer.ui.content.EditPlayListDialogFragment;
import org.qstuff.qplayer.ui.content.EditTrackDialogFragment;
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
            PlaylistBrowserFragment.class,
            PlayListController.class,
            AbstractBaseDialogFragment.class,
            ChoosePlayListDialogFragment.class,
            AddTrackToPlayListDialogFragment.class,
            EditPlayListDialogFragment.class,
            EditTrackDialogFragment.class
        },
        complete = false,
        library = true
    )

    static final class MyModule {
        private final Context appContext;

        Bus bus;
        
        MyModule(Context appContext) {
            this.appContext = appContext;
        }
        @Provides @Singleton Application provideApplicationContext() {
            return (Application)appContext;
        }

        @Provides @Singleton
        Bus provideBus() {
            bus = new Bus();
            return bus;
        }

        @Provides @Singleton
        SharedPreferences provideSharedPreferences(Application app) {
            return app.getSharedPreferences("qplayer", Context.MODE_PRIVATE);
        }

        @Provides @Singleton
        PlayListController providePlayListController(Application app, Bus eventBus, 
                                                     SharedPreferences sharedPreferences) {
            return new PlayListController(app.getApplicationContext(), eventBus, sharedPreferences);
        }
    }
}
