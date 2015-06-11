package org.qstuff.qplayer.ui.content;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.data.PlayList;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.AddTrackToPlayListEvent;
import org.qstuff.qplayer.events.NewPlayListEvent;
import org.qstuff.qplayer.ui.AbstractBaseDialogFragment;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import timber.log.Timber;

/**
 *
 */
public class ChoosePlayListDialogFragment extends AbstractBaseDialogFragment {

    @Inject Bus bus;
    @Inject PlayListController playListController;
    
    @InjectView(R.id.choose_dialog_fragment_edittext) EditText editText;
    @InjectView(R.id.choose_dialog_fragment_listview) ListView listView;
    
    private PlayListIndexerArrayAdapter<String> playListAdapter;
    private ArrayList<PlayList>                 playLists;
    private boolean                             isPlayListList;
    private Track                               selectedTrack;

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.choose_playlist_dialog_fragment, container, false);
        
        ButterKnife.inject(this, v);
        getDialog().setTitle(getString(R.string.choose_playlist_dialog_title));
        
        playLists = playListController.getPlayLists();
        isPlayListList = true;
        
        playListAdapter = new PlayListIndexerArrayAdapter<>(
            getActivity(),
            R.layout.playlist_item,
            R.id.playlist_item_text,
            playListController.getPlayListNames(),
            isPlayListList);
        
        listView.setAdapter(playListAdapter);
        
        return v;
    }
        
    //
    // Input Handlers
    //
    @OnItemClick(R.id.choose_dialog_fragment_listview)
    public void onListItemClicked(int position) {
        Timber.d("onListItemClicked: pos: " + position);

        PlayList pl = playLists.get(position);
        bus.post(new AddTrackToPlayListEvent(pl));
        getDialog().dismiss();
    }
    
    @OnEditorAction(R.id.choose_dialog_fragment_edittext)
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
