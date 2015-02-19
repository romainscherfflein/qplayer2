package org.qstuff.qplayer.content;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.qstuff.qplayer.ui.OnSlidingPaneControl;
import org.qstuff.qplayer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class FilesystemBrowserFragment extends BaseBrowserFragment {

    private final static String TAG = "FilesystemBrowserFragment";

    private ListView     listView;
	private TextView     headerText;
	private TextView     browserParentDir;

    private IndexerArrayAdapter<String> dirListAdapter;
    private List<String> currentDirEntries;

    private String paneTag;
    private String rootdir;
	private File   currentDir;


    private OnSlidingPaneControl onSlidingPaneControl;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach():");

        if (activity instanceof OnSlidingPaneControl)
        {
            onSlidingPaneControl = (OnSlidingPaneControl) activity;
            if (onSlidingPaneControl == null)
                throw new ClassCastException(activity.toString()
                    + " must implement OnSlidingPaneControl");
        }
        else
        {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSlidingPaneControl");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate():");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView():");

        View view = inflater.inflate(R.layout.filesystem_browser_fragment, container, false);

        Bundle args = getArguments();
        if (null != args) {
            rootdir = args.getString("path");
            paneTag = args.getString("paneTag");
            Log.d(TAG, "onCreateView(): rootDir: "+rootdir+", paneTag: "+paneTag);
        }
        if(rootdir == null) {
            // FIXME: this is different on different API levels. this is ok for >= 4.2
            rootdir = new String(Environment.getExternalStorageDirectory().getPath() + "/Music");
        }

        headerText = (TextView) view.findViewById(R.id.filesystem_fragment_header);
        headerText.setText(rootdir);

        browserParentDir = (TextView) view.findViewById(R.id.filesystem_parentdir);
        browserParentDir.setVisibility(View.VISIBLE);

        listView = (ListView) view.findViewById(R.id.filesystem_listview);
        listView.setItemsCanFocus(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setFastScrollEnabled(true);

        currentDir = new File(rootdir);
        browseTo(currentDir);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long id) {
                Log.d(TAG, "listView.onItemLongClick(): pos: " + position);

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
                Log.d(TAG, "listView.onItemClick(): pos: " + position);

                String dir = dirListAdapter.getItem(position);
                Log.d(TAG, "listView.onItemClick(): item: #" + (position) + " dir: " + dir);

                final File item = new File(currentDir.getAbsolutePath() + "/" + dir);

                if (item.isFile()) {
                    Log.d(TAG, "listView.onItemClick(): is a file ");

                    // TODO: open play dialog
                }
                else if (item.isDirectory()) {
                    Log.d(TAG, "listView.onItemClick(): is a directory ");

                    ((SlidingPaneContainerFragment)getParentFragment())
                            .pushInNext(item.getAbsolutePath(), paneTag);
                }
                else {
                    Log.d(TAG, "listView.onItemClick(): WHAT ?");
                }
            }
        });

        browserParentDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "browserParentDir.onItemClick():");


            }

        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume():");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setPath(String path) {
        Log.d(TAG, "setPath()");
        currentDir = new File(path);
        browseTo(currentDir);
    }

    public void setPaneTag(String paneTag)
    {
        Log.d(TAG, "setPaneTag(): "+paneTag);
        this.paneTag = paneTag;
    }

    private void browseTo(final File dir) {
		Log.i(TAG, "browseTo(): " + dir.getAbsolutePath());

		if (dir.isDirectory()) {
			Log.d(TAG, "browseTo(): directory ");
			this.currentDir = dir;
			listDirEntries(dir.listFiles());
		}
		else if (dir.isFile()) {
			Log.d(TAG, "browseTo(): file");
		}
		else {
			Log.d(TAG, "browseTo(): doesn't exist ...");
		}
	}

    private void listDirEntries(File[] entries) {
		Log.i(TAG, "listDirEntries(): got "+ entries.length + " entries");

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
