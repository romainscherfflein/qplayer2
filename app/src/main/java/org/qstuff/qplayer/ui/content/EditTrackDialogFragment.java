package org.qstuff.qplayer.ui.content;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.qstuff.qplayer.Constants;
import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.data.PlayList;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.DeleteTrackFromPlayListEvent;
import org.qstuff.qplayer.events.EditPlayListEvent;
import org.qstuff.qplayer.ui.AbstractBaseDialogFragment;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import timber.log.Timber;

/**
 *
 */
public class EditTrackDialogFragment extends AbstractBaseDialogFragment {

    @Inject Bus bus;

    private Track track;
    private PlayList playList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.edit_track_dialog_fragment, container, false);
        
        ButterKnife.inject(this, v);
                
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(Constants.EXTRA_PLAYLIST)) {
                playList = (PlayList) bundle.getSerializable(Constants.EXTRA_PLAYLIST);
            }
            if (bundle.containsKey(Constants.EXTRA_TRACK)) {
                track = (Track) bundle.getSerializable(Constants.EXTRA_TRACK);
                getDialog().setTitle(getString(R.string.edit_track_dialog_title));
            }
            else
                getDialog().dismiss();
        }
        return v;
    }
        
    //
    // Input Handlers
    //
    
    @OnClick(R.id.dialog_delete)
    public void onDialogDelete(View view) {
        Timber.d("onDialogDelete():");

        bus.post(new DeleteTrackFromPlayListEvent(playList, track));
        getDialog().dismiss();
    }
            
    //
    // Private
    //
    
}
