package org.qstuff.qplayer.events;

import java.io.File;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class AddFilesToQueueEvent {

    public File files[];

    public AddFilesToQueueEvent(File files[]) {
        this.files = files;
    }
}
