package org.qstuff.qplayer;

import org.qstuff.qplayer.content.ContentFragment;
import org.qstuff.qplayer.content.FilesystemBrowserFragment;
import org.qstuff.qplayer.player.PlayerFragment;
import org.qstuff.qplayer.ui.OnSlidingPaneControl;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

/**
 * 
 * @author claus chierici (cc@codeyard.de)
 *
 */
public class QPlayerMainActivity extends FragmentActivity
    implements OnSlidingPaneControl {

    private final static String TAG = "QPlayerMainActivity";

    private ContentFragment contentFragment;
    private PlayerFragment  playerFragment;
	
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
        ft.replace(R.id.content_area, contentFragment);

        ft.commitAllowingStateLoss();
    }

    // OnSlidingPaneControl
    @Override
    public void addNewRightPane(FilesystemBrowserFragment fragment, String path) {

        contentFragment.addNewRightPane(fragment, path);
    }

    
}
