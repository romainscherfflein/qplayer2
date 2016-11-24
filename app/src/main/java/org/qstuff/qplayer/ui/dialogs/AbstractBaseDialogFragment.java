package org.qstuff.qplayer.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.negusoft.holoaccent.dialog.DividerPainter;
import com.squareup.otto.Bus;

import org.qstuff.qplayer.QPlayerApplication;
import org.qstuff.qplayer.R;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.TrackSelectedFromFilesEvent;

import javax.inject.Inject;


/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class AbstractBaseDialogFragment extends DialogFragment {

    @Inject Bus bus;

    ///////////////////////////////////////////////////////////////////////////
    // Fragment lifecycle
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((QPlayerApplication) getActivity().getApplication()).inject(this);
    }
    
    @Override
    public void onStart() {
        super.onStart();

        // Set the correct accent color to the divider
        Dialog d = getDialog();
        if (d != null)
            new DividerPainter(getActivity()).paint(d.getWindow());
    }
        
    public void onAddToPlaylistButtonClicked() {
        ChoosePlayListDialogFragment dialog = new ChoosePlayListDialogFragment();
        dialog.show(getFragmentManager(), getString(R.string.choose_laylist_dialog_tag));
    }
    
    public void onPlayNowButtonClicked(Track track) {
        bus.post(new TrackSelectedFromFilesEvent(track, true));
    }

    public void onAddToQueueButtonClicked(Track track) {
        bus.post(new TrackSelectedFromFilesEvent(track, false));
    }
}
