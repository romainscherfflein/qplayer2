package org.qstuff.qplayer.player;

import org.qstuff.qplayer.R;
import org.qstuff.qplayer.ui.VerticalSeekBar;

import android.app.Activity;
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

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 
 * @author claus chierici (cc@codeyard.de)
 *
 */
public class PlayerFragment extends Fragment 
	implements OnSeekBarChangeListener {

    private static final String TAG = "PlayerFragment";

    @InjectView(R.id.pitch_control)             VerticalSeekBar pitchControl;
    @InjectView(R.id.player_button_previous)    ImageButton buttonPrevious;
    @InjectView(R.id.player_button_play)        ImageButton buttonPlay;
    @InjectView(R.id.player_button_next)        ImageButton buttonNext;
    @InjectView(R.id.player_button_shuffle)     ImageButton buttonShuffle;
    @InjectView(R.id.player_button_repeat)      ImageButton buttonRepeat;
    @InjectView(R.id.player_button_fullscreen)  ImageButton buttonFullscreen;
    @InjectView(R.id.player_text_current_track) TextView textCurrentTrack;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);    //To change body of overridden methods use File | Settings | File Templates.
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
        
        View v = inflater.inflate(R.layout.player_fragment, container, false);
        ButterKnife.inject(this, v);

        pitchControl.setOnSeekBarChangeListener(this);
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
    }
    
    /////////////////////////////////////////////////////////////////////
    // OnSeekbarChangeListener
    
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 		Log.d(TAG, "onProgressChanged(): SB ID: " + seekBar.getId());
		
		if (seekBar.getId() == R.id.pitch_control) {
			Log.i(TAG, "onProgressChanged(): pitch bar value:" + progress);

			int diff = progress - 50;
			
			// float currPitch = (float) (((float)diff / ((float)pitchRange) * 2.0f * pitchFaktor));
			//String pitch = String.format("%.02f", currPitch);
			//Log.i(TAG, "onProgressChanged(): current: " + pitch);
			
			//mediaPlayer.setPlaybackRate(1000 + (diff * pitchRange));
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
}
