package org.qstuff.qplayer.ui.player;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.qstuff.qplayer.AbstractBaseFragment;
import org.qstuff.qplayer.Constants;
import org.qstuff.qplayer.QPlayerApplication;
import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.ExoPlayerImpl;
import org.qstuff.qplayer.controller.MediaPlayerImpl;
import org.qstuff.qplayer.controller.NativePlayerImpl;
import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.controller.QPlayerEventListener;
import org.qstuff.qplayer.controller.QPlayerWrapper;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.PlayQueueUpdateEvent;
import org.qstuff.qplayer.events.TrackSelectedFromQueueEvent;
import org.qstuff.qplayer.events.TrackSelectedToPlayEvent;
import org.qstuff.qplayer.ui.util.PitchbarChangedListener;
import org.qstuff.qplayer.ui.util.VerticalSeekBar;
import org.qstuff.qplayer.util.TrackUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
               QPlayerEventListener {
    
    private static final String ARG_PLAYER_TYPE = "ARG_PLAYER_TYPE";
    
    private static final int PLAYER_TYPE_ANDROID = 0;
    private static final int PLAYER_TYPE_EXO     = 1;
    private static final int PLAYER_TYPE_NATIVE  = 2;


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


    @Inject Bus bus;
    @Inject PlayListController playListController;


    private QPlayerWrapper player;
    
    private boolean          isPrepared;
    private boolean          isPlaying;
    private boolean          shallPlayImmediately;

    private ArrayList<Track> trackList;
    private Track            currentTrack = null;

    private Timer            updateTimer;
    private boolean          updateTaskRunning;

    private boolean          isContinousPlayEnabled = false; // TODO: need a button to toggle
    private boolean          isRepeatAllEnabled;
    private boolean          isRepeatOneEnabled;
    private boolean          isShufflePlayEnabled;
    private boolean          showRemainingTime;
    
    private int              playerType;


    public static PlayerFragment newInstance(int playerType) {

        PlayerFragment myFragment = new PlayerFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_PLAYER_TYPE, playerType);
        myFragment.setArguments(args);

        return myFragment;
    }
    
    //
    // Fragment Lifecycle
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate():");

        bus.register(this);
        isPrepared = false;

        Bundle args = getArguments();
        playerType = args.getInt(ARG_PLAYER_TYPE, -1);
        createPlayer();
        
        trackList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Timber.d("onCreateView():");

        View v = inflater.inflate(R.layout.player_fragment, container, false);
        ButterKnife.inject(this, v);

        playerWaveform.setOnSeekBarChangeListener(new WaveformChangedListener());
        
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
            
            textVersion.setText(appName + " " 
                + pInfo.versionName 
                + " (" + pInfo.versionCode + ")"
                + "   DPI: " + QPlayerApplication.getInstance().getDPI());
        
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        
        return v;
    }
        
    @Override
    public void onResume() {
        super.onResume(); 
        Timber.d("onResume()");

        trackList             = restoreTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST);
        isPlaying             = restoreState(Constants.PREFS_KEY_PLAYER_IS_PLAYING);
        int currentTrackIndex = restoreIndex(Constants.PREFS_KEY_PLAYER_CURRENT_INDEX);       
        
        if (trackList == null) {
            trackList = new ArrayList<>();
        }
        
        if (!player.isPlaying()
            &! trackList.isEmpty()
            && currentTrackIndex >= 0) {
            loadTrack(currentTrackIndex);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        saveTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST, trackList);
        saveState(Constants.PREFS_KEY_PLAYER_IS_PLAYING, isPlaying);
        saveIndex(Constants.PREFS_KEY_PLAYER_CURRENT_INDEX, trackList.indexOf(currentTrack));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // cleanupPlayer();
        bus.unregister(this);
    }

    private void createPlayer() {
        
        switch (playerType) {
            case PLAYER_TYPE_ANDROID:
                player = MediaPlayerImpl.getInstance();
                player.create(this, getContext());
                break;
            case PLAYER_TYPE_NATIVE:
                player = NativePlayerImpl.getInstance();
                player.create(this, getContext());
                break;
            case PLAYER_TYPE_EXO:
                player = ExoPlayerImpl.getInstance();
                player.create(this, getContext());
                break;
            default:
                player = null;
                break;
        }        
    }
    
    
    //
    // Event Subscriptions
    //
        
    @Subscribe
    public void onTrackSelectedFromQueueEvent(TrackSelectedFromQueueEvent event) {
        Timber.d("onTrackSelectedFromQueueEvent(): %s", event.track.getName());

        int trackIndex = TrackUtils.trackListContainsTrack(trackList, event.track);
        
        if (trackIndex < 0) {
            trackList.add(0, event.track);
            trackIndex = 0;
        }

        shallPlayImmediately = true;
        loadTrack(trackIndex);
    }

    @Subscribe
    public void onPlayQueueUpdateEvent(PlayQueueUpdateEvent event) {
        Timber.d("onPlayQueueUpdateEvent(): size: %s", event.tracks.size());
        Timber.d("onPlayQueueUpdateEvent(): play: %s", event.indexTrackImmediatePlay);


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

        if (player != null && player.isPlaying()) {
            shallPlayImmediately = player.isPlaying();
        }
        if (event.indexTrackImmediatePlay >= 0)
            shallPlayImmediately = true;

        loadTrack(event.indexTrackImmediatePlay < 0 ? nextIndex : event.indexTrackImmediatePlay);
        saveTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST, trackList);
    }

    //
    // Private
    //

    private void loadTrack(int index) {
        Timber.d("loadTrack(): index: %d", index);

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

    @SuppressLint("SetTextI18n")
    private void cleanupPlayer() {
        Timber.d("cleanupPlayer():");

        if (showRemainingTime) {
            textDynamicTime.setText("remain: " + TrackUtils.milisecondsToTimeFormattedString(0));
        } else {
            textDynamicTime.setText("current: " + TrackUtils.milisecondsToTimeFormattedString(0));
        }
        
        textTotalTime.setText("total: " + TrackUtils.milisecondsToTimeFormattedString(0));
        
        textCurrentTrack.setText(getString(R.string.player_no_track_loaded));
        
        if (player != null) {
            
            resetUpdateTimer();
            player.destroy();
            player = null;

            isPrepared = false;
            isPlaying = false;
        }
    }

    private void resetUpdateTimer() {
        
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
        updateTaskRunning = false;
    }
    
    private void startUpdateTimer() {
        Timber.d("startUpdateTimer(): ");

        if (!isPrepared) return;

        final int total = player.getDuration();
        updateTimer = new Timer();
        updateTaskRunning = true;
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateWaveformUI(total, player.getCurrentPosition());
            }
        }, 0, 1000);
    }
 
    private void updateWaveformUI(final int total, final int currentPosition) {
        Timber.v("updateWaveformUI(): total %d, current %d", total, currentPosition);

        getActivity().runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {

                if (isPlaying) {
                    if (showRemainingTime) {
                        textDynamicTime.setText("remain: " + (TrackUtils.milisecondsToTimeFormattedString(total - currentPosition)));
                    } else {
                        textDynamicTime.setText("current: " + (TrackUtils.milisecondsToTimeFormattedString(currentPosition)));
                    }
                }

                int progress = TrackUtils.getProgressPercentage(currentPosition, total);
                playerWaveform.setProgress(progress);
            }
        });
    }
    
    private void preparePlayer(File file) {
        Timber.d("preparePlayer(): %s", file.getAbsolutePath());
        
        isPrepared = false;
        
        if (player == null) {
            createPlayer();
        }

        player.loadTrackSync(file);

        pitchControl.setOnSeekBarChangeListener(
            new PitchbarChangedListener(this));
    }

    @SuppressLint("SetTextI18n")
    private void onPrepared() {
        Timber.d("onPrepared(): play now: %s", shallPlayImmediately);

        isPrepared = true;

        int duration = player.getDuration();

        textTotalTime.setText("total: " + TrackUtils.milisecondsToTimeFormattedString(duration));
        
        if (showRemainingTime) {
            textDynamicTime.setText("remain: " + TrackUtils.milisecondsToTimeFormattedString(duration));
        } else {
            textDynamicTime.setText("current: " + TrackUtils.milisecondsToTimeFormattedString(0));
        }
        
        if (!updateTaskRunning) {
            startUpdateTimer();
        }

        if (shallPlayImmediately) {
            onPlayButtonClicked();
            shallPlayImmediately = false;
        }
    }

    //
    // Click Handlers for Player Buttons
    //

    // Play / Pause
    
    @OnClick(R.id.player_button_play)
    public void onPlayButtonClicked() {
        Timber.d("onPlayButtonClicked(): ");

        togglePlayPause();
    }

    private void togglePlayPause() {
        
        if (isPrepared) {
            if (player.isPlaying()) {
                
                player.pause();
                buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.button_play_selected));
                isPlaying = false;

            } else {
                
                player.play();
                buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.button_pause_selected));
                isPlaying = true;
            }
        }
    }
    
    // Repeat All / Repeat One (disables Shuffle)

    @OnClick(R.id.player_button_repeat)
    public void onRepeatButtonClicked() {
        Timber.d("onRepeatButtonClicked(): all: %s", isRepeatAllEnabled);
        Timber.d("onRepeatButtonClicked(): one: %s", isRepeatOneEnabled);

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
        Timber.d("onPreviousButtonClicked(): %d", trackList.size());
        
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

        shallPlayImmediately = player.isPlaying();
        loadTrack(nextIndex);
    }

    // Next

    @OnClick(R.id.player_button_next)
    public void onNextButtonClicked() {
        Timber.d("onNextButtonClicked(): %d", trackList.size());

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

        shallPlayImmediately = player.isPlaying();
        loadTrack(nextIndex);
    }

    // Toggle remain / current time display
    
    @SuppressLint("SetTextI18n")
    @OnClick(R.id.player_text_dynamic_time)
    public void onDynamicTimeTextViewClicked() {
        Timber.d("onDynamicTimeTextViewClicked(): %d", trackList.size());

        showRemainingTime = !showRemainingTime;

        if (!isPlaying) {

            if (showRemainingTime) {
                if (isPrepared) {
                    textDynamicTime.setText("remain: " +
                                                TrackUtils.milisecondsToTimeFormattedString(0));
                } else {
                    textDynamicTime.setText("remain: " +
                                                TrackUtils.milisecondsToTimeFormattedString(
                                                    player.getDuration()));
                }
            } else {
                textDynamicTime.setText("current: " +
                                            TrackUtils.milisecondsToTimeFormattedString(0));
            }
        }
    }

    //
    // QPlayerEventListener
    // 
    
    @Override
    public void onError() {
        Timber.d("onError(): PLAYER reorted ERROR:");
    }

    @Override
    public void onCompletion() {
        Timber.d("onCompletion(): tracklist size: %d", trackList.size());

        int currentTrackIndex = trackList.indexOf(currentTrack);
        int lastIndex = trackList.size() -1;
        int nextIndex = -1;
        
        resetUpdateTimer();
        
        // If list is empty or the current track is the last in the list 
        // just cleanup the player, reset the update handler - and return ...
        if (trackList.isEmpty()) {
            // nothing to do here
        }
        
        // If REPEAT ONE is enabled: play the current track again and again and again and again
        else if (isRepeatOneEnabled) {
            nextIndex = currentTrackIndex;
            shallPlayImmediately = true;
        }
        
        // If REPEAT ALL is enabled play one after the other and repeat when end the is reached
        else if (isRepeatAllEnabled) {
        
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
        
            Random rand = new Random();
            nextIndex = rand.nextInt(trackList.size() -1);
            shallPlayImmediately = true;
        }
        
        // 
        if (nextIndex >= 0) {
            loadTrack(nextIndex);
        } else {
            cleanupPlayer();
            shallPlayImmediately = false;
        }
    }

    @Override
    public void onPitchControllChanged(int progress) {
        Timber.d("onProgressChanged():");

        float diff = progress - 50;

        // float currPitch = (float) (((float)diff / ((float)100) * 2.0f * pitchFaktor));
        String pre = diff > 0 ? "+":"";
        if (diff == 0)
            pre = "   ";
        if (diff < 10 && diff > 0)
            pre = "  +";
        if (diff > -10 && diff < 0)
            pre = "  ";

        String pitch = String.format(" " + pre + "%02.01f", diff);

        // player.setPlaybackRate(1000 + (diff * pitchRange));
        pitchControlValue.setText(pitch + " %");

        Timber.d("onProgressChanged(): pitch bar value: %f", diff);
        Timber.d("onProgressChanged(): pitch bar value: %f", 1.0f + Math.abs(diff/100));

        if (player != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            player.setSpeed(1.0f + (diff/100));
        }
    }

    @Override
    public void onPrepared(boolean prepared) {
        Timber.d("onPrepared(): %s", prepared);

        isPrepared = prepared;
        if (isPrepared) {
            onPrepared();
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
        
    //
    // Inner Classes
    //
    
    private class WaveformChangedListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            if (isPrepared) {
                int currentPosition = TrackUtils.progressToTimer(seekBar.getProgress(), 
                    player.getDuration());
                player.seekTo(currentPosition);
            }
        }
    }
}
