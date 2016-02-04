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
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.AddTracksToQueueEvent;
import org.qstuff.qplayer.events.PlayQueueUpdateEvent;
import org.qstuff.qplayer.events.TrackSelectedFromFilesEvent;
import org.qstuff.qplayer.events.TrackSelectedFromQueueEvent;
import org.qstuff.qplayer.events.TrackSelectedToPlayEvent;
import org.qstuff.qplayer.util.TrackUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
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
    
    @InjectView(R.id.queue_empty_text)
    TextView queueEmptyText;

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

        tracks = restoreTrackList(Constants.PREFS_KEY_QUEUE_TRACKLIST);
        if (tracks == null)
            tracks = new ArrayList<Track>();
        trackNames = getTrackNames(tracks);

        listView.setItemsCanFocus(true);
        listView.setFastScrollEnabled(true);

        queueListAdapter = new TrackListIndexerArrayAdapter<>(
            getActivity(),
            R.layout.tracklist_item,
            R.id.tracklist_item_text,
            trackNames);

        listView.setAdapter(queueListAdapter);
        
        bus.register(this);
        // for those who want to know ...
        bus.post(new PlayQueueUpdateEvent(tracks));
    }

    @Override
    public void onPause() {
        super.onPause();
        
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
    
    //
    // Event Subscriptions
    //

    @Subscribe
    public void onAddTracksToQueueEvent(AddTracksToQueueEvent event) {
        Timber.d("onAddTracksToQueueEvent(): num " + event.tracks.size());
        
        if (!event.tracks.isEmpty())
            queueEmptyText.setVisibility(View.GONE);
        
        // add to local (and adapter)
        tracks.addAll(event.tracks);
        trackNames.addAll(getTrackNames(event.tracks));
        
        // notify to adapter
        queueListAdapter.notifyDataSetChanged();
        
        // TODO notify player of updated queue, but not here !!!
        // bus.post(new PlayQueueUpdateEvent(tracks));
    }

    // FIXME: we need a policy on how to add tracks to the queue
    // for now a single track from frilebrowser is added to the top
    
    @Subscribe
    public void onTrackSelectedEvent(TrackSelectedFromFilesEvent event) {
        Timber.d("onTrackSelectedEvent(): " + event.track.getName());
        
        if (tracks == null)
            tracks = new ArrayList<>();

        if (!TrackUtils.trackListContainsTrack(tracks, event.track)) {
            tracks.add(0, event.track);
            trackNames.add(0, event.track.getName());
        }
        
        // notify to adapter
        queueListAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onTrackSelectedToPlayEvent(TrackSelectedToPlayEvent event) {
        Timber.d("onTrackSelectedToPlayEvent(): " + event.track.getName());
        
        queueListAdapter.setSelectedItemIndex(event.queueIndex);
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
