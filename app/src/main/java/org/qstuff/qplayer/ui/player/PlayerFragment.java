package org.qstuff.qplayer.ui.player;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.qstuff.qplayer.AbstractBaseFragment;
import org.qstuff.qplayer.Constants;
import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.TrackSelectedFromFilesEvent;
import org.qstuff.qplayer.events.PlayQueueUpdateEvent;
import org.qstuff.qplayer.events.TrackSelectedFromQueueEvent;
import org.qstuff.qplayer.events.TrackSelectedToPlayEvent;
import org.qstuff.qplayer.ui.util.HorizontalSeekBar;
import org.qstuff.qplayer.ui.util.VerticalSeekBar;
import org.qstuff.qplayer.util.TrackUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
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
    
    @InjectView(R.id.player_waveform)
    SeekBar playerWaveform;
    
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

    @InjectView(R.id.player_text_version)
    TextView textVersion;
    
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

    private boolean          isContinousPlayEnabled = false; // TODO: need a button to toggle
    private boolean          isRepeatAllEnabled;
    private boolean          isRepeatOneEnabled;
    private boolean          isShufflePlayEnabled;
    private boolean          showRemainingTime;


    //
    // Fragment Lifecycle
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate():");

        bus.register(this);
        
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

        try {
            PackageManager packageManager = getActivity().getPackageManager();
            String packageName = getActivity().getPackageName();
            PackageInfo pInfo = packageManager.getPackageInfo(packageName, 0);
            String appName = (String) packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            
            textVersion.setText(appName + " " + pInfo.versionName + " (" + pInfo.versionCode + ")");
        
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        
        return v;
    }
        
    @Override
    public void onResume() {
        super.onResume(); 
        Timber.d("onResume()");
        // bus.register(this);

        trackList    = restoreTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST);
        isPlaying    = restoreState(Constants.PREFS_KEY_PLAYER_IS_PLAYING);
        int currentTrackIndex = restoreIndex(Constants.PREFS_KEY_PLAYER_CURRENT_INDEX);       
        
        if (trackList == null)
            trackList = new ArrayList<>();
        
        if (!player.isPlaying()
            &! trackList.isEmpty()
            && currentTrackIndex >= 0)
            loadTrack(currentTrackIndex);
    }

    @Override
    public void onPause() {
        super.onPause();

        saveTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST, trackList);
        saveState(Constants.PREFS_KEY_PLAYER_IS_PLAYING, player.isPlaying());
        saveIndex(Constants.PREFS_KEY_PLAYER_CURRENT_INDEX, trackList.indexOf(currentTrack));

        // bus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        cleanupPlayer();
        bus.unregister(this);
    }

    //
    // Event Subscriptions
    //

    @Subscribe
    public void onTrackSelectedFromFilesEvent(TrackSelectedFromFilesEvent event) {
        Timber.d("onTrackSelectedFromFilesEvent(): " + event.track.getName());
        
        int trackIndex = TrackUtils.trackListContainsTrack(trackList, event.track);
        if (trackIndex < 0) {
            trackList.add(0, event.track);
            trackIndex = 0;
        }

        saveTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST, trackList);
        
        loadTrack(trackIndex);
        shallPlayImmediately = false;
    }

    @Subscribe
    public void onTrackSelectedFromQueueEvent(TrackSelectedFromQueueEvent event) {
        Timber.d("onTrackSelectedFromQueueEvent(): " + event.track.getName());

        int trackIndex = TrackUtils.trackListContainsTrack(trackList, event.track);
        
        if (trackIndex < 0) {
            trackList.add(0, event.track);
            trackIndex = 0;
        }

        loadTrack(trackIndex);
        shallPlayImmediately = false;
    }

    @Subscribe
    public void onPlayQueueUpdateEvent(PlayQueueUpdateEvent event) {
        Timber.d("onPlayQueueUpdateEvent(): size: " + event.tracks.size());

        int nextIndex = -1;
        
        // Empty list we can bail out
        if (event.tracks.isEmpty()) {
            trackList = event.tracks;
        // should swap the lists: proceed at index 0
        } else if (event.forceSwap) {
            trackList = event.tracks;
            nextIndex = 0;
        
        // TODO: add append / prepend
        } else if (event.append) {
            //
        } else if (event.prepend) {
            //
        }
        
        loadTrack(nextIndex);
        shallPlayImmediately = false;
        saveTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST, trackList);
    }
    
    //
    // Private
    //

    private void loadTrack(int index) {
        Timber.d("loadTrack(): index: " + index);

        cleanupPlayer();
        
        if (index < 0) {
            currentTrack = new Track();
            return;
        }
        
        currentTrack = trackList.get(index);
        preparePlayer(new File(currentTrack.getUri()));
        
        bus.post(new TrackSelectedToPlayEvent(index, currentTrack));
        
        textCurrentTrack.setText(currentTrack.getName().replaceFirst("[.][^.]+$", ""));
        buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.button_play_selected));
        saveTrack(Constants.PREFS_KEY_PLAYER_CURRENT_TRACK, currentTrack);
        saveIndex(Constants.PREFS_KEY_PLAYER_CURRENT_INDEX, index);
    }

    private void cleanupPlayer() {
        Timber.d("cleanupPlayer():");

        if (showRemainingTime)
            textDynamicTime.setText("remain: " + milisecondsToTimeFormattedString(0));
        else
            textDynamicTime.setText("current: " + milisecondsToTimeFormattedString(0));

        textTotalTime.setText("total: " + milisecondsToTimeFormattedString(0));
        textCurrentTrack.setText(getString(R.string.player_no_track_loaded));
        
        if (player != null) {
            resetUpdateTimer();
            player.stop();
            player.release();
            player = null;
            isPrepared = false;
            isPlaying = false;
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

    //
    // Click Handlers for Player Buttons
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

    // Repeat All / Repeat One (disables Shuffle)

    @OnClick(R.id.player_button_repeat)
    public void onRepeatButtonClicked() {
        Timber.d("onRepeatButtonClicked(): all: " + isRepeatAllEnabled);
        Timber.d("onRepeatButtonClicked(): one: " + isRepeatOneEnabled);

        if (isRepeatAllEnabled) {
            
            if (isRepeatOneEnabled) {
                buttonRepeat.setImageDrawable(getResources().getDrawable(R.drawable.button_loop));
                isRepeatAllEnabled = false;
                isRepeatOneEnabled = false;
            } else {
                buttonRepeat.setImageDrawable(getResources().getDrawable(R.drawable.button_loop1_selected));
                isRepeatAllEnabled = true;
                isRepeatOneEnabled = true;
            }
        
        } else {
            buttonRepeat.setImageDrawable(getResources().getDrawable(R.drawable.button_loop_selected));
            isRepeatAllEnabled = true;
            isRepeatOneEnabled = false;
            
            buttonShuffle.setImageDrawable(getResources().getDrawable(R.drawable.button_shuffle));
            isShufflePlayEnabled = false;
        }
    }

    // Shuffle  (disables Repeat)
    
    @OnClick(R.id.player_button_shuffle)
    public void onShuffleButtonClicked() {
        Timber.d("onShuffleButtonClicked(): ");

        if (isShufflePlayEnabled) {
            buttonShuffle.setImageDrawable(getResources().getDrawable(R.drawable.button_shuffle));
            isShufflePlayEnabled = false;
        } else {
            buttonShuffle.setImageDrawable(getResources().getDrawable(R.drawable.button_shuffle_selected));
            isShufflePlayEnabled = true;

            buttonRepeat.setImageDrawable(getResources().getDrawable(R.drawable.button_loop));
            isRepeatAllEnabled = false;
            isRepeatOneEnabled = false;    
        }
    }

    // Previous

    @OnClick(R.id.player_button_previous)
    public void onPreviousButtonClicked() {
        Timber.d("onPreviousButtonClicked(): " + trackList.size());
        
        int currentTrackIndex = trackList.indexOf(currentTrack);
        int nextIndex;
        
        // If list is empty return;
        if (trackList.isEmpty()) {
            return;        
        }
        // If first track jump back to the end 
        if (currentTrackIndex == 0) {
            nextIndex = trackList.size() - 1 ;
        } else {
            nextIndex = currentTrackIndex -1;
        }

        loadTrack(nextIndex);
        shallPlayImmediately = false;
    }

    // Next

    @OnClick(R.id.player_button_next)
    public void onNextButtonClicked() {
        Timber.d("onNextButtonClicked(): " + trackList.size());

        int currentTrackIndex = trackList.indexOf(currentTrack);
        int nextIndex;

        // If list is empty return;
        if (trackList.isEmpty()) {
            return;
        }
        // If last track jump back to the top
        if (currentTrackIndex == trackList.size() -1) {
            nextIndex = 0 ;
        } else {
            nextIndex = currentTrackIndex + 1;
        }

        loadTrack(nextIndex);
        shallPlayImmediately = false;
    }

    // Toggle remain / current time display
    
    @OnClick(R.id.player_text_dynamic_time)
    public void onDynamicTimeTextViewClicked() {
        Timber.d("onDynamicTimeTextViewClicked(): " + trackList.size());

        if (showRemainingTime)
            showRemainingTime = false;
        else
            showRemainingTime = true;

        if (!isPlaying) {
            if (showRemainingTime)
                textDynamicTime.setText("remain: " + milisecondsToTimeFormattedString(0));
            else
                textDynamicTime.setText("current: " + milisecondsToTimeFormattedString(0));
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
        
        isPrepared = true;
        
        int duration = mp.getDuration();
        
        textTotalTime.setText("total: " + milisecondsToTimeFormattedString(duration));
        if (showRemainingTime)
            textDynamicTime.setText("remain: " + milisecondsToTimeFormattedString(duration));
        else
            textDynamicTime.setText("current: " + milisecondsToTimeFormattedString(0));

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
        Timber.d("onError(): PLAYER reorted ERROR: " + what);
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Timber.d("onCompletion(): tracklist size: " + trackList.size());

        int currentTrackIndex = trackList.indexOf(currentTrack);
        int lastIndex = trackList.size() -1;
        int nextIndex = -1;
        
        Timber.d("onCompletion(): current track is: " + currentTrackIndex);

        // If list is empty or the current track is the last in the list 
        // just cleanup the player, reset the update handler - and return ...
        if (trackList.isEmpty()) {
            Timber.d("onCompletion(): tracklist is empty");
            
            // nothing to do here
        }
        // If REPEAT ONE is enabled: play the current track again and again and again and again
        else if (isRepeatOneEnabled) {
            Timber.d("onCompletion(): REPEAT ONE");
                
            nextIndex = currentTrackIndex;
            shallPlayImmediately = true;
        }
        // If REPEAT ALL is enabled play one after the other and repeat when end the is reached
        else if (isRepeatAllEnabled) {
            Timber.d("onCompletion(): REPEAT ALL");
            
            if (currentTrackIndex == lastIndex) {
                nextIndex = 0;
            } else {
                nextIndex = currentTrackIndex +1;
            }
            shallPlayImmediately = true;
        }
        
        // If SHUFFLE is enabled choose a random track from the tracklist
        // this overrides the selection from random
        if (isShufflePlayEnabled) {
            Timber.d("onCompletion(): SHUFFLE");

            Random rand = new Random();
            nextIndex = rand.nextInt(trackList.size() -1);
            shallPlayImmediately = true;
        }
        
        // 
        if (nextIndex >= 0) {
            Timber.d("onCompletion(): moving to next track: " + nextIndex);
            loadTrack(nextIndex);
        } else {
            Timber.d("onCompletion(): no more next track: ");
            cleanupPlayer();
            resetUpdateTimer();
            shallPlayImmediately = false;
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
                if (showRemainingTime)
                    textDynamicTime.setText("remain: " +
                        (milisecondsToTimeFormattedString(duration - player.getCurrentPosition())));
                else
                    textDynamicTime.setText("current: " +
                        (milisecondsToTimeFormattedString(player.getCurrentPosition())));
            }
            updateHandler.postDelayed(this, 100);
        }
    };
}
