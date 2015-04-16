package org.qstuff.qplayer.content;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.qstuff.qplayer.R;
import org.qstuff.qplayer.events.AudioFileSelectedEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

/**
 *
 */
public class FilesystemBrowserFragment extends BaseBrowserFragment {

    private final static String TAG = "FilesystemBrowserFragment";

    @InjectView(R.id.filesystem_listview)  ListView listView;
    @InjectView(R.id.filesystem_header)    TextView headerText;
    @InjectView(R.id.filesystem_parentdir) TextView browserParentDir;

    private IndexerArrayAdapter<String> dirListAdapter;
    private List<String>                currentDirEntries;

    private String paneTag;
    private String rootdir;
	private File   currentDir;


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

        listView.setItemsCanFocus(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setFastScrollEnabled(true);

        currentDir = new File(rootdir);
        browseTo(currentDir);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long id) {
                Timber.d("listView.onItemLongClick(): pos: " + position);

                String dir = dirListAdapter.getItem(position);
                final File item = new File(currentDir.getAbsolutePath() + "/" + dir);

                if (item.isDirectory()) {

                    // TODO: shift in next panel ...

                }

                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("listView.onItemClick(): pos: " + position);

                String dir = dirListAdapter.getItem(position);
                Timber.d("listView.onItemClick(): item: #" + (position) + " dir: " + dir);

                final File item = new File(currentDir.getAbsolutePath() + "/" + dir);

                if (item.isFile()) {
                    Timber.d("listView.onItemClick(): is a file ");

                    bus.post(new AudioFileSelectedEvent(item));

                }
                else if (item.isDirectory()) {
                    Timber.d("listView.onItemClick(): is a directory ");

                    // TODO: FIXME
//                    ((SlidingPaneContainerFragment)getParentFragment())
//                            .pushInNext(item.getAbsolutePath(), paneTag);
                }
                else {
                    Timber.d("listView.onItemClick(): WHAT ?");
                }
            }
        });

        browserParentDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("browserParentDir.onItemClick():");


            }

        });

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

    public void setPath(String path) {
        Timber.d("setPath()");
        currentDir = new File(path);
        browseTo(currentDir);
    }

    public void setPaneTag(String paneTag)
    {
        Timber.d("setPaneTag(): "+paneTag);
        this.paneTag = paneTag;
    }

    private void browseTo(final File dir) {
		Log.i(TAG, "browseTo(): " + dir.getAbsolutePath());

		if (dir.isDirectory()) {
			Timber.d("browseTo(): directory ");
			this.currentDir = dir;

            if (dir.listFiles() != null)
    			listDirEntries(dir.listFiles());
            else
                Timber.w("browseTo(): empty directory ...");
        }
		else if (dir.isFile()) {
			Timber.d("browseTo(): file");
		}
		else {
			Timber.d("browseTo(): doesn't exist ...");
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
    			R.layout.file_list_item,
    			R.id.file_list_item_text,
    			currentDirEntries,
    			currentDir.getPath());

		listView.setAdapter(dirListAdapter);
		dirListAdapter.notifyDataSetChanged();
	}
}
