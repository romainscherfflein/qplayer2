package org.qstuff.qplayer.ui.content;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.qstuff.qplayer.Constants;
import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.data.PlayList;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.AddTrackToPlayListEvent;
import org.qstuff.qplayer.events.DeleteTrackFromPlayListEvent;
import org.qstuff.qplayer.events.EditPlayListEvent;
import org.qstuff.qplayer.events.FileSelectedEvent;
import org.qstuff.qplayer.events.NewPlayListEvent;
import org.qstuff.qplayer.ui.AbstractBaseDialogFragment;

import java.io.File;
import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import timber.log.Timber;

/**
 *
 */
public class PlaylistBrowserFragment extends BaseBrowserFragment {

    @Inject Bus bus;
    @Inject PlayListController playListController;
    
    @InjectView(R.id.playlist_fragment_listview)       ListView listView;
    @InjectView(R.id.playlist_fragment_header)         TextView headerText;
    @InjectView(R.id.playlist_fragment_backnavigation) TextView backText;
    
    private PlayListIndexerArrayAdapter<String> playListAdapter;
    private PlayList                            currentPlayList;
    private ArrayList<PlayList>                 playLists;
    private boolean                             isPlayListList;

    //
    // Fragment Lifecycle
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate():");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Timber.d("onCreateView():");

        View view = inflater.inflate(R.layout.playlist_browser_fragment, container, false);
        ButterKnife.inject(this, view);
        Bundle args = getArguments();
        
        listView.setItemsCanFocus(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setFastScrollEnabled(true);
                
        playLists = playListController.getPlayLists();
        isPlayListList = true;
        
        playListAdapter = new PlayListIndexerArrayAdapter<>(
            getActivity(),
            R.layout.playlist_item,
            R.id.playlist_item_text,
            playListController.getPlayListNames(),
            isPlayListList);

        listView.setAdapter(playListAdapter);
                
        return view;
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
    // Event Subscriptions
    //

    @Subscribe
    public void onNewPlayListEvent(NewPlayListEvent event) {
        Timber.d("onNewPlayListEvent(): " + event.name);
        
        updateListView();
    }

    @Subscribe
    public void onAddTrackToPlayListEvent(AddTrackToPlayListEvent event) {
        Timber.d("onAddTrackToPlayListEvent(): " + event.playList.getName());

        updateListView();
    }
    
    @Subscribe
    public void onEditPlayListEvent(EditPlayListEvent event) {
        Timber.d("onEditPlayListEvent(): " + event.playList.getName());
        
        if (event.nameChanged) {
            playListController.renamePlayList(event.playList, event.newName);
            updateListView();
        }
        if (event.deleted) {
            playListController.deletePlaylist(event.playList);
            updateListView();
        }
    }
    
    @Subscribe
    public void onDeleteTrackFromPlayListEvent(DeleteTrackFromPlayListEvent event) {
        event.playList.deleteTrack(event.track);
        playListController.updatePlayList(event.playList);

        updateListView();
    }
    
    //
    // Input Handlers
    //
    
    @OnItemClick(R.id.playlist_fragment_listview)
    public void onListItemClicked(int position) {
        Timber.d("onListItemClicked: pos: " + position);
        
        if (isPlayListList) {
            currentPlayList = playLists.get(position);
            headerText.setText("Playlist: " + currentPlayList.getName());
            backText.setText(getString(R.string.playlist_browser_back));
            showTrackList(currentPlayList);            
        } else {
            Track track = currentPlayList.getTrackList().get(position);
            bus.post(new FileSelectedEvent(new File(track.getUri())));
        }        
    }

    @OnItemLongClick(R.id.playlist_fragment_listview)
    public boolean onListItemLongClick(int position) {
        Timber.d("onListItemLongClick(): pos: " + position);
    
        if (isPlayListList)
            openEditPlayListDialog(playLists.get(position));
        else
            openEditTrackInPlayListDialog(currentPlayList.getTrackList().get(position));
        return true;
    }
    
    @OnClick(R.id.playlist_fragment_backnavigation)
    public void onBackNavigationClick() {
        Timber.d("onBackNavigationClick():");
        
        headerText.setText(getString(R.string.Playlists));
        
        if(!isPlayListList) {
            backText.setText(getString(R.string.playlist_browser_add_playlist));
            isPlayListList = true;
            updateListView();
        } else {
            openNewPlayListDialog();
        }
    }

    //
    // Private
    //
    
    private void updateListView() {
        Timber.d("updateListView():");
        
        if (isPlayListList)
            showPlayListList();
        else
            showTrackList(currentPlayList);
    }
    
    private void showPlayListList() {
        Timber.d("showPlayListList():");

        if (playListController.getPlayListNames().size() == 0) {
            // TODO: Toast
            return;
        }
        
        isPlayListList = true;
        
        playListAdapter = new PlayListIndexerArrayAdapter<>(
            getActivity(),
            R.layout.playlist_item,
            R.id.playlist_item_text,
            playListController.getPlayListNames(),
            isPlayListList);

        listView.setAdapter(playListAdapter);
    }
    
    private void showTrackList(PlayList pl) {
        Timber.d("showTrackList():");

        if (pl.getTrackListNames().size() == 0) {
            // TODO: Toast
            return;
        }
        
        isPlayListList = false;
        
        playListAdapter = new PlayListIndexerArrayAdapter<>(
            getActivity(),
            R.layout.playlist_item,
            R.id.playlist_item_text,
            pl.getTrackListNames(),
            isPlayListList);
        
        listView.setAdapter(playListAdapter);        
    }

    private void openEditPlayListDialog(PlayList pl) {
        Timber.d("openEditPlayListDialog(): " + pl.getName());
        
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.EXTRA_PLAYLIST, pl);
        
        AbstractBaseDialogFragment dialog = new EditPlayListDialogFragment();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), getString(R.string.edit_playlist_dialog_tag));
    }

    private void openNewPlayListDialog() {
        Timber.d("openNewPlayListDialog(): ");

        Bundle bundle = new Bundle();

        AbstractBaseDialogFragment dialog = new NewPlayListDialogFragment();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), getString(R.string.new_playlist_dialog_tag));
    }


    private void openEditTrackInPlayListDialog(Track track) {
        Timber.d("openEditTrackInPlayListDialog(): " + track.getName());

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.EXTRA_PLAYLIST, currentPlayList);
        bundle.putSerializable(Constants.EXTRA_TRACK, track);

        AbstractBaseDialogFragment dialog = new EditTrackDialogFragment();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), getString(R.string.edit_track_dialog_tag));
    }
}
