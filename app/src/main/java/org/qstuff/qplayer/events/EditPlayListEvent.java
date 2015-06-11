package org.qstuff.qplayer.events;

import org.qstuff.qplayer.data.PlayList;

/**
 * Created by Claus Chierici (chierici@karlmax-berlin.com) on 4/16/15
 * for Karlmax Berlin GmbH & Co. KG
 * <p/>
 * Copyright (C) 2014 Karlmax Berlin GmbH & Co. KG, All rights reserved.
 */
public class EditPlayListEvent {

    public PlayList playList;
    public String   newName;
    public boolean  nameChanged;
    public boolean  deleted;

    public EditPlayListEvent(PlayList playList, String newName, boolean nameChanged, boolean deleted) {
        this.deleted = deleted;
        this.newName = newName;
        this.nameChanged = nameChanged;
        this.playList = playList;
    }
}
