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
import org.qstuff.qplayer.events.NewPlayListEvent;
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
public class NewPlayListDialogFragment extends AbstractBaseDialogFragment {

    @Inject Bus bus;
    
    @InjectView(R.id.new_playlist_dialog_fragment_edittext) EditText editText;
    
    private PlayList playList;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.new_playlist_dialog_fragment, container, false);
        
        ButterKnife.inject(this, v);
        getDialog().setTitle(getString(R.string.new_playlist_dialog_title));
        
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
    
    @OnClick(R.id.dialog_ok)
    public void onDialogOk(View view) {
        Timber.d("onDialogOk():");

        if (editText.getText().length() > 0)
            getTextInputData();
        else
            Toast.makeText(this.getActivity(), 
                getString(R.string.dialog_must_enter_name),
                Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.dialog_cancel)
    protected void onDialogCancel(View view) {
        Timber.d("onDialogCancel():");
        getDialog().dismiss();
    }
    
    //
    // Private
    //
    
    private void getTextInputData() {
        Timber.d("getInputData(): " + editText.getText().toString());
        
        bus.post(new NewPlayListEvent(editText.getText().toString(), false));
        getDialog().dismiss();
    }
}
