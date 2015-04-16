package org.qstuff.qplayer.content;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import org.qstuff.qplayer.R;

/**
 * 
 * @author claus chierici (cc@codeyard.de)
 *
 */
public class SlidingPaneContainerFragment extends Fragment {

    private static final String TAG = "SlidingPaneContainerFragment";

    private static final String TAG_FRAG_LEFT  = "FRAGMENT_LEFT";
    private static final String TAG_FRAG_RIGHT = "FRAGMENT_RIGHT";

    private FilesystemBrowserFragment fragmentLeft;
    private FilesystemBrowserFragment fragmentRight;

    private View paneLeft;
    private View paneRight;
    private View paneSeparator;

    private GestureDetector gestureDetector;
    View.OnTouchListener    gestureListener;

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

        View v = inflater.inflate(R.layout.sliding_pane_container_fragment, container, false);

        paneLeft  = v.findViewById(R.id.pane_left);
        paneRight = v.findViewById(R.id.pane_right);
        paneSeparator = v.findViewById(R.id.pane_separator);

        fragmentLeft = new FilesystemBrowserFragment();
        Bundle args = new Bundle();
        args.putString("paneTag", TAG_FRAG_LEFT);
        fragmentLeft.setArguments(args);

        getChildFragmentManager().beginTransaction()
                .add(R.id.pane_left, fragmentLeft).commit();
        getChildFragmentManager().executePendingTransactions();

        // Gesture detection on the whole container
        gestureDetector = new GestureDetector(getActivity(), new LocalFlingGestureListener());
        gestureListener = new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch():");
                gestureDetector.onTouchEvent(event);
                return true;
            }
        };
        v.setOnTouchListener(gestureListener);

        return v;
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

        paneLeft.setVisibility(View.VISIBLE);
        paneRight.setVisibility(View.GONE);
        paneSeparator.setVisibility(View.GONE);
    }

    /**
     * Create and push in the next view hierarchy fragment from right.
     *
     * @param
     */
    public void pushInNext(String path, String tag)
    {
        Log.d(TAG, "pushInNext(): fragment: " + tag);

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft;
        int backstackSize = fm.getBackStackEntryCount();

        if (tag == TAG_FRAG_LEFT)
        {
            // Trigger from left fragment and left fragment only, adding the first right one
            // add it to backstack
            if (backstackSize == 0)
            {
                Log.d(TAG, "pushInNext(): one active, adding RIGHT");

                fragmentRight = new FilesystemBrowserFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("path", path);
                bundle.putString("paneTag", TAG_FRAG_RIGHT);
                fragmentRight.setArguments(bundle);

                ft = fm.beginTransaction();
                ft.replace(R.id.pane_right, fragmentRight);
                ft.addToBackStack(null);
                ft.commit();
                fm.executePendingTransactions();

                paneRight.setVisibility(View.VISIBLE);
                paneSeparator.setVisibility(View.VISIBLE);
            }
            // Trigger from left fragment and left & right fragment, replacing the right fragment
            // not adding it to backstack
            else if (backstackSize > 0)
            {
                Log.d(TAG, "pushInNext(): two active, replacing RIGHT");

                fragmentRight = new FilesystemBrowserFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("path", path);
                bundle.putString("paneTag", TAG_FRAG_RIGHT);
                fragmentRight.setArguments(bundle);

                ft = fm.beginTransaction();
                ft.replace(R.id.pane_right, fragmentRight);
                ft.commit();
                fm.executePendingTransactions();
            }
        }
        else if (tag == TAG_FRAG_RIGHT)
        {
            // Trigger from right fragment, move right to left and add new right fragment
            // not adding it to backstack
            Log.d(TAG, "pushInNext(): two active");

            ft = fm.beginTransaction();
            ft.remove(fragmentLeft);
            ft.commit();
            fm.executePendingTransactions();

            fragmentRight.setPaneTag(TAG_FRAG_LEFT);
            ft = fm.beginTransaction();
            ft.replace(R.id.pane_left, fragmentRight);
            ft.commit();
            fm.executePendingTransactions();

            fragmentRight = new FilesystemBrowserFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("path", path);
            bundle.putString("paneTag", TAG_FRAG_RIGHT);

            ft = fm.beginTransaction();
            ft.replace(R.id.pane_right, fragmentRight);
            ft.commit();
            fm.executePendingTransactions();
        }
        else
        {
            Log.e(TAG, "pushInNext(): Only three allowed");
            assert(null != null);
        }
    }

    /**
     *
     */
    private void pushOutLast()
    {
        Log.d(TAG, "pushOutLast(): ");

        FragmentManager fm = getChildFragmentManager();

        if (fm.getBackStackEntryCount() == 0)
            return;

        fm.popBackStackImmediate();
        int backstackSize = fm.getBackStackEntryCount();

        Log.d(TAG, "pushOutLast(): backstack size: " + backstackSize);
    }

    /**
     * GestureDetector. We are interested in the swipe to right events only.
     *
     */
    private class LocalFlingGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        private static final int SWIPE_MIN_DISTANCE       = 80;
        private static final int SWIPE_THRESHOLD_VELOCITY = 100;

        public LocalFlingGestureListener() {
            super();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            Log.d(TAG, "onFling()");

            try {
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                {
                    Toast.makeText(getActivity(), "Right -> Left", Toast.LENGTH_SHORT).show();
                }
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                {
                    Toast.makeText(getActivity(), "Left -> Right", Toast.LENGTH_SHORT).show();
                    pushOutLast();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            Log.v(TAG , "onDown():");
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.v(TAG , "onLongPress():");
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            Log.v(TAG, "onSingleTapConfirmed ()");
            return true;
        }
    }

}
