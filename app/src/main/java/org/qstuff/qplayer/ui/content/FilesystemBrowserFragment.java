package org.qstuff.qplayer.ui.content;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import org.qstuff.qplayer.R;
import org.qstuff.qplayer.events.FileSelectedEvent;
import org.qstuff.qplayer.ui.AbstractBaseDialogFragment;

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
 *
 */
public class FilesystemBrowserFragment extends BaseBrowserFragment {

    @Inject Bus bus;

    @InjectView(R.id.filesystem_listview)  ListView listView;
    @InjectView(R.id.filesystem_header)    TextView headerText;
    @InjectView(R.id.filesystem_parentdir) TextView browserParentDir;

    private IndexerArrayAdapter<String> dirListAdapter;
    private List<String>                currentDirEntries;

    private String paneTag;
    private String rootdir;
	private File   currentDir;


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
            Timber.d("onCreateView(): rootDir: "+rootdir+", paneTag: "+paneTag);
        }
        if(rootdir == null) {
            // FIXME: this is different on different API levels. this is ok for >= 4.2
            rootdir = new String(Environment.getExternalStorageDirectory().getPath() + "/Music");
        }

        headerText.setText(rootdir);
        browserParentDir.setVisibility(View.VISIBLE);
        currentDir = new File(rootdir);

        listView.setItemsCanFocus(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setFastScrollEnabled(true);

        browseTo(currentDir);

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
    // Click Handlers
    //

    @OnItemClick (R.id.filesystem_listview)
    public void onListItemClicked(int position) {
        Timber.d("onListItemClicked: pos: " + position);

        String dir = dirListAdapter.getItem(position);

        final File item = new File(currentDir.getAbsolutePath() + "/" + dir);

        if (item.isFile())
            bus.post(new FileSelectedEvent(item));
        else if (item.isDirectory())
            browseTo(item);
        else
            Timber.w("onListItemClicked(): WHAT ?");

    }

    @OnItemLongClick (R.id.filesystem_listview)
    public boolean onListItemLongClick(int position) {
        Timber.d("onListItemLongClick(): pos: " + position);

        String dir = dirListAdapter.getItem(position);
        final File item = new File(currentDir.getAbsolutePath() + "/" + dir);

        if (item.isDirectory())
            browseTo(item);
        else
            openAddToPlayListDialog();

        return  false;
    }

    @OnClick (R.id.filesystem_parentdir)
    public void onParentDirectoryClick() {
        Timber.d("onParentDirectoryClick():");

        if (currentDir.getAbsolutePath().equals(rootdir)) return;
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

		dirListAdapter = new IndexerArrayAdapter<String>(getActivity(),
    			R.layout.tracklist_item,
    			R.id.tracklist_item_text,
    			currentDirEntries,
    			currentDir.getPath());

		listView.setAdapter(dirListAdapter);
		dirListAdapter.notifyDataSetChanged();
	}

    private void openAddToPlayListDialog() {
        AbstractBaseDialogFragment dialog = new AddToPlayListDialogFragment();
        dialog.show(getFragmentManager(), getString(R.string.add_track_to_playlist_dialog_tag));
    }
}
