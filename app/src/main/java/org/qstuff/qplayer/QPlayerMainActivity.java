package org.qstuff.qplayer;

import org.qstuff.qplayer.ui.content.ContentFragment;
import org.qstuff.qplayer.ui.player.PlayerFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class QPlayerMainActivity extends FragmentActivity {

    private final static String TAG = "QPlayerMainActivity";

    private ContentFragment contentFragment;
    private PlayerFragment      playerFragment;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate():");

        setContentView(R.layout.qplayer_main);

        // load fragments dynamically
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft;
        ft = fm.beginTransaction();

        playerFragment = new PlayerFragment();
        ft.replace(R.id.player_area, playerFragment);

        contentFragment = new ContentFragment();
        ft.replace(R.id.browser_area, contentFragment);

        ft.commitAllowingStateLoss();
    }
}
