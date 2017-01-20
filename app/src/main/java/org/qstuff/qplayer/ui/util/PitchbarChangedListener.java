package org.qstuff.qplayer.ui.util;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.SeekBar;
import android.widget.TextView;

import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.QPlayerEventListener;
import org.qstuff.qplayer.controller.QPlayerWrapper;

import timber.log.Timber;


public class PitchbarChangedListener 
    implements SeekBar.OnSeekBarChangeListener {

    private QPlayerEventListener qPlayerEventListener;

    public PitchbarChangedListener(@NonNull QPlayerEventListener qPlayerEventListener) {
        
        this.qPlayerEventListener = qPlayerEventListener;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Timber.d("onProgressChanged():");

        if (seekBar.getId() == R.id.pitch_control) {
            qPlayerEventListener.onPitchControllChanged(progress);
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
