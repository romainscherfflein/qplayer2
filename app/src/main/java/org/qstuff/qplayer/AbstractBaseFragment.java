package org.qstuff.qplayer;



import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * 
 */
public class AbstractBaseFragment extends Fragment {

    @Inject protected SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((QPlayerApplication) getActivity()
                .getApplication())
                .inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
