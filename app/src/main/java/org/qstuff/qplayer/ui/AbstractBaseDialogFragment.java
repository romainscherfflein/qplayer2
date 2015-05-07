package org.qstuff.qplayer.ui;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;

import com.negusoft.holoaccent.dialog.DividerPainter;

/**
 * Created by Claus Chierici (chierici@karlmax-berlin.com) on 5/7/15
 * for Karlmax Berlin GmbH & Co. KG
 * <p/>
 * Copyright (C) 2014 Karlmax Berlin GmbH & Co. KG, All rights reserved.
 */
public class AbstractBaseDialogFragment extends DialogFragment {

    ///////////////////////////////////////////////////////////////////////////
    // Fragment lifecycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onStart() {
        super.onStart();

        // Set the correct accent color to the divider
        Dialog d = getDialog();
        if (d != null)
            new DividerPainter(getActivity()).paint(d.getWindow());
    }
}
