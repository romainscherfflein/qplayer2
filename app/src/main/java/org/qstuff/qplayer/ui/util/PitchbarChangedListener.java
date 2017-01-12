package org.qstuff.qplayer.ui.util;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.SeekBar;
import android.widget.TextView;

import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.QPlayerWrapper;

import timber.log.Timber;


public class PitchbarChangedListener 
    implements SeekBar.OnSeekBarChangeListener {

    private TextView       pitchControlValue;
    private QPlayerWrapper player;

    public PitchbarChangedListener(@NonNull TextView pitchControlValue, 
                                   @Nullable QPlayerWrapper player) {
        
        this.pitchControlValue = pitchControlValue;
        this.player = player;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Timber.d("onProgressChanged():");

        if (seekBar.getId() == R.id.pitch_control) {

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
                player.setSpeed(1.0f + Math.abs(diff/100));
            }
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
