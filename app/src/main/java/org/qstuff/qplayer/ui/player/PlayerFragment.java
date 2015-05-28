package org.qstuff.qplayer.ui.player;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.qstuff.qplayer.AbstractBaseFragment;
import org.qstuff.qplayer.Constants;
import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.data.PlayList;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.FileSelectedEvent;
import org.qstuff.qplayer.events.NewPlayListEvent;
import org.qstuff.qplayer.ui.util.VerticalSeekBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 
 * @author claus chierici (cc@codeyard.de)
 *
 */
public class PlayerFragment extends AbstractBaseFragment
	implements OnSeekBarChangeListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {


    @Inject Bus bus;
    @Inject PlayListController playListController;

    @InjectView(R.id.pitch_control)             VerticalSeekBar pitchControl;
    @InjectView(R.id.player_button_previous)    ImageButton     buttonPrevious;
    @InjectView(R.id.player_button_play)        ImageButton     buttonPlay;
    @InjectView(R.id.player_button_next)        ImageButton     buttonNext;
    @InjectView(R.id.player_button_shuffle)     ImageButton     buttonShuffle;
    @InjectView(R.id.player_button_repeat)      ImageButton     buttonRepeat;
    @InjectView(R.id.player_button_fullscreen)  ImageButton     buttonFullscreen;
    @InjectView(R.id.player_text_current_track) TextView        textCurrentTrack;


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
        buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.av_play));
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
        editor.commit();
    }

    private void savePlayingState() {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.PREFS_KEY_PLAYING_STATE, player.isPlaying());
        editor.commit();
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
            buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.av_play));
        } else {
            player.start();
            buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.av_pause));
        }
    }

    //
    //  OnSeekbarChangeListener
    //

 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 		Timber.d("onProgressChanged(): SB ID: " + seekBar.getId());
		
		if (seekBar.getId() == R.id.pitch_control) {
            Timber.d("onProgressChanged(): pitch bar value:" + progress);

			int diff = progress - 50;
			
			// float currPitch = (float) (((float)diff / ((float)pitchRange) * 2.0f * pitchFaktor));
			//String pitch = String.format("%.02f", currPitch);
			//Log.i(TAG, "onProgressChanged(): current: " + pitch);
			
			// player.setPlaybackRate(1000 + (diff * pitchRange));
			//pitchCurrent.setText(pitch+"%");
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
}
