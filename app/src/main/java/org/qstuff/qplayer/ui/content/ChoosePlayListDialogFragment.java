package org.qstuff.qplayer.ui.content;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import org.qstuff.qplayer.R;
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
public class ChoosePlayListDialogFragment extends AbstractBaseDialogFragment {

    @Inject Bus bus;

    @InjectView(R.id.edittext_new_playlist_name) EditText editText;
    
    private PlayListIndexerArrayAdapter playListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.choose_playlist_dialog_fragment, container, false);
        
        ButterKnife.inject(this, v);
        getDialog().setTitle(getString(R.string.choose_playlist_dialog_title));
        return v;
    }
        
    //
    // Input Handlers
    //
    
    @OnEditorAction(R.id.edittext_new_playlist_name)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        } else if (actionId == EditorInfo.IME_ACTION_SEARCH
            || event == null
            || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            getTextInputData();
            getDialog().dismiss();
            return true;
        }
        return false;
    }    
    
    @OnClick(R.id.dialog_ok)
    public void onDialogOk(View view) {
        Timber.d("onDialogOk():");

        if (editText.getText().length() > 0)
            getTextInputData();
        
    }

    @OnClick(R.id.dialog_cancel)
    public void onDialogCancel(View view) {
        Timber.d("onDialogCancel():");
        getDialog().dismiss();
    }
        
    //
    // Private
    //
    
    private void getTextInputData() {
        Timber.d("getInputData(): " + editText.getText());
        
        bus.post(new NewPlayListEvent(editText.getText().toString(), true));
        getDialog().dismiss();
    }
}
