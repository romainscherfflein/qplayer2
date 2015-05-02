package org.qstuff.qplayer.content;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.qstuff.qplayer.R;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * This class can be used to have the alphabetical fast-scroll display.
 * 
 * @author claus chierici ((c)2012)
 */
public class IndexerArrayAdapter<T> extends ArrayAdapter<T>
	implements SectionIndexer, Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = -8402036170262443824L;

	private HashMap<String, Integer> alphaIndexer;
	private String[] sections;	
	
	private Context context;
	private int layoutResourceId;
	List<T> objects;
	String path;
	
	/**
	 * 
	 * @param context
	 * @param layoutResourceId
	 * @param textViewId
	 * @param objects
	 * @param path 
	 */
	public IndexerArrayAdapter(Context context, 
			int layoutResourceId,
			int textViewId,
			List<T> objects,
			String path) {
	    super(context, layoutResourceId, textViewId, objects);
	    Timber.d("IndexerArrayAdapter():");
        	    
	    this.context = context;
	    this.layoutResourceId = layoutResourceId;
	    this.objects = objects;
	    this.path = path;
	    
	    alphaIndexer = new HashMap<String, Integer>();
	    
	    int size = objects.size();    
	    for (int i = size - 1; i >= 0; i--) {
	    	T t = (T) objects.get(i);
	    	String element = t.toString();
	    	alphaIndexer.put(element.substring(0, 1), i);
	    }
	    
	    Set<String> keys = alphaIndexer.keySet();
	    ArrayList<String> keyList = new ArrayList<String>(keys);
	    Collections.sort(keyList, String.CASE_INSENSITIVE_ORDER);
        sections = new String[keyList.size()];
        keyList.toArray(sections);
    }

	/**
	 * 
	 */
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		
		View row = convertView;
        ListHolder holder = null;
        
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ListHolder();
            holder.imgIcon 	= (ImageView)row.findViewById(R.id.tracklist_item_icon);
            holder.txtTitle = (TextView)row.findViewById(R.id.tracklist_item_text);
            row.setTag(holder);
        } else {
            holder = (ListHolder)row.getTag();
        }
       
        String title = ((T)objects.get(position)).toString();
        // Timber.d("TITLE : "+ title);
        holder.txtTitle.setText(title);
        
        File file = new File (path + "/" + title);

        if (file.isFile())
			holder.imgIcon.setImageResource(R.drawable.ic_my_library_music_white_18dp);
        else if (file.isDirectory())
			holder.imgIcon.setImageResource(R.drawable.ic_folder_white_18dp);
        else
        	Timber.e("WWWWWWWWWWWWWWWWWWWW");
        
        return row;
    }
	
	/**
	 * 
	 * @author claus
	 *
	 */
	static class ListHolder {
        ImageView imgIcon;
        TextView txtTitle;
    }
	
	/**
	 * 
	 */
	public int getPositionForSection(int section) {
//		Timber.d("getPositionForSection(): sec: " + section);
		String letter = sections[section];
//		Timber.d("getPositionForSection(): pos: " + alphaIndexer.get(letter));
		return alphaIndexer.get(letter);
    }

	/**
	 * 
	 */
	public int getSectionForPosition(int position) {
//        Log.d(TAG, "getSectionForPosition(): " + position);
	    return 0;
    }

	/**
	 * 
	 */
	public Object[] getSections() {
	    return sections;
    }
}
