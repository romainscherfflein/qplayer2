package org.qstuff.qplayer;

import org.qstuff.qplayer.content.FilesystemBrowserFragment;

/**
 * Created with IntelliJ IDEA.
 * User: claus
 * Date: 10/29/13
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public interface OnSlidingPaneControl {

    public void addNewRightPane(FilesystemBrowserFragment fragment, String path);
}
