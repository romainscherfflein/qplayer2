package org.qstuff.qplayer.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.squareup.otto.Bus;

import org.qstuff.qplayer.R;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.AddTrackListToQueueEvent;

import java.io.File;
import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;


public class AddTracksToQueueDialogFragment extends AbstractBaseDialogFragment {

    
    private static final String ARG_DIRECTORY = "ARG_DIRECTORY";
    private File directory;
    
    public static AddTracksToQueueDialogFragment newInstance(File directory) {
        
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIRECTORY, directory);

        AddTracksToQueueDialogFragment frg = new AddTracksToQueueDialogFragment();
        frg.setArguments(args);
        
        return frg;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        directory = (File) getArguments().getSerializable(ARG_DIRECTORY);
        
        if (directory == null)
            dismiss();
        
        StringBuffer list = new StringBuffer();
        final File files[] = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            list.append(files[i].getName());
            list.append("\n");
        }
                
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Dialog dialog = builder
            .setCancelable(false)
            .setTitle(getString(R.string.add_tracks_to_queue_dialog_title))
            .setMessage(list.toString())
            .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    bus.post(new AddTrackListToQueueEvent(createTrackList(files)));
                    dismiss();
                }
            })
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .create();

        ButterKnife.inject(dialog);
        
        return dialog;
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
    
    private ArrayList<Track> createTrackList(File files[]) {
        
        ArrayList<Track> ret = new ArrayList<>();
        for (File f : files) 
            ret.add(new Track(f));
        return ret;
    }
}
