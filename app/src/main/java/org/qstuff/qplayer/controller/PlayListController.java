package org.qstuff.qplayer.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.squareup.otto.Bus;

import org.qstuff.qplayer.Constants;
import org.qstuff.qplayer.QPlayerApplication;
import org.qstuff.qplayer.data.PlayList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 
 */
@Singleton
public class PlayListController {

    private final Context context;
    private final Bus eventBus;
    private final SharedPreferences sharedPreferences;

    private ArrayList<PlayList> playLists;
        
    
    @Inject
    public PlayListController(Context context, Bus eventBus, SharedPreferences sharedPreferences) {
        this.context = context;
        this.eventBus = eventBus;
        this.sharedPreferences = sharedPreferences;
        
        loadPlayLists();
    }

    public void addPlayList(PlayList playlist) {
        playLists.add(playlist);
        savePlayLists();
    }
    
    public void deletePlaylist(PlayList playlist) {
        playLists.remove(playlist);
        savePlayLists();
    }
    

    public List<PlayList> getPlayLists() {
        return playLists;
    }

    public void setPlayLists(ArrayList<PlayList> playLists) {
        this.playLists = playLists;
    }
        
    private void loadPlayLists() {
         
        String jsonString = sharedPreferences.getString(Constants.PREFS_KEY_PLAYLISTS, "");
        if (jsonString.isEmpty()) {
            playLists = new ArrayList<PlayList>();
            return;
        }
        
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<PlayList>>(){}.getType();
        playLists = gson.fromJson(jsonString, listType);
    }
    
    private void savePlayLists() {
        Gson gson = new GsonBuilder().create();
        JsonArray jsonArray = gson.toJsonTree(playLists).getAsJsonArray();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREFS_KEY_PLAYLISTS, jsonArray.toString());
        editor.commit();
    }
}
