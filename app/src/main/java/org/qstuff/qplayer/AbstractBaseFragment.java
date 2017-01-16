package org.qstuff.qplayer;



import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.qstuff.qplayer.data.Track;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import timber.log.Timber;

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


    //
    // Preference storing for the fragments
    //
    
    protected void saveTrackList(@NonNull String key, 
                                 @NonNull ArrayList<Track> trackList) {

        Timber.d("saveTrackList(): saving " + trackList.size());
        
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(trackList);
        editor.putString(key, json);
        editor.apply();
    }

    @Nullable
    protected ArrayList<Track> restoreTrackList(@NonNull String key) {

        Gson gson = new Gson();
        String json = preferences.getString(key, "");
        if (json.isEmpty()) return null;
        return gson.fromJson(json, new TypeToken<ArrayList<Track>>(){}.getType());
    }
    
    protected void saveTrack(@NonNull String key,
                             @NonNull Track track) {

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(track);
        editor.putString(key, json);
        editor.apply();
    }

    @Nullable
    protected Track restoreTrack(@NonNull String key) {

        Gson gson = new Gson();
        String json = preferences.getString(key, "");
        if (json.isEmpty()) return null;
        return gson.fromJson(json, Track.class);
    }

    protected void saveIndex(@NonNull String key, int index) {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, index);
        editor.apply();
    }

    protected int restoreIndex(@NonNull String key) {
        return preferences.getInt(key, -1);
    }

    protected void saveState(@NonNull String key, boolean state) {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, state);
        editor.apply();
    }

    protected boolean restoreState(@NonNull String key) {
        return preferences.getBoolean(key, false);
    }
}
