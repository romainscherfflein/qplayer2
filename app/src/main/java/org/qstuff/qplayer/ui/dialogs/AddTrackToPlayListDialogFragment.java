package org.qstuff.qplayer.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import org.qstuff.qplayer.R;
import org.qstuff.qplayer.ui.dialogs.AbstractBaseDialogFragment;

import butterknife.ButterKnife;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class AddTrackToPlayListDialogFragment extends AbstractBaseDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Dialog dialog = builder
            .setCancelable(false)
            .setTitle(getString(R.string.add_track_to_playlist_dialog_title))
            .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    openChoosePlayListDialog();
                    dismiss();
                }
            })
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .create();

        ButterKnife.inject(dialog);
        
        return dialog;
    }
}
