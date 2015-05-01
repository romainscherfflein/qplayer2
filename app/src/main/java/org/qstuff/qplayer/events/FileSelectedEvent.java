package org.qstuff.qplayer.events;

import java.io.File;

/**
 * Created by Claus Chierici (chierici@karlmax-berlin.com) on 4/16/15
 * for Karlmax Berlin GmbH & Co. KG
 * <p/>
 * Copyright (C) 2014 Karlmax Berlin GmbH & Co. KG, All rights reserved.
 */
public class FileSelectedEvent {

    public File audioFile;

    public FileSelectedEvent(File audioFile) {
        this.audioFile = audioFile;
    }
}
