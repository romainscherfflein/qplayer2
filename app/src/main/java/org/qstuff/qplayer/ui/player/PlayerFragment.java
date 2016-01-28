package org.qstuff.qplayer.ui.player;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
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

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.qstuff.qplayer.AbstractBaseFragment;
import org.qstuff.qplayer.Constants;
import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.FileSelectedEvent;
import org.qstuff.qplayer.ui.util.VerticalSeekBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
    private ArrayList<Track> trackList;

    private File             currentTrack = null;
    
    //
    // Fragment Lifecycle
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate():");

        if (player == null)
            player = new MediaPlayer();
        player.reset();
        
        trackList = playListController.getLatestTrackList();
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
                Timber.d("onWheelChanged(): " + arg);

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

        currentTrack = restoreCurrentPlayingTrack();
        isPlaying = restorePlayingState();

        if (currentTrack == null) return;

        if (!player.isPlaying())
            loadAudioFile(currentTrack);
    }

    @Override
    public void onPause() {
        super.onPause();

        saveCurrentSelectedTrack();
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
    public void onAudioFileSelectedEvent(FileSelectedEvent event) {
        Timber.d("onAudioFileSelectedEvent(): " + event.audioFile.getAbsolutePath());
        loadAudioFile(event.audioFile);
    }
    
    //
    // Private
    //

    private void loadAudioFile(File file) {

        cleanupPlayer();
        preparePlayer(file);
        currentTrack = file;

        textCurrentTrack.setText(file.getName());
        buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.button_play_selected));
    }

    private void cleanupPlayer() {
        Timber.d("cleanupPlayer():");

        if (player != null) {
//            resetUpdateTimer();
            player.stop();
            player.release();
            player = null;
            isPrepared = false;
        }
    }

    private void preparePlayer(File file) {
        Timber.d("preparePlayer(): " + file.getAbsolutePath());

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

    private void saveCurrentSelectedTrack() {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREFS_KEY_LAST_PLAYED_TRACK_PATH, currentTrack.getAbsolutePath());
        editor.apply();
    }

    private void savePlayingState() {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.PREFS_KEY_PLAYING_STATE, player.isPlaying());
        editor.apply();
    }

    private File restoreCurrentPlayingTrack() {

        String path = preferences.getString(Constants.PREFS_KEY_LAST_PLAYED_TRACK_PATH, null);

        if (path != null)
            return new File(path);
        else
            return null;
    }

    private boolean restorePlayingState() {
        return preferences.getBoolean(Constants.PREFS_KEY_PLAYING_STATE, false);
    }

    //
    // Click Handlers
    //

    @OnClick(R.id.player_button_play)
    public void onPlayButtonClicked() {
        Timber.d("onPlayButtonClicked(): ");

        if (player.isPlaying()) {
            player.pause();
            buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.button_play_selected));
        } else {
            player.start();
            buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.button_pause_selected));
        }
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
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Timber.d("onError():");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Timber.d("onCompletion():");
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
}
