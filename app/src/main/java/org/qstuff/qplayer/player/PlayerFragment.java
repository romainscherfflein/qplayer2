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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * 
 * @author claus chierici (cc@codeyard.de)
 *
 */
public class PlayerFragment extends Fragment 
	implements OnSeekBarChangeListener {

    private static final String TAG = "PlayerFragment";

    private VerticalSeekBar seekBarPitch;

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
        
        seekBarPitch = (VerticalSeekBar) v.findViewById(R.id.pitch_bar);
        seekBarPitch.setOnSeekBarChangeListener(this);
        seekBarPitch.setProgress(50);
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
		
		if (seekBar.getId() == R.id.pitch_bar) {
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
