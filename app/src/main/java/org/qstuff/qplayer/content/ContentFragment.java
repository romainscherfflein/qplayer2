package org.qstuff.qplayer.content;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.qstuff.qplayer.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 *
 */
public class ContentFragment extends Fragment {

    private static final String TAG = "ContentPagerFragment";

    private static final String TAB_TAG_1 = "CURRENT_TAB";
    private static final String TAB_TAG_2 = "FILESYSTEM_TAB";
    private static final String TAB_TAG_3 = "PLAYLIST_TAB";

    private ContentPagerAdapter     contentPagerAdapter;

    @InjectView(R.id.content_pager) ViewPager contentPager;


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

        View view = inflater.inflate(R.layout.content_fragment, container, false);
        ButterKnife.inject(this, view);

        contentPagerAdapter = new ContentPagerAdapter(getFragmentManager());
        contentPager.setAdapter(contentPagerAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume():");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause():");
    }


    //
    // PagerAdapter
    //

    class ContentPagerAdapter extends FragmentPagerAdapter {

        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;

            switch (position) {
                case 0:
                    fragment = new CurrentBrowserFragment();
                    break;
                case 1:
                    fragment = new FilesystemBrowserFragment();
                    break;
                case 2:
                    fragment = new PlaylistBrowserFragment();
                    break;
                default:
                    return null;
                    // should not happen
            }
            Bundle args = new Bundle();

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }
}
