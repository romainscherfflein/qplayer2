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
public class EditPlayListDialogFragment extends AbstractBaseDialogFragment {

    @Inject Bus bus;
    @Inject PlayListController playListController;
    
    @InjectView(R.id.edit_playlist_dialog_fragment_edittext) EditText editText;
    
    private PlayList playList;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.edit_playlist_dialog_fragment, container, false);
        
        ButterKnife.inject(this, v);
                
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(Constants.EXTRA_PLAYLIST)) {
                playList = (PlayList) bundle.getSerializable(Constants.EXTRA_PLAYLIST);
                getDialog().setTitle(getString(R.string.edit_playlist_dialog_title )
                    + "\n  " + playList.getName());
            }
            else
                getDialog().dismiss();
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }
    
    //
    // Input Handlers
    //
    
    @OnEditorAction(R.id.edit_playlist_dialog_fragment_edittext)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        } else if (actionId == EditorInfo.IME_ACTION_SEARCH
            || event == null
            || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            getTextInputData();
            return true;
        }
        return false;
    }

    @OnClick(R.id.dialog_delete)
    public void onDialogDelete(View view) {
        Timber.d("onDialogDelete():");

        bus.post(new EditPlayListEvent(playList, null, false, true));
        getDialog().dismiss();
    }
    
    @OnClick(R.id.dialog_rename)
    public void onDialogRename(View view) {
        Timber.d("onDialogRename():");

        if (editText.getText().length() > 0)
            getTextInputData();
        else
            Toast.makeText(this.getActivity(), 
                getString(R.string.dialog_must_enter_name),
                Toast.LENGTH_SHORT).show();
    }
   
        
    //
    // Private
    //
    
    private void getTextInputData() {
        Timber.d("getInputData(): " + editText.getText());
        
        bus.post(new EditPlayListEvent(playList, editText.getText().toString(), true, false));
        getDialog().dismiss();
    }
}
