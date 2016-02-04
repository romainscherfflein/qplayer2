package org.qstuff.qplayer.ui.content;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.qstuff.qplayer.Constants;
import org.qstuff.qplayer.R;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.AddTrackListToQueueEvent;
import org.qstuff.qplayer.events.PlayQueueUpdateEvent;
import org.qstuff.qplayer.events.TrackSelectedFromFilesEvent;
import org.qstuff.qplayer.events.TrackSelectedFromQueueEvent;
import org.qstuff.qplayer.events.TrackSelectedToPlayEvent;
import org.qstuff.qplayer.util.TrackUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import timber.log.Timber;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class QueueBrowserFragment extends BaseBrowserFragment {

    @Inject Bus bus;

    @InjectView(R.id.queue_fragment_listview)   
    ListView listView;
    

    private TrackListIndexerArrayAdapter<String> queueListAdapter;
    private ArrayList<Track>                     tracks;
    private ArrayList<String>                    trackNames;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Timber.d("onCreateView():");

        View v = inflater.inflate(R.layout.queue_browser_fragment, container, false);
        ButterKnife.inject(this, v);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
        bus.register(this);
        
        tracks = restoreTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST);
        if (tracks == null)
            tracks = new ArrayList<Track>();
        trackNames = getTrackNames(tracks);
        
        Timber.d("onResume(): tracks:" + tracks.size());
        Timber.d("onResume(): tracks:" + trackNames.size());


        listView.setItemsCanFocus(true);
        listView.setFastScrollEnabled(true);

        queueListAdapter = new TrackListIndexerArrayAdapter<>(
            getActivity(),
            R.layout.tracklist_item,
            R.id.tracklist_item_text,
            trackNames);

        listView.setAdapter(queueListAdapter);
        
        int currentTrackIndex = restoreIndex(Constants.PREFS_KEY_PLAYER_CURRENT_INDEX);
        
        if (currentTrackIndex >= 0) {
            queueListAdapter.setSelectedItemIndex(currentTrackIndex);
            listView.setSelection(currentTrackIndex);
        }
        // for those who want to know ...
        // bus.post(new PlayQueueUpdateEvent(tracks, true, false, false));
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause()");

        Timber.d("onPause(): tracks:" + tracks.size());

        saveTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST, tracks);
        bus.unregister(this);
    }

    //
    // Input Handlers
    //

    @OnItemClick(R.id.queue_fragment_listview)
    public void onListItemClicked(int position) {
        Timber.d("onListItemClicked: pos: " + position);

        Track track = tracks.get(position);
        bus.post(new TrackSelectedFromQueueEvent(track));
    }

    @OnClick(R.id.queue_clear_button)
    public void onClearButtonClicked(View view) {
        Timber.d("onClearButtonClicked: ");
        
        listView.setAdapter(null);
        tracks = new ArrayList<>();
        trackNames = new ArrayList<>();
        
        bus.post(new PlayQueueUpdateEvent(tracks, true, false, false));
        saveTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST, tracks);
    }
    
    //
    // Event Subscriptions
    //

    @Subscribe
    public void onAddTrackListToQueueEvent(AddTrackListToQueueEvent event) {
        Timber.d("onAddTrackListToQueueEvent(): num " + event.tracks.size());
        
        // add to local (and adapter)
        tracks.addAll(event.tracks);
        trackNames.addAll(getTrackNames(event.tracks));

        queueListAdapter = new TrackListIndexerArrayAdapter<>(
            getActivity(),
            R.layout.tracklist_item,
            R.id.tracklist_item_text,
            trackNames);

        listView.setAdapter(queueListAdapter);
        
        bus.post(new PlayQueueUpdateEvent(tracks, true, false, false));
        saveTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST, tracks);
    }
    
    @Subscribe
    public void onTrackSelectedFromFilesEvent(TrackSelectedFromFilesEvent event) {
        Timber.d("onTrackSelectedFromFilesEvent(): " + event.track.getName());

        tracks = restoreTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST);
        
        if (tracks == null) return;

        trackNames = getTrackNames(tracks);

        queueListAdapter = new TrackListIndexerArrayAdapter<>(
            getActivity(),
            R.layout.tracklist_item,
            R.id.tracklist_item_text,
            trackNames);

        listView.setAdapter(queueListAdapter);
    }

    @Subscribe
    public void onTrackSelectedToPlayEvent(TrackSelectedToPlayEvent event) {
        Timber.d("onTrackSelectedToPlayEvent(): " + event.track.getName());
        
        queueListAdapter.setSelectedItemIndex(event.queueIndex);
        listView.setSelection(event.queueIndex);
    }

    //
    // private helpers
    //
    
    private ArrayList<String> getTrackNames(ArrayList<Track> tracks) {
        
        ArrayList<String> ret = new ArrayList<>();
        for (Track t : tracks) {
            ret.add(t.getName());
        }
        return ret;
    }
}
