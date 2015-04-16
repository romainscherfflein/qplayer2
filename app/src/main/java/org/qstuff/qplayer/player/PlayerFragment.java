package org.qstuff.qplayer.player;

import org.qstuff.qplayer.AbstractBaseFragment;
import org.qstuff.qplayer.R;
import org.qstuff.qplayer.events.AudioFileSelectedEvent;
import org.qstuff.qplayer.ui.VerticalSeekBar;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.io.IOException;

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

    private static final String TAG = "PlayerFragment";

    @InjectView(R.id.pitch_control)             VerticalSeekBar pitchControl;
    @InjectView(R.id.player_button_previous)    ImageButton     buttonPrevious;
    @InjectView(R.id.player_button_play)        ImageButton     buttonPlay;
    @InjectView(R.id.player_button_next)        ImageButton     buttonNext;
    @InjectView(R.id.player_button_shuffle)     ImageButton     buttonShuffle;
    @InjectView(R.id.player_button_repeat)      ImageButton     buttonRepeat;
    @InjectView(R.id.player_button_fullscreen)  ImageButton     buttonFullscreen;
    @InjectView(R.id.player_text_current_track) TextView        textCurrentTrack;

    // private QNativeMediaPlayer player;
    private MediaPlayer        player;
    private boolean            isPrepared;


    //
    // Fragment Lifecycle
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate():");

        if (player == null)
            player = new MediaPlayer();
        player.reset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView():");

        View v = inflater.inflate(R.layout.player_fragment, container, false);
        ButterKnife.inject(this, v);

        pitchControl.setOnSeekBarChangeListener(this);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);

        // FIXME:
        try {
            AssetFileDescriptor afd = getActivity().getAssets().openFd("haijaijai.mp3");
            player.reset();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            player.prepare();
            isPrepared = true;

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        textCurrentTrack.setText("Echolozn: haijaijai");
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void onDestroy() {
        cleanupPlayer();
    }

    //
    // Event Subscriptions
    //

    @Subscribe
    public void onAudioFileSelectedEvent(AudioFileSelectedEvent event) {
        Timber.d("onAudioFileSelectedEvent():" + event.audioFile.getAbsolutePath());
        loadNewAudioFile(event.audioFile.getAbsolutePath());
    }

    //
    //
    //

    public void loadNewAudioFile(String uri) {
        Log.d(TAG, "loadNewAudioFile():");

        preparePlayer(uri);
    }

    private void cleanupPlayer() {
        Log.d(TAG, "cleanupPlayer():");

        if (player != null) {
//            resetUpdateTimer();
            player.stop();
            player.release();
            player = null;
            isPrepared = false;
        }
    }

    private void preparePlayer(String uri) {
        Log.d(TAG, "preparePlayer(): " + uri);

        if (player == null) {
            Log.w(TAG, "preparePlayer(): player null !");
            return;
        }

        try {
            player.setDataSource(uri);
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
    // Player Buttons
    //

    @OnClick(R.id.player_button_play)
    public void playButtonClicked() {
        Log.d(TAG, "playButtonClicked(): ");

        if (!isPrepared) return;

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
 		Log.d(TAG, "onProgressChanged(): SB ID: " + seekBar.getId());
		
		if (seekBar.getId() == R.id.pitch_control) {
			Log.i(TAG, "onProgressChanged(): pitch bar value:" + progress);

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
        Log.d(TAG, "onPrepared():");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "onError():");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion():");
    }
}
