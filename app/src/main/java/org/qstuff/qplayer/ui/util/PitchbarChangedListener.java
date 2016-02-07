package org.qstuff.qplayer.ui.util;

import android.widget.SeekBar;
import android.widget.TextView;

import org.qstuff.qplayer.R;

import butterknife.InjectView;
import timber.log.Timber;

/**
 * Created by Claus Chierici (chierici@karlmax-berlin.com) on 2/6/16
 * for Karlmax Berlin GmbH & Co. KG
 * <p/>
 * Copyright (C) 2013, 2014, 2015 Karlmax Berlin GmbH & Co. KG,
 * All rights reserved.
 */
public class PitchbarChangedListener 
    implements SeekBar.OnSeekBarChangeListener {


    TextView pitchControlValue;

    public PitchbarChangedListener(TextView pitchControlValue) {
        this.pitchControlValue = pitchControlValue;
    }

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

}
