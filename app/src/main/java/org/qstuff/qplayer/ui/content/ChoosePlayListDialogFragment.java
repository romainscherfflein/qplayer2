package org.qstuff.qplayer.ui.content;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.squareup.otto.Bus;

import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.data.PlayList;
import org.qstuff.qplayer.ui.AbstractBaseDialogFragment;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

/**
 *
 */
public class ChoosePlayListDialogFragment extends AbstractBaseDialogFragment {

    @Inject PlayListController playListController;
    @Inject Bus bus;

    @InjectView(R.id.edittext_new_playlist_name) EditText editText;

    private PlayListIndexerArrayAdapter playListAdapter;
        
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.choose_playlist_dialog_fragment, null);
        Dialog dialog = builder.setView(v)
            .setCancelable(false)
            .setTitle(getString(R.string.choose_playlist_dialog_title))
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .create();
                
        playListAdapter = new PlayListIndexerArrayAdapter<PlayList>(getActivity(),
            R.layout.playlist_item,
            R.id.playlist_item_text,
            playListController.getPlayLists());
        
        ListView listView = (ListView) v.findViewById(R.id.choose_dialog_fragment_listview);
        listView.setAdapter(playListAdapter);
        
        return dialog;
    }
}
