package org.qstuff.qplayer.ui.content;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.qstuff.qplayer.R;
import org.qstuff.qplayer.controller.PlayListController;
import org.qstuff.qplayer.data.PlayList;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.FileSelectedEvent;
import org.qstuff.qplayer.events.NewPlayListEvent;

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
    
    @InjectView(R.id.playlist_fragment_listview) ListView listView;
    @InjectView(R.id.playlist_fragment_header)   TextView headerText;

    private PlayListIndexerArrayAdapter<String> dirListAdapter;
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
        
        dirListAdapter = new PlayListIndexerArrayAdapter<>(
            getActivity(),
            R.layout.playlist_item,
            R.id.playlist_item_text,
            playListController.getPlayListNames(),
            isPlayListList);

        listView.setAdapter(dirListAdapter);
                
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    
    //
    // Event Subscriptions
    //

    @Subscribe
    public void onNewPlayListEvent(NewPlayListEvent event) {
        Timber.d("onNewPlayListEvent(): " + event.name);
        
        dirListAdapter.notifyDataSetChanged();
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
            showTrackList(currentPlayList);            
        } else {
            Track track = currentPlayList.getTrackList().get(position);
            bus.post(new FileSelectedEvent(new File(track.getUri())));
        }        
    }

    @OnItemLongClick(R.id.playlist_fragment_listview)
    public boolean onListItemLongClick(int position) {
        Timber.d("onListItemLongClick(): pos: " + position);
    
        // TODO: PlayList / Track edit dialog
        
        return  false;
    }
    
    @OnClick(R.id.playlist_fragment_backnavigation)
    public void onBackNavigationClick() {
        Timber.d("onBackNavigationClick():");

        showPlayListList();
    }

    //
    // Private
    //
    
    private void showPlayListList() {
        Timber.d("showPlayListList():");

        if (playListController.getPlayListNames().size() == 0) {
            // TODO: Toast
            return;
        }
        isPlayListList = true;
        
        dirListAdapter = new PlayListIndexerArrayAdapter<>(
            getActivity(),
            R.layout.playlist_item,
            R.id.playlist_item_text,
            playListController.getPlayListNames(),
            isPlayListList);

        listView.setAdapter(dirListAdapter);
        
//        dirListAdapter.setObjects(playListController.getPlayListNames(), true);
//        dirListAdapter.notifyDataSetChanged();
    }
    
    private void showTrackList(PlayList pl) {
        Timber.d("showTrackList():");

        if (pl.getTrackListNames().size() == 0) {
            // TODO: Toast
            return;
        }
        isPlayListList = false;
        
        dirListAdapter = new PlayListIndexerArrayAdapter<>(
            getActivity(),
            R.layout.playlist_item,
            R.id.playlist_item_text,
            pl.getTrackListNames(),
            isPlayListList);
        
        listView.setAdapter(dirListAdapter);
        
//        dirListAdapter.setObjects(pl.getTrackListNames(), false);
//        dirListAdapter.notifyDataSetChanged();
    }
}
