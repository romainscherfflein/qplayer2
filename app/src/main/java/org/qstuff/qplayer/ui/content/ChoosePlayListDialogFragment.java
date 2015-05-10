package org.qstuff.qplayer.ui.content;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.ListView;

import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.data.PlayList;
import org.qstuff.qplayer.ui.AbstractBaseDialogFragment;

import javax.inject.Inject;

import butterknife.InjectView;

/**
 *
 */
public class ChoosePlayListDialogFragment extends AbstractBaseDialogFragment {

    @Inject PlayListController          playListController;

    @InjectView(R.id.choose_dialog_fragment_listview) ListView listView;

    private PlayListIndexerArrayAdapter playListAdapter;

    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        playListAdapter = new PlayListIndexerArrayAdapter<PlayList>(getActivity(),
            R.layout.playlist_item,
            R.id.playlist_item_text,
            playListController.getPlayLists());
        
        return new AlertDialog.Builder(getActivity())
            .setTitle(getString(R.string.choose_playlist_dialog_title))
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .create();
    }
  
}
