package org.qstuff.qplayer.ui.content;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import org.qstuff.qplayer.events.AddTrackToPlayListEvent;
import org.qstuff.qplayer.events.NewPlayListEvent;
import org.qstuff.qplayer.events.TrackSelectedFromFilesEvent;
import org.qstuff.qplayer.ui.dialogs.AbstractBaseDialogFragment;
import org.qstuff.qplayer.ui.dialogs.FileLongClickHandleDialogFragment;
import org.qstuff.qplayer.ui.dialogs.AddTracksToQueueDialogFragment;
import org.qstuff.qplayer.ui.dialogs.StoragePermissionStatementDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import timber.log.Timber;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class FilesystemBrowserFragment extends BaseBrowserFragment 
    implements StoragePermissionStatementDialogFragment.DialogListener {

    private static final int REQUEST_CODE_PERMISSIONS_STORAGE = 11;
    
    @Inject Bus bus;
    @Inject PlayListController playListController;

    @InjectView(R.id.filesystem_fragment_listview)  ListView listView;
    @InjectView(R.id.filesystem_fragment_header)    TextView headerText;
    @InjectView(R.id.filesystem_fragment_parentdir) TextView browserParentDir;

    private FileListIndexerArrayAdapter<String> dirListAdapter;
    private List<String>                        currentDirEntries;

    private String paneTag;

    private String rootdir;

    private File   currentDir;
    private File   selectedTrack;


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

        View view = inflater.inflate(R.layout.filesystem_browser_fragment, container, false);
        ButterKnife.inject(this, view);

        Bundle args = getArguments();
        
        if (null != args) {
            rootdir = args.getString("path");
            paneTag = args.getString("paneTag");
            Timber.d("onCreateView(): rootDir: " + rootdir + ", paneTag: " + paneTag);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkStoragePermission();
        } else {
            loadRootDir();
        }
        
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //
    // Event Subscriptions
    //

    @Subscribe
    public void onNewPlayListEvent(NewPlayListEvent event) {
        Timber.d("onNewPlayListEvent(): " + event.name);

        PlayList pl = new PlayList();
        pl.setName(event.name);
        if (event.addCurrentTrack) {
            pl.addTrack(new Track(selectedTrack.getName(), selectedTrack.getAbsolutePath()));
        }
        playListController.addPlayList(pl);
    }

    @Subscribe
    public void onAddTrackToPlayListEvent(AddTrackToPlayListEvent event) {
        Timber.d("onAddTrackToPlayListEvent(): " + event.playList.getName());

        if (event.playList == null) return;
        PlayList pl = event.playList;
        pl.addTrack(new Track(selectedTrack.getName(), selectedTrack.getAbsolutePath()));

        playListController.updatePlayList(pl);
    }
    
    //
    // Input Handlers
    //

    @OnItemClick (R.id.filesystem_fragment_listview)
    public void onListItemClicked(int position) {
        Timber.d("onListItemClicked: pos: " + position);

        String dir = dirListAdapter.getItem(position);

        final File item = new File(currentDir.getAbsolutePath() + "/" + dir);

        if (item.isFile())
            bus.post(new TrackSelectedFromFilesEvent(new Track(item), true));
        else if (item.isDirectory())
            browseTo(item);
        else
            Timber.w("onListItemClicked(): WHAT ?");
    }

    @OnItemLongClick (R.id.filesystem_fragment_listview)
    public boolean onListItemLongClick(int position) {
        Timber.d("onListItemLongClick(): pos: " + position);

        String dir = dirListAdapter.getItem(position);
        final File item = new File(currentDir.getAbsolutePath() + "/" + dir);
        selectedTrack = item;
        
        if (item.isDirectory()) {
            openAddToQueueDialog(item);
        } else {
            openLongClickFileHandleDialog(new Track(item));
        }
        
        return true;
    }

    @OnClick (R.id.filesystem_fragment_parentdir)
    public void onParentDirectoryClick() {
        Timber.d("onParentDirectoryClick():");

        if (currentDir.getAbsolutePath().equals(rootdir)) {
            return;
        }
        browseTo(currentDir.getParentFile());
    }
    
    //
    // Public API
    //

    public void setPath(String path) {
        Timber.d("setPath()");
        currentDir = new File(path);
        browseTo(currentDir);
    }

    public void setPaneTag(String paneTag) {
        Timber.d("setPaneTag(): "+paneTag);
        this.paneTag = paneTag;
    }

    //
    // Private
    //

    private void loadRootDir() {

        if (rootdir == null) {
            // FIXME: this is different on different API levels. OK for >= 4.2
            rootdir = Environment.getExternalStorageDirectory().getPath() + "/Music";
        }

        headerText.setText(rootdir);

        browserParentDir.setVisibility(View.VISIBLE);
        currentDir = new File(rootdir);

        listView.setItemsCanFocus(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setFastScrollEnabled(true);

        browseTo(currentDir);
    }
    
    
    private void browseTo(final File dir) {
		Timber.d("browseTo(): " + dir.getAbsolutePath());

		if (dir.isDirectory()) {
			Timber.d("browseTo(): directory ");
            currentDir = dir;

            if (dir.listFiles() != null)
    			listDirEntries(dir.listFiles());
            else
                Timber.w("browseTo(): empty directory ...");
        }
		else if (dir.isFile()) {
			Timber.w("browseTo(): file");
		}
		else {
			Timber.w("browseTo(): doesn't exist ...");
		}
	}

    private void listDirEntries(File[] entries) {
		Timber.d("listDirEntries(): got "+ entries.length + " entries");

		currentDirEntries = new ArrayList<String>();
		int i = 0;
		for (File entry:entries) {

			// Hide files starting with "."
			if (!entry.getName().startsWith("."))
				currentDirEntries.add(entry.getName());
		}

		headerText.setText(currentDir.getPath());
		Collections.sort(this.currentDirEntries,
                String.CASE_INSENSITIVE_ORDER);

		dirListAdapter = new FileListIndexerArrayAdapter<String>(getActivity(),
    			R.layout.tracklist_item,
    			R.id.tracklist_item_text,
    			currentDirEntries,
    			currentDir.getPath());

		listView.setAdapter(dirListAdapter);
		dirListAdapter.notifyDataSetChanged();
	}

    private void openLongClickFileHandleDialog(Track track) {
        AbstractBaseDialogFragment dialog = FileLongClickHandleDialogFragment.newInstance(track);
        dialog.show(getFragmentManager(), getString(R.string.file_short_click_handle_dialog_tag));
    }

    private void openAddToQueueDialog(File directory) {
        AbstractBaseDialogFragment dialog = AddTracksToQueueDialogFragment.newInstance(directory);
        dialog.show(getFragmentManager(), getString(R.string.add_tracks_to_queue_dialog_tag));
    }

    //
    // Permission Check 
    //
    
    private void checkStoragePermission() {

        int permissionCheckReadStorage =
            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheckWriteStorage =
            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheckReadStorage != PackageManager.PERMISSION_GRANTED
            || permissionCheckWriteStorage != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show the explanation once again.
                StoragePermissionStatementDialogFragment permissionEducationDialogFragment = 
                    StoragePermissionStatementDialogFragment.newInstance();
                permissionEducationDialogFragment.setDialogListener(this);
                permissionEducationDialogFragment.show(getFragmentManager(), StoragePermissionStatementDialogFragment.class.getName());
            } else {
                requestExternalStoragePermission();
            }
        } else {
            loadRootDir();
           
        }
    }

    private void requestExternalStoragePermission() {
        requestPermissions(new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
            REQUEST_CODE_PERMISSIONS_STORAGE);
    }

    @Override
    public void onPositiveButtonClicked() {
        Timber.d("onPositiveButtonClicked(): ");
        
        requestExternalStoragePermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        loadRootDir();
    }
}
