package org.qstuff.qplayer.ui.player;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.qstuff.qplayer.AbstractBaseFragment;
import org.qstuff.qplayer.Constants;
import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.TrackSelectedFromFilesEvent;
import org.qstuff.qplayer.events.PlayQueueUpdateEvent;
import org.qstuff.qplayer.ui.util.VerticalSeekBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class PlayerFragment extends AbstractBaseFragment
	implements AdapterView.OnItemSelectedListener,
        OnSeekBarChangeListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    
    @Inject Bus bus;
    @Inject PlayListController playListController;

    //
    // UI Elements
    //
    
    @InjectView(R.id.pitch_control)             
    VerticalSeekBar pitchControl;
    
    @InjectView(R.id.player_button_previous)
    ImageView buttonPrevious;
    
    @InjectView(R.id.player_button_play)
    ImageView buttonPlay;
    
    @InjectView(R.id.player_button_next)
    ImageView buttonNext;
    
    @InjectView(R.id.player_button_shuffle)
    ImageView buttonShuffle;
    
    @InjectView(R.id.player_button_repeat)
    ImageView buttonRepeat;
        
    @InjectView(R.id.player_text_current_track) 
    TextView textCurrentTrack;

    @InjectView(R.id.player_text_total_time)
    TextView textTotalTime;

    @InjectView(R.id.player_text_dynamic_time)
    TextView textDynamicTime;

    @InjectView(R.id.pitch_control_value)
    TextView pitchControlValue;

    @InjectView(R.id.jog_wheel)                 
    JogWheelImageView jogView;

    @InjectView(R.id.jog_wheel_frame)
    FrameLayout jogViewFrame;

    @InjectView(R.id.pitch_range_setting)
    Spinner pitchRangeSetting;
    
    private MediaPlayer      player;
    private boolean          isPrepared;
    private boolean          isPlaying;
    private boolean          shallPlayImmediately;

    private ArrayList<Track> trackList;

    private Track            currentTrack = null;

    private Handler          updateHandler = new Handler();
    private UpdateRunnable   updateRunnable;

    private boolean          isContinousPlayEnabled;
    private boolean          isRepeatPlayEnabled;
    private boolean          isRepeatPlayOnceEnabled;


    //
    // Fragment Lifecycle
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate():");

        isPrepared = false;

        if (player == null)
            player = new MediaPlayer();
        player.reset();

        trackList = new ArrayList<Track>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Timber.d("onCreateView():");

        View v = inflater.inflate(R.layout.player_fragment, container, false);
        ButterKnife.inject(this, v);

        pitchControl.setOnSeekBarChangeListener(this);
        
        jogView.setWheelListener(new JogWheelImageView.JogWheelListener() {

            @Override
            public void onWheelChanged(int arg) {
                //Timber.d("onWheelChanged(): " + arg);

                if (arg > 0)
                    ;
                else
                    ;
            }
        });

        ArrayAdapter<String> spinnerAdapter = 
            new ArrayAdapter<String>(getActivity(), 
                android.R.layout.simple_spinner_item, 
                getResources().getStringArray(R.array.pitch_range_values));

        spinnerAdapter.setDropDownViewResource(R.layout.spinner_pitch_range);
        pitchRangeSetting.setAdapter(spinnerAdapter);
        pitchRangeSetting.setOnItemSelectedListener(this);

        jogViewFrame.bringToFront();
        return v;
    }
        
    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);

        currentTrack = restoreTrack();
        isPlaying = restorePlayingState();

        if (currentTrack == null) return;

        if (!player.isPlaying())
            loadTrack(currentTrack);
    }

    @Override
    public void onPause() {
        super.onPause();

        saveTrack();
        savePlayingState();

        bus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        cleanupPlayer();
    }

    //
    // Event Subscriptions
    //

    @Subscribe
    public void onTrackSelectedEvent(TrackSelectedFromFilesEvent event) {
        Timber.d("onTrackSelectedEvent(): " + event.track.getName());
        trackList.add(0, event.track);
        loadTrack(event.track);
        shallPlayImmediately = false;
        
    }

    @Subscribe
    public void onPlayQueueUpdateEvent(PlayQueueUpdateEvent event) {
        Timber.d("onPlayQueueUpdateEvent(): ");
        
        // Tracklist will completely exchanged by new list
        // current track keeps playing
       
        trackList = event.tracks;
    }
    
    //
    // Private
    //

    private void loadTrack(Track track) {

        cleanupPlayer();
        preparePlayer(new File(track.getUri()));
        currentTrack = track;
        
        textCurrentTrack.setText(currentTrack.getName().replaceFirst("[.][^.]+$", ""));
        buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.button_play_selected));
    }

    private void cleanupPlayer() {
        Timber.d("cleanupPlayer():");

        textDynamicTime.setText("remain: " + milisecondsToTimeFormattedString(0));
        textTotalTime.setText("total: " + milisecondsToTimeFormattedString(0));
        textCurrentTrack.setText("");
        
        if (player != null) {
            resetUpdateTimer();
            player.stop();
            player.release();
            player = null;
            isPrepared = false;
        }
    }

    private void resetUpdateTimer () {
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
            updateRunnable = null;
        }
    }
    
    private void preparePlayer(File file) {
        Timber.d("preparePlayer(): " + file.getAbsolutePath());
        
        isPrepared = false;
        
        if (player == null) {
            player = new MediaPlayer();
        }

        try {
            player.setDataSource(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.prepareAsync();
    }

    private void saveTrack() {

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(currentTrack);
        editor.putString(Constants.PREFS_KEY_LAST_PLAYED_TRACK_PATH, json);
        editor.apply();
    }

    private void savePlayingState() {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.PREFS_KEY_PLAYING_STATE, player.isPlaying());
        editor.apply();
    }

    private Track restoreTrack() {

        Gson gson = new Gson();
        String json = preferences.getString(Constants.PREFS_KEY_LAST_PLAYED_TRACK_PATH, "");
        if (json.isEmpty()) return null;

        Timber.d("JSON: " + json);
        return gson.fromJson(json, Track.class);
    }

    private boolean restorePlayingState() {
        return preferences.getBoolean(Constants.PREFS_KEY_PLAYING_STATE, false);
    }

    //
    // Click Handlers
    //

    // Play / Pause
    
    @OnClick(R.id.player_button_play)
    public void onPlayButtonClicked() {
        Timber.d("onPlayButtonClicked(): ");

        if (isPrepared) {
            if (player.isPlaying()) {
                player.pause();
                buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.button_play_selected));

                isPlaying = false;

            } else {
                player.start();
                buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.button_pause_selected));

                isPlaying = true;
            }
        }
    }

    // Repeat / Repeat 1 (TODO)

    @OnClick(R.id.player_button_repeat)
    public void onRepeatButtonClicked() {
        Timber.d("onRepeatButtonClicked(): ");

        if (isRepeatPlayEnabled) {
            buttonRepeat.setImageDrawable(getResources().getDrawable(R.drawable.button_loop));
            isRepeatPlayEnabled = false;
        } else {
            buttonRepeat.setImageDrawable(getResources().getDrawable(R.drawable.button_loop_selected));
            isRepeatPlayEnabled = true;
        }
    }

    // Shuffle
    
    @OnClick(R.id.player_button_shuffle)
    public void onShuffleButtonClicked() {
        Timber.d("onShuffleButtonClicked(): ");

        if (isContinousPlayEnabled) {
            buttonShuffle.setImageDrawable(getResources().getDrawable(R.drawable.button_shuffle));
            isContinousPlayEnabled = false;
        } else {
            buttonShuffle.setImageDrawable(getResources().getDrawable(R.drawable.button_shuffle_selected));
            isContinousPlayEnabled = true;
        }
    }

    // Previous

    @OnClick(R.id.player_button_previous)
    public void onPreviousButtonClicked() {
        Timber.d("onPreviousButtonClicked(): " + trackList.size());
        
        int currentTrackIndex = trackList.indexOf(currentTrack);
        
        // If list is empty or the current track is the first in the list: 
        // just return ...
        if (! (!trackList.isEmpty() && !(currentTrackIndex == 0))) {
            Timber.d("onPreviousButtonClicked(): is first track ");
            return;
        }

        loadTrack(trackList.get(currentTrackIndex - 1));
        shallPlayImmediately = true;
    }

    // Next

    @OnClick(R.id.player_button_next)
    public void onNextButtonClicked() {
        Timber.d("onNextButtonClicked(): " + trackList.size());

        int currentTrackIndex = trackList.indexOf(currentTrack);

        // If list is empty or the current track is the last in the list: 
        // just return ...
        if (! (!trackList.isEmpty() && !(currentTrackIndex == trackList.size() - 1))) {
            Timber.d("onNextButtonClicked(): is last track");
            return;
        }

        loadTrack(trackList.get(currentTrackIndex + 1));
        shallPlayImmediately = true;
    }



    //
    //  OnSeekbarChangeListener
    //

 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 		Timber.d("onProgressChanged(): SB ID: " + seekBar.getId());
		
		if (seekBar.getId() == R.id.pitch_control) {

			int diff = progress - 50;
            Timber.d("onProgressChanged(): pitch bar value:" + diff);
            
			// float currPitch = (float) (((float)diff / ((float)100) * 2.0f * pitchFaktor));
            String pre = diff > 0 ? "+":"";
			if (diff == 0)
                pre = "   ";
            if (diff < 10 && diff > 0)
                pre = "  +";
            if (diff > -10 && diff < 0)
                pre = "  ";

            String pitch = String.format(" " + pre + "%02.01f", (float) diff);
			
			// player.setPlaybackRate(1000 + (diff * pitchRange));
			pitchControlValue.setText(pitch + " %");
		}
 	}

 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 		
 	}

 	@Override
 	public void onStopTrackingTouch(SeekBar arg0) {
 		// TODO Auto-generated method stub
 		
 	}

    @Override
    public void onPrepared(MediaPlayer mp) {
        Timber.d("onPrepared():");
        
        isPrepared = true;
        
        int duration = mp.getDuration();
        
        textTotalTime.setText("total: " + milisecondsToTimeFormattedString(duration));
        textDynamicTime.setText("remain: " + milisecondsToTimeFormattedString(duration));
        
        updateRunnable = new UpdateRunnable();
        updateRunnable.setDuration(duration);
        updateHandler.post(updateRunnable);
        
        if (shallPlayImmediately) {
            onPlayButtonClicked();
            shallPlayImmediately = false;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Timber.d("onError():");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Timber.d("onCompletion(): tracklist size: " + trackList.size());

        int currentTrackIndex = trackList.indexOf(currentTrack);
        Timber.d("onCompletion(): current track is: " + currentTrackIndex);

        // If list is empty or the current track is the last in the list 
        // just cleanup the player, reset the update handler - and return ...
        if (!(!trackList.isEmpty() && !(currentTrackIndex == trackList.size() - 1))) {
            Timber.d("onCompletion(): tracklist is empty");

            cleanupPlayer();
            resetUpdateTimer();
            return;
        }
       
        // If continous play is enabled play the next track (if there is any)
        // else cleanup etc.
       
        if (isContinousPlayEnabled && !trackList.isEmpty()) {
            loadTrack(trackList.get(currentTrackIndex + 1));
            shallPlayImmediately = true;
        } else {
            cleanupPlayer();
            resetUpdateTimer();
        }
    }
    
    //
    //  OnItemSelectedListener
    //
    
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Timber.d("onItemSelected(): " + position);

        String item = (String) parent.getItemAtPosition(position);

        /*
        TextView selected = ((TextView) parent.getChildAt(0));
        selected.setTextSize(getResources().getDimension(R.dimen.font_size_version_title));
        selected.setTypeface(Typeface.DEFAULT_BOLD);
        selected.setTextColor(getResources().getColor(R.color.q_orange));
        */
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    
    
    private String milisecondsToTimeFormattedString(int millis) {
        return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    
    private class UpdateRunnable implements Runnable {
        
        int duration;

        public UpdateRunnable() {
            
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }
        
        @Override
        public void run() {
                        
            if (isPlaying) {
                textDynamicTime.setText("remain: " +
                    (milisecondsToTimeFormattedString(duration - player.getCurrentPosition())));
            }
            updateHandler.postDelayed(this, 100);
        }
    };
}
