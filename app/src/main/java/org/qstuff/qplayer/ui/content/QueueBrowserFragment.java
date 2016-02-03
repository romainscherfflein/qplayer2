package org.qstuff.qplayer.ui.content;

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
import org.qstuff.qplayer.data.PlayList;
import org.qstuff.qplayer.data.Track;
import org.qstuff.qplayer.events.AddTracksToQueueEvent;
import org.qstuff.qplayer.events.FileSelectedEvent;

import java.io.File;
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

        // TODO: restore queue from args
        
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

    @OnItemClick(R.id.queue_fragment_listview)
    public void onListItemClicked(int position) {
        Timber.d("onListItemClicked: pos: " + position);

        Track track = tracks.get(position);
        bus.post(new FileSelectedEvent(new File(track.getUri())));
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
