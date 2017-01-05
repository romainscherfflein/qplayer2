package org.qstuff.qplayer;

import org.qstuff.qplayer.ui.content.ContentFragment;
import org.qstuff.qplayer.ui.player.PlayerFragment;
import org.qstuff.qplayer.ui.player.QNativePlayerFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class QPlayerMainActivity extends FragmentActivity {
	
    @InjectView(R.id.player_area)
    FrameLayout playerArea;

    @InjectView(R.id.browser_area)
    FrameLayout browserArea;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate():");

        setContentView(R.layout.qplayer_main);

        ButterKnife.inject(this);
        
        // setFragmentHeights();
        
        // load fragments dynamically
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft;
        ft = fm.beginTransaction();

        Fragment frg;
        
        if (BuildConfig.PLAYER_TYPE == 0) {
            frg = new QNativePlayerFragment();
        } else if (BuildConfig.PLAYER_TYPE == 1) {
            frg = new PlayerFragment();
        }

        ft.replace(R.id.player_area, frg);
        
        ContentFragment contentFragment = new ContentFragment();
        ft.replace(R.id.browser_area, contentFragment);

        ft.commitAllowingStateLoss();QPlayerApplication.getInstance().collectScreenStats();
    }

    private void setFragmentHeights() {
        ViewGroup.LayoutParams playerLayout = playerArea.getLayoutParams();
        ViewGroup.LayoutParams browserLayout = browserArea.getLayoutParams();
        
        playerLayout.height = 600;
        browserLayout.height = getScreenHeight() - 600;

        playerArea.setLayoutParams(playerLayout);
        browserArea.setLayoutParams(browserLayout);
    }
    
    private int getScreenHeight() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }
}
