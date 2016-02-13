package org.qstuff.qplayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.squareup.otto.Bus;

import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.ui.dialogs.AbstractBaseDialogFragment;
import org.qstuff.qplayer.ui.dialogs.AddTrackToPlayListDialogFragment;
import org.qstuff.qplayer.ui.dialogs.AddTracksToQueueDialogFragment;
import org.qstuff.qplayer.ui.dialogs.ChoosePlayListDialogFragment;
import org.qstuff.qplayer.ui.content.QueueBrowserFragment;
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

    private DisplayMetrics metrics;
    private int            smallestWidth;
    
    private static QPlayerApplication    instance;
    
    
    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(new MyModule(this));

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());

        } else {
            Timber.plant(new TimberCrashReportingTree());
        }

        instance = this;
    }

    public static QPlayerApplication getInstance() {
        return instance;
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
            QueueBrowserFragment.class,
            PlaylistBrowserFragment.class,
            PlayListController.class,
            AbstractBaseDialogFragment.class,
            ChoosePlayListDialogFragment.class,
            AddTrackToPlayListDialogFragment.class,
            AddTracksToQueueDialogFragment.class
       
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


    public String getDPI() {

        StringBuilder str = new StringBuilder();
        metrics = getResources().getDisplayMetrics();
        str.append(metrics.densityDpi);

        if (metrics.densityDpi >= DisplayMetrics.DENSITY_LOW
            && metrics.densityDpi < DisplayMetrics.DENSITY_MEDIUM)
            str.append(" (LDPI)");
        if (metrics.densityDpi >= DisplayMetrics.DENSITY_MEDIUM
            && metrics.densityDpi < DisplayMetrics.DENSITY_HIGH)
            str.append(" (MDPI)");
        if (metrics.densityDpi >= DisplayMetrics.DENSITY_HIGH
            && metrics.densityDpi < DisplayMetrics.DENSITY_XHIGH)
            str.append(" (HDPI)");
        if (metrics.densityDpi >= DisplayMetrics.DENSITY_XHIGH
            && metrics.densityDpi < DisplayMetrics.DENSITY_XXHIGH)
            str.append(" (XHDPI)");
        if (metrics.densityDpi >= DisplayMetrics.DENSITY_XXHIGH
            && metrics.densityDpi < DisplayMetrics.DENSITY_XXXHIGH)
            str.append(" (XXHDPI)");
        if (metrics.densityDpi >= DisplayMetrics.DENSITY_XXXHIGH)
            str.append(" (XXXHDPI)");

        return str.toString();
    }
    
    /**
     * For debugging multiple screen layouts
     *
     * @return
     */
    public String collectScreenStats() {
        
        StringBuilder str = new StringBuilder();
        metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int dpWidth = (int) (width / metrics.density);
        int dpHeight = (int) (height / metrics.density);
        smallestWidth = Math.min(dpWidth, dpHeight);
        str.append("\n").append(Build.MANUFACTURER);
        str.append(" ");
        str.append(Build.MODEL);
        str.append("\npixels:            ");
        str.append(width);
        str.append(" x ");
        str.append(height);
        str.append("\ndp (px / density): ");
        str.append(dpWidth);
        str.append("dp x ");
        str.append(dpHeight);
        str.append("dp");
        str.append("\nsmallest width:    ").append(smallestWidth);
        str.append("\ndensity:           ");
        str.append(metrics.density);
        str.append("\ndensityDpi:        ");
        str.append(metrics.densityDpi);
        str.append(getDPI());
        
        Timber.d("collectScreenStats():"+ str.toString());
        return str.toString();
    }
}
