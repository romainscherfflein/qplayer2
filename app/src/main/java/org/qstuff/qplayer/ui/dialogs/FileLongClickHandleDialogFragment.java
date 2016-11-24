package org.qstuff.qplayer.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.squareup.otto.Bus;

import org.qstuff.qplayer.R;
import org.qstuff.qplayer.data.Track;

import java.io.File;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class FileLongClickHandleDialogFragment extends AbstractBaseDialogFragment {
    
    private static final String ARG_TRACK = "ARG_TRACK";
    private Track track;
    
    public static FileLongClickHandleDialogFragment newInstance(Track track) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_TRACK, track);

        FileLongClickHandleDialogFragment frg = new FileLongClickHandleDialogFragment();
        frg.setArguments(args);

        return frg;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        track = (Track) getArguments().getSerializable(ARG_TRACK);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Dialog dialog = builder
            .setCancelable(false)
            .setTitle(getString(R.string.file_short_click_handle_dialog_title))
            .setNegativeButton(getString(R.string.dialog_add_to_queue), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    onAddToQueueButtonClicked(track);
                    dismiss();
                }
            })
            .setNeutralButton(getString(R.string.dialog_play), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    
                    onPlayNowButtonClicked(track);
                    dismiss();
                }
            })
            .setPositiveButton(getString(R.string.dialog_add_to_playlist), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onAddToPlaylistButtonClicked();
                    dismiss();
                }
            })
            .create();

        ButterKnife.inject(dialog);
        
        return dialog;
    }
}
