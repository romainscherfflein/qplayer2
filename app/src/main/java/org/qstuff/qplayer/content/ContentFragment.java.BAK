package org.qstuff.qplayer.content;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.qstuff.qplayer.R;

/**
 *
 */
public class ContentFragment extends Fragment {

    private static final String TAG = "ContentFragment";

    private static final String TAB_TAG_1 = "CURRENT_TAB";
    private static final String TAB_TAG_2 = "FILESYSTEM_TAB";
    private static final String TAB_TAG_3 = "PLAYLIST_TAB";

    private FragmentTabHost     tabHost;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach():");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate():");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView():");
        View v = inflater.inflate(R.layout.content_fragment, container, false);

        tabHost = new FragmentTabHost(getActivity());
        tabHost.setup(getActivity(), getChildFragmentManager(), R.id.content_tab_area);

        tabHost.addTab(tabHost.newTabSpec(TAB_TAG_1).setIndicator("Current"),
                CurrentBrowserFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(TAB_TAG_2).setIndicator("Filesystem"),
                SlidingPaneContainerFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(TAB_TAG_3).setIndicator("Playlists"),
                PlaylistBrowserFragment.class, null);

        return tabHost;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume():");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void addNewRightPane(FilesystemBrowserFragment fragment, String path) {
        Log.d(TAG, "addNewRightPane()");

        Fragment f = getChildFragmentManager().findFragmentByTag(TAB_TAG_2);
        if (f instanceof  SlidingPaneContainerFragment) {
//            ((SlidingPaneContainerFragment)f).addNewRightPane(fragment, path);
        }
    }
}
