package org.qstuff.qplayer.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import org.qstuff.qplayer.R;

import butterknife.ButterKnife;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class FileShortClickHandleDialogFragment extends AbstractBaseDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Dialog dialog = builder
            .setCancelable(false)
            .setTitle(getString(R.string.file_short_click_handle_dialog_title))
            .setNegativeButton(getString(R.string.dialog_add_to_queue), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    // Add to queue

                    dismiss();
                }
            })
            .setNeutralButton(getString(R.string.dialog_play), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    
                    // Add to queue
                    
                    dismiss();
                }
            })
            .setPositiveButton(getString(R.string.dialog_add_to_playlist), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    openChoosePlayListDialog();
                    dismiss();
                }
            })
            .create();

        ButterKnife.inject(dialog);
        
        return dialog;
    }
}
