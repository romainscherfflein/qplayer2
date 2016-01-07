package org.qstuff.qplayer.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.negusoft.holoaccent.dialog.DividerPainter;

import org.qstuff.qplayer.QPlayerApplication;
import org.qstuff.qplayer.R;
import org.qstuff.qplayer.ui.content.ChoosePlayListDialogFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;


/**
 * 
 */
public class AbstractBaseDialogFragment extends DialogFragment {

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
    
    @OnClick(R.id.dialog_cancel)
    protected void onDialogCancel(View view) {
        Timber.d("onDialogCancel():");
        getDialog().dismiss();
    }
    
    public void openChoosePlayListDialog() {
        ChoosePlayListDialogFragment dialog = new ChoosePlayListDialogFragment();
        dialog.show(getFragmentManager(), getString(R.string.choose_laylist_dialog_tag));
    }
}
